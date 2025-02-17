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

package io.github.mzmine.modules.io.exportscans;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.ArrayUtils;

import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.modules.MZmineModuleCategory;
import io.github.mzmine.modules.MZmineProcessingModule;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.selectors.ScanSelection;
import io.github.mzmine.taskcontrol.Task;
import io.github.mzmine.util.ExitCode;

/**
 * Exports scans around a center time
 */
public class ExportScansFromRawFilesModule implements MZmineProcessingModule {

    private static final String MODULE_NAME = "Export scans into one file";
    private static final String MODULE_DESCRIPTION = "Export scans or mass lists into one file ";

    @Override
    public @Nonnull String getName() {
        return MODULE_NAME;
    }

    @Override
    public @Nonnull String getDescription() {
        return MODULE_DESCRIPTION;
    }

    @Override
    @Nonnull
    public ExitCode runModule(@Nonnull MZmineProject project,
            @Nonnull ParameterSet parameters, @Nonnull Collection<Task> tasks) {

        ScanSelection select = parameters
                .getParameter(ExportScansFromRawFilesParameters.scanSelect)
                .getValue();
        Scan[] scans = new Scan[0];
        for (RawDataFile raw : parameters
                .getParameter(ExportScansFromRawFilesParameters.dataFiles)
                .getValue().getMatchingRawDataFiles()) {
            scans = ArrayUtils.addAll(scans, select.getMatchingScans(raw));
        }

        ExportScansTask task = new ExportScansTask(scans, parameters);
        tasks.add(task);
        return ExitCode.OK;
    }

    @Override
    public @Nonnull MZmineModuleCategory getModuleCategory() {
        return MZmineModuleCategory.RAWDATA;
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return ExportScansFromRawFilesParameters.class;
    }

}
