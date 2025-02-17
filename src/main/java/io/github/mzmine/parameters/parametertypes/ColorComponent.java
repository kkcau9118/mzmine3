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
package io.github.mzmine.parameters.parametertypes;

import java.awt.Color;
import javax.swing.JPanel;

import io.github.mzmine.gui.framework.JColorPickerButton;

public class ColorComponent extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final JColorPickerButton colorPicker;

    public ColorComponent(Color color) {
        colorPicker = new JColorPickerButton(this, color);
        add(colorPicker);
    }

    public void setColor(Color color) {
        colorPicker.setColor(color);
    }

    public Color getColor() {
        return colorPicker.getColor();
    }

    @Override
    public void setToolTipText(String toolTip) {
        colorPicker.setToolTipText(toolTip);
    }
}
