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

package io.github.mzmine.modules.tools.mzrangecalculator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.Range;

import io.github.mzmine.datamodel.IonizationType;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.MZmineModule;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import io.github.mzmine.util.ExitCode;
import io.github.mzmine.util.FormulaUtils;

/**
 * m/z range calculator module. Calculates m/z range from a given chemical
 * formula and m/z tolerance.
 */
public class MzRangeFormulaCalculatorModule implements MZmineModule {

    private static final String MODULE_NAME = "m/z range calculator from formula";

    @Override
    public @Nonnull String getName() {
        return MODULE_NAME;
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return MzRangeFormulaCalculatorParameters.class;
    }

    /**
     * Shows the calculation dialog and returns the calculated m/z range. May
     * return null in case user clicked Cancel.
     */
    @Nullable
    public static Range<Double> showRangeCalculationDialog() {

        ParameterSet myParameters = MZmineCore.getConfiguration()
                .getModuleParameters(MzRangeFormulaCalculatorModule.class);

        if (myParameters == null)
            return null;

        ExitCode exitCode = myParameters.showSetupDialog(null, true);
        if (exitCode != ExitCode.OK)
            return null;

        return getMzRangeFromFormula(myParameters);
    }

    @Nullable
    public static Range<Double> getMzRangeFromFormula(
            ParameterSet myParameters) {
        String formula = myParameters
                .getParameter(MzRangeFormulaCalculatorParameters.formula)
                .getValue().trim();
        IonizationType ionType = myParameters
                .getParameter(MzRangeFormulaCalculatorParameters.ionType)
                .getValue();
        MZTolerance mzTolerance = myParameters
                .getParameter(MzRangeFormulaCalculatorParameters.mzTolerance)
                .getValue();
        Integer charge = myParameters
                .getParameter(MzRangeFormulaCalculatorParameters.charge)
                .getValue();

        return getMzRangeFromFormula(formula, ionType, mzTolerance, charge);
    }

    @Nullable
    public static Range<Double> getMzRangeFromFormula(String formula,
            IonizationType ionType, MZTolerance mzTolerance, Integer charge) {
        if ((formula == null) || (ionType == null) || (mzTolerance == null)
                || (charge == null))
            return null;

        String ionizedFormula = FormulaUtils.ionizeFormula(formula, ionType,
                charge);
        double calculatedMZ = FormulaUtils.calculateExactMass(ionizedFormula,
                charge) / charge;

        return mzTolerance.getToleranceRange(calculatedMZ);
    }

}
