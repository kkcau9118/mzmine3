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

package io.github.mzmine.modules.dataprocessing.id_isotopepeakscanner;

import java.util.ArrayList;

/**
 * This class serves as a buffer for possible result peaks. It stores the number
 * of possibly matching peaks for every expected isotope peak. The row index
 * (addRow), row ID (addID) are also stored so you can either manage via id or
 * row index.
 *
 * @author Steffen Heuckeroth steffen.heuckeroth@gmx.de /
 *         s_heuc03@uni-muenster.de
 *
 */
public class ResultBuffer {
    private int found;
    private ArrayList<Integer> row;
    private ArrayList<Integer> ID;

    public int getFoundCount() {
        return found;
    }

    public void addFound() {
        this.found++;
    }

    public int getSize() {
        return row.size();
    }

    public void addRow(int r) {
        row.add((Integer) r);
    }

    public void addID(int id) {
        ID.add((Integer) id);
    }

    public int getRow(int i) {
        return row.get(i).intValue();
    }

    public int getID(int i) {
        return ID.get(i).intValue();
    }

    public ResultBuffer() {
        found = 0;
        row = new ArrayList<Integer>();
        ID = new ArrayList<Integer>();
    }
}
