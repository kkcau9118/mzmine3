/*
 * Copyright 2006-2015 The du-lab Development Team
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
/*
 * author Owen Myers (Oweenm@gmail.com)
 */
package io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.ADAPpeakpicking;

import javax.annotation.Nonnull;

import io.github.mzmine.parameters.ParameterSet;

public class IntensityWindowsSNEstimator implements SNEstimatorChoice {
    @Override
    public @Nonnull String getName() {
        return "Intensity window SN";
    }

    public String getSNCode() {
        return "Intensity Window Estimator";
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return IntensityWindowsSNParameters.class;
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
}
