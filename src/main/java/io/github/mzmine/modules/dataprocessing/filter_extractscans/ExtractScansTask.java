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

package io.github.mzmine.modules.dataprocessing.filter_extractscans;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Range;

import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.MassList;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.taskcontrol.AbstractTask;
import io.github.mzmine.taskcontrol.TaskStatus;
import io.github.mzmine.util.files.FileAndPathUtil;

class ExtractScansTask extends AbstractTask {

    private double perc = 0;
    private int scans, scanMaxTIC = -1;
    private double centerTime;
    private File file;
    private List<RawDataFile> dataFiles;
    private boolean useMassList, autoMax, exportSummary, exportHeader;
    private String delimiter = "\t";
    private String massList;

    private double minTime, maxTime;
    private boolean useCenterTime;

    ExtractScansTask(ParameterSet parameters) {
        dataFiles = Arrays.asList(
                parameters.getParameter(ExtractScansParameters.dataFiles)
                        .getValue().getMatchingRawDataFiles());
        scans = parameters.getParameter(ExtractScansParameters.scans).getValue()
                .intValue();
        centerTime = parameters.getParameter(ExtractScansParameters.centerTime)
                .getValue();
        file = parameters.getParameter(ExtractScansParameters.file).getValue();
        autoMax = parameters.getParameter(ExtractScansParameters.autoMax)
                .getValue();
        // exportSummary =
        // parameters.getParameter(ExtractScansParameters.exportSummary).getValue();
        exportHeader = parameters
                .getParameter(ExtractScansParameters.exportHeader).getValue();
        // export mass list
        useMassList = parameters
                .getParameter(ExtractScansParameters.useMassList).getValue();
        massList = parameters.getParameter(ExtractScansParameters.useMassList)
                .getEmbeddedParameter().getValue();
        useCenterTime = parameters
                .getParameter(ExtractScansParameters.useCenterTime).getValue();
        Range<Double> r = parameters
                .getParameter(ExtractScansParameters.rangeTime).getValue();
        if (r != null) {
            minTime = r.lowerEndpoint();
            maxTime = r.upperEndpoint();
        }
    }

    public double getFinishedPercentage() {
        return perc;
    }

    public String getTaskDescription() {
        return "Extracting scans to CSV file(s)";
    }

    public void run() {
        setStatus(TaskStatus.PROCESSING);
        if (useCenterTime) {
            // scans arround a center time
            for (int r = 0; r < dataFiles.size(); r++) {
                RawDataFile raw = dataFiles.get(r);
                int start = 0;
                double max = 0;
                // find center scan
                for (int i = 0; i < raw.getNumOfScans(); i++) {
                    Scan scan = raw.getScan(raw.getScanNumbers()[i]);
                    double rt = scan.getRetentionTime();
                    if (autoMax) {
                        double tic = scan.getTIC();
                        if (tic > max) {
                            start = i;
                            max = tic;
                        }
                    } else {
                        if (rt > centerTime || i == raw.getNumOfScans() - 1) {
                            // export scans
                            start = i;
                            break;
                        }
                    }
                }
                if (autoMax) {
                    scanMaxTIC = start;
                }
                start = start - scans / 2;
                if (start + scans > raw.getNumOfScans())
                    start = raw.getNumOfScans() - scans;
                if (start < 0)
                    start = 0;

                exportScans(file, raw, start, scans,
                        (double) 1.0 / dataFiles.size());
                //
                perc = (double) (r + 1) / dataFiles.size();
            }
        } else {
            // export between min/max time
            for (int r = 0; r < dataFiles.size(); r++) {
                RawDataFile raw = dataFiles.get(r);
                int start = -1;
                int end = -1;
                for (int i = 0; i < raw.getNumOfScans(); i++) {
                    Scan scan = raw.getScan(raw.getScanNumbers()[i]);
                    double rt = scan.getRetentionTime();

                    if (i == raw.getNumOfScans() - 1) {
                        // no minimum was set?
                        if (start == -1) {
                            setErrorMessage(
                                    "And of scans reached. Minimum was set too high?");
                            setStatus(TaskStatus.ERROR);
                            return;
                        } else {
                            // set end to max
                            end = i;
                        }
                    } else if (rt >= minTime && start == -1) {
                        // export scans
                        start = i;
                    } else if (rt > maxTime) {
                        // export scans
                        end = i - 1;
                        break;
                    }
                }
                if (start != -1 && end != -1)
                    exportScans(file, raw, start, end - start,
                            (double) 1.0 / dataFiles.size());
                //
                perc = (double) (r + 1) / dataFiles.size();
            }
        }

        if (getStatus() == TaskStatus.PROCESSING)
            setStatus(TaskStatus.FINISHED);
    }

