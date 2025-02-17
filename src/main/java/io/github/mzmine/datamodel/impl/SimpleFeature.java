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
 */

package io.github.mzmine.datamodel.impl;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.Range;
import com.google.common.primitives.Doubles;
import io.github.msdk.datamodel.Chromatogram;
import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.Feature;
import io.github.mzmine.datamodel.FeatureStatus;
import io.github.mzmine.datamodel.IsotopePattern;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.util.PeakUtils;
import io.github.mzmine.util.RawDataFileUtils;
import io.github.mzmine.util.scans.ScanUtils;

/**
 * This class is a simple implementation of the peak interface.
 */
public class SimpleFeature implements Feature {

    private SimplePeakInformation peakInfo;
    private FeatureStatus peakStatus;
    private RawDataFile dataFile;

    // Scan numbers
    private int scanNumbers[];

    private DataPoint dataPointsPerScan[];

    // M/Z, RT, Height and Area, FWHM, Tailing factor, Asymmetry factor
    private double mz, rt, height, area;
    private Double fwhm, tf, af;

    // Boundaries of the peak raw data points
    private Range<Double> rtRange, mzRange, intensityRange;

    // Number of representative scan
    private int representativeScan;

    // Number of most intense fragment scan
    private int fragmentScanNumber;

    // Numbers of all MS2 fragment scans
    private int[] allMS2FragmentScanNumbers;

    // Isotope pattern. Null by default but can be set later by deisotoping
    // method.
    private IsotopePattern isotopePattern;
    private int charge = 0;

    // PeakListRow.ID of the chromatogram where this feature is detected. Null
    // by default but can be
    // set by
    // chromatogram deconvolution method.
    private Integer parentChromatogramRowID;

    /**
     * Initializes a new peak using given values
     * 
     */
    public SimpleFeature(RawDataFile dataFile, double MZ, double RT,
            double height, double area, int[] scanNumbers,
            DataPoint[] dataPointsPerScan, FeatureStatus peakStatus,
            int representativeScan, int fragmentScanNumber,
            int[] allMS2FragmentScanNumbers, Range<Double> rtRange,
            Range<Double> mzRange, Range<Double> intensityRange) {

        if (dataPointsPerScan.length == 0) {
            throw new IllegalArgumentException(
                    "Cannot create a SimplePeak instance with no data points");
        }

        this.dataFile = dataFile;
        this.mz = MZ;
        this.rt = RT;
        this.height = height;
        this.area = area;
        this.scanNumbers = scanNumbers;
        this.peakStatus = peakStatus;
        this.representativeScan = representativeScan;
        this.fragmentScanNumber = fragmentScanNumber;
        this.allMS2FragmentScanNumbers = allMS2FragmentScanNumbers;
        this.rtRange = rtRange;
        this.mzRange = mzRange;
        this.intensityRange = intensityRange;
        this.dataPointsPerScan = dataPointsPerScan;
        this.fwhm = null;
        this.tf = null;
        this.af = null;
        this.parentChromatogramRowID = null;
    }

    /**
     * Copy constructor
     */
    public SimpleFeature(Feature p) {

        this.dataFile = p.getDataFile();

        this.mz = p.getMZ();
        this.rt = p.getRT();
        this.height = p.getHeight();
        this.area = p.getArea();
        this.fwhm = p.getFWHM();
        this.tf = p.getTailingFactor();
        this.af = p.getAsymmetryFactor();

        this.rtRange = p.getRawDataPointsRTRange();
        this.mzRange = p.getRawDataPointsMZRange();
        this.intensityRange = p.getRawDataPointsIntensityRange();

        this.scanNumbers = p.getScanNumbers();

        this.dataPointsPerScan = new DataPoint[scanNumbers.length];

        for (int i = 0; i < scanNumbers.length; i++) {
            dataPointsPerScan[i] = p.getDataPoint(scanNumbers[i]);

        }

        this.peakStatus = p.getFeatureStatus();

        this.representativeScan = p.getRepresentativeScanNumber();
        this.fragmentScanNumber = p.getMostIntenseFragmentScanNumber();
        this.allMS2FragmentScanNumbers = p.getAllMS2FragmentScanNumbers();

        this.parentChromatogramRowID = p.getParentChromatogramRowID();
    }

    /**
     * Copy constructor
     */
    public SimpleFeature(RawDataFile dataFile, FeatureStatus status,
            io.github.msdk.datamodel.Feature msdkFeature) {

        this.dataFile = dataFile;

        this.mz = msdkFeature.getMz();
        this.rt = msdkFeature.getRetentionTime() / 60.0;
        this.height = msdkFeature.getHeight();
        this.area = msdkFeature.getArea();

        Chromatogram msdkFeatureChromatogram = msdkFeature.getChromatogram();
        final double mzValues[] = msdkFeatureChromatogram.getMzValues();
        final float rtValues[] = msdkFeatureChromatogram.getRetentionTimes();
        final float intensityValues[] = msdkFeatureChromatogram
                .getIntensityValues();

        this.rtRange = Range.closed(
                msdkFeatureChromatogram.getRtRange().lowerEndpoint()
                        .doubleValue() / 60.0,
                msdkFeatureChromatogram.getRtRange().upperEndpoint()
                        .doubleValue() / 60.0);
        this.mzRange = Range.encloseAll(Doubles.asList(mzValues));
        this.intensityRange = Range.closed(0.0,
                msdkFeature.getHeight().doubleValue());

        this.scanNumbers = new int[rtValues.length];
        this.dataPointsPerScan = new DataPoint[scanNumbers.length];
        for (int i = 0; i < scanNumbers.length; i++) {
            scanNumbers[i] = RawDataFileUtils.getClosestScanNumber(dataFile,
                    rtValues[i] / 60.0);
            dataPointsPerScan[i] = new SimpleDataPoint(mzValues[i],
                    intensityValues[i]);
        }

        this.peakStatus = status;

        this.representativeScan = RawDataFileUtils
                .getClosestScanNumber(dataFile, this.rt);
        this.fragmentScanNumber = ScanUtils.findBestFragmentScan(dataFile,
                this.rtRange, this.mzRange);
        this.allMS2FragmentScanNumbers = ScanUtils
                .findAllMS2FragmentScans(dataFile, this.rtRange, this.mzRange);

        for (int i = 0; i < scanNumbers.length; i++) {
            if (height < dataPointsPerScan[i].getIntensity()) {
                representativeScan = scanNumbers[i];
            }
        }

        this.parentChromatogramRowID = null; // TODO: ask Tomas and update
    }

