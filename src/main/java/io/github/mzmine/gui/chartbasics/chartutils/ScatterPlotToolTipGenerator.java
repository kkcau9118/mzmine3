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

package io.github.mzmine.gui.chartbasics.chartutils;

import java.text.NumberFormat;
import org.jfree.chart.labels.XYZToolTipGenerator;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

import io.github.mzmine.datamodel.PeakListRow;
import io.github.mzmine.main.MZmineCore;

/**
 * Tooltip generator for any type of scatter plots
 * 
 * @author Ansgar Korf (ansgar.korf@uni-muenster.de)
 */
public class ScatterPlotToolTipGenerator
        implements XYZToolTipGenerator, PublicCloneable {

    private String xAxisLabel, yAxisLabel, zAxisLabel;
    private NumberFormat numberFormat = MZmineCore.getConfiguration()
            .getMZFormat();
    private PeakListRow rows[];
    private String featureIdentity;

    public ScatterPlotToolTipGenerator(String xAxisLabel, String yAxisLabel,
            String zAxisLabel, PeakListRow rows[]) {
        super();
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.zAxisLabel = zAxisLabel;
        this.rows = rows;

    }

    @Override
    public String generateToolTip(XYZDataset dataset, int series, int item) {
        if (rows[item].getPreferredPeakIdentity() != null) {
            featureIdentity = rows[item].getPreferredPeakIdentity().getName();
            return String.valueOf(featureIdentity + "\n" + xAxisLabel + ": "
                    + numberFormat.format(dataset.getXValue(series, item)) + " "
                    + yAxisLabel + ": "
                    + numberFormat.format(dataset.getYValue(series, item)) + " "
                    + zAxisLabel + ": "
                    + numberFormat.format(dataset.getZValue(series, item)));
        } else {
            return String.valueOf(xAxisLabel + ": "
                    + numberFormat.format(dataset.getXValue(series, item)) + " "
                    + yAxisLabel + ": "
                    + numberFormat.format(dataset.getYValue(series, item)) + " "
                    + zAxisLabel + ": "
                    + numberFormat.format(dataset.getZValue(series, item)));
        }
    }

    @Override
    public String generateToolTip(XYDataset dataset, int series, int item) {
        if (rows[item].getPreferredPeakIdentity() != null) {
            featureIdentity = rows[item].getPreferredPeakIdentity().getName();
            return String.valueOf(featureIdentity + "\n" + xAxisLabel + ": "
                    + numberFormat.format(dataset.getXValue(series, item)) + " "
                    + yAxisLabel + ": "
                    + numberFormat.format(dataset.getYValue(series, item)));
        } else {
            return String.valueOf(xAxisLabel + ": "
                    + numberFormat.format(dataset.getXValue(series, item)) + " "
                    + yAxisLabel + ": "
                    + numberFormat.format(dataset.getYValue(series, item)));
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
