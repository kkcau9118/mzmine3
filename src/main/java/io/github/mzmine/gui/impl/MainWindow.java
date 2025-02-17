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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;

import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.gui.Desktop;
import io.github.mzmine.gui.impl.helpwindow.HelpWindow;
import io.github.mzmine.gui.preferences.ErrorMail;
import io.github.mzmine.gui.preferences.ErrorMailSettings;
import io.github.mzmine.gui.preferences.MZminePreferences;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.MZmineModule;
import io.github.mzmine.modules.MZmineModuleCategory;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.WindowSettingsParameter;
import io.github.mzmine.util.ExceptionUtils;
import io.github.mzmine.util.ExitCode;
import io.github.mzmine.util.TextUtils;
import javafx.application.Platform;

/**
 * This class is the main window of application
 * 
 */
public class MainWindow extends JFrame
        implements MZmineModule, Desktop, WindowListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    static final String aboutHelpID = "net/sf/mzmine/desktop/help/AboutMZmine.html";

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private MainPanel mainPanel;
    private StatusBar statusBar;

    private Image mzmineIcon;

    private MainMenu menuBar;

    private int mailCounter;

    public MainMenu getMainMenu() {
        return menuBar;
    }

    /**
     * WindowListener interface implementation
     */
    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        exitMZmine();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void setStatusBarText(String text) {
        setStatusBarText(text, Color.black);
    }

    /**
     */
    @Override
    public void displayMessage(Window window, String msg) {
        displayMessage(window, "Message", msg, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     */
    @Override
    public void displayMessage(Window window, String title, String msg) {
        displayMessage(window, title, msg, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void displayErrorMessage(Window window, String msg) {
        displayMessage(window, "Error", msg);
    }

    @Override
    public void displayErrorMessage(Window window, String title, String msg) {
        displayMessage(window, title, msg, JOptionPane.ERROR_MESSAGE);
    }

    public void displayMessage(Window window, String title, String msg,
            int type) {

        // sending error message with a maximum of 5
        if (MZminePreferences.sendErrorEMail.getValue() != null
                && MZminePreferences.sendErrorEMail.getValue()
                && mailCounter < 5) {
            ErrorMail errorMail = new ErrorMail();
            try {
                errorMail.sendErrorEmail(
                        ErrorMailSettings.eMailAddress.getValue(),
                        ErrorMailSettings.eMailAddress.getValue(),
                        ErrorMailSettings.smtpHost.getValue(),
                        "MZmine 2 error! ", msg,
                        ErrorMailSettings.eMailPassword.getValue(),
                        ErrorMailSettings.smtpPort.getValue());
                mailCounter++;
            } catch (IOException e) {
                e.printStackTrace();
                logger.info("Sending mail error");
            }
        }

        assert msg != null;

        // If the message does not contain newline characters, wrap it
        // automatically
        String wrappedMsg;
        if (msg.contains("\n"))
            wrappedMsg = msg;
        else
            wrappedMsg = TextUtils.wrapText(msg, 80);

        JOptionPane.showMessageDialog(window, wrappedMsg, title, type);
    }

    public void addMenuItem(MZmineModuleCategory parentMenu,
            JMenuItem newItem) {
        menuBar.addMenuItem(parentMenu, newItem);
    }

    /**
     * @see io.github.mzmine.gui.Desktop#getSelectedDataFiles()
     */
    @Override
    public RawDataFile[] getSelectedDataFiles() {
        return mainPanel.getRawDataTree().getSelectedObjects(RawDataFile.class);
    }

    @Override
    public PeakList[] getSelectedPeakLists() {
        return mainPanel.getPeakListTree().getSelectedObjects(PeakList.class);
    }

    public void initModule() {

        assert SwingUtilities.isEventDispatchThread();

        try {
            final InputStream mzmineIconStream = DesktopSetup.class
                    .getClassLoader().getResourceAsStream("MZmineIcon.png");
            this.mzmineIcon = ImageIO.read(mzmineIconStream);
            mzmineIconStream.close();
            setIconImage(mzmineIcon);
        } catch (Throwable e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Could not set application icon", e);
        }

        DesktopSetup desktopSetup = new DesktopSetup();
        desktopSetup.init();

        setLayout(new BorderLayout());

        mainPanel = new MainPanel();
        add(mainPanel, BorderLayout.CENTER);

        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);

        // Construct menu
        menuBar = new MainMenu();
        setJMenuBar(menuBar);

        // Initialize window listener for responding to user events
        addWindowListener(this);

        pack();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        // Set initial window size to 1000x700 pixels, but check the screen size
        // first
        int width = Math.min(screenSize.width, 1000);
        int height = Math.min(screenSize.height, 700);
        setBounds(0, 0, width, height);
        setLocationRelativeTo(null);

        // Application wants to control closing by itself
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        updateTitle();

        // get the window settings parameter
        ParameterSet paramSet = MZmineCore.getConfiguration().getPreferences();
        WindowSettingsParameter settings = paramSet
                .getParameter(MZminePreferences.windowSetttings);

        // listen for changes
        this.addComponentListener(settings);

    }

    public void updateTitle() {
        String projectName = MZmineCore.getProjectManager().getCurrentProject()
                .toString();
        setTitle(
                "MZmine " + MZmineCore.getMZmineVersion() + ": " + projectName);
    }

    /**
     * @see io.github.mzmine.gui.Desktop#getMainFrame()
     */
    @Override
    public JFrame getMainWindow() {
        return this;
    }

    /**
     * @see io.github.mzmine.gui.Desktop#setStatusBarText(java.lang.String,
     *      java.awt.Color)
     */
    @Override
    public void setStatusBarText(String text, Color textColor) {

        // If the request was caused by exception during MZmine startup, desktop
        // may not be initialized yet
        if ((mainPanel == null) || (statusBar == null))
            return;

        statusBar.setStatusText(text, textColor);
    }

    @Override
    public void displayException(Window window, Exception e) {
        displayErrorMessage(window, ExceptionUtils.exceptionToString(e));
    }

    public MainPanel getMainPanel() {
        return mainPanel;
    }

    public void showAboutDialog() {
        Platform.runLater(() -> {
            final URL aboutPage = getClass().getClassLoader()
                    .getResource("aboutpage/AboutMZmine.html");
            HelpWindow aboutWindow = new HelpWindow(aboutPage.toString());
            aboutWindow.show();
        });

    }

    @Override
    public void addRawDataTreeListener(TreeModelListener listener) {
        TreeModel model = getMainPanel().getRawDataTree().getModel();
        model.addTreeModelListener(listener);
    }

    @Override
    public void removeRawDataTreeListener(TreeModelListener listener) {
        TreeModel model = getMainPanel().getRawDataTree().getModel();
        model.removeTreeModelListener(listener);
    }

    @Override
    public void addPeakListTreeListener(TreeModelListener listener) {
        TreeModel model = getMainPanel().getPeakListTree().getModel();
        model.addTreeModelListener(listener);
    }

    @Override
    public void removePeakListTreeListener(TreeModelListener listener) {
        TreeModel model = getMainPanel().getPeakListTree().getModel();
        model.removeTreeModelListener(listener);
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return SimpleParameterSet.class;
    }

    @Override
    public @Nonnull ExitCode exitMZmine() {

        int selectedValue = JOptionPane.showInternalConfirmDialog(
                this.getContentPane(), "Are you sure you want to exit?",
                "Exiting...", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (selectedValue != JOptionPane.YES_OPTION)
            return ExitCode.CANCEL;

        this.dispose();

        logger.info("Exiting MZmine");

        System.exit(0);

        return ExitCode.OK;
    }

    @Override
    public @Nonnull String getName() {
        return "MZmine main window";
    }

    /**
     * Menu items for the last used projects
     * 
     * @param list
     */
    public void createLastUsedProjectsMenu(List<File> list) {
        getMainMenu().setLastProjects(list);
    }

    @Override
    public @Nullable Image getMZmineIcon() {
        return mzmineIcon;
    }

}
