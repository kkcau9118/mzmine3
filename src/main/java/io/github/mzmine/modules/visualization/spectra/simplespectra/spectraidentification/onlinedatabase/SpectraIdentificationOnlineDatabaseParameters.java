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

package io.github.mzmine.modules.visualization.spectra.simplespectra.spectraidentification.onlinedatabase;

import io.github.mzmine.datamodel.IonizationType;
import io.github.mzmine.modules.dataprocessing.id_onlinecompounddb.OnlineDatabases;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.ComboParameter;
import io.github.mzmine.parameters.parametertypes.DoubleParameter;
import io.github.mzmine.parameters.parametertypes.ModuleComboParameter;
import io.github.mzmine.parameters.parametertypes.tolerances.MZToleranceParameter;

/**
 * Parameters for identifying peaks by searching on-line databases.
 * 
 * @author Ansgar Korf (ansgar.korf@uni-muenster.de)
 */
public class SpectraIdentificationOnlineDatabaseParameters
        extends SimpleParameterSet {

    public static final ModuleComboParameter<OnlineDatabases> database = new ModuleComboParameter<OnlineDatabases>(
            "Database", "Database to search", OnlineDatabases.values());

    public static final ComboParameter<IonizationType> ionizationType = new ComboParameter<IonizationType>(
            "Ionization type", "Ionization type", IonizationType.values());

    public static final MZToleranceParameter mzTolerance = new MZToleranceParameter();

    public static final DoubleParameter noiseLevel = new DoubleParameter(
            "Noise level", "Set a noise level");

    public SpectraIdentificationOnlineDatabaseParameters() {
        super(new Parameter[] { database, ionizationType, mzTolerance,
                noiseLevel });
    }

}
