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

package io.github.mzmine.modules.dataprocessing.align_join;

import io.github.mzmine.modules.tools.isotopepatternscore.IsotopePatternScoreParameters;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.BooleanParameter;
import io.github.mzmine.parameters.parametertypes.DoubleParameter;
import io.github.mzmine.parameters.parametertypes.StringParameter;
import io.github.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import io.github.mzmine.parameters.parametertypes.submodules.OptionalModuleParameter;
import io.github.mzmine.parameters.parametertypes.tolerances.MZToleranceParameter;
import io.github.mzmine.parameters.parametertypes.tolerances.RTToleranceParameter;

public class JoinAlignerParameters extends SimpleParameterSet {

    public static final PeakListsParameter peakLists = new PeakListsParameter();

    public static final StringParameter peakListName = new StringParameter(
            "Feature list name", "Feature list name", "Aligned feature list");

    public static final MZToleranceParameter MZTolerance = new MZToleranceParameter();

    public static final DoubleParameter MZWeight = new DoubleParameter(
            "Weight for m/z", "Score for perfectly matching m/z values");

    public static final RTToleranceParameter RTTolerance = new RTToleranceParameter();

    public static final DoubleParameter RTWeight = new DoubleParameter(
            "Weight for RT", "Score for perfectly matching RT values");

    public static final BooleanParameter SameChargeRequired = new BooleanParameter(
            "Require same charge state",
            "If checked, only rows having same charge state can be aligned");

    public static final BooleanParameter SameIDRequired = new BooleanParameter(
            "Require same ID",
            "If checked, only rows having same compound identities (or no identities) can be aligned");

    public static final OptionalModuleParameter compareIsotopePattern = new OptionalModuleParameter(
            "Compare isotope pattern",
            "If both peaks represent an isotope pattern, add isotope pattern score to match score",
            new IsotopePatternScoreParameters());

    public static final OptionalModuleParameter compareSpectraSimilarity = new OptionalModuleParameter(
            "Compare spectra similarity",
            "Compare MS1 or MS2 spectra similarity",
            new JoinAlignerSpectraSimilarityScoreParameters());

    public JoinAlignerParameters() {
        super(new Parameter[] { peakLists, peakListName, MZTolerance, MZWeight,
                RTTolerance, RTWeight, SameChargeRequired, SameIDRequired,
                compareIsotopePattern, compareSpectraSimilarity });
    }

}
