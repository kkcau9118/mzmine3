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

package io.github.mzmine.project.impl;

import java.io.File;

import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.io.projectload.ProjectLoadModule;
import io.github.mzmine.modules.io.projectload.ProjectLoaderParameters;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.project.ProjectManager;

/**
 * Project manager implementation
 */
public class ProjectManagerImpl implements ProjectManager {

    private static ProjectManagerImpl myInstance;

    MZmineProject currentProject;

    /**
     * @see io.github.mzmine.modules.MZmineModule#initModule(io.github.mzmine.main.MZmineCore)
     */
    public void initModule() {
        currentProject = new MZmineProjectImpl();
        myInstance = this;
    }

    public MZmineProject getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(MZmineProject project) {

        if (project == currentProject)
            return;

        // Close previous data files
        if (currentProject != null) {
            RawDataFile prevDataFiles[] = currentProject.getDataFiles();
            for (RawDataFile prevDataFile : prevDataFiles) {
                prevDataFile.close();
            }
        }

        this.currentProject = project;

        // This is a hack to keep correct value of last opened directory (this
        // value was overwritten when configuration file was loaded from the new
        // project)
        if (project.getProjectFile() != null) {
            File projectFile = project.getProjectFile();
            ParameterSet loaderParams = MZmineCore.getConfiguration()
                    .getModuleParameters(ProjectLoadModule.class);
            loaderParams.getParameter(ProjectLoaderParameters.projectFile)
                    .setValue(projectFile);
        }

        // Notify the GUI about project structure change
        ((MZmineProjectImpl) project).activateProject();

    }

    public static ProjectManagerImpl getInstance() {
        return myInstance;
    }

}
