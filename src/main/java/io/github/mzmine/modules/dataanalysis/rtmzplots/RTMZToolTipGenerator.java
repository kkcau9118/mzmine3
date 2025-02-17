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

package io.github.mzmine.modules.dataanalysis.rtmzplots;

import org.jfree.chart.labels.XYZToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

public class RTMZToolTipGenerator implements XYZToolTipGenerator {

    public String generateToolTip(RTMZDataset dataset, int series, int item) {
        return dataset.getPeakListRow(item).toString();
    }

    public String generateToolTip(XYDataset dataset, int series, int item) {
        if (dataset instanceof RTMZDataset)
            return ((RTMZDataset) dataset).getPeakListRow(item).toString();
        return null;
    }

    public String generateToolTip(XYZDataset dataset, int series, int item) {
        if (dataset instanceof RTMZDataset)
            return ((RTMZDataset) dataset).getPeakListRow(item).toString();
        return null;
    }
}
