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

package io.github.mzmine.modules.dataprocessing.filter_duplicatefilter;

import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.BooleanParameter;
import io.github.mzmine.parameters.parametertypes.ComboParameter;
import io.github.mzmine.parameters.parametertypes.StringParameter;
import io.github.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import io.github.mzmine.parameters.parametertypes.tolerances.MZToleranceParameter;
import io.github.mzmine.parameters.parametertypes.tolerances.RTToleranceParameter;

public class DuplicateFilterParameters extends SimpleParameterSet {

    public enum FilterMode {
        OLD_AVERAGE, NEW_AVERAGE, SINGLE_FEATURE;

        @Override
        public String toString() {
            return super.toString().replaceAll("_", " ");
        }
    }

    public static final PeakListsParameter peakLists = new PeakListsParameter();

    public static final StringParameter suffix = new StringParameter(
            "Name suffix", "Suffix to be added to feature list name",
            "filtered");

    public static final ComboParameter<FilterMode> filterMode = new ComboParameter<>(
            "Filter mode",
            "Old average: Only keep the row with the maximum avg area.\n New average: Create consensus row from duplicates (DETECTED>ESTIMATED>UNKNOWN).\n "
                    + "Single feature: Marks rows as duplicates if they share at least one feature (in one raw data file) with the same RT and m/z. Creates a consensus row.",
            FilterMode.values(), FilterMode.NEW_AVERAGE);

    public static final MZToleranceParameter mzDifferenceMax = new MZToleranceParameter(
            "m/z tolerance", "Maximum m/z difference between duplicate peaks");
    public static final RTToleranceParameter rtDifferenceMax = new RTToleranceParameter(
            "RT tolerance",
            "Maximum retention time difference between duplicate peaks");

    public static final BooleanParameter requireSameIdentification = new BooleanParameter(
            "Require same identification",
            "If checked, duplicate peaks must have same identification(s)");

    public static final BooleanParameter autoRemove = new BooleanParameter(
            "Remove original peaklist",
            "If checked, original peaklist will be removed and only deisotoped version remains");

    public DuplicateFilterParameters() {
        super(new Parameter[] { peakLists, suffix, filterMode, mzDifferenceMax,
                rtDifferenceMax, requireSameIdentification, autoRemove, });
    }

}
