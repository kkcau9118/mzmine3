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

package io.github.mzmine.gui.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.gui.preferences.MZminePreferences;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.main.NewVersionCheck;
import io.github.mzmine.main.NewVersionCheck.CheckType;
import io.github.mzmine.modules.MZmineModuleCategory;
import io.github.mzmine.modules.MZmineRunnableModule;
import io.github.mzmine.modules.io.projectload.ProjectOpeningTask;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import io.github.mzmine.parameters.parametertypes.selectors.PeakListsSelectionType;
import io.github.mzmine.parameters.parametertypes.selectors.RawDataFilesParameter;
import io.github.mzmine.parameters.parametertypes.selectors.RawDataFilesSelectionType;
import io.github.mzmine.project.parameterssetup.ProjectParametersSetupDialog;
import io.github.mzmine.taskcontrol.Task;
import io.github.mzmine.util.ExitCode;
import io.github.mzmine.util.GUIUtils;

/**
 * This class represents the main menu of MZmine desktop
 */
public class MainMenu extends JMenuBar implements ActionListener {

    private static final long serialVersionUID = 1L;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private JMenu projectMenu, rawDataMenu, peakListMenu, visualizationMenu,
            helpMenu, rawDataFilteringMenu, peakDetectionMenu, gapFillingMenu,
            isotopesMenu, peakListPeakPickingMenu, peakListFilteringMenu,
            alignmentMenu, normalizationMenu, identificationMenu,
            dataAnalysisMenu, peakListExportMenu,
            peakListSpectralDeconvolutionMenu, toolsMenu;

    private JMenu lastProjectsSub;

    private WindowsMenu windowsMenu;

    private JMenuItem projectSampleParameters, projectPreferences,
            projectSaveParameters, projectLoadParameters, projectExit,
            showAbout, checkUpdate;

    private int projectIOMenuIndex = 0, projectMenuIndex = 1,
            rawDataMenuIndex = 0, peakListMenuIndex = 0,
            visualizationMenuIndex = 0, exportMenuIndex = 0;

    private Map<JMenuItem, MZmineRunnableModule> moduleMenuItems = new Hashtable<JMenuItem, MZmineRunnableModule>();

    private List<File> lastProjects;

    MainMenu() {

        /*
         * Project menu
         */
        projectMenu = new JMenu("Project");
        projectMenu.setMnemonic(KeyEvent.VK_P);
        add(projectMenu);

        lastProjectsSub = new JMenu("Open last project ...");
        lastProjectsSub.setEnabled(false);
        projectMenu.add(lastProjectsSub);
        projectMenuIndex++;
        projectIOMenuIndex++;

        // project IO items go here (e.g. project load, save)

        projectMenu.addSeparator();

        // module items go here (e.g. batch mode)

        projectMenu.addSeparator();

        projectSampleParameters = GUIUtils.addMenuItem(projectMenu,
                "Set sample parameters", this, KeyEvent.VK_P);

        projectMenu.addSeparator();

        projectPreferences = GUIUtils.addMenuItem(projectMenu,
                "Set preferences", this, KeyEvent.VK_S);

        projectMenu.addSeparator();

        projectSaveParameters = GUIUtils.addMenuItem(projectMenu,
                "Save MZmine parameters", this);
        projectLoadParameters = GUIUtils.addMenuItem(projectMenu,
                "Load MZmine parameters", this);

        projectMenu.addSeparator();

        projectExit = GUIUtils.addMenuItem(projectMenu, "Exit", this,
                KeyEvent.VK_X, true);

        /*
         * Raw data methods menu
         */

        rawDataMenu = new JMenu("Raw data methods");
        rawDataMenu.setMnemonic(KeyEvent.VK_R);
        add(rawDataMenu);

        rawDataFilteringMenu = new JMenu("Filtering");
        rawDataFilteringMenu.setMnemonic(KeyEvent.VK_F);
        rawDataMenu.add(rawDataFilteringMenu);

        peakDetectionMenu = new JMenu("Feature detection");
        peakDetectionMenu.setMnemonic(KeyEvent.VK_D);
        rawDataMenu.add(peakDetectionMenu);

        /*
         * Feature list methods menu
         */

        peakListMenu = new JMenu("Feature list methods");
        peakListMenu.setMnemonic(KeyEvent.VK_L);
        this.add(peakListMenu);

        peakListPeakPickingMenu = new JMenu("Feature detection");
        peakListPeakPickingMenu.setMnemonic(KeyEvent.VK_P);
        peakListMenu.add(peakListPeakPickingMenu);

        peakListSpectralDeconvolutionMenu = new JMenu("Spectral deconvolution");
        peakListSpectralDeconvolutionMenu.setMnemonic(KeyEvent.VK_S);
        peakListMenu.add(peakListSpectralDeconvolutionMenu);

        gapFillingMenu = new JMenu("Gap filling");
        gapFillingMenu.setMnemonic(KeyEvent.VK_G);
        peakListMenu.add(gapFillingMenu);

        isotopesMenu = new JMenu("Isotopes");
        isotopesMenu.setMnemonic(KeyEvent.VK_D);
        peakListMenu.add(isotopesMenu);

        peakListFilteringMenu = new JMenu("Filtering");
        peakListFilteringMenu.setMnemonic(KeyEvent.VK_P);
        peakListMenu.add(peakListFilteringMenu);

        alignmentMenu = new JMenu("Alignment");
        alignmentMenu.setMnemonic(KeyEvent.VK_A);
        peakListMenu.add(alignmentMenu);

        normalizationMenu = new JMenu("Normalization");
        normalizationMenu.setMnemonic(KeyEvent.VK_N);
        peakListMenu.add(normalizationMenu);

        identificationMenu = new JMenu("Identification");
        identificationMenu.setMnemonic(KeyEvent.VK_I);
        peakListMenu.add(identificationMenu);

        dataAnalysisMenu = new JMenu("Data analysis");
        dataAnalysisMenu.setMnemonic(KeyEvent.VK_S);
        peakListMenu.add(dataAnalysisMenu);

        peakListExportMenu = new JMenu("Export/Import");
        peakListExportMenu.setMnemonic(KeyEvent.VK_E);
        peakListMenu.add(peakListExportMenu);

        peakListExportMenu.addSeparator();

        /*
         * Visualization menu
         */

        visualizationMenu = new JMenu("Visualization");
        visualizationMenu.setMnemonic(KeyEvent.VK_V);
        this.add(visualizationMenu);

        visualizationMenu.addSeparator();

        /*
         * Tools menu
         */

        toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        this.add(toolsMenu);

        /*
         * Windows menu
         */

        windowsMenu = new WindowsMenu();
        windowsMenu.setMnemonic(KeyEvent.VK_W);
        this.add(windowsMenu);

        /*
         * Help menu
         */

        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        this.add(helpMenu);

        showAbout = new JMenuItem("About MZmine 2");
        showAbout.addActionListener(this);
        addMenuItem(MZmineModuleCategory.HELPSYSTEM, showAbout);

        checkUpdate = GUIUtils.addMenuItem(helpMenu, "Check for update", this);
    }

