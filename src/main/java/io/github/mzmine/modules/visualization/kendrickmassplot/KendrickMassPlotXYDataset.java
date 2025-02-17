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

package io.github.mzmine.modules.visualization.kendrickmassplot;

import org.jfree.data.xy.AbstractXYDataset;

import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.datamodel.PeakListRow;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.util.FormulaUtils;

/**
 * XYDataset for Kendrick mass plots
 * 
 * @author Ansgar Korf (ansgar.korf@uni-muenster.de)
 */
class KendrickMassPlotXYDataset extends AbstractXYDataset {

    private static final long serialVersionUID = 1L;

    private PeakListRow selectedRows[];
    private String xAxisKMBase;
    private String customYAxisKMBase;
    private String customXAxisKMBase;
    private double[] xValues;
    private double[] yValues;
    private ParameterSet parameters;

    public KendrickMassPlotXYDataset(ParameterSet parameters) {

        PeakList peakList = parameters
                .getParameter(KendrickMassPlotParameters.peakList).getValue()
                .getMatchingPeakLists()[0];

        this.parameters = parameters;

        this.selectedRows = parameters
                .getParameter(KendrickMassPlotParameters.selectedRows)
                .getMatchingRows(peakList);

        this.customYAxisKMBase = parameters
                .getParameter(
                        KendrickMassPlotParameters.yAxisCustomKendrickMassBase)
                .getValue();

        if (parameters
                .getParameter(
                        KendrickMassPlotParameters.xAxisCustomKendrickMassBase)
                .getValue() == true) {
            this.customXAxisKMBase = parameters.getParameter(
                    KendrickMassPlotParameters.xAxisCustomKendrickMassBase)
                    .getEmbeddedParameter().getValue();
        } else {
            this.xAxisKMBase = parameters
                    .getParameter(KendrickMassPlotParameters.xAxisValues)
                    .getValue();
        }

        // Calc xValues
        xValues = new double[selectedRows.length];
        if (parameters
                .getParameter(
                        KendrickMassPlotParameters.xAxisCustomKendrickMassBase)
                .getValue() == true) {
            for (int i = 0; i < selectedRows.length; i++) {
                xValues[i] = Math
                        .ceil(selectedRows[i].getAverageMZ()
                                * getKendrickMassFactor(customXAxisKMBase))
                        - selectedRows[i].getAverageMZ()
                                * getKendrickMassFactor(customXAxisKMBase);
            }
        } else {
            for (int i = 0; i < selectedRows.length; i++) {

                // simply plot m/z values as x axis
                if (xAxisKMBase.equals("m/z")) {
                    xValues[i] = selectedRows[i].getAverageMZ();
                }

                // plot Kendrick masses as x axis
                else if (xAxisKMBase.equals("KM")) {
                    xValues[i] = selectedRows[i].getAverageMZ()
                            * getKendrickMassFactor(customYAxisKMBase);
                }
            }
        }

        // Calc yValues
        yValues = new double[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            yValues[i] = Math
                    .ceil((selectedRows[i].getAverageMZ())
                            * getKendrickMassFactor(customYAxisKMBase))
                    - (selectedRows[i].getAverageMZ())
                            * getKendrickMassFactor(customYAxisKMBase);
        }
    }

    public ParameterSet getParameters() {
        return parameters;
    }

    public void setParameters(ParameterSet parameters) {
        this.parameters = parameters;
    }

    @Override
    public int getItemCount(int series) {
        return selectedRows.length;
    }

    @Override
    public Number getX(int series, int item) {
        return xValues[item];
    }

    @Override
    public Number getY(int series, int item) {
        return yValues[item];
    }

    @Override
    public int getSeriesCount() {
        return 1;
    }

    public Comparable<?> getRowKey(int row) {
        return selectedRows[row].toString();
    }

    @Override
    public Comparable<?> getSeriesKey(int series) {
        return getRowKey(series);
    }

    public double[] getxValues() {
        return xValues;
    }

    public double[] getyValues() {
        return yValues;
    }

    public void setxValues(double[] values) {
        xValues = values;
    }

    public void setyValues(double[] values) {
        yValues = values;
    }

    private double getKendrickMassFactor(String formula) {
        double exactMassFormula = FormulaUtils.calculateExactMass(formula);
        return ((int) (exactMassFormula + 0.5d)) / exactMassFormula;
    }
}
