/*
 * Copyright 2006-2020 The MZmine Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 *
 * Edited and modified by Owen Myers (Oweenm@gmail.com)
 */

package io.github.mzmine.modules.dataprocessing.featdet_ADAPchromatogrambuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import javax.annotation.Nonnull;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.Feature;
import io.github.mzmine.datamodel.FeatureStatus;
import io.github.mzmine.datamodel.IsotopePattern;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.datamodel.impl.SimplePeakInformation;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.util.scans.ScanUtils;

/**
 * Chromatogram implementing ChromatographicPeak.
 */
public class ADAPChromatogram implements Feature {
    private SimplePeakInformation peakInfo;

    // Data file of this chromatogram
    private RawDataFile dataFile;

    // Data points of the chromatogram (map of scan number -> m/z peak)
    // private Hashtable<Integer, DataPoint> dataPointsMap;
    private HashMap<Integer, DataPoint> dataPointsMap;

    // Chromatogram m/z, RT, height, area. The mz value will be the highest
    // points mz value
    private double mz, rt, height, area, weightedMz;
    private Double fwhm = null, tf = null, af = null;

    // Top intensity scan, fragment scan
    private int representativeScan = -1, fragmentScan = -1;

    // All MS2 fragment scan numbers
    private int[] allMS2FragmentScanNumbers = new int[] {};

    // Ranges of raw data points
    private Range<Double> rawDataPointsIntensityRange, rawDataPointsMZRange,
            rawDataPointsRTRange;

    // A set of scan numbers of a segment which is currently being connected
    private Vector<Integer> buildingSegment;
    // A full list of all the scan numbers as points get added
    private List<Integer> chromScanList;

    // Keep track of last added data point
    private DataPoint lastMzPeak;

    // Number of connected segments, which have been committed by
    // commitBuildingSegment()
    private int numOfCommittedSegments = 0;

    // Isotope pattern. Null by default but can be set later by deisotoping
    // method.
    private IsotopePattern isotopePattern;
    private int charge = 0;

    // Victor Trevino
    private double mzSum = 0;
    private int mzN = 0;
    private double weightedMzSum = 0;
    private int weightedMzN = 0;
    private double sumOfWeights = 0;

    private double highPointMZ = 0;

    private final int scanNumbers[];

    public int tmp_see_same_scan_count = 0;

    /**
     * Initializes this Chromatogram
     */
    public ADAPChromatogram(RawDataFile dataFile, int scanNumbers[]) {
        this.dataFile = dataFile;
        this.scanNumbers = scanNumbers;

        rawDataPointsRTRange = dataFile.getDataRTRange(1);

        dataPointsMap = new HashMap<Integer, DataPoint>();
        buildingSegment = new Vector<Integer>();
        chromScanList = new ArrayList<Integer>();
    }

    public double getHighPointMZ() {
        return highPointMZ;
    }

    public void setHighPointMZ(double toSet) {
        highPointMZ = toSet;
    }

    public List getIntensitiesForCDFOut() {
        // Need all scans with no intensity to be set to zero

        List intensityList = new ArrayList();

        for (int curScanNum = 0; curScanNum < scanNumbers.length; curScanNum++) {
            if (dataPointsMap.get(curScanNum) == null) {
                intensityList.add(0.0);
            } else {
                intensityList.add(dataPointsMap.get(curScanNum).getIntensity());
            }
        }

        return intensityList;

    }