    public synchronized void addMenuItem(MZmineModuleCategory parentMenu,
            JMenuItem newItem) {
        switch (parentMenu) {
        case PROJECTIO:
            projectMenu.add(newItem, projectIOMenuIndex);
            projectIOMenuIndex++;
            projectMenuIndex++;
            break;
        case PROJECT:
            projectMenu.add(newItem, projectMenuIndex);
            projectMenuIndex++;
            break;
        case RAWDATA:
            rawDataMenu.add(newItem, rawDataMenuIndex);
            rawDataMenuIndex++;
            break;
        case PEAKLIST:
            peakListMenu.add(newItem, peakListMenuIndex);
            peakListMenuIndex++;
            break;
        case RAWDATAFILTERING:
            rawDataFilteringMenu.add(newItem);
            break;
        case PEAKPICKING:
            peakDetectionMenu.add(newItem);
            break;
        case PEAKLISTPICKING:
            peakListPeakPickingMenu.add(newItem);
            break;
        case SPECTRALDECONVOLUTION:
            peakListSpectralDeconvolutionMenu.add(newItem);
            break;
        case GAPFILLING:
            gapFillingMenu.add(newItem);
            break;
        case ISOTOPES:
            isotopesMenu.add(newItem);
            break;
        case PEAKLISTFILTERING:
            peakListFilteringMenu.add(newItem);
            break;
        case ALIGNMENT:
            alignmentMenu.add(newItem);
            break;
        case NORMALIZATION:
            normalizationMenu.add(newItem);
            break;
        case IDENTIFICATION:
            identificationMenu.add(newItem);
            break;
        case PEAKLISTEXPORT:
            peakListExportMenu.add(newItem, exportMenuIndex);
            exportMenuIndex++;
            break;
        case PEAKLISTIMPORT:
            peakListExportMenu.add(newItem);
            break;
        case VISUALIZATIONRAWDATA:
            visualizationMenu.add(newItem, visualizationMenuIndex);
            visualizationMenuIndex++;
            break;
        case VISUALIZATIONPEAKLIST:
            visualizationMenu.add(newItem);
            break;
        case DATAANALYSIS:
            dataAnalysisMenu.add(newItem);
            break;
        case HELPSYSTEM:
            helpMenu.add(newItem);
            break;
        case TOOLS:
            toolsMenu.add(newItem);
            break;
        }
    }

