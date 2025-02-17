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

package io.github.mzmine.modules.dataanalysis.clustering.farthestfirst;

import io.github.mzmine.modules.dataanalysis.clustering.VisualizationType;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.ComboParameter;
import io.github.mzmine.parameters.parametertypes.IntegerParameter;

public class FarthestFirstClustererParameters extends SimpleParameterSet {

    public static final ComboParameter<VisualizationType> visualization = new ComboParameter<VisualizationType>(
            "Visualization type",
            "Select the kind of visualization for the clustering result",
            VisualizationType.values());

    public static final IntegerParameter numberOfGroups = new IntegerParameter(
            "Number of clusters to generate",
            "Specify the number of clusters to generate.", 3);

    public FarthestFirstClustererParameters() {
        super(new Parameter[] { visualization, numberOfGroups });
    }
}
