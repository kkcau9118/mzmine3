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

package io.github.mzmine.gui.framework.fontspecs;

import java.awt.Font;
import javax.swing.JComboBox;

public class JFontStyleBox extends JComboBox<Object> {

    private enum Style {
        PLAIN, BOLD, ITALIC, BOLDITALIC
    }

    public JFontStyleBox() {
        super(Style.values());
        setSelectedIndex(0);
    }

    public int getSelectedStyle() {
        switch ((Style) getSelectedItem()) {
        case PLAIN:
            return Font.PLAIN;
        case BOLD:
            return Font.BOLD;
        case ITALIC:
            return Font.ITALIC;
        case BOLDITALIC:
            return Font.BOLD + Font.ITALIC;
        }
        return Font.PLAIN;
    }

}
