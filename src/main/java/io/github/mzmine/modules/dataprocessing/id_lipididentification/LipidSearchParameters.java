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

package io.github.mzmine.modules.dataprocessing.id_lipididentification;

import java.awt.Window;

import io.github.mzmine.datamodel.IonizationType;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.lipids.AllLipidClasses;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.lipids.lipidmodifications.LipidModification;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.BooleanParameter;
import io.github.mzmine.parameters.parametertypes.ComboParameter;
import io.github.mzmine.parameters.parametertypes.DoubleParameter;
import io.github.mzmine.parameters.parametertypes.IntegerParameter;
import io.github.mzmine.parameters.parametertypes.LipidClassParameter;
import io.github.mzmine.parameters.parametertypes.LipidModificationChoiceParameter;
import io.github.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import io.github.mzmine.parameters.parametertypes.tolerances.MZToleranceParameter;
import io.github.mzmine.util.ExitCode;

/**
 * Parameters for lipid search module
 * 
 * @author Ansgar Korf (ansgar.korf@uni-muenster.de)
 */
public class LipidSearchParameters extends SimpleParameterSet {

    public static final PeakListsParameter peakLists = new PeakListsParameter();

    public static final LipidClassParameter<Object> lipidClasses = new LipidClassParameter<Object>(
            "Lipid classes", "Selection of lipid backbones",
            AllLipidClasses.getList().toArray());

    public static final IntegerParameter minChainLength = new IntegerParameter(
            "Minimum number of carbon in chains",
            "Minimum number of carbon in chains");

    public static final IntegerParameter maxChainLength = new IntegerParameter(
            "Maximum number of carbon in chains",
            "Maximum number of carbon in chains");

    public static final IntegerParameter minDoubleBonds = new IntegerParameter(
            "Minimum number of double bonds",
            "Minumum number of double bonds in all chains");

    public static final IntegerParameter maxDoubleBonds = new IntegerParameter(
            "Maximum number of double bonds",
            "Maximum number of double bonds in all chains");

    public static final MZToleranceParameter mzTolerance = new MZToleranceParameter(
            "m/z tolerance MS1 level:",
            "Enter m/z tolerance for exact mass database matching on MS1 level");

    public static final ComboParameter<IonizationType> ionizationMethod = new ComboParameter<IonizationType>(
            "Ionization method",
            "Type of ion used to calculate the ionized mass",
            IonizationType.values());

    public static final BooleanParameter searchForMSMSFragments = new BooleanParameter(
            "Search for lipid class specific fragments in MS/MS spectra",
            "Search for lipid class specific fragments in MS/MS spectra");

    public static final MZToleranceParameter mzToleranceMS2 = new MZToleranceParameter(
            "m/z tolerance MS2 level:",
            "Enter m/z tolerance for exact mass database matching on MS2 level");

    public static final DoubleParameter noiseLevel = new DoubleParameter(
            "Noise level for MS/MS scans",
            "Intensities less than this value are interpreted as noise",
            MZmineCore.getConfiguration().getIntensityFormat(), 0.0);

    public static final BooleanParameter useModification = new BooleanParameter(
            "Search for lipid modification",
            "If checked the algorithm searches for lipid modifications");

    public static final LipidModificationChoiceParameter modification = new LipidModificationChoiceParameter(
            "Lipid modifications", "Add lipid modifications",
            new LipidModification[0], 0);

    public LipidSearchParameters() {
        super(new Parameter[] { peakLists, lipidClasses, minChainLength,
                maxChainLength, minDoubleBonds, maxDoubleBonds,
                ionizationMethod, mzTolerance, searchForMSMSFragments,
                mzToleranceMS2, noiseLevel, useModification, modification });

    }

    @Override
    public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired) {
        LipidSearchParameterSetupDialog dialog = new LipidSearchParameterSetupDialog(
                parent, valueCheckRequired, this);
        dialog.setVisible(true);
        return dialog.getExitCode();
    }

}
