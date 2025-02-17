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

package io.github.mzmine.modules.dataprocessing.norm_rtcalibration;

import java.util.Vector;
import java.util.logging.Logger;

import com.google.common.collect.Range;

import io.github.mzmine.datamodel.Feature;
import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.datamodel.PeakIdentity;
import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.datamodel.PeakListRow;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.PeakList.PeakListAppliedMethod;
import io.github.mzmine.datamodel.impl.SimpleFeature;
import io.github.mzmine.datamodel.impl.SimplePeakList;
import io.github.mzmine.datamodel.impl.SimplePeakListAppliedMethod;
import io.github.mzmine.datamodel.impl.SimplePeakListRow;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import io.github.mzmine.parameters.parametertypes.tolerances.RTTolerance;
import io.github.mzmine.taskcontrol.AbstractTask;
import io.github.mzmine.taskcontrol.TaskStatus;
import io.github.mzmine.util.PeakUtils;

class RTCalibrationTask extends AbstractTask {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private final MZmineProject project;
    private PeakList originalPeakLists[], normalizedPeakLists[];

    // Processed rows counter
    private int processedRows, totalRows;

    private String suffix;
    private MZTolerance mzTolerance;
    private RTTolerance rtTolerance;
    private double minHeight;
    private boolean removeOriginal;
    private ParameterSet parameters;

    public RTCalibrationTask(MZmineProject project, ParameterSet parameters) {

        this.project = project;
        this.originalPeakLists = parameters
                .getParameter(RTCalibrationParameters.peakLists).getValue()
                .getMatchingPeakLists();
        this.parameters = parameters;

        suffix = parameters.getParameter(RTCalibrationParameters.suffix)
                .getValue();
        mzTolerance = parameters
                .getParameter(RTCalibrationParameters.MZTolerance).getValue();
        rtTolerance = parameters
                .getParameter(RTCalibrationParameters.RTTolerance).getValue();
        minHeight = parameters.getParameter(RTCalibrationParameters.minHeight)
                .getValue();
        removeOriginal = parameters
                .getParameter(RTCalibrationParameters.autoRemove).getValue();

    }

    public double getFinishedPercentage() {
        if (totalRows == 0)
            return 0f;
        return (double) processedRows / (double) totalRows;
    }

    public String getTaskDescription() {
        return "Retention time normalization of " + originalPeakLists.length
                + " feature lists";
    }

    public void run() {

        setStatus(TaskStatus.PROCESSING);
        logger.info("Running retention time normalizer");

        // First we need to find standards by iterating through first feature
        // list
        totalRows = originalPeakLists[0].getNumberOfRows();

        // Create new feature lists
        normalizedPeakLists = new SimplePeakList[originalPeakLists.length];
        for (int i = 0; i < originalPeakLists.length; i++) {
            normalizedPeakLists[i] = new SimplePeakList(
                    originalPeakLists[i] + " " + suffix,
                    originalPeakLists[i].getRawDataFiles());

            // Remember how many rows we need to normalize
            totalRows += originalPeakLists[i].getNumberOfRows();

        }

        // goodStandards Vector contains identified standard rows, represented
        // by arrays. Each array has same length as originalPeakLists array.
        // Array items represent particular standard peak in each PeakList
        Vector<PeakListRow[]> goodStandards = new Vector<PeakListRow[]>();

        // Iterate the first peaklist
        standardIteration: for (PeakListRow candidate : originalPeakLists[0]
                .getRows()) {

            // Cancel?
            if (isCanceled()) {
                return;
            }

            processedRows++;

            // Check that all peaks of this row have proper height
            for (Feature p : candidate.getPeaks()) {
                if (p.getHeight() < minHeight)
                    continue standardIteration;
            }

            PeakListRow goodStandardCandidate[] = new PeakListRow[originalPeakLists.length];
            goodStandardCandidate[0] = candidate;

            double candidateMZ = candidate.getAverageMZ();
            double candidateRT = candidate.getAverageRT();

            // Find matching rows in remaining peaklists
            for (int i = 1; i < originalPeakLists.length; i++) {
                Range<Double> rtRange = rtTolerance
                        .getToleranceRange(candidateRT);
                Range<Double> mzRange = mzTolerance
                        .getToleranceRange(candidateMZ);
                PeakListRow matchingRows[] = originalPeakLists[i]
                        .getRowsInsideScanAndMZRange(rtRange, mzRange);

                // If we have not found exactly 1 matching peak, move to next
                // standard candidate
                if (matchingRows.length != 1)
                    continue standardIteration;

                // Check that all peaks of this row have proper height
                for (Feature p : matchingRows[0].getPeaks()) {
                    if (p.getHeight() < minHeight)
                        continue standardIteration;
                }

                // Save reference to matching peak in this feature list
                goodStandardCandidate[i] = matchingRows[0];

            }

            // If we found a match of same peak in all peaklists, mark it as a
            // good standard
            goodStandards.add(goodStandardCandidate);
            logger.finest(
                    "Found a good standard for RT normalization: " + candidate);

        }

        // Check if we have any standards
        if (goodStandards.size() == 0) {
            setStatus(TaskStatus.ERROR);
            setErrorMessage("No good standard peak was found");
            return;
        }

        // Calculate average retention times of all standards
        double averagedRTs[] = new double[goodStandards.size()];
        for (int i = 0; i < goodStandards.size(); i++) {
            double rtAverage = 0;
            for (PeakListRow row : goodStandards.get(i))
                rtAverage += row.getAverageRT();
            rtAverage /= (double) originalPeakLists.length;
            averagedRTs[i] = rtAverage;
        }

        // Normalize each feature list
        for (int peakListIndex = 0; peakListIndex < originalPeakLists.length; peakListIndex++) {

            // Get standard rows for this feature list only
            PeakListRow standards[] = new PeakListRow[goodStandards.size()];
            for (int i = 0; i < goodStandards.size(); i++) {
                standards[i] = goodStandards.get(i)[peakListIndex];
            }

            normalizePeakList(originalPeakLists[peakListIndex],
                    normalizedPeakLists[peakListIndex], standards, averagedRTs);

        }

        // Cancel?
        if (isCanceled()) {
            return;
        }

        // Add new peaklists to the project

        for (int i = 0; i < originalPeakLists.length; i++) {

            project.addPeakList(normalizedPeakLists[i]);

            // Load previous applied methods
            for (PeakListAppliedMethod proc : originalPeakLists[i]
                    .getAppliedMethods()) {
                normalizedPeakLists[i].addDescriptionOfAppliedTask(proc);
            }

            // Add task description to peakList
            normalizedPeakLists[i].addDescriptionOfAppliedTask(
                    new SimplePeakListAppliedMethod(
                            "Retention time normalization", parameters));

            // Remove the original peaklists if requested
            if (removeOriginal)
                project.removePeakList(originalPeakLists[i]);

        }

        logger.info("Finished retention time normalizer");
        setStatus(TaskStatus.FINISHED);

    }

