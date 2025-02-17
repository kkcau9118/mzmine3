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

package io.github.mzmine.modules.dataprocessing.featdet_shapemodeler.peakmodels;

import java.util.TreeMap;
import javax.annotation.Nonnull;
import com.google.common.collect.Range;
import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.Feature;
import io.github.mzmine.datamodel.FeatureStatus;
import io.github.mzmine.datamodel.IsotopePattern;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.impl.SimpleDataPoint;
import io.github.mzmine.datamodel.impl.SimplePeakInformation;
import io.github.mzmine.util.PeakUtils;

public class TrianglePeakModel implements Feature {
    private SimplePeakInformation peakInfo;

    // Model information
    private double rtRight = -1, rtLeft = -1;
    private double alpha, beta;

    // Peak information
    private double rt, height, mz, area;
    private Double fwhm = null, tf = null, af = null;
    private int[] scanNumbers;
    private RawDataFile rawDataFile;
    private FeatureStatus status;
    private int representativeScan = -1, fragmentScan = -1;
    private int[] allMS2FragmentScanNumbers = new int[] {};
    private Range<Double> rawDataPointsIntensityRange, rawDataPointsMZRange,
            rawDataPointsRTRange;
    private TreeMap<Integer, DataPoint> dataPointsMap;

    // Isotope pattern. Null by default but can be set later by deisotoping
    // method.
    private IsotopePattern isotopePattern;
    private int charge = 0;

    @Override
    public double getArea() {
        return area;
    }

    @Override
    public @Nonnull RawDataFile getDataFile() {
        return rawDataFile;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getMZ() {
        return mz;
    }

    @Override
    public int getMostIntenseFragmentScanNumber() {
        return fragmentScan;
    }

    @Override
    public DataPoint getDataPoint(int scanNumber) {
        return dataPointsMap.get(scanNumber);
    }

    @Override
    public @Nonnull FeatureStatus getFeatureStatus() {
        return status;
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

    public String getName() {
        return "Triangle peak " + PeakUtils.peakToString(this);
    }

    @Override
    public IsotopePattern getIsotopePattern() {
        return isotopePattern;
    }

    @Override
    public void setIsotopePattern(@Nonnull IsotopePattern isotopePattern) {
        this.isotopePattern = isotopePattern;
    }

    public TrianglePeakModel(Feature originalDetectedShape, int[] scanNumbers,
            double[] intensities, double[] retentionTimes, double resolution) {

        height = originalDetectedShape.getHeight();
        rt = originalDetectedShape.getRT();
        mz = originalDetectedShape.getMZ();
        this.scanNumbers = scanNumbers;
        rawDataFile = originalDetectedShape.getDataFile();

        rawDataPointsIntensityRange = originalDetectedShape
                .getRawDataPointsIntensityRange();
        rawDataPointsMZRange = originalDetectedShape.getRawDataPointsMZRange();
        rawDataPointsRTRange = originalDetectedShape.getRawDataPointsRTRange();

        dataPointsMap = new TreeMap<Integer, DataPoint>();
        status = originalDetectedShape.getFeatureStatus();

        rtRight = retentionTimes[retentionTimes.length - 1];
        rtLeft = retentionTimes[0];

        alpha = Math.atan(height / (rt - rtLeft));
        beta = Math.atan(height / (rtRight - rt));

        // Calculate intensity of each point in the shape.
        double shapeHeight, currentRT, previousRT, previousHeight;

        previousHeight = calculateIntensity(retentionTimes[0]);
        previousRT = retentionTimes[0] * 60d;

        for (int i = 0; i < retentionTimes.length; i++) {

            currentRT = retentionTimes[i] * 60d;

            shapeHeight = calculateIntensity(currentRT);
            SimpleDataPoint mzPeak = new SimpleDataPoint(mz, shapeHeight);
            dataPointsMap.put(scanNumbers[i], mzPeak);

            area += (currentRT - previousRT) * (shapeHeight + previousHeight)
                    / 2;
            previousRT = currentRT;
            previousHeight = shapeHeight;
        }

    }

    private double calculateIntensity(double retentionTime) {

        double intensity = 0;
        if ((retentionTime > rtLeft) && (retentionTime < rtRight)) {
            if (retentionTime <= rt) {
                intensity = Math.tan(alpha) * (retentionTime - rtLeft);
            }
            if (retentionTime > rt) {
                intensity = Math.tan(beta) * (rtRight - retentionTime);
            }
        }

        return intensity;
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

    @Override
    public int[] getAllMS2FragmentScanNumbers() {
        return allMS2FragmentScanNumbers;
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
                if (tic < rawDataFile.getScan(i).getTIC())
                    best = i;
            }
        }
        setFragmentScanNumber(best);
    }
}
