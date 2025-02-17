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
/*
 * This module was prepared by Abi Sarvepalli, Christopher Jensen, and Zheng Zhang at the Dorrestein
 * Lab (University of California, San Diego).
 * 
 * It is freely available under the GNU GPL licence of MZmine2.
 * 
 * For any questions or concerns, please refer to:
 * https://groups.google.com/forum/#!forum/molecular_networking_bug_reports
 * 
 * Credit to the Du-Lab development team for the initial commitment to the MGF export module.
 */

package io.github.mzmine.modules.io.gnpsexport.gc;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import com.google.common.util.concurrent.AtomicDouble;

import io.github.msdk.MSDKRuntimeException;
import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.io.adapmgfexport.AdapMgfExportModule;
import io.github.mzmine.modules.io.adapmgfexport.AdapMgfExportParameters;
import io.github.mzmine.modules.io.adapmgfexport.AdapMgfExportParameters.MzMode;
import io.github.mzmine.modules.io.adapmgfexport.AdapMgfExportTask;
import io.github.mzmine.modules.io.csvexport.CSVExportTask;
import io.github.mzmine.modules.io.csvexport.ExportRowCommonElement;
import io.github.mzmine.modules.io.csvexport.ExportRowDataFileElement;
import io.github.mzmine.modules.io.gnpsexport.GNPSUtils;
import io.github.mzmine.modules.io.gnpsexport.fbmn.GnpsFbmnExportAndSubmitParameters.RowFilter;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.taskcontrol.AbstractTask;
import io.github.mzmine.taskcontrol.AllTasksFinishedListener;
import io.github.mzmine.taskcontrol.Task;
import io.github.mzmine.taskcontrol.TaskPriority;
import io.github.mzmine.taskcontrol.TaskStatus;
import io.github.mzmine.util.PeakMeasurementType;
import io.github.mzmine.util.files.FileAndPathUtil;

/**
 * Exports all files needed for GNPS GC-MS workflow
 * 
 * @author Robin Schmid (robinschmid@uni-muenster.de)
 *
 */
public class GnpsGcExportAndSubmitTask extends AbstractTask {
    // Logger.
    private final Logger LOG = Logger.getLogger(getClass().getName());

    private ParameterSet parameters;
    private AtomicDouble progress = new AtomicDouble(0);

    private PeakList peakList;
    private MzMode representativeMZ;
    private PeakMeasurementType peakMeasure;

    private File file;
    private boolean submit;
    private boolean openFolder;

    GnpsGcExportAndSubmitTask(ParameterSet parameters) {
        this.parameters = parameters;

        this.peakList = parameters
                .getParameter(GnpsGcExportAndSubmitParameters.PEAK_LISTS)
                .getValue().getMatchingPeakLists()[0];
        this.representativeMZ = parameters
                .getParameter(GnpsGcExportAndSubmitParameters.REPRESENTATIVE_MZ)
                .getValue();
        this.peakMeasure = parameters
                .getParameter(GnpsGcExportAndSubmitParameters.PEAK_INTENSITY)
                .getValue();
        openFolder = parameters
                .getParameter(GnpsGcExportAndSubmitParameters.OPEN_FOLDER)
                .getValue();
        file = parameters.getParameter(GnpsGcExportAndSubmitParameters.FILENAME)
                .getValue();
        file = FileAndPathUtil.eraseFormat(file);
        parameters.getParameter(GnpsGcExportAndSubmitParameters.FILENAME)
                .setValue(file);
        // submit =
        // parameters.getParameter(GnpsGcExportAndSubmitParameters.SUBMIT).getValue();
        submit = false;
    }

    @Override
    public TaskPriority getTaskPriority() {
        // to not block mzmine with single process (1 thread)
        return TaskPriority.HIGH;
    }

    @Override
    public String getTaskDescription() {
        return "Exporting files GNPS feature based molecular networking job";
    }

    @Override
    public double getFinishedPercentage() {
        return progress.get();
    }

