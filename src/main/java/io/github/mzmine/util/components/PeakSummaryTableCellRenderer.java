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

package io.github.mzmine.util.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class PeakSummaryTableCellRenderer extends DefaultTableCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        Component newComponent = super.getTableCellRendererComponent(table,
                value, isSelected, hasFocus, row, column);

        PeakSummaryTableModel listElementModel = (PeakSummaryTableModel) table
                .getModel();

        if (column == 0) {
            newComponent.setForeground(listElementModel.getPeakColor(row));
            ((JLabel) newComponent).setHorizontalAlignment(SwingConstants.LEFT);
        } else {
            newComponent.setForeground(Color.BLACK);
            ((JLabel) newComponent)
                    .setHorizontalAlignment(SwingConstants.CENTER);
        }

        return newComponent;
    }

}
