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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 */
public class PercentComponent extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JTextField percentField;

    public PercentComponent() {

        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        percentField = new JTextField();
        percentField.setColumns(4);
        add(percentField);

        add(new JLabel("%"));

    }

    public void setValue(double value) {
        String stringValue = String.valueOf(value * 100);
        percentField.setText(stringValue);
    }

    public Double getValue() {
        String stringValue = percentField.getText();
        try {
            double doubleValue = Double.parseDouble(stringValue) / 100;
            return doubleValue;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void setToolTipText(String toolTip) {
        percentField.setToolTipText(toolTip);
    }

}
