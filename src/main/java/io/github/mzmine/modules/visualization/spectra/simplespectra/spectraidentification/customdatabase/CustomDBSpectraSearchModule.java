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

package io.github.mzmine.modules.visualization.spectra.simplespectra.spectraidentification.customdatabase;

import javax.annotation.Nonnull;

import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.MZmineModule;
import io.github.mzmine.modules.visualization.spectra.simplespectra.SpectraPlot;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.util.ExitCode;

/**
 * Module for identifying peaks by searching custom databases file.
 * 
 * @author Ansgar Korf (ansgar.korf@uni-muenster)
 */
public class CustomDBSpectraSearchModule implements MZmineModule {

    private static final String MODULE_NAME = "Custom database search";
    private static final String MODULE_DESCRIPTION = "This module attepts to annotate signals in selected mass spectra";

    @Override
    public @Nonnull String getName() {
        return MODULE_NAME;
    }

    public @Nonnull String getDescription() {
        return MODULE_DESCRIPTION;
    }

    /**
     * Show dialog for identifying a single peak-list row.
     * 
     */
    public static void showSpectraIdentificationDialog(final Scan scan,
            final SpectraPlot spectraPlot) {

        final SpectraIdentificationCustomDatabaseParameters parameters = (SpectraIdentificationCustomDatabaseParameters) MZmineCore
                .getConfiguration()
                .getModuleParameters(CustomDBSpectraSearchModule.class);
        ;

        // Run task.
        if (parameters.showSetupDialog(MZmineCore.getDesktop().getMainWindow(),
                true) == ExitCode.OK) {

            MZmineCore.getTaskController()
                    .addTask(new SpectraIdentificationCustomDatabaseTask(
                            parameters.cloneParameterSet(), scan, spectraPlot));
        }
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return SpectraIdentificationCustomDatabaseParameters.class;
    }
}
