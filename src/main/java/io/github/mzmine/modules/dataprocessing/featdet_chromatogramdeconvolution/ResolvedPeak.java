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

package io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.Range;
import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.Feature;
import io.github.mzmine.datamodel.FeatureStatus;
import io.github.mzmine.datamodel.IsotopePattern;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.datamodel.impl.SimpleDataPoint;
import io.github.mzmine.datamodel.impl.SimplePeakInformation;
import io.github.mzmine.util.PeakUtils;
import io.github.mzmine.util.maths.CenterFunction;
import io.github.mzmine.util.scans.ScanUtils;

/**
 * ResolvedPeak
 * 
 */
public class ResolvedPeak implements Feature {

    private SimplePeakInformation peakInfo;

    // Data file of this chromatogram
    private RawDataFile dataFile;

    // Chromatogram m/z, RT, height, area
    private double mz, rt, height, area;
    private Double fwhm = null, tf = null, af = null;

    // Scan numbers
    private int scanNumbers[];

    // We store the values of data points as double[] arrays in order to save
    // memory, which would be wasted by keeping a lot of instances of
    // SimpleDataPoint (each instance takes 16 or 32 bytes of extra memory)
    private double dataPointMZValues[], dataPointIntensityValues[];

    // Top intensity scan, fragment scan
    private int representativeScan, fragmentScan;

    // All MS2 fragment scan numbers
    private int[] allMS2FragmentScanNumbers;

    // Ranges of raw data points
    private Range<Double> rawDataPointsIntensityRange, rawDataPointsMZRange,
            rawDataPointsRTRange;

    // Isotope pattern. Null by default but can be set later by deisotoping
    // method.
    private IsotopePattern isotopePattern = null;
    private int charge = 0;

    // PeakListRow.ID of the chromatogram where this feature is detected. Null
    // by default but can be
    // set by
    // chromatogram deconvolution method.
    private Integer parentChromatogramRowID = null;

