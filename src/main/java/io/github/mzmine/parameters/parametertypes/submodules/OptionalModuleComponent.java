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

package io.github.mzmine.parameters.parametertypes.submodules;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.dialogs.ParameterSetupDialog;

/**
 */
public class OptionalModuleComponent extends JPanel implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JCheckBox checkBox;
    private JButton setButton;
    private ParameterSet embeddedParameters;

    public OptionalModuleComponent(ParameterSet embeddedParameters) {

        super(new FlowLayout(FlowLayout.LEFT));

        this.embeddedParameters = embeddedParameters;

        checkBox = new JCheckBox();
        checkBox.addActionListener(this);
        add(checkBox);

        setButton = new JButton("Setup..");
        setButton.addActionListener(this);
        setButton.setEnabled(false);
        add(setButton);

    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public void setSelected(boolean selected) {
        checkBox.setSelected(selected);
        setButton.setEnabled(selected);
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        Object src = event.getSource();

        if (src == checkBox) {
            boolean checkBoxSelected = checkBox.isSelected();
            setButton.setEnabled(checkBoxSelected);
        }

        if (src == setButton) {
            ParameterSetupDialog dialog = (ParameterSetupDialog) SwingUtilities
                    .getAncestorOfClass(ParameterSetupDialog.class, this);
            if (dialog != null)
                embeddedParameters.showSetupDialog(dialog,
                        dialog.isValueCheckRequired());
            else {
                // regular window? or null
                Window window = (Window) SwingUtilities
                        .getAncestorOfClass(Window.class, this);
                embeddedParameters.showSetupDialog(window, false);
            }
        }

    }

    @Override
    public void setToolTipText(String toolTip) {
        checkBox.setToolTipText(toolTip);
    }

    public void addItemListener(ItemListener il) {
        checkBox.addItemListener(il);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setButton.setEnabled(enabled);
        checkBox.setEnabled(enabled);
    }
}
