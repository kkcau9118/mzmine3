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

package io.github.mzmine.modules.visualization.spectra.simplespectra.spectraidentification.lipidsearch;

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
import io.github.mzmine.parameters.parametertypes.tolerances.MZToleranceParameter;

/**
 * Parameters for lipid search module for speactra identification
 * 
 * @author Ansgar Korf (ansgar.korf@uni-muenster.de)
 */
public class SpectraIdentificationLipidSearchParameters
        extends SimpleParameterSet {

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
            "m/z tolerance:",
            "Enter m/z tolerance for exact mass database matching");

    public static final DoubleParameter noiseLevel = new DoubleParameter(
            "Noise level",
            "Intensities less than this value are interpreted as noise",
            MZmineCore.getConfiguration().getIntensityFormat(), 0.0);

    public static final ComboParameter<IonizationType> ionizationMethod = new ComboParameter<IonizationType>(
            "Ionization method",
            "Type of ion used to calculate the ionized mass",
            IonizationType.values());

    public static final BooleanParameter useModification = new BooleanParameter(
            "Search for lipid modification",
            "If checked the algorithm searches for lipid modifications");

    public static final LipidModificationChoiceParameter modification = new LipidModificationChoiceParameter(
            "Lipid modifications", "Add lipid modifications",
            new LipidModification[0], 0);

    public SpectraIdentificationLipidSearchParameters() {
        super(new Parameter[] { lipidClasses, minChainLength, maxChainLength,
                minDoubleBonds, maxDoubleBonds, ionizationMethod, mzTolerance,
                noiseLevel, useModification, modification });
    }
}