    public void addMenuItemForModule(MZmineRunnableModule module) {
        MZmineModuleCategory parentMenu = module.getModuleCategory();
        String menuItemText = module.getName();
        String menuItemToolTip = module.getDescription();

        JMenuItem newItem = new JMenuItem(menuItemText);
        newItem.setToolTipText(menuItemToolTip);
        newItem.addActionListener(this);

        /*
         * Shortcuts keys to open, save and close a project. Implementation will
         * be changed with JavaFX.
         */
        if (menuItemText == "Open project") {
            newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                    ActionEvent.CTRL_MASK));
        }
        if (menuItemText == "Save project") {
            newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    ActionEvent.CTRL_MASK));
        }
        if (menuItemText == "Close project") {
            newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                    ActionEvent.CTRL_MASK));
        }

        moduleMenuItems.put(newItem, module);

        addMenuItem(parentMenu, newItem);
    }

    public void setLastProjects(List<File> list) {
        lastProjects = list;

        SwingUtilities.invokeLater(() -> {
            lastProjectsSub.removeAll();

            if (list == null || list.isEmpty()) {
                lastProjectsSub.setEnabled(false);
                return;
            }
            lastProjectsSub.setEnabled(true);

            // add items to load last used projects directly
            lastProjects.stream().map(File::getAbsolutePath).forEach(name -> {
                JMenuItem item = new JMenuItem(name);
                item.addActionListener(e -> {
                    JMenuItem c = (JMenuItem) e.getSource();
                    if (c != null) {
                        File f = new File(c.getText());
                        if (f.exists()) {
                            // load file
                            ProjectOpeningTask newTask = new ProjectOpeningTask(
                                    f);
                            MZmineCore.getTaskController().addTask(newTask);
                        }
                    }
                });
                lastProjectsSub.add(item);
            });
        });
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        Object src = e.getSource();

        MZmineRunnableModule module = moduleMenuItems.get(src);
        if (module != null) {
            ParameterSet moduleParameters = MZmineCore.getConfiguration()
                    .getModuleParameters(module.getClass());

            RawDataFile selectedFiles[] = MZmineCore.getDesktop()
                    .getSelectedDataFiles();
            if (selectedFiles.length > 0) {
                for (Parameter<?> p : moduleParameters.getParameters()) {
                    if (p instanceof RawDataFilesParameter) {
                        RawDataFilesParameter rdp = (RawDataFilesParameter) p;
                        rdp.setValue(
                                RawDataFilesSelectionType.GUI_SELECTED_FILES);
                    }
                }

            }
            PeakList selectedPeakLists[] = MZmineCore.getDesktop()
                    .getSelectedPeakLists();
            if (selectedPeakLists.length > 0) {
                for (Parameter<?> p : moduleParameters.getParameters()) {
                    if (p instanceof PeakListsParameter) {
                        PeakListsParameter plp = (PeakListsParameter) p;
                        plp.setValue(
                                PeakListsSelectionType.GUI_SELECTED_PEAKLISTS);
                    }
                }
            }

            logger.finest("Setting parameters for module " + module.getName());
            ExitCode exitCode = moduleParameters.showSetupDialog(
                    MZmineCore.getDesktop().getMainWindow(), true);
            if (exitCode == ExitCode.OK) {
                ParameterSet parametersCopy = moduleParameters
                        .cloneParameterSet();
                logger.finest("Starting module " + module.getName()
                        + " with parameters " + parametersCopy);
                ArrayList<Task> tasks = new ArrayList<Task>();
                MZmineProject project = MZmineCore.getProjectManager()
                        .getCurrentProject();
                module.runModule(project, parametersCopy, tasks);
                MZmineCore.getTaskController()
                        .addTasks(tasks.toArray(new Task[0]));
            }
            return;
        }

        if (src == projectExit) {
            MZmineCore.getDesktop().exitMZmine();
        }

        if (src == projectSaveParameters) {
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser
                    .showSaveDialog(MZmineCore.getDesktop().getMainWindow());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File configFile = chooser.getSelectedFile();
                try {
                    MZmineCore.getConfiguration().saveConfiguration(configFile);
                } catch (Exception ex) {
                    MZmineCore.getDesktop().displayException(
                            MZmineCore.getDesktop().getMainWindow(), ex);
                }
            }
        }

        if (src == projectLoadParameters) {
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser
                    .showOpenDialog(MZmineCore.getDesktop().getMainWindow());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File configFile = chooser.getSelectedFile();
                try {
                    MZmineCore.getConfiguration().loadConfiguration(configFile);
                } catch (Exception ex) {
                    MZmineCore.getDesktop().displayException(
                            MZmineCore.getDesktop().getMainWindow(), ex);
                }
            }
        }

        if (src == projectSampleParameters) {
            ProjectParametersSetupDialog dialog = new ProjectParametersSetupDialog();
            dialog.setVisible(true);
        }

        if (src == projectPreferences) {
            MZminePreferences preferences = MZmineCore.getConfiguration()
                    .getPreferences();
            preferences.showSetupDialog(MZmineCore.getDesktop().getMainWindow(),
                    true);
        }

        if (src == showAbout) {
            MainWindow mainWindow = (MainWindow) MZmineCore.getDesktop();
            mainWindow.showAboutDialog();
        }

        if (src == checkUpdate) { // Check for updated version
            NewVersionCheck NVC = new NewVersionCheck(CheckType.MENU);
            new Thread(NVC).start();
        }

    }
}
