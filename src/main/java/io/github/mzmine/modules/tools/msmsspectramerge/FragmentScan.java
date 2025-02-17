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
 * It is freely available under the GNU GPL licence of MZmine2.
 *
 * For any questions or concerns, please refer to:
 * https://groups.google.com/forum/#!forum/molecular_networking_bug_reports
 */

package io.github.mzmine.modules.tools.msmsspectramerge;

/**
 * A fragment scan consists of a list of MS/MS spectra surrounded by MS1 scans
 */

import com.google.common.collect.Range;

import io.github.mzmine.datamodel.*;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import io.github.mzmine.util.scans.ScanUtils;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An MS/MS scan with some statistics about its precursor in MS
 */
class FragmentScan {

    private static final double CHIMERIC_INTENSITY_THRESHOLD = 0.1d;
    /**
     * The raw data file this scans are derived from
     */
    protected final RawDataFile origin;

    /**
     * The feature this scans are derived from
     */
    protected final Feature feature;

    /**
     * mass list to use
     */
    protected final String massList;

    /**
     * the MS1 scan that comes before the first MS/MS
     */
    protected final Integer ms1ScanNumber;
    /**
     * the MS1 scan that comes after the last MS/MS
     */
    protected final Integer ms1SucceedingScanNumber;
    /**
     * all consecutive(!) MS/MS scans. There should ne no other MS1 scan between
     * them
     */
    protected final int[] ms2ScanNumbers;
    /**
     * the intensity of the precursor peak in MS (left or right from MS/MS
     * scans)
     */
    protected double precursorIntensityLeft, precursorIntensityRight;
    /**
     * the sumed up intensity of chimeric peaks (left or right from MS/MS scans)
     */
    protected double chimericIntensityLeft, chimericIntensityRight;

    /**
     * precursor charge of fragment scan
     */
    protected int precursorCharge;
    private PolarityType polarity;

    static FragmentScan[] getAllFragmentScansFor(Feature feature,
            String massList, Range<Double> isolationWindow,
            MZTolerance massAccuracy) {
        final RawDataFile file = feature.getDataFile();
        final int[] ms2 = feature.getAllMS2FragmentScanNumbers().clone();
        Arrays.sort(ms2);
        final List<FragmentScan> fragmentScans = new ArrayList<>();
        // search for ms1 scans
        int i = 0;
        while (i < ms2.length) {
            int scanNumber = ms2[i];
            Scan scan = file.getScan(scanNumber);
            Scan precursorScan = ScanUtils.findPrecursorScan(scan);
            Scan precursorScan2 = ScanUtils.findSucceedingPrecursorScan(scan);
            int j = precursorScan2 == null ? ms2.length
                    : Arrays.binarySearch(ms2, precursorScan2.getScanNumber());
            if (j < 0)
                j = -j - 1;
            final int[] subms2 = new int[j - i];
            for (int k = i; k < j; ++k)
                subms2[k - i] = ms2[k];

            fragmentScans.add(new FragmentScan(file, feature, massList,
                    precursorScan != null ? precursorScan.getScanNumber()
                            : null,
                    precursorScan2 != null ? precursorScan2.getScanNumber()
                            : null,
                    subms2, isolationWindow, massAccuracy));
            i = j;
        }
        return fragmentScans.toArray(new FragmentScan[0]);
    }

    FragmentScan(RawDataFile origin, Feature feature, String massList,
            Integer ms1ScanNumber, Integer ms1ScanNumber2, int[] ms2ScanNumbers,
            Range<Double> isolationWindow, MZTolerance massAccuracy) {
        this.origin = origin;
        this.feature = feature;
        this.massList = massList;
        this.ms1ScanNumber = ms1ScanNumber;
        this.ms1SucceedingScanNumber = ms1ScanNumber2;
        this.ms2ScanNumbers = ms2ScanNumbers;
        double[] precInfo = new double[2];
        if (ms1ScanNumber != null) {
            detectPrecursor(ms1ScanNumber, feature.getMZ(), isolationWindow,
                    massAccuracy, precInfo);
            this.precursorIntensityLeft = precInfo[0];
            this.chimericIntensityLeft = precInfo[1];
        } else {
            this.precursorIntensityLeft = 0d;
            this.chimericIntensityLeft = 0d;
        }
        if (ms1SucceedingScanNumber != null) {
            detectPrecursor(ms1SucceedingScanNumber, feature.getMZ(),
                    isolationWindow, massAccuracy, precInfo);
            this.precursorIntensityRight = precInfo[0];
            this.chimericIntensityRight = precInfo[1];
        } else {
            this.precursorIntensityRight = 0d;
            this.chimericIntensityRight = 0d;
        }
    }