    /**
     * This method returns the status of the peak
     */
    @Override
    public @Nonnull FeatureStatus getFeatureStatus() {
        return peakStatus;
    }

    /**
     * This method returns M/Z value of the peak
     */
    @Override
    public double getMZ() {
        return mz;
    }

    public void setMZ(double mz) {
        this.mz = mz;
    }

    public void setRT(double rt) {
        this.rt = rt;
    }

    /**
     * This method returns retention time of the peak
     */
    @Override
    public double getRT() {
        return rt;
    }

    /**
     * This method returns the raw height of the peak
     */
    @Override
    public double getHeight() {
        return height;
    }

    /**
     * @param height
     *            The height to set.
     */
    public void setHeight(double height) {
        this.height = height;

        intensityRange = Range.closed(0.0, height);
    }

    /**
     * This method returns the raw area of the peak
     */
    @Override
    public double getArea() {
        return area;
    }

    /**
     * @param area
     *            The area to set.
     */
    public void setArea(double area) {
        this.area = area;
    }

    /**
     * This method returns numbers of scans that contain this peak
     */
    @Override
    public @Nonnull int[] getScanNumbers() {
        return scanNumbers;
    }

    /**
     * This method returns a representative datapoint of this peak in a given
     * scan
     */
    @Override
    public DataPoint getDataPoint(int scanNumber) {
        int index = Arrays.binarySearch(scanNumbers, scanNumber);
        if (index < 0)
            return null;
        return dataPointsPerScan[index];
    }

    /**
     * @see io.github.mzmine.datamodel.Feature#getDataFile()
     */
    @Override
    public @Nonnull RawDataFile getDataFile() {
        return dataFile;
    }

    /**
     * @see io.github.mzmine.datamodel.Feature#setDataFile()
     */
    public void setDataFile(RawDataFile dataFile) {
        this.dataFile = dataFile;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return PeakUtils.peakToString(this);
    }

    /**
     * @see io.github.mzmine.datamodel.Feature#getRawDataPointsIntensityRange()
     */
    @Override
    public @Nonnull Range<Double> getRawDataPointsIntensityRange() {
        return intensityRange;
    }

    /**
     * @see io.github.mzmine.datamodel.Feature#getRawDataPointsMZRange()
     */
    @Override
    public @Nonnull Range<Double> getRawDataPointsMZRange() {
        return mzRange;
    }

    /**
     * @see io.github.mzmine.datamodel.Feature#getRawDataPointsRTRange()
     */
    @Override
    public @Nonnull Range<Double> getRawDataPointsRTRange() {
        return rtRange;
    }

    /**
     * @see io.github.mzmine.datamodel.Feature#getRepresentativeScanNumber()
     */
    @Override
    public int getRepresentativeScanNumber() {
        return representativeScan;
    }

    @Override
    public int getMostIntenseFragmentScanNumber() {
        return fragmentScanNumber;
    }

    @Override
    public int[] getAllMS2FragmentScanNumbers() {
        return allMS2FragmentScanNumbers;
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
    public int getCharge() {
        return charge;
    }

    @Override
    public void setCharge(int charge) {
        this.charge = charge;
    }

    /**
     * This method returns the full width at half maximum (FWHM) of the peak
     */
    @Override
    public Double getFWHM() {
        return fwhm;
    }

    /**
     * @param fwhm
     *            The full width at half maximum (FWHM) to set.
     */
    @Override
    public void setFWHM(Double fwhm) {
        this.fwhm = fwhm;
    }

    /**
     * This method returns the tailing factor of the peak
     */
    @Override
    public Double getTailingFactor() {
        return tf;
    }

    /**
     * @param tf
     *            The tailing factor to set.
     */
    @Override
    public void setTailingFactor(Double tf) {
        this.tf = tf;
    }

    /**
     * This method returns the asymmetry factor of the peak
     */
    @Override
    public Double getAsymmetryFactor() {
        return af;
    }

    /**
     * @param af
     *            The asymmetry factor to set.
     */
    @Override
    public void setAsymmetryFactor(Double af) {
        this.af = af;
    }

    // dulab Edit
    @Override
    public void outputChromToFile() {

    }

    @Override
    public void setPeakInformation(SimplePeakInformation peakInfoIn) {
        this.peakInfo = peakInfoIn;
    }

    @Override
    public SimplePeakInformation getPeakInformation() {
        return peakInfo;
    }
    // End dulab Edit

    public void setParentChromatogramRowID(@Nullable Integer id) {
        this.parentChromatogramRowID = id;
    }

    @Override
    @Nullable
    public Integer getParentChromatogramRowID() {
        return this.parentChromatogramRowID;
    }

    @Override
    public void setFragmentScanNumber(int fragmentScanNumber) {
        this.fragmentScanNumber = fragmentScanNumber;
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
