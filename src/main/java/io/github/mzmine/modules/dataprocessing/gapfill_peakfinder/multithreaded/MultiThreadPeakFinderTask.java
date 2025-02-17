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

package io.github.mzmine.modules.dataprocessing.gapfill_peakfinder.multithreaded;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import com.google.common.collect.Range;

import io.github.mzmine.datamodel.Feature;
import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.datamodel.PeakListRow;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.modules.dataprocessing.gapfill_peakfinder.Gap;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import io.github.mzmine.parameters.parametertypes.tolerances.RTTolerance;
import io.github.mzmine.taskcontrol.AbstractTask;
import io.github.mzmine.taskcontrol.TaskStatus;

class MultiThreadPeakFinderTask extends AbstractTask {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private PeakList peakList, processedPeakList;
    private double intTolerance;
    private MZTolerance mzTolerance;
    private RTTolerance rtTolerance;
    private int processedScans, totalScans;

    // start and end (exclusive) for raw data file processing
    private int start;
    private int endexcl;

    // takes care of adding the final result
    private SubTaskFinishListener listener;

    private int taskIndex;

    MultiThreadPeakFinderTask(MZmineProject project, PeakList peakList,
            PeakList processedPeakList, ParameterSet parameters, int start,
            int endexcl, SubTaskFinishListener listener, int taskIndex) {

        this.listener = listener;
        this.taskIndex = taskIndex;

        this.peakList = peakList;
        this.processedPeakList = processedPeakList;

        intTolerance = parameters
                .getParameter(MultiThreadPeakFinderParameters.intTolerance)
                .getValue();
        mzTolerance = parameters
                .getParameter(MultiThreadPeakFinderParameters.MZTolerance)
                .getValue();
        rtTolerance = parameters
                .getParameter(MultiThreadPeakFinderParameters.RTTolerance)
                .getValue();

        this.start = start;
        this.endexcl = endexcl;
    }

    public void run() {

        setStatus(TaskStatus.PROCESSING);
        logger.info("Running multithreaded gap filler " + taskIndex
                + " on raw files " + (start + 1) + "-" + endexcl + " of pkl:"
                + peakList);

        // Calculate total number of scans in all files
        for (int i = start; i < endexcl; i++) {
            RawDataFile dataFile = peakList.getRawDataFile(i);
            totalScans += dataFile.getNumOfScans(1);
        }

        // Process all raw data files
        for (int i = start; i < endexcl; i++) {
            RawDataFile dataFile = peakList.getRawDataFile(i);

            // Canceled?
            if (isCanceled()) {
                return;
            }

            List<Gap> gaps = new ArrayList<Gap>();

            // Fill each row of this raw data file column, create new empty
            // gaps
            // if necessary
            for (int row = 0; row < peakList.getNumberOfRows(); row++) {
                PeakListRow sourceRow = peakList.getRow(row);
                PeakListRow newRow = processedPeakList.getRow(row);

                Feature sourcePeak = sourceRow.getPeak(dataFile);

                if (sourcePeak == null) {

                    // Create a new gap

                    Range<Double> mzRange = mzTolerance
                            .getToleranceRange(sourceRow.getAverageMZ());
                    Range<Double> rtRange = rtTolerance
                            .getToleranceRange(sourceRow.getAverageRT());

                    Gap newGap = new Gap(newRow, dataFile, mzRange, rtRange,
                            intTolerance);

                    gaps.add(newGap);

                } else {
                    newRow.addPeak(dataFile, sourcePeak);
                }
            }

            // Stop processing this file if there are no gaps
            if (gaps.isEmpty()) {
                processedScans += dataFile.getNumOfScans();
                continue;
            }

            // Get all scans of this data file
            int scanNumbers[] = dataFile.getScanNumbers(1);

            // Process each scan
            for (int scanNumber : scanNumbers) {
                // Canceled?
                if (isCanceled()) {
                    return;
                }

                // Get the scan
                Scan scan = dataFile.getScan(scanNumber);

                // Feed this scan to all gaps
                for (Gap gap : gaps) {
                    gap.offerNextScan(scan);
                }

                processedScans++;
            }

            // Finalize gaps
            for (Gap gap : gaps) {
                gap.noMoreOffers();
            }
        }

        // first notify listener
        listener.accept(processedPeakList);

        logger.info("Finished sub task: Multithreaded gap filler " + taskIndex
                + " on raw files " + (start + 1) + "-" + endexcl + " of pkl:"
                + peakList);
        setStatus(TaskStatus.FINISHED);
    }

    public double getFinishedPercentage() {
        if (totalScans == 0) {
            return 0;
        }
        return (double) processedScans / (double) totalScans;
    }

    public String getTaskDescription() {
        return "Sub task " + taskIndex + ": Gap filling on raw files "
                + (start + 1) + "-" + endexcl + " of pkl:" + peakList;
    }

    PeakList getPeakList() {
        return peakList;
    }

}