    public int findNumberOfContinuousPointsAboveNoise(double noise) {
        // sort the array containing all of the scan numbers of the point added
        // loop over the sorted array now.
        // if you find a point with intensity higher than noise start the count
        // if the next scan contains a point higher than the noise update the
        // count
        // otherwise start it oer when you hit a sufficiently high point.
        // keep track of the largest count which will be returned.
        Collections.sort(chromScanList);

        int bestCount = 0;
        int curCount = 0;
        int lastScanNum = 0;
        int scanListLength = chromScanList.size();

        int curScanNum;
        DataPoint curDataPoint;

        for (int i = 1; i < scanListLength; i++) {

            curScanNum = chromScanList.get(i);
            curDataPoint = dataPointsMap.get(curScanNum);

            if (curDataPoint.getIntensity() > noise) {

                lastScanNum = chromScanList.get(i - 1);
                int lastScanNumsActIndex = Arrays.binarySearch(scanNumbers,
                        lastScanNum);
                int seqNextScanShouldBe = scanNumbers[lastScanNumsActIndex + 1];

                if (seqNextScanShouldBe == curScanNum) {
                    curCount += 1;
                    if (curCount > bestCount) {
                        bestCount = curCount;
                    }
                } else {
                    curCount = 0;
                }

            } else {
                curCount = 0;
            }

        }

        // System.out.println("bestCount");
        // System.out.println(bestCount);

        // plus one because first point considered in advancing curcount is
        // actualy going to be the
        // second point/
        return bestCount + 1;

    }

    /**
     * This method adds a MzPeak to this Chromatogram. All values of this
     * Chromatogram (rt, m/z, intensity and ranges) are updated on request
     * 
     * @param mzValue
     */
    public void addMzPeak(int scanNumber, DataPoint mzValue) {
        double curIntensity;
        // System.out.println("---------------- Adding MZ value to Chromatogram
        // ----------------");

        // If we already have a mzvalue for the scan number then we need to add
        // the intensities
        // together before putting it into the dataPointsMap, otherwise the
        // chromatogram is only
        // representing the intesities of the llast added point for that scan.
        //
        // For now just don't add the point if we have it already. The highest
        // point will be the
        // first one added
        if (dataPointsMap.containsKey(scanNumber)) {
            tmp_see_same_scan_count += 1;
            return;

        }

        dataPointsMap.put(scanNumber, mzValue);
        lastMzPeak = mzValue;
        mzSum += mzValue.getMZ();
        mzN++;
        mz = mzSum / mzN;
        buildingSegment.add(scanNumber);
        chromScanList.add(scanNumber);

        curIntensity = mzValue.getIntensity();

        weightedMzN++;
        weightedMzSum += curIntensity * mzValue.getMZ();
        sumOfWeights += curIntensity;

        weightedMz = weightedMzSum / sumOfWeights;

    }

    @Override
    public DataPoint getDataPoint(int scanNumber) {
        return dataPointsMap.get(scanNumber);
    }

    /**
     * Returns m/z value of last added data point
     */
    public DataPoint getLastMzPeak() {
        return lastMzPeak;
    }

    /**
     * This method returns m/z value of the chromatogram
     */
    @Override
    public double getMZ() {
        return mz;
    }

    /**
     * This method returns weighted mean of m/z values comprising the
     * chromatogram
     */
    public double getWeightedMZ() {
        return weightedMz;
    }

    /**
     * This method returns a string with the basic information that defines this
     * peak
     * 
     * @return String information
     */
    @Override
    public String toString() {
        return "Chromatogram "
                + MZmineCore.getConfiguration().getMZFormat().format(mz)
                + " m/z";
    }