    /**
     * interpolate the precursor intensity and chimeric intensity of the MS1
     * scans linearly by retention time to estimate this values for the MS2
     * scans
     * 
     * @return two arrays, one for precursor intensities, one for chimeric
     *         intensities, for all MS2 scans
     */
    protected double[][] getInterpolatedPrecursorAndChimericIntensities() {
        final double[][] values = new double[2][ms2ScanNumbers.length];
        if (ms1ScanNumber == null) {
            Arrays.fill(values[0], precursorIntensityRight);
            Arrays.fill(values[1], chimericIntensityRight);
        } else if (ms1SucceedingScanNumber == null) {
            Arrays.fill(values[0], precursorIntensityLeft);
            Arrays.fill(values[1], chimericIntensityLeft);
        } else {
            Scan left = origin.getScan(ms1ScanNumber);
            Scan right = origin.getScan(ms1SucceedingScanNumber);
            for (int k = 0; k < ms2ScanNumbers.length; ++k) {
                Scan ms2 = origin.getScan(ms2ScanNumbers[k]);
                double rtRange = (ms2.getRetentionTime()
                        - left.getRetentionTime())
                        / (right.getRetentionTime() - left.getRetentionTime());
                if (rtRange >= 0 && rtRange <= 1) {
                    values[0][k] = (1d - rtRange) * precursorIntensityLeft
                            + (rtRange) * precursorIntensityRight;
                    values[1][k] = (1d - rtRange) * chimericIntensityLeft
                            + (rtRange) * chimericIntensityRight;
                } else {
                    LoggerFactory.getLogger(FragmentScan.class).warn(
                            "Retention time is non-monotonic within scan numbers.");
                    values[0][k] = precursorIntensityLeft
                            + precursorIntensityRight;
                    values[1][k] = chimericIntensityLeft
                            + chimericIntensityRight;
                }
            }
        }
        return values;
    }

    /**
     * search for precursor peak in MS1
     */
    private void detectPrecursor(int ms1Scan, double precursorMass,
            Range<Double> isolationWindow, MZTolerance massAccuracy,
            double[] precInfo) {
        Scan spectrum = origin.getScan(ms1Scan);
        this.precursorCharge = spectrum.getPrecursorCharge();
        this.polarity = spectrum.getPolarity();
        DataPoint[] dps = spectrum.getDataPointsByMass(
                Range.closed(precursorMass + isolationWindow.lowerEndpoint(),
                        precursorMass + isolationWindow.upperEndpoint()));
        // for simplicity, just use the most intense peak within massAccuracy
        // range
        int bestPeak = -1;
        double highestIntensity = 0d;
        for (int mppm = 1; mppm < 3; ++mppm) {
            final double maxDiff = massAccuracy
                    .getMzToleranceForMass(precursorMass) * mppm;
            for (int i = 0; i < dps.length; ++i) {
                final DataPoint p = dps[i];
                if (p.getIntensity() <= highestIntensity)
                    continue;
                final double mzdiff = Math.abs(p.getMZ() - precursorMass);
                if (mzdiff <= maxDiff) {
                    highestIntensity = p.getIntensity();
                    bestPeak = i;
                }
            }
            if (bestPeak >= 0)
                break;
        }
        // now sum up all remaining intensities. Leave out isotopes. leave out
        // peaks with intensity below 10%
        // of the precursor. They won't contaminate fragment scans anyways
        precInfo[0] = highestIntensity;
        precInfo[1] = 0d;
        final double threshold = highestIntensity
                * CHIMERIC_INTENSITY_THRESHOLD;
        foreachpeak: for (int i = 0; i < dps.length; ++i) {
            if (i != bestPeak && dps[i].getIntensity() > threshold) {
                // check for isotope peak
                final double maxDiff = massAccuracy
                        .getMzToleranceForMass(precursorMass) + 0.03;
                for (int k = 1; k < 5; ++k) {
                    final double isoMz = precursorMass + k * 1.0015;
                    final double diff = isoMz - dps[i].getMZ();
                    if (Math.abs(diff) <= maxDiff) {
                        continue foreachpeak;
                    } else if (diff > 0.5) {
                        break;
                    }
                }
                precInfo[1] += dps[i].getIntensity();
            }
        }
    }
}