    /**
     * Initializes this peak using data points from a given chromatogram -
     * regionStart marks the index of the first data point (inclusive),
     * regionEnd marks the index of the last data point (inclusive). The
     * selected region MUST NOT contain any zero-intensity data points,
     * otherwise exception is thrown.
     */
    public ResolvedPeak(Feature chromatogram, int regionStart, int regionEnd,
            CenterFunction mzCenterFunction, double msmsRange,
            double RTRangeMSMS) {

        assert regionEnd > regionStart;

        this.dataFile = chromatogram.getDataFile();

        // Make an array of scan numbers of this peak
        scanNumbers = new int[regionEnd - regionStart + 1];

        int chromatogramScanNumbers[] = chromatogram.getScanNumbers();

        System.arraycopy(chromatogramScanNumbers, regionStart, scanNumbers, 0,
                regionEnd - regionStart + 1);

        dataPointMZValues = new double[regionEnd - regionStart + 1];
        dataPointIntensityValues = new double[regionEnd - regionStart + 1];

        // Set raw data point ranges, height, rt and representative scan
        height = Double.MIN_VALUE;

        double mzValue = chromatogram.getMZ();
        for (int i = 0; i < scanNumbers.length; i++) {

            dataPointMZValues[i] = mzValue;

            DataPoint dp = chromatogram.getDataPoint(scanNumbers[i]);
            if (dp == null) {
                continue;
                /*
                 * String error =
                 * "Cannot create a resolved peak in a region with missing data points: chromatogram "
                 * + chromatogram + " scans " +
                 * chromatogramScanNumbers[regionStart] + "-" +
                 * chromatogramScanNumbers[regionEnd] +
                 * ", missing data point in scan " + scanNumbers[i];
                 * 
                 * throw new IllegalArgumentException(error);
                 */
            }

            dataPointMZValues[i] = dp.getMZ();
            dataPointIntensityValues[i] = dp.getIntensity();

            if (rawDataPointsIntensityRange == null) {
                rawDataPointsIntensityRange = Range
                        .singleton(dp.getIntensity());
                rawDataPointsRTRange = Range.singleton(
                        dataFile.getScan(scanNumbers[i]).getRetentionTime());
                rawDataPointsMZRange = Range.singleton(dp.getMZ());
            } else {
                rawDataPointsRTRange = rawDataPointsRTRange
                        .span(Range.singleton(dataFile.getScan(scanNumbers[i])
                                .getRetentionTime()));
                rawDataPointsIntensityRange = rawDataPointsIntensityRange
                        .span(Range.singleton(dp.getIntensity()));
                rawDataPointsMZRange = rawDataPointsMZRange
                        .span(Range.singleton(dp.getMZ()));
            }

            if (height < dp.getIntensity()) {
                height = dp.getIntensity();
                rt = dataFile.getScan(scanNumbers[i]).getRetentionTime();
                representativeScan = scanNumbers[i];

            }
        }

        // Calculate m/z as median, average or weighted-average
        mz = mzCenterFunction.calcCenter(dataPointMZValues,
                dataPointIntensityValues);

        // Update area
        area = 0;
        for (int i = 1; i < scanNumbers.length; i++) {

            // For area calculation, we use retention time in seconds
            double previousRT = dataFile.getScan(scanNumbers[i - 1])
                    .getRetentionTime() * 60d;
            double currentRT = dataFile.getScan(scanNumbers[i])
                    .getRetentionTime() * 60d;

            double previousHeight = dataPointIntensityValues[i - 1];
            double currentHeight = dataPointIntensityValues[i];
            area += (currentRT - previousRT) * (currentHeight + previousHeight)
                    / 2;
        }

        // Update fragment scan
        double lowerBound = rawDataPointsMZRange.lowerEndpoint();
        double upperBound = rawDataPointsMZRange.upperEndpoint();
        double mid = (upperBound + lowerBound) / 2;
        lowerBound = mid - msmsRange / 2;
        upperBound = mid + msmsRange / 2;
        if (lowerBound < 0) {
            lowerBound = 0;
        }
        Range<Double> searchingRange = Range.closed(lowerBound, upperBound);
        double lowerBoundRT = rawDataPointsRTRange.lowerEndpoint();
        double upperBoundRT = rawDataPointsRTRange.upperEndpoint();
        double midRT = (upperBoundRT + lowerBoundRT) / 2;
        lowerBoundRT = midRT - RTRangeMSMS / 2;
        upperBoundRT = midRT + RTRangeMSMS / 2;
        if (lowerBound < 0) {
            lowerBound = 0;
        }
        Range<Double> searchingRangeRT = Range.closed(lowerBoundRT,
                upperBoundRT);

        if (msmsRange == 0)
            searchingRange = rawDataPointsMZRange;
        if (RTRangeMSMS == 0)
            searchingRangeRT = rawDataPointsRTRange;

        fragmentScan = ScanUtils.findBestFragmentScan(dataFile,
                searchingRangeRT, searchingRange);
        allMS2FragmentScanNumbers = ScanUtils.findAllMS2FragmentScans(dataFile,
                searchingRangeRT, searchingRange);

        if (fragmentScan > 0) {
            Scan fragmentScanObject = dataFile.getScan(fragmentScan);
            int precursorCharge = fragmentScanObject.getPrecursorCharge();
            if (precursorCharge > 0)
                this.charge = precursorCharge;
        }

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
        SimpleDataPoint dp = new SimpleDataPoint(dataPointMZValues[index],
                dataPointIntensityValues[index]);
        return dp;
    }

    /**
     * This method returns m/z value of the chromatogram
     */
    @Override
    public double getMZ() {
        return mz;
    }

    /**
     * This method returns a string with the basic information that defines this
     * peak
     * 
     * @return String information
     */
    @Override
    public String toString() {
        return PeakUtils.peakToString(this);
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

    // dulab Edit
    @Override
    public void outputChromToFile() {
        int nothing = -1;
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