    @Override
    public double getArea() {
        return area;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public int getMostIntenseFragmentScanNumber() {
        return fragmentScan;
    }

    @Override
    public int[] getAllMS2FragmentScanNumbers() {
        return allMS2FragmentScanNumbers;
    }

    @Override
    public @Nonnull FeatureStatus getFeatureStatus() {
        return FeatureStatus.DETECTED;
    }

    @Override
    public double getRT() {
        return rt;
    }

    @Override
    public @Nonnull Range<Double> getRawDataPointsIntensityRange() {
        return rawDataPointsIntensityRange;
    }

    @Override
    public @Nonnull Range<Double> getRawDataPointsMZRange() {
        return rawDataPointsMZRange;
    }

    @Override
    public @Nonnull Range<Double> getRawDataPointsRTRange() {
        return rawDataPointsRTRange;
    }

    @Override
    public int getRepresentativeScanNumber() {
        return representativeScan;
    }

    @Override
    public @Nonnull int[] getScanNumbers() {
        return scanNumbers;
    }

    @Override
    public @Nonnull RawDataFile getDataFile() {
        return dataFile;
    }

    @Override
    public IsotopePattern getIsotopePattern() {
        return isotopePattern;
    }

    @Override
    public void setIsotopePattern(@Nonnull IsotopePattern isotopePattern) {
        this.isotopePattern = isotopePattern;
    }

    @Override
    public void outputChromToFile() {
        // int allScanNumbers[] = Ints.toArray(dataPointsMap.keySet());
        int allScanNumbers[] = getScanNumbers();
        Arrays.sort(allScanNumbers);
        try {
            String fileName = dataFile.getName();

            String fileNameNoExtention = fileName.split("\\.")[0];

            // PrintWriter fileWriter = new
            // PrintWriter("owen_mzmine_chrom_out.txt","UTF-8");
            PrintWriter fileWriter = new PrintWriter(new FileOutputStream(
                    new File(fileNameNoExtention + "_mzmine_chrom_out.csv"),
                    true));

            String mzStr = String.valueOf(getMZ());
            // fileWriter.println("New chromatogram. mz: "+mzStr);
            String curIntStr;
            String curRtStr;
            String curScStr;
            String curMzStr;
            double curInt;
            double curRt;
            double curMz;
            String spacer = ",";

            for (int i : allScanNumbers) {

                curRt = dataFile.getScan(i).getRetentionTime();
                DataPoint mzPeak = getDataPoint(i);

                if (mzPeak == null) {
                    curInt = 0.0;
                    curMz = -1;
                } else {
                    curInt = mzPeak.getIntensity();
                    curMz = mzPeak.getMZ();
                }

                curIntStr = String.valueOf(curInt);
                curRtStr = String.valueOf(curRt);
                curScStr = String.valueOf(i);
                curMzStr = String.valueOf(curMz);

                fileWriter.println(
                        curRtStr + spacer + curIntStr + spacer + curMzStr);
            }
            fileWriter.close();
        }

        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    public void finishChromatogram() {

        int allScanNumbers[] = Ints.toArray(dataPointsMap.keySet());
        Arrays.sort(allScanNumbers);

        mz = highPointMZ;

        // Update raw data point ranges, height, rt and representative scan
        height = Double.MIN_VALUE;
        for (int i = 0; i < allScanNumbers.length; i++) {

            DataPoint mzPeak = dataPointsMap.get(allScanNumbers[i]);

            // Replace the MzPeak instance with an instance of SimpleDataPoint,
            // to reduce the memory usage. After we finish this Chromatogram, we
            // don't need the additional data provided by the MzPeak

            dataPointsMap.put(allScanNumbers[i], mzPeak);

            if (i == 0) {
                rawDataPointsIntensityRange = Range
                        .singleton(mzPeak.getIntensity());
                rawDataPointsMZRange = Range.singleton(mzPeak.getMZ());
            } else {
                rawDataPointsIntensityRange = rawDataPointsIntensityRange
                        .span(Range.singleton(mzPeak.getIntensity()));
                rawDataPointsMZRange = rawDataPointsMZRange
                        .span(Range.singleton(mzPeak.getMZ()));
            }

            if (height < mzPeak.getIntensity()) {
                height = mzPeak.getIntensity();
                rt = dataFile.getScan(allScanNumbers[i]).getRetentionTime();
                representativeScan = allScanNumbers[i];
            }
        }

        // Update area
        area = 0;
        for (int i = 1; i < allScanNumbers.length; i++) {
            // For area calculation, we use retention time in seconds
            double previousRT = dataFile.getScan(allScanNumbers[i - 1])
                    .getRetentionTime() * 60d;
            double currentRT = dataFile.getScan(allScanNumbers[i])
                    .getRetentionTime() * 60d;
            double previousHeight = dataPointsMap.get(allScanNumbers[i - 1])
                    .getIntensity();
            double currentHeight = dataPointsMap.get(allScanNumbers[i])
                    .getIntensity();
            area += (currentRT - previousRT) * (currentHeight + previousHeight)
                    / 2;
        }

        // Update fragment scan
        fragmentScan = ScanUtils.findBestFragmentScan(dataFile,
                dataFile.getDataRTRange(1), rawDataPointsMZRange);

        allMS2FragmentScanNumbers = ScanUtils.findAllMS2FragmentScans(dataFile,
                dataFile.getDataRTRange(1), rawDataPointsMZRange);

        if (fragmentScan > 0) {
            Scan fragmentScanObject = dataFile.getScan(fragmentScan);
            int precursorCharge = fragmentScanObject.getPrecursorCharge();
            if (precursorCharge > 0)
                this.charge = precursorCharge;
        }

        rawDataPointsRTRange = null;

        for (int scanNum : allScanNumbers) {
            double scanRt = dataFile.getScan(scanNum).getRetentionTime();
            DataPoint dp = getDataPoint(scanNum);

            if ((dp == null) || (dp.getIntensity() == 0.0))
                continue;

            if (rawDataPointsRTRange == null)
                rawDataPointsRTRange = Range.singleton(scanRt);
            else
                rawDataPointsRTRange = rawDataPointsRTRange
                        .span(Range.singleton(scanRt));
        }

        // Discard the fields we don't need anymore
        buildingSegment = null;
        lastMzPeak = null;

    }

    public double getBuildingSegmentLength() {
        if (buildingSegment.size() < 2)
            return 0;
        int firstScan = buildingSegment.firstElement();
        int lastScan = buildingSegment.lastElement();
        double firstRT = dataFile.getScan(firstScan).getRetentionTime();
        double lastRT = dataFile.getScan(lastScan).getRetentionTime();
        return (lastRT - firstRT);
    }

    public int getNumberOfCommittedSegments() {
        return numOfCommittedSegments;
    }

    public void removeBuildingSegment() {
        for (int scanNumber : buildingSegment)
            dataPointsMap.remove(scanNumber);
        buildingSegment.clear();
    }

    public void commitBuildingSegment() {
        buildingSegment.clear();
        numOfCommittedSegments++;
    }

    public void addDataPointsFromChromatogram(ADAPChromatogram ch) {
        for (Entry<Integer, DataPoint> dp : ch.dataPointsMap.entrySet()) {
            addMzPeak(dp.getKey(), dp.getValue());
        }
    }

    @Override
    public int getCharge() {
        return charge;
    }

    @Override
    public void setCharge(int charge) {
        this.charge = charge;
    }

    @Override
    public Double getFWHM() {
        return fwhm;
    }

    @Override
    public void setFWHM(Double fwhm) {
        this.fwhm = fwhm;
    }

    @Override
    public Double getTailingFactor() {
        return tf;
    }

    @Override
    public void setTailingFactor(Double tf) {
        this.tf = tf;
    }

    @Override
    public Double getAsymmetryFactor() {
        return af;
    }

    @Override
    public void setAsymmetryFactor(Double af) {
        this.af = af;
    }

    @Override
    public void setPeakInformation(SimplePeakInformation peakInfoIn) {
        this.peakInfo = peakInfoIn;
    }

    @Override
    public SimplePeakInformation getPeakInformation() {
        return peakInfo;
    }

    @Override
    public void setFragmentScanNumber(int fragmentScanNumber) {
        this.fragmentScan = fragmentScanNumber;
    }

    @Override
    public void setAllMS2FragmentScanNumbers(int[] allMS2FragmentScanNumbers) {
        this.allMS2FragmentScanNumbers = allMS2FragmentScanNumbers;
        // also set best scan by TIC
        int best = -1;
        double tic = 0;
        if (allMS2FragmentScanNumbers != null) {
            for (int i : allMS2FragmentScanNumbers) {
                if (tic < dataFile.getScan(i).getTIC())
                    best = i;
            }
        }
        setFragmentScanNumber(best);
    }
}
