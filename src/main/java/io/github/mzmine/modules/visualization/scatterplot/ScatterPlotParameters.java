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

package io.github.mzmine.modules.visualization.scatterplot;

import java.awt.Window;

import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.WindowSettingsParameter;
import io.github.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import io.github.mzmine.util.ExitCode;

public class ScatterPlotParameters extends SimpleParameterSet {

    public static final PeakListsParameter peakLists = new PeakListsParameter(1,
            1);

    /**
     * Windows size and position
     */
    public static final WindowSettingsParameter windowSettings = new WindowSettingsParameter();

    public ScatterPlotParameters() {
        super(new Parameter[] { peakLists, windowSettings });
    }

    @Override
    public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired) {
        return ExitCode.OK;
    }

}
