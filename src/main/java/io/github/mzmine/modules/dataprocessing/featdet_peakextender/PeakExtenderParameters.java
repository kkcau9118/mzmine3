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

package io.github.mzmine.modules.dataprocessing.featdet_peakextender;

import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.BooleanParameter;
import io.github.mzmine.parameters.parametertypes.DoubleParameter;
import io.github.mzmine.parameters.parametertypes.StringParameter;
import io.github.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import io.github.mzmine.parameters.parametertypes.tolerances.MZToleranceParameter;

public class PeakExtenderParameters extends SimpleParameterSet {

    public static final PeakListsParameter peakLists = new PeakListsParameter();

    public static final StringParameter suffix = new StringParameter(
            "Name suffix", "Suffix to be added to feature list name",
            "extended");

    public static final MZToleranceParameter mzTolerance = new MZToleranceParameter();

    public static final DoubleParameter minimumHeight = new DoubleParameter(
            "Min height", "Minimum allowed intensity for succesive scans",
            MZmineCore.getConfiguration().getIntensityFormat());

    public static final BooleanParameter autoRemove = new BooleanParameter(
            "Remove original peaklist",
            "If checked, original peaklist will be removed and only deisotoped version remains");

    public PeakExtenderParameters() {
        super(new Parameter[] { peakLists, suffix, mzTolerance, minimumHeight,
                autoRemove });
    }

}
