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

/*
 * Code created was by or on behalf of Syngenta and is released under the open source license in use
 * for the pre-existing code or project. Syngenta does not assert ownership or copyright any over
 * pre-existing work.
 */
package io.github.mzmine.parameters.parametertypes.filenames;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.FutureTask;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;

/**
 */
public class DirectoryComponent extends JPanel implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // Text field width.
    private static final int TEXT_FIELD_COLUMNS = 15;

    // Text field font.
    private static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN,
            10);

    // Chooser title.
    private static final String TITLE = "Select Directory";

    // Text field.
    private final JTextField txtDirectory;

    /**
     * Create the component.
     */
    public DirectoryComponent() {

        super(new BorderLayout());

        // Create text field.
        txtDirectory = new JTextField();
        txtDirectory.setColumns(TEXT_FIELD_COLUMNS);
        txtDirectory.setFont(SMALL_FONT);

        // Chooser button.
        final JButton btnFileBrowser = new JButton("...");
        btnFileBrowser.addActionListener(this);

        add(txtDirectory, BorderLayout.CENTER);
        add(btnFileBrowser, BorderLayout.EAST);
    }

    public File getValue() {

        return new File(txtDirectory.getText());
    }

    public void setValue(final File value) {

        txtDirectory.setText(value.getPath());
    }

    @Override
    public void setToolTipText(final String text) {

        txtDirectory.setToolTipText(text);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {

        // Create chooser.
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle(TITLE);

        // Set current directory.
        final String currentPath = txtDirectory.getText();
        if (currentPath.length() > 0) {

            final File currentFile = new File(currentPath);
            final File currentDir = currentFile.getParentFile();
            if (currentDir != null && currentDir.exists()) {
                fileChooser.setInitialDirectory(currentDir);
            }
        }

        // Open chooser.
        final FutureTask<File> task = new FutureTask<>(
                () -> fileChooser.showDialog(null));
        Platform.runLater(task);

        try {
            File selectedFile = task.get();
            if (selectedFile == null)
                return;
            txtDirectory.setText(selectedFile.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
