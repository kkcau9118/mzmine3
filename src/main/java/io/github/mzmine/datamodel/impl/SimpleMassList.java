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

package io.github.mzmine.datamodel.impl;

import javax.annotation.Nonnull;

import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.MassList;
import io.github.mzmine.datamodel.Scan;

/**
 * This class represent detected masses (ions) in one mass spectrum
 */
public class SimpleMassList implements MassList {

    private String name;
    private Scan scan;
    private DataPoint mzPeaks[];

    public SimpleMassList(String name, Scan scan, DataPoint mzPeaks[]) {
        this.name = name;
        this.scan = scan;
        this.mzPeaks = mzPeaks;
    }

    @Override
    public @Nonnull String getName() {
        return name;
    }

    @Override
    public @Nonnull Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    @Override
    public @Nonnull DataPoint[] getDataPoints() {
        return mzPeaks;
    }

    public void setDataPoints(DataPoint mzPeaks[]) {
        this.mzPeaks = mzPeaks;
    }

    @Override
    public String toString() {
        return name;
    }

}
