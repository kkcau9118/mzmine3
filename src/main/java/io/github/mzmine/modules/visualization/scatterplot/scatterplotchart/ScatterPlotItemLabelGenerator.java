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

package io.github.mzmine.modules.visualization.scatterplot.scatterplotchart;

import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.data.xy.XYDataset;

import io.github.mzmine.datamodel.PeakIdentity;
import io.github.mzmine.datamodel.PeakListRow;

public class ScatterPlotItemLabelGenerator implements XYItemLabelGenerator {

    /**
     * @see org.jfree.chart.labels.XYItemLabelGenerator#generateLabel(org.jfree.data.xy.XYDataset,
     *      int, int)
     */
    public String generateLabel(XYDataset dataSet, int series, int item) {

        ScatterPlotDataSet scatterDataSet = (ScatterPlotDataSet) dataSet;

        PeakListRow row = scatterDataSet.getRow(series, item);
        PeakIdentity identity = row.getPreferredPeakIdentity();
        if (identity != null) {
            return identity.getName();
        } else {
            return row.toString();
        }

    }
}
