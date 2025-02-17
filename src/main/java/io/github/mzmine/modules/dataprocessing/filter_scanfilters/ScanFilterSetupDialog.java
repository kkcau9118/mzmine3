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

package io.github.mzmine.modules.dataprocessing.filter_scanfilters;

import java.awt.Color;
import java.awt.Window;

import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.modules.visualization.spectra.simplespectra.SpectraPlot;
import io.github.mzmine.modules.visualization.spectra.simplespectra.SpectraVisualizerWindow;
import io.github.mzmine.modules.visualization.spectra.simplespectra.datasets.ScanDataSet;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.dialogs.ParameterSetupDialogWithScanPreview;

/**
 * This class extends ParameterSetupDialog class, including a spectraPlot. This
 * is used to preview how the selected raw data filter and his parameters works
 * over the raw data file.
 */
public class ScanFilterSetupDialog extends ParameterSetupDialogWithScanPreview {

    private static final long serialVersionUID = 1L;
    private ParameterSet filterParameters;
    private ScanFilter rawDataFilter;

    /**
     * @param parameters
     * @param rawDataFilterTypeNumber
     */
    public ScanFilterSetupDialog(Window parent, boolean valueCheckRequired,
            ParameterSet filterParameters,
            Class<? extends ScanFilter> filterClass) {

        super(parent, valueCheckRequired, filterParameters);
        this.filterParameters = filterParameters;

        try {
            this.rawDataFilter = filterClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This function set all the information into the plot chart
     * 
     * @param scanNumber
     */
    protected void loadPreview(SpectraPlot spectrumPlot, Scan previewScan) {

        Scan newScan = rawDataFilter.filterScan(previewScan, filterParameters);

        ScanDataSet spectraDataSet = new ScanDataSet("Filtered scan", newScan);
        ScanDataSet spectraOriginalDataSet = new ScanDataSet("Original scan",
                previewScan);

        spectrumPlot.removeAllDataSets();

        spectrumPlot.addDataSet(spectraOriginalDataSet,
                SpectraVisualizerWindow.scanColor, true);
        spectrumPlot.addDataSet(spectraDataSet, Color.green, true);

        // if the scan is centroided, switch to centroid mode
        spectrumPlot.setPlotMode(previewScan.getSpectrumType());

    }
}