    @Override
    public void run() {
        final AbstractTask thistask = this;
        setStatus(TaskStatus.PROCESSING);

        List<AbstractTask> list = new ArrayList<>(3);
        // add mgf export task
        list.add(addAdapMgfTask(parameters));

        // add csv quant table
        list.add(addQuantTableTask(parameters, null));

        // finish listener to submit
        final File fileName = file;
        final File folder = file.getParentFile();
        new AllTasksFinishedListener(list, true,
                // succeed
                l -> {
                    try {
                        LOG.info("succeed" + thistask.getStatus().toString());
                        if (submit) {
                            GnpsGcSubmitParameters param = parameters
                                    .getParameter(
                                            GnpsGcExportAndSubmitParameters.SUBMIT)
                                    .getEmbeddedParameters();
                            submit(fileName, param);
                        }

                        // open folder
                        try {
                            if (openFolder && Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(folder);
                            }
                        } catch (Exception ex) {
                        }
                    } finally {
                        // finish task
                        if (thistask.getStatus() == TaskStatus.PROCESSING)
                            thistask.setStatus(TaskStatus.FINISHED);
                    }
                }, lerror -> {
                    setErrorMessage(
                            "GNPS-GC submit was not started due too errors while file export");
                    thistask.setStatus(TaskStatus.ERROR);
                    throw new MSDKRuntimeException(
                            "GNPS-GC submit was not started due too errors while file export");
                },
                // cancel if one was cancelled
                listCancelled -> cancel()) {
            @Override
            public void taskStatusChanged(Task task, TaskStatus newStatus,
                    TaskStatus oldStatus) {
                super.taskStatusChanged(task, newStatus, oldStatus);
                // show progress
                progress.getAndSet(getProgress());
            }
        };

        MZmineCore.getTaskController()
                .addTasks(list.toArray(new AbstractTask[list.size()]));

        // wait till finish
        while (!(isCanceled() || isFinished())) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, "Error in GNPS-GC export/submit task", e);
            }
        }
    }

    /**
     * Export mgf (adap mgf export) of clustered spectra
     * 
     * @param parameters
     * @return
     */
    private AbstractTask addAdapMgfTask(ParameterSet parameters) {
        File full = parameters
                .getParameter(GnpsGcExportAndSubmitParameters.FILENAME)
                .getValue();
        String name = FilenameUtils.removeExtension(full.getName());
        full = FileAndPathUtil.getRealFilePath(full.getParentFile(), name,
                "mgf");

        ParameterSet mgfParam = MZmineCore.getConfiguration()
                .getModuleParameters(AdapMgfExportModule.class);
        mgfParam.getParameter(AdapMgfExportParameters.FILENAME).setValue(full);
        mgfParam.getParameter(AdapMgfExportParameters.FRACTIONAL_MZ)
                .setValue(true);
        mgfParam.getParameter(AdapMgfExportParameters.REPRESENTATIVE_MZ)
                .setValue(representativeMZ);
        return new AdapMgfExportTask(mgfParam, new PeakList[] { peakList });
    }

    /**
     * Submit GNPS job
     * 
     * @param fileName
     * @param param
     */
    private void submit(File fileName, GnpsGcSubmitParameters param) {
        try {
            String url = GNPSUtils.submitGcJob(fileName, param);
            if (url == null || url.isEmpty())
                LOG.log(Level.WARNING, "GNPS-GC submit failed (url empty)");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "GNPS-GC submit failed", e);
        }
    }

    /**
     * Export quant table
     * 
     * @param parameters
     * @param tasks
     */
    private AbstractTask addQuantTableTask(ParameterSet parameters,
            Collection<Task> tasks) {
        File full = parameters
                .getParameter(GnpsGcExportAndSubmitParameters.FILENAME)
                .getValue();
        String name = FilenameUtils.removeExtension(full.getName());
        full = FileAndPathUtil.getRealFilePath(full.getParentFile(),
                name + "_quant", "csv");

        ExportRowCommonElement[] common = new ExportRowCommonElement[] {
                ExportRowCommonElement.ROW_ID, ExportRowCommonElement.ROW_MZ,
                ExportRowCommonElement.ROW_RT };

        // height or area?
        ExportRowDataFileElement[] rawdata = new ExportRowDataFileElement[] {
                peakMeasure.equals(PeakMeasurementType.AREA)
                        ? ExportRowDataFileElement.PEAK_AREA
                        : ExportRowDataFileElement.PEAK_HEIGHT };

        CSVExportTask quanExport = new CSVExportTask(
                new PeakList[] { peakList }, full, ",", common, rawdata, false,
                ";", RowFilter.ALL);
        if (tasks != null)
            tasks.add(quanExport);
        return quanExport;
    }

}