    private void exportScans(File dir, RawDataFile raw, int start, int scans,
            double pp) {
        // Open file
        DecimalFormat format = new DecimalFormat("00");
        File fileDir = new File(dir,
                FilenameUtils.removeExtension(raw.getName()));
        FileAndPathUtil.createDirectory(fileDir);
        int end = Math.min(scans + start, raw.getNumOfScans());
        String linescans = "scan" + delimiter + raw.getScanNumbers()[start]
                + delimiter + "to" + delimiter + raw.getScanNumbers()[end - 1]
                + "\n";
        String lineRT = "RT" + delimiter
                + raw.getScan(raw.getScanNumbers()[start]).getRetentionTime()
                + "to"
                + raw.getScan(raw.getScanNumbers()[end - 1]).getRetentionTime()
                + "\n";
        String linePath = raw.getName() + "\n";
        String lineOptions = "export of" + delimiter;
        if (!useCenterTime) {
            double st = raw.getScan(raw.getScanNumbers()[start])
                    .getRetentionTime();
            double et = raw.getScan(raw.getScanNumbers()[end])
                    .getRetentionTime();
            lineOptions += scans + delimiter + "scans from time " + delimiter
                    + st + " to " + et + "\n";
        } else if (autoMax) {
            lineOptions += scans + delimiter
                    + "scans around scan with maximum intensity:" + delimiter
                    + raw.getScanNumbers()[scanMaxTIC] + "\n";
        } else {
            lineOptions += scans + delimiter + "scans around center time:"
                    + delimiter + centerTime + "\n";
        }

        for (int i = start; i < end; i++) {
            FileWriter writer;
            File file = new File(fileDir,
                    "scan" + format.format((i - start + 1)) + ".csv");
            StringBuilder out = new StringBuilder();
            try {
                writer = new FileWriter(file);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            // EXPORT
            try {
                Scan s = raw.getScan(raw.getScanNumbers()[i]);
                // write header
                if (exportHeader) {
                    out.append(linePath);
                    out.append(linescans);
                    out.append(lineRT);
                    out.append(lineOptions);

                    out.append("TIC" + delimiter + s.getTIC() + "\n");
                    out.append("scan number" + delimiter
                            + raw.getScanNumbers()[i] + "\n");
                    out.append("retention time" + delimiter
                            + s.getRetentionTime() + "\n\n");
                    out.append("mz" + delimiter + "intensity\n");
                }
                // write data
                if (useMassList) {
                    MassList mass = s.getMassList(massList);
                    DataPoint[] dp = mass.getDataPoints();
                    for (int k = 0; k < dp.length; k++) {
                        out.append(dp[k].getMZ() + delimiter
                                + dp[k].getIntensity() + "\n");
                    }
                } else {
                    for (int k = 0; k < s.getNumberOfDataPoints(); k++) {
                        DataPoint dp = s.getDataPoints()[k];
                        out.append(dp.getMZ() + delimiter + dp.getIntensity()
                                + "\n");
                    }
                }

                writeBuffered(writer, out.toString(), 8192);
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                // Close file
                try {
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            //
            perc += pp / (double) scans;
        }
    }

    private static void writeBuffered(FileWriter writer, String out,
            int bufSize) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(writer, bufSize);
        bufferedWriter.write(out);
        bufferedWriter.flush();
    }
}
