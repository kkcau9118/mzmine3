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

package io.github.mzmine.modules.dataprocessing.align_hierarchical;

import java.util.Comparator;

import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.util.SortingDirection;

/**
 * 
 * 
 */
public class RawDataFileSorter implements Comparator<RawDataFile> {

    private SortingDirection direction;

    public RawDataFileSorter(SortingDirection direction) {
        this.direction = direction;
    }

    public int compare(RawDataFile rdf1, RawDataFile rdf2) {

        String rdf1Value = rdf1.getName();
        String rdf2Value = rdf2.getName();

        if (direction == SortingDirection.Ascending)
            return rdf1Value.compareTo(rdf2Value);
        else
            return rdf2Value.compareTo(rdf1Value);

    }

}
