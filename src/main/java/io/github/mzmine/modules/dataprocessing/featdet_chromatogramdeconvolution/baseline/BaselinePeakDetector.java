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

package io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.baseline;

import static io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.baseline.BaselinePeakDetectorParameters.BASELINE_LEVEL;
import static io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.baseline.BaselinePeakDetectorParameters.MIN_PEAK_HEIGHT;
import static io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.baseline.BaselinePeakDetectorParameters.PEAK_DURATION;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import com.google.common.collect.Range;

import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.Feature;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.PeakResolver;
import io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.ResolvedPeak;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.util.R.REngineType;
import io.github.mzmine.util.R.RSessionWrapper;
import io.github.mzmine.util.maths.CenterFunction;

/**
 * This class implements a simple peak deconvolution algorithm. Continuous peaks
 * above a given baseline threshold level are detected.
 */
public class BaselinePeakDetector implements PeakResolver {

    @Override
    public @Nonnull String getName() {
        return "Baseline cut-off";
    }

    @Override
    public ResolvedPeak[] resolvePeaks(final Feature chromatogram,
            ParameterSet parameters, RSessionWrapper rSession,
            CenterFunction mzCenterFunction, double msmsRange,
            double rTRangeMSMS) {

        int scanNumbers[] = chromatogram.getScanNumbers();
        final int scanCount = scanNumbers.length;
        double retentionTimes[] = new double[scanCount];
        double intensities[] = new double[scanCount];
        RawDataFile dataFile = chromatogram.getDataFile();
        for (int i = 0; i < scanCount; i++) {
            final int scanNum = scanNumbers[i];
            retentionTimes[i] = dataFile.getScan(scanNum).getRetentionTime();
            DataPoint dp = chromatogram.getDataPoint(scanNum);
            if (dp != null)
                intensities[i] = dp.getIntensity();
            else
                intensities[i] = 0.0;
        }

        // Get parameters.
        final double minimumPeakHeight = parameters
                .getParameter(MIN_PEAK_HEIGHT).getValue();
        final double baselineLevel = parameters.getParameter(BASELINE_LEVEL)
                .getValue();
        final Range<Double> durationRange = parameters
                .getParameter(PEAK_DURATION).getValue();

        final List<ResolvedPeak> resolvedPeaks = new ArrayList<ResolvedPeak>(2);

        // Current region is a region of consecutive scans which all have
        // intensity above baseline level.
        for (int currentRegionStart = 0; currentRegionStart < scanCount; currentRegionStart++) {

            // Find a start of the region.
            final DataPoint startPeak = chromatogram
                    .getDataPoint(scanNumbers[currentRegionStart]);
            if (startPeak != null
                    && startPeak.getIntensity() >= baselineLevel) {

                double currentRegionHeight = startPeak.getIntensity();

                // Search for end of the region
                int currentRegionEnd;
                for (currentRegionEnd = currentRegionStart
                        + 1; currentRegionEnd < scanCount; currentRegionEnd++) {

                    final DataPoint endPeak = chromatogram
                            .getDataPoint(scanNumbers[currentRegionEnd]);
                    if (endPeak == null
                            || endPeak.getIntensity() < baselineLevel) {

                        break;
                    }

                    currentRegionHeight = Math.max(currentRegionHeight,
                            endPeak.getIntensity());
                }

                // Subtract one index, so the end index points at the last data
                // point of current region.
                currentRegionEnd--;

                // Check current region, if it makes a good peak.
                if (durationRange
                        .contains(retentionTimes[currentRegionEnd]
                                - retentionTimes[currentRegionStart])
                        && currentRegionHeight >= minimumPeakHeight) {

                    // Create a new ResolvedPeak and add it.
                    resolvedPeaks.add(new ResolvedPeak(chromatogram,
                            currentRegionStart, currentRegionEnd,
                            mzCenterFunction, msmsRange, rTRangeMSMS));
                }

                // Find next peak region, starting from next data point.
                currentRegionStart = currentRegionEnd;

            }
        }

        return resolvedPeaks.toArray(new ResolvedPeak[resolvedPeaks.size()]);
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return BaselinePeakDetectorParameters.class;
    }

    @Override
    public boolean getRequiresR() {
        return false;
    }

    @Override
    public String[] getRequiredRPackages() {
        return null;
    }

    @Override
    public String[] getRequiredRPackagesVersions() {
        return null;
    }

    @Override
    public REngineType getREngineType(ParameterSet parameters) {
        return null;
    }

}
