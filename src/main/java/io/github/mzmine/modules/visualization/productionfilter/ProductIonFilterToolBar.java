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

package io.github.mzmine.modules.visualization.productionfilter;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JToolBar;

import io.github.mzmine.util.GUIUtils;
import io.github.mzmine.util.swing.IconUtil;

/**
 * Product Ion visualizer's toolbar class
 */
class ProductIonFilterToolBar extends JToolBar {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    static final Icon dataPointsIcon = IconUtil
            .loadIconFromResources("icons/datapointsicon.png");

    ProductIonFilterToolBar(ProductIonFilterVisualizerWindow masterFrame) {

        super(JToolBar.VERTICAL);

        setFloatable(false);
        setFocusable(false);
        setMargin(new Insets(5, 5, 5, 5));
        setBackground(Color.white);

        GUIUtils.addButton(this, null, dataPointsIcon, masterFrame, "HIGHLIGHT",
                "Highlight selected precursor mass range");

    }

}
