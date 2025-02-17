/*
 * Copyright (C) 2017 Du-Lab Team <dulab.binf@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package io.github.mzmine.modules.dataprocessing.featdet_adap3decompositionV2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.google.common.collect.Sets;

import dulab.adap.datamodel.BetterComponent;
import dulab.adap.datamodel.BetterPeak;
import dulab.adap.workflow.decomposition.ComponentSelector;
import dulab.adap.workflow.decomposition.RetTimeClusterer;
import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.datamodel.PeakListRow;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.dialogs.ParameterSetupDialog;
import io.github.mzmine.util.GUIUtils;

/**
 * @author Du-Lab Team <dulab.binf@gmail.com>
 */

public class ADAP3DecompositionV2SetupDialog extends ParameterSetupDialog {
    /**
     * Minimum dimensions of plots
     */
    private static final Dimension MIN_DIMENSIONS = new Dimension(400, 300);

    /**
     * Font for the preview combo elements
     */
    private static final Font COMBO_FONT = new Font("SansSerif", Font.PLAIN,
            10);

    private static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);

    /**
     * One of three states: > no changes made, > change in the first phase
     * parameters, > change in the second phase parameters
     */
    private enum CHANGE_STATE {
        NONE, FIRST_PHASE, SECOND_PHASE
    }

    /**
     * Elements of the interface
     */
    private JPanel pnlUIElements;
    private JPanel pnlComboBoxes;
    private JPanel pnlPlots;
    private JCheckBox chkPreview;
    private JComboBox<ChromatogramPeakPair> cboPeakLists;
    private JComboBox<RetTimeClusterer.Cluster> cboClusters;
    private JButton btnRefresh;
    private SimpleScatterPlot retTimeMZPlot;
    private EICPlot retTimeIntensityPlot;

    /**
     * Current values of the parameters
     */
    private Object[] currentParameters;

    /**
     * Creates an instance of the class and saves the current values of all
     * parameters
     */
    ADAP3DecompositionV2SetupDialog(Window parent, boolean valueCheckRequired,
            @Nonnull final ParameterSet parameters) {
        super(parent, valueCheckRequired, parameters);

        Parameter[] params = parameters.getParameters();
        int size = params.length;

        currentParameters = new Object[size];
        for (int i = 0; i < size; ++i)
            currentParameters[i] = params[i].getValue();
    }

    /**
     * Creates the interface elements
     */
    @Override
    protected void addDialogComponents() {
        super.addDialogComponents();

        // -----------------------------
        // Panel with preview UI elements
        // -----------------------------

        // Preview CheckBox
        chkPreview = new JCheckBox("Show preview");
        chkPreview.addActionListener(this);
        chkPreview.setHorizontalAlignment(SwingConstants.CENTER);
        chkPreview.setEnabled(true);

        // Preview panel that will contain ComboBoxes
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JSeparator(), BorderLayout.NORTH);
        panel.add(chkPreview, BorderLayout.CENTER);
        panel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        pnlUIElements = new JPanel(new BorderLayout());
        pnlUIElements.add(panel, BorderLayout.NORTH);

        // ComboBox for Feature lists
        cboPeakLists = new JComboBox<>();
        cboPeakLists.setFont(COMBO_FONT);
        for (ChromatogramPeakPair p : ChromatogramPeakPair
                .fromParameterSet(parameterSet).values())
            cboPeakLists.addItem(p);
        cboPeakLists.addActionListener(this);

        URL refreshImageURL = this.getClass()
                .getResource("images/refresh.16.png");
        btnRefresh = refreshImageURL != null
                ? new JButton(new ImageIcon(refreshImageURL))
                : new JButton("Refresh");
        btnRefresh.addActionListener(this);

        // ComboBox with Clusters
        cboClusters = new JComboBox<>();
        cboClusters.setFont(COMBO_FONT);
        cboClusters.addActionListener(this);

        pnlComboBoxes = GUIUtils.makeTablePanel(2, 3, 1,
                new JComponent[] { new JLabel("Feature Lists"), cboPeakLists,
                        btnRefresh, new JLabel("Clusters"), cboClusters,
                        new JPanel() });

        // --------------------------------------------------------------------
        // ----- Panel with plots --------------------------------------
        // --------------------------------------------------------------------

        pnlPlots = new JPanel();
        pnlPlots.setLayout(new BoxLayout(pnlPlots, BoxLayout.Y_AXIS));

        // Plot with retention-time clusters
        retTimeMZPlot = new SimpleScatterPlot("Retention time", "m/z");
        retTimeMZPlot.setMinimumSize(MIN_DIMENSIONS);
        retTimeMZPlot.setPreferredSize(MIN_DIMENSIONS);

        final JPanel pnlPlotRetTimeClusters = new JPanel(new BorderLayout());
        pnlPlotRetTimeClusters.setBackground(Color.white);
        pnlPlotRetTimeClusters.add(retTimeMZPlot, BorderLayout.CENTER);
        GUIUtils.addMarginAndBorder(pnlPlotRetTimeClusters, 10);

        // Plot with chromatograms
        retTimeIntensityPlot = new EICPlot();
        retTimeIntensityPlot.setMinimumSize(MIN_DIMENSIONS);
        retTimeIntensityPlot.setPreferredSize(MIN_DIMENSIONS);

        JPanel pnlPlotShapeClusters = new JPanel(new BorderLayout());
        pnlPlotShapeClusters.setBackground(Color.white);
        pnlPlotShapeClusters.add(retTimeIntensityPlot, BorderLayout.CENTER);
        GUIUtils.addMarginAndBorder(pnlPlotShapeClusters, 10);

        pnlPlots.add(pnlPlotRetTimeClusters);
        pnlPlots.add(pnlPlotShapeClusters);

        super.mainPanel.add(pnlUIElements, 0, super.getNumberOfParameters() + 3,
                2, 1, 0, 0, GridBagConstraints.HORIZONTAL);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        final Object source = e.getSource();

        if (source.equals(chkPreview)) {
            if (chkPreview.isSelected()) {
                // Set the height of the chkPreview to 200 cells, so it will
                // span
                // the whole vertical length of the dialog (buttons are at row
                // no 100). Also, we set the weight to 10, so the chkPreview
                // component will consume most of the extra available space.
                mainPanel.add(pnlPlots, 3, 0, 1, 200, 10, 10,
                        GridBagConstraints.BOTH);
                pnlUIElements.add(pnlComboBoxes, BorderLayout.CENTER);

                refresh();
            } else {
                mainPanel.remove(pnlPlots);
                pnlUIElements.remove(pnlComboBoxes);
            }

            updateMinimumSize();
            pack();
            setLocationRelativeTo(MZmineCore.getDesktop().getMainWindow());
        } else if (source.equals(btnRefresh))
            refresh();

        else if (source.equals(cboPeakLists))
            retTimeCluster();

        else if (source.equals(cboClusters))
            shapeCluster();
    }

    @Override
    public void parametersChanged() {
        super.updateParameterSetFromComponents();

        if (!chkPreview.isSelected())
            return;

        Cursor cursor = this.getCursor();
        this.setCursor(WAIT_CURSOR);

        switch (compareParameters(parameterSet.getParameters())) {
        case FIRST_PHASE:
            retTimeCluster();
            break;

        case SECOND_PHASE:
            shapeCluster();
            break;
        }

        this.setCursor(cursor);
    }

    private void refresh() {
        cboPeakLists.removeActionListener(this);
        cboPeakLists.removeAllItems();
        for (ChromatogramPeakPair p : ChromatogramPeakPair
                .fromParameterSet(parameterSet).values())
            cboPeakLists.addItem(p);
        cboPeakLists.addActionListener(this);

        if (cboPeakLists.getItemCount() > 0)
            cboPeakLists.setSelectedIndex(0);
    }

    /**
     * Cluster all peaks in PeakList based on retention time
     */
    private void retTimeCluster() {
        ChromatogramPeakPair chromatogramPeakPair = cboPeakLists
                .getItemAt(cboPeakLists.getSelectedIndex());
        if (chromatogramPeakPair == null)
            return;

        PeakList chromatogramList = chromatogramPeakPair.chromatograms;
        PeakList peakList = chromatogramPeakPair.peaks;
        if (chromatogramList == null || peakList == null)
            return;

        Double minDistance = parameterSet
                .getParameter(ADAP3DecompositionV2Parameters.PREF_WINDOW_WIDTH)
                .getValue();
        if (minDistance == null || minDistance <= 0.0)
            return;

        // Convert peakList into ranges
        List<RetTimeClusterer.Interval> ranges = Arrays
                .stream(peakList.getRows()).map(PeakListRow::getBestPeak)
                .map(p -> new RetTimeClusterer.Interval(
                        p.getRawDataPointsRTRange(), p.getMZ()))
                .collect(Collectors.toList());

        List<BetterPeak> peaks = new ADAP3DecompositionV2Utils()
                .getPeaks(peakList);

        // Form clusters of ranges
        List<RetTimeClusterer.Cluster> retTimeClusters = new RetTimeClusterer(
                minDistance).execute(peaks);

        cboClusters.removeAllItems();
        cboClusters.removeActionListener(this);
        for (RetTimeClusterer.Cluster cluster : retTimeClusters) {
            int i;

            for (i = 0; i < cboClusters.getItemCount(); ++i) {
                double retTime = cboClusters.getItemAt(i).retTime;
                if (cluster.retTime < retTime) {
                    cboClusters.insertItemAt(cluster, i);
                    break;
                }
            }

            if (i == cboClusters.getItemCount())
                cboClusters.addItem(cluster);
        }
        cboClusters.addActionListener(this);

        retTimeMZPlot.updateData(retTimeClusters);

        shapeCluster();
    }

    /**
     * Cluster list of PeakInfo based on the chromatographic shapes
     */
    private void shapeCluster() {
        ChromatogramPeakPair chromatogramPeakPair = cboPeakLists
                .getItemAt(cboPeakLists.getSelectedIndex());
        if (chromatogramPeakPair == null)
            return;

        PeakList chromatogramList = chromatogramPeakPair.chromatograms;
        PeakList peakList = chromatogramPeakPair.peaks;
        if (chromatogramList == null || peakList == null)
            return;

        final RetTimeClusterer.Cluster cluster = cboClusters
                .getItemAt(cboClusters.getSelectedIndex());
        if (cluster == null)
            return;

        Double retTimeTolerance = parameterSet
                .getParameter(ADAP3DecompositionV2Parameters.RET_TIME_TOLERANCE)
                .getValue();
        Boolean adjustApexRetTime = parameterSet
                .getParameter(
                        ADAP3DecompositionV2Parameters.ADJUST_APEX_RET_TIME)
                .getValue();
        Integer minClusterSize = parameterSet
                .getParameter(ADAP3DecompositionV2Parameters.MIN_CLUSTER_SIZE)
                .getValue();
        if (retTimeTolerance == null || retTimeTolerance <= 0.0
                || adjustApexRetTime == null || minClusterSize == null
                || minClusterSize <= 0)
            return;

        List<BetterPeak> chromatograms = new ADAP3DecompositionV2Utils()
                .getPeaks(chromatogramList);

        List<BetterComponent> components = null;
        try {
            components = new ComponentSelector().execute(chromatograms, cluster,
                    retTimeTolerance, adjustApexRetTime, minClusterSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (components != null)
            retTimeIntensityPlot.updateData(chromatograms, components); // chromatograms
    }

    private CHANGE_STATE compareParameters(Parameter[] newValues) {
        if (currentParameters == null) {
            int size = newValues.length;
            currentParameters = new Object[size];
            for (int i = 0; i < size; ++i)
                currentParameters[i] = newValues[i].getValue();

            return CHANGE_STATE.FIRST_PHASE;
        }

        final Set<Integer> firstPhaseIndices = new HashSet<>(
                Collections.singleton(2));
        final Set<Integer> secondPhaseIndices = new HashSet<>(
                Arrays.asList(3, 4, 5));

        int size = Math.min(currentParameters.length, newValues.length);

        Set<Integer> changedIndices = new HashSet<>();

        for (int i = 0; i < size; ++i) {
            Object oldValue = currentParameters[i];
            Object newValue = newValues[i].getValue();

            if (newValue != null && oldValue != null
                    && oldValue.equals(newValue))
                continue;

            changedIndices.add(i);
        }

        CHANGE_STATE result = CHANGE_STATE.NONE;

        if (!Sets.intersection(firstPhaseIndices, changedIndices).isEmpty())
            result = CHANGE_STATE.FIRST_PHASE;

        else if (!Sets.intersection(secondPhaseIndices, changedIndices)
                .isEmpty())
            result = CHANGE_STATE.SECOND_PHASE;

        for (int i = 0; i < size; ++i)
            currentParameters[i] = newValues[i].getValue();

        return result;
    }
}
