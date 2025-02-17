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

package io.github.mzmine.modules.dataprocessing.featdet_massdetection.centroid;

import javax.annotation.Nonnull;

import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.modules.dataprocessing.featdet_massdetection.MassDetector;
import io.github.mzmine.parameters.ParameterSet;

import java.util.ArrayList;

/**
 * Remove peaks below the given noise level. Note that the module is bypassed in
 * the MassDetectionTask to speed up noise removal. Thus, changes within this
 * module will have no effect in some cases. This is just a temporary hack to
 * speed up noise removal in MZMine2.
 */
public class CentroidMassDetector implements MassDetector {

    public DataPoint[] getMassValues(Scan scan, ParameterSet parameters) {
        return getMassValues(scan.getDataPoints(), parameters);
    }

    public DataPoint[] getMassValues(DataPoint dataPoints[],
            ParameterSet parameters) {

        double noiseLevel = parameters
                .getParameter(CentroidMassDetectorParameters.noiseLevel)
                .getValue();

        ArrayList<DataPoint> mzPeaks = new ArrayList<DataPoint>();

        // Find possible mzPeaks
        for (int j = 0; j < dataPoints.length; j++) {

            // Is intensity above the noise level?
            if (dataPoints[j].getIntensity() >= noiseLevel) {
                // Yes, then mark this index as mzPeak
                mzPeaks.add(dataPoints[j]);
            }
        }
        return mzPeaks.toArray(new DataPoint[0]);
    }

    public @Nonnull String getName() {
        return "Centroid";
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return CentroidMassDetectorParameters.class;
    }

}
