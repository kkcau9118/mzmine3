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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import io.github.mzmine.gui.framework.listener.DelayedDocumentListener;

public class DoubleComponent extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final NumberFormat format;
    private final Double minimum;
    private final Double maximum;
    private final JTextField textField;

    public DoubleComponent(int inputsize, Double minimum, Double maximum,
            NumberFormat format) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.format = format;

        textField = new JTextField();
        textField.setPreferredSize(
                new Dimension(inputsize, textField.getPreferredSize().height));
        // Add an input verifier if any bounds are specified.
        if (minimum != null || maximum != null) {
            textField.setInputVerifier(new MinMaxVerifier());
        }

        add(textField);
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public String getText() {
        return textField.getText().trim();
    }

    @Override
    public void setToolTipText(String toolTip) {
        textField.setToolTipText(toolTip);
    }

    private boolean checkBounds(final double number) {

        return (minimum == null || number >= minimum)
                && (maximum == null || number <= maximum);
    }

    /**
     * Input verifier used when minimum or maximum bounds are defined.
     */
    private class MinMaxVerifier extends InputVerifier {

        @Override
        public boolean shouldYieldFocus(final JComponent input) {

            final boolean yield = super.shouldYieldFocus(input);
            if (!yield) {

                // Beep and highlight.
                Toolkit.getDefaultToolkit().beep();
                ((JTextComponent) input).selectAll();
            }

            return yield;
        }

        @Override
        public boolean verify(final JComponent input) {

            boolean verified = false;
            try {

                verified = checkBounds(
                        format.parse(((JTextComponent) input).getText())
                                .doubleValue());
            } catch (ParseException e) {

                // Not a number.
            }
            return verified;
        }
    }

    public JTextField getTextField() {
        return textField;
    }

    public void addDocumentListener(DelayedDocumentListener dl) {
        textField.getDocument().addDocumentListener(dl);
    }
}
