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

package io.github.mzmine.modules.visualization.featurelisttable;

import javax.annotation.Nonnull;

import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.MZmineModule;
import io.github.mzmine.parameters.ParameterSet;

public class PeakListTableModule implements MZmineModule {

    private static final String MODULE_NAME = "Feature list table";

    @Override
    public @Nonnull String getName() {
        return MODULE_NAME;
    }

    public static void showNewPeakListVisualizerWindow(PeakList peakList) {
        ParameterSet parameters = MZmineCore.getConfiguration()
                .getModuleParameters(PeakListTableModule.class);
        final PeakListTableWindow window = new PeakListTableWindow(peakList,
                parameters);
        window.setVisible(true);
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return PeakListTableParameters.class;
    }

}
