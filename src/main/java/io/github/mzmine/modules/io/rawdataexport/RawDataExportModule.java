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

package io.github.mzmine.modules.io.rawdataexport;

import java.io.File;
import java.util.Collection;
import javax.annotation.Nonnull;
import io.github.msdk.MSDKRuntimeException;
import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.modules.MZmineModuleCategory;
import io.github.mzmine.modules.MZmineProcessingModule;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.taskcontrol.Task;
import io.github.mzmine.util.ExitCode;
import io.github.mzmine.util.files.FileAndPathUtil;

/**
 * Raw data export module
 */
public class RawDataExportModule implements MZmineProcessingModule {

    private static final String MODULE_NAME = "Raw data export";
    private static final String MODULE_DESCRIPTION = "This module exports raw data files from your MZmine project into various formats";

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
    public ExitCode runModule(final @Nonnull MZmineProject project,
            @Nonnull ParameterSet parameters, @Nonnull Collection<Task> tasks) {
        RawDataFileType type = parameters
                .getParameter(RawDataExportParameters.type).getValue();
        String extension = "";
        switch (type) {
        case MZML:
            extension = "mzML";
            break;
        case NETCDF:
            extension = "cdf";
            break;
        default:
            throw new MSDKRuntimeException(
                    "This format is not covered in the export module");
        }

        File folder = parameters.getParameter(RawDataExportParameters.fileName)
                .getValue();
        if (!folder.isDirectory())
            folder = folder.getParentFile();

        RawDataFile[] dataFile = parameters
                .getParameter(RawDataExportParameters.dataFiles).getValue()
                .getMatchingRawDataFiles();

        for (RawDataFile r : dataFile) {
            File fullName = FileAndPathUtil.getRealFilePath(folder, r.getName(),
                    extension);
            Task newTask = new RawDataExportTask(r, fullName);
            tasks.add(newTask);
        }
        return ExitCode.OK;
    }

    @Override
    public @Nonnull MZmineModuleCategory getModuleCategory() {
        return MZmineModuleCategory.RAWDATA;
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return RawDataExportParameters.class;
    }

}
