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

package io.github.mzmine.modules.visualization.twod;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import io.github.mzmine.util.GUIUtils;
import io.github.mzmine.util.swing.IconUtil;

/**
 * 2D visualizer's toolbar class
 */
class TwoDToolBar extends JToolBar {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    static final Icon paletteIcon = IconUtil
            .loadIconFromResources("icons/colorbaricon.png");
    static final Icon dataPointsIcon = IconUtil
            .loadIconFromResources("icons/datapointsicon.png");
    static final Icon axesIcon = IconUtil
            .loadIconFromResources("icons/axesicon.png");
    static final Icon centroidIcon = IconUtil
            .loadIconFromResources("icons/centroidicon.png");
    static final Icon continuousIcon = IconUtil
            .loadIconFromResources("icons/continuousicon.png");
    static final Icon tooltipsIcon = IconUtil
            .loadIconFromResources("icons/tooltips2dploticon.png");
    static final Icon notooltipsIcon = IconUtil
            .loadIconFromResources("icons/notooltips2dploticon.png");
    static final Icon logScaleIcon = IconUtil
            .loadIconFromResources("icons/logicon.png");

    private JButton centroidContinuousButton, toggleContinuousModeButton,
            toggleTooltipButton;

    TwoDToolBar(TwoDVisualizerWindow masterFrame) {

        super(JToolBar.VERTICAL);

        setFloatable(false);
        setFocusable(false);
        setMargin(new Insets(5, 5, 5, 5));
        setBackground(Color.white);

        GUIUtils.addButton(this, null, paletteIcon, masterFrame,
                "SWITCH_PALETTE", "Switch palette");

        addSeparator();

        toggleContinuousModeButton = GUIUtils.addButton(this, null,
                dataPointsIcon, masterFrame, "SHOW_DATA_POINTS",
                "Toggle displaying of data points in continuous mode");

        addSeparator();

        GUIUtils.addButton(this, null, axesIcon, masterFrame, "SETUP_AXES",
                "Setup ranges for axes");

        addSeparator();

        centroidContinuousButton = GUIUtils.addButton(this, null, centroidIcon,
                masterFrame, "SWITCH_PLOTMODE",
                "Switch between continuous and centroided mode");

        addSeparator();

        toggleTooltipButton = GUIUtils.addButton(this, null, tooltipsIcon,
                masterFrame, "SWITCH_TOOLTIPS",
                "Toggle displaying of tool tips on the peaks");

        addSeparator();

        GUIUtils.addButton(this, null, logScaleIcon, masterFrame,
                "SWITCH_LOG_SCALE", "Set Log Scale");

    }

    void setCentroidButton(boolean centroid) {
        if (centroid) {
            centroidContinuousButton.setIcon(centroidIcon);
        } else {
            centroidContinuousButton.setIcon(continuousIcon);
        }
    }

    void toggleContinuousModeButtonSetEnable(boolean enable) {
        toggleContinuousModeButton.setEnabled(enable);
    }

    void setTooltipButton(boolean tooltip) {
        if (tooltip) {
            toggleTooltipButton.setIcon(tooltipsIcon);
        } else {
            toggleTooltipButton.setIcon(notooltipsIcon);
        }
    }

}
