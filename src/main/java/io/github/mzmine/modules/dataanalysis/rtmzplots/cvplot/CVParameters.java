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

package io.github.mzmine.modules.dataanalysis.rtmzplots.cvplot;

import java.awt.Window;

import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.ComboParameter;
import io.github.mzmine.parameters.parametertypes.MultiChoiceParameter;
import io.github.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import io.github.mzmine.util.ExitCode;
import io.github.mzmine.util.PeakMeasurementType;

public class CVParameters extends SimpleParameterSet {

    public static final PeakListsParameter peakLists = new PeakListsParameter(1,
            1);

    public static final MultiChoiceParameter<RawDataFile> dataFiles = new MultiChoiceParameter<RawDataFile>(
            "Data files", "Samples for CV analysis", new RawDataFile[0], null,
            2);

    public static final ComboParameter<PeakMeasurementType> measurementType = new ComboParameter<PeakMeasurementType>(
            "Peak measurement type",
            "Determines whether peak's area or height is used in computations.",
            PeakMeasurementType.values());

    public CVParameters() {
        super(new Parameter[] { peakLists, dataFiles, measurementType });
    }

    @Override
    public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired) {
        PeakList selectedPeakLists[] = getParameter(peakLists).getValue()
                .getMatchingPeakLists();
        if (selectedPeakLists.length > 0) {
            RawDataFile plDataFiles[] = selectedPeakLists[0].getRawDataFiles();
            getParameter(dataFiles).setChoices(plDataFiles);
        }
        return super.showSetupDialog(parent, valueCheckRequired);
    }

}
