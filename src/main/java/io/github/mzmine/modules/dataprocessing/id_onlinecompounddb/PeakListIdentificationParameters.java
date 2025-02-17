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

package io.github.mzmine.modules.dataprocessing.id_onlinecompounddb;

import io.github.mzmine.datamodel.IonizationType;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.ComboParameter;
import io.github.mzmine.parameters.parametertypes.selectors.PeakListsParameter;

public class PeakListIdentificationParameters extends SimpleParameterSet {

    public static final PeakListsParameter peakLists = new PeakListsParameter();

    public static final ComboParameter<IonizationType> ionizationType = new ComboParameter<IonizationType>(
            "Ionization type", "Ionization type", IonizationType.values());

    public PeakListIdentificationParameters() {
        super(new Parameter[] { peakLists,
                SingleRowIdentificationParameters.DATABASE, ionizationType,
                SingleRowIdentificationParameters.MAX_RESULTS,
                SingleRowIdentificationParameters.MZ_TOLERANCE,
                SingleRowIdentificationParameters.ISOTOPE_FILTER });
    }

}
