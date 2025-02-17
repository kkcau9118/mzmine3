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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

import io.github.mzmine.datamodel.MassList;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.main.MZmineCore;

public class MassListComponent extends JPanel implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JTextField nameField;
    private JButton lookupButton;
    private JPopupMenu lookupMenu;

    public MassListComponent() {

        super(new BorderLayout());

        setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        nameField = new JTextField(15);

        add(nameField, BorderLayout.CENTER);

        lookupButton = new JButton("Choose...");
        lookupButton.addActionListener(this);
        add(lookupButton, BorderLayout.EAST);

        lookupMenu = new JPopupMenu("Select name");

    }

    public String getValue() {
        return nameField.getText();
    }

    public void setValue(String value) {
        nameField.setText(value);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Object src = e.getSource();

        if (src == lookupButton) {
            List<String> currentNames = getMassListNames();

            lookupMenu.removeAll();
            for (String name : currentNames) {
                JMenuItem item = new JMenuItem(name);
                item.addActionListener(this);
                lookupMenu.add(item);
            }

            lookupMenu.show(lookupButton, 0, 0);

        }

        if (src instanceof JMenuItem) {
            String name = ((JMenuItem) src).getText();
            nameField.setText(name);
        }

    }

    @Override
    public void setToolTipText(String toolTip) {
        nameField.setToolTipText(toolTip);
    }

    /**
     * Method returns the list of all identified MassList names in scans
     * 
     * @return unique MassList names
     */
    public static List<String> getMassListNames() {
        ArrayList<String> names = new ArrayList<>();
        RawDataFile dataFiles[] = MZmineCore.getProjectManager()
                .getCurrentProject().getDataFiles();
        for (RawDataFile dataFile : dataFiles) {
            int scanNums[] = dataFile.getScanNumbers();
            for (int scanNum : scanNums) {
                Scan scan = dataFile.getScan(scanNum);
                MassList massLists[] = scan.getMassLists();
                for (MassList massList : massLists) {
                    String name = massList.getName();
                    if (!names.contains(name))
                        names.add(name);
                }
            }
        }

        return names;
    }

    public void addDocumentListener(DocumentListener dl) {
        nameField.getDocument().addDocumentListener(dl);
    }
}