    /**
     * Normalize retention time of all rows in given feature list and save
     * normalized rows into new peak list.
     * 
     * @param originalPeakList
     *            Feature list to be normalized
     * @param normalizedPeakList
     *            New feature list, where normalized rows are to be saved
     * @param standards
     *            Standard rows in same feature list
     * @param normalizedStdRTs
     *            Normalized retention times of standard rows
     */
    private void normalizePeakList(PeakList originalPeakList,
            PeakList normalizedPeakList, PeakListRow standards[],
            double normalizedStdRTs[]) {

        PeakListRow originalRows[] = originalPeakList.getRows();

        // Iterate feature list rows
        for (PeakListRow originalRow : originalRows) {

            // Cancel?
            if (isCanceled()) {
                return;
            }

            // Normalize one row
            PeakListRow normalizedRow = normalizeRow(originalRow, standards,
                    normalizedStdRTs);

            // Copy comment and identification
            normalizedRow.setComment(originalRow.getComment());
            for (PeakIdentity ident : originalRow.getPeakIdentities())
                normalizedRow.addPeakIdentity(ident, false);
            normalizedRow.setPreferredPeakIdentity(
                    originalRow.getPreferredPeakIdentity());

            // Add the new row to normalized feature list
            normalizedPeakList.addRow(normalizedRow);

            processedRows++;

        }

    }

    /**
     * Normalize retention time of given row using selected standards
     * 
     * @param originalRow
     *            Feature list row to be normalized
     * @param standards
     *            Standard rows in same feature list
     * @param normalizedStdRTs
     *            Normalized retention times of standard rows
     * @return New feature list row with normalized retention time
     */
    private PeakListRow normalizeRow(PeakListRow originalRow,
            PeakListRow standards[], double normalizedStdRTs[]) {

        PeakListRow normalizedRow = new SimplePeakListRow(originalRow.getID());

        // Standard rows preceding and following this row
        int prevStdIndex = -1, nextStdIndex = -1;

        for (int stdIndex = 0; stdIndex < standards.length; stdIndex++) {

            // If this standard peak is actually originalRow
            if (standards[stdIndex] == originalRow) {
                prevStdIndex = stdIndex;
                nextStdIndex = stdIndex;
                break;
            }

            // If this standard peak is before our originalRow
            if (standards[stdIndex].getAverageRT() < originalRow
                    .getAverageRT()) {
                if ((prevStdIndex == -1) || (standards[stdIndex]
                        .getAverageRT() > standards[prevStdIndex]
                                .getAverageRT()))
                    prevStdIndex = stdIndex;
            }

            // If this standard peak is after our originalRow
            if (standards[stdIndex].getAverageRT() > originalRow
                    .getAverageRT()) {
                if ((nextStdIndex == -1) || (standards[stdIndex]
                        .getAverageRT() < standards[nextStdIndex]
                                .getAverageRT()))
                    nextStdIndex = stdIndex;
            }

        }

        // Calculate normalized retention time of this row
        double normalizedRT = -1;

        if ((prevStdIndex == -1) || (nextStdIndex == -1)) {
            normalizedRT = originalRow.getAverageRT();
        } else

        if (prevStdIndex == nextStdIndex) {
            normalizedRT = normalizedStdRTs[prevStdIndex];
        } else {
            double weight = (originalRow.getAverageRT()
                    - standards[prevStdIndex].getAverageRT())
                    / (standards[nextStdIndex].getAverageRT()
                            - standards[prevStdIndex].getAverageRT());
            normalizedRT = normalizedStdRTs[prevStdIndex]
                    + (weight * (normalizedStdRTs[nextStdIndex]
                            - normalizedStdRTs[prevStdIndex]));
        }

        // Set normalized retention time to all peaks in this row
        for (RawDataFile file : originalRow.getRawDataFiles()) {
            Feature originalPeak = originalRow.getPeak(file);
            if (originalPeak != null) {
                SimpleFeature normalizedPeak = new SimpleFeature(originalPeak);
                PeakUtils.copyPeakProperties(originalPeak, normalizedPeak);
                normalizedPeak.setRT(normalizedRT);
                normalizedRow.addPeak(file, normalizedPeak);
            }
        }

        return normalizedRow;

    }

}
