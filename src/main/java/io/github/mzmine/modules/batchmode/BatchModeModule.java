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

package io.github.mzmine.modules.batchmode;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.modules.MZmineModuleCategory;
import io.github.mzmine.modules.MZmineProcessingModule;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.taskcontrol.Task;
import io.github.mzmine.taskcontrol.TaskStatus;
import io.github.mzmine.util.ExitCode;

/**
 * Batch mode module
 */
public class BatchModeModule implements MZmineProcessingModule {

    private static Logger logger = Logger
            .getLogger(BatchModeModule.class.getName());

    private static final String MODULE_NAME = "Batch mode";
    private static final String MODULE_DESCRIPTION = "This module allows execution of multiple processing tasks in a batch.";

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
        BatchTask newTask = new BatchTask(project, parameters);

        /*
         * We do not add the task to the tasks collection, but instead directly
         * submit to the task controller, because we need to set the priority to
         * HIGH. If the priority is not HIGH and the maximum number of
         * concurrent tasks is set to 1 in the MZmine preferences, then this
         * BatchTask would block all other tasks. See getTaskPriority in
         * BatchTask
         */
        tasks.add(newTask);

        return ExitCode.OK;
    }

    @Override
    public @Nonnull MZmineModuleCategory getModuleCategory() {
        return MZmineModuleCategory.PROJECT;
    }

    public static ExitCode runBatch(@Nonnull MZmineProject project,
            File batchFile) {

        logger.info("Running batch from file " + batchFile);

        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document parsedBatchXML = docBuilder.parse(batchFile);
            BatchQueue newQueue = BatchQueue
                    .loadFromXml(parsedBatchXML.getDocumentElement());
            ParameterSet parameters = new BatchModeParameters();
            parameters.getParameter(BatchModeParameters.batchQueue)
                    .setValue(newQueue);
            Task batchTask = new BatchTask(project, parameters);
            batchTask.run();
            if (batchTask.getStatus() == TaskStatus.FINISHED)
                return ExitCode.OK;
            else
                return ExitCode.ERROR;
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Error while running batch", e);
            e.printStackTrace();
            return ExitCode.ERROR;
        }

    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return BatchModeParameters.class;
    }

}
