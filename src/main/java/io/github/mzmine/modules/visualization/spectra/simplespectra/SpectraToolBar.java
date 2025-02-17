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

package io.github.mzmine.modules.visualization.spectra.simplespectra;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import io.github.mzmine.datamodel.MassSpectrumType;
import io.github.mzmine.util.GUIUtils;
import io.github.mzmine.util.swing.IconUtil;

/**
 * Spectra visualizer's toolbar class
 */
public class SpectraToolBar extends JToolBar {

    private static final long serialVersionUID = 1L;
    static final Icon centroidIcon = IconUtil
            .loadIconFromResources("icons/centroidicon.png");
    static final Icon continuousIcon = IconUtil
            .loadIconFromResources("icons/continuousicon.png");
    static final Icon dataPointsIcon = IconUtil
            .loadIconFromResources("icons/datapointsicon.png");
    static final Icon annotationsIcon = IconUtil
            .loadIconFromResources("icons/annotationsicon.png");
    static final Icon pickedPeakIcon = IconUtil
            .loadIconFromResources("icons/pickedpeakicon.png");
    static final Icon isotopePeakIcon = IconUtil
            .loadIconFromResources("icons/isotopepeakicon.png");
    static final Icon axesIcon = IconUtil
            .loadIconFromResources("icons/axesicon.png");
    static final Icon exportIcon = IconUtil
            .loadIconFromResources("icons/exporticon.png");
    static final Icon dbOnlineIcon = IconUtil
            .loadIconFromResources("icons/DBOnlineIcon.png");
    static final Icon dbCustomIcon = IconUtil
            .loadIconFromResources("icons/DBCustomIcon.png");
    static final Icon dbLipidsIcon = IconUtil
            .loadIconFromResources("icons/DBLipidsIcon.png");
    static final Icon dbSpectraIcon = IconUtil
            .loadIconFromResources("icons/DBSpectraIcon.png");
    static final Icon sumFormulaIcon = IconUtil
            .loadIconFromResources("icons/search.png");

    private JButton centroidContinuousButton, dataPointsButton;

    public SpectraToolBar(ActionListener masterFrame) {

        super(JToolBar.VERTICAL);

        setFloatable(false);
        setFocusable(false);
        setMargin(new Insets(5, 5, 5, 5));
        setBackground(Color.white);

        centroidContinuousButton = GUIUtils.addButton(this, null, centroidIcon,
                masterFrame, "TOGGLE_PLOT_MODE",
                "Toggle centroid/continuous mode");

        addSeparator();

        dataPointsButton = GUIUtils.addButton(this, null, dataPointsIcon,
                masterFrame, "SHOW_DATA_POINTS",
                "Toggle displaying of data points  in continuous mode");

        addSeparator();

        GUIUtils.addButton(this, null, annotationsIcon, masterFrame,
                "SHOW_ANNOTATIONS", "Toggle displaying of peak values");

        addSeparator();

        GUIUtils.addButton(this, null, pickedPeakIcon, masterFrame,
                "SHOW_PICKED_PEAKS", "Toggle displaying of picked peaks");

        addSeparator();

        GUIUtils.addButton(this, null, isotopePeakIcon, masterFrame,
                "SHOW_ISOTOPE_PEAKS",
                "Toggle displaying of predicted isotope peaks");

        addSeparator();

        GUIUtils.addButton(this, null, axesIcon, masterFrame, "SETUP_AXES",
                "Setup ranges for axes");

        addSeparator();

        GUIUtils.addButton(this, null, exportIcon, masterFrame,
                "EXPORT_SPECTRA", "Export spectra to spectra file");

        addSeparator();

        GUIUtils.addButton(this, null, exportIcon, masterFrame,
                "CREATE_LIBRARY_ENTRY", "Create spectral library entry");

        addSeparator();

        GUIUtils.addButton(this, null, dbOnlineIcon, masterFrame,
                "ONLINEDATABASESEARCH",
                "Select online database for annotation");

        addSeparator();

        GUIUtils.addButton(this, null, dbCustomIcon, masterFrame,
                "CUSTOMDATABASESEARCH",
                "Select custom database for annotation");

        addSeparator();

        GUIUtils.addButton(this, null, dbLipidsIcon, masterFrame, "LIPIDSEARCH",
                "Select target lipid classes for annotation");

        addSeparator();

        GUIUtils.addButton(this, null, dbSpectraIcon, masterFrame,
                "SPECTRALDATABASESEARCH",
                "Compare spectrum with spectral database");

        addSeparator();

        GUIUtils.addButton(this, null, sumFormulaIcon, masterFrame,
                "SUMFORMULA", "Predict sum formulas for annotation");

    }

    public void setCentroidButton(MassSpectrumType spectrumType) {
        if (spectrumType == MassSpectrumType.CENTROIDED) {
            centroidContinuousButton.setIcon(continuousIcon);
            dataPointsButton.setEnabled(false);
        } else {
            centroidContinuousButton.setIcon(centroidIcon);
            dataPointsButton.setEnabled(true);
        }
    }

}
