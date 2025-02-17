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

package io.github.mzmine.datamodel.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;
import com.google.common.collect.Range;

import io.github.mzmine.datamodel.Feature;
import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.datamodel.PeakListRow;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.gui.impl.projecttree.PeakListTreeModel;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.project.impl.MZmineProjectImpl;

/**
 * Simple implementation of the PeakList interface.
 */
public class SimplePeakList implements PeakList {

    private String name;
    private RawDataFile[] dataFiles;
    private ArrayList<PeakListRow> peakListRows;
    private double maxDataPointIntensity = 0;
    private Vector<PeakListAppliedMethod> descriptionOfAppliedTasks;
    private String dateCreated;
    private Range<Double> mzRange, rtRange;

    public static DateFormat dateFormat = new SimpleDateFormat(
            "yyyy/MM/dd HH:mm:ss");

    public SimplePeakList(String name, RawDataFile dataFile) {
        this(name, new RawDataFile[] { dataFile });
    }

    public SimplePeakList(String name, RawDataFile[] dataFiles) {
        if ((dataFiles == null) || (dataFiles.length == 0)) {
            throw (new IllegalArgumentException(
                    "Cannot create a feature list with no data files"));
        }
        this.name = name;
        this.dataFiles = new RawDataFile[dataFiles.length];

        RawDataFile dataFile;
        for (int i = 0; i < dataFiles.length; i++) {
            dataFile = dataFiles[i];
            this.dataFiles[i] = dataFile;
        }
        peakListRows = new ArrayList<PeakListRow>();
        descriptionOfAppliedTasks = new Vector<PeakListAppliedMethod>();

        dateCreated = dateFormat.format(new Date());

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns number of raw data files participating in the alignment
     */
    @Override
    public int getNumberOfRawDataFiles() {
        return dataFiles.length;
    }

    /**
     * Returns all raw data files participating in the alignment
     */
    @Override
    public RawDataFile[] getRawDataFiles() {
        return dataFiles;
    }

    @Override
    public RawDataFile getRawDataFile(int position) {
        return dataFiles[position];
    }

    /**
     * Returns number of rows in the alignment result
     */
    @Override
    public int getNumberOfRows() {
        return peakListRows.size();
    }

    /**
     * Returns the peak of a given raw data file on a give row of the alignment
     * result
     * 
     * @param row
     *            Row of the alignment result
     * @param rawDataFile
     *            Raw data file where the peak is detected/estimated
     */
    @Override
    public Feature getPeak(int row, RawDataFile rawDataFile) {
        return peakListRows.get(row).getPeak(rawDataFile);
    }

    /**
     * Returns all peaks for a raw data file
     */
    @Override
    public Feature[] getPeaks(RawDataFile rawDataFile) {
        Vector<Feature> peakSet = new Vector<Feature>();
        for (int row = 0; row < getNumberOfRows(); row++) {
            Feature p = peakListRows.get(row).getPeak(rawDataFile);
            if (p != null)
                peakSet.add(p);
        }
        return peakSet.toArray(new Feature[0]);
    }

    /**
     * Returns all peaks on one row
     */
    @Override
    public PeakListRow getRow(int row) {
        return peakListRows.get(row);
    }

    @Override
    public PeakListRow[] getRows() {
        return peakListRows.toArray(new PeakListRow[0]);
    }

    @Override
    public PeakListRow[] getRowsInsideMZRange(Range<Double> mzRange) {
        Range<Double> all = Range.all();
        return getRowsInsideScanAndMZRange(all, mzRange);
    }

    @Override
    public PeakListRow[] getRowsInsideScanRange(Range<Double> rtRange) {
        Range<Double> all = Range.all();
        return getRowsInsideScanAndMZRange(rtRange, all);
    }

    @Override
    public PeakListRow[] getRowsInsideScanAndMZRange(Range<Double> rtRange,
            Range<Double> mzRange) {
        Vector<PeakListRow> rowsInside = new Vector<PeakListRow>();

        for (PeakListRow row : peakListRows) {
            if (rtRange.contains(row.getAverageRT())
                    && mzRange.contains(row.getAverageMZ()))
                rowsInside.add(row);
        }

        return rowsInside.toArray(new PeakListRow[0]);
    }

    @Override
    public void addRow(PeakListRow row) {
        List<RawDataFile> myFiles = Arrays.asList(this.getRawDataFiles());
        for (RawDataFile testFile : row.getRawDataFiles()) {
            if (!myFiles.contains(testFile))
                throw (new IllegalArgumentException("Data file " + testFile
                        + " is not in this feature list"));
        }

        peakListRows.add(row);
        if (row.getDataPointMaxIntensity() > maxDataPointIntensity) {
            maxDataPointIntensity = row.getDataPointMaxIntensity();
        }

        if (mzRange == null) {
            mzRange = Range.singleton(row.getAverageMZ());
            rtRange = Range.singleton(row.getAverageRT());
        } else {
            mzRange = mzRange.span(Range.singleton(row.getAverageMZ()));
            rtRange = rtRange.span(Range.singleton(row.getAverageRT()));
        }
    }

    /**
     * Returns all peaks overlapping with a retention time range
     * 
     * @param startRT
     *            Start of the retention time range
     * @param endRT
     *            End of the retention time range
     * @return
     */
    @Override
    public Feature[] getPeaksInsideScanRange(RawDataFile file,
            Range<Double> rtRange) {
        Range<Double> all = Range.all();
        return getPeaksInsideScanAndMZRange(file, rtRange, all);
    }

    /**
     * @see io.github.mzmine.datamodel.PeakList#getPeaksInsideMZRange(double,
     *      double)
     */
    @Override
    public Feature[] getPeaksInsideMZRange(RawDataFile file,
            Range<Double> mzRange) {
        Range<Double> all = Range.all();
        return getPeaksInsideScanAndMZRange(file, all, mzRange);
    }

    /**
     * @see io.github.mzmine.datamodel.PeakList#getPeaksInsideScanAndMZRange(double,
     *      double, double, double)
     */
    @Override
    public Feature[] getPeaksInsideScanAndMZRange(RawDataFile file,
            Range<Double> rtRange, Range<Double> mzRange) {
        Vector<Feature> peaksInside = new Vector<Feature>();

        Feature[] peaks = getPeaks(file);
        for (Feature p : peaks) {
            if (rtRange.contains(p.getRT()) && mzRange.contains(p.getMZ()))
                peaksInside.add(p);
        }

        return peaksInside.toArray(new Feature[0]);
    }

    /**
     * @see io.github.mzmine.datamodel.PeakList#removeRow(io.github.mzmine.datamodel.PeakListRow)
     */
    @Override
    public void removeRow(PeakListRow row) {
        peakListRows.remove(row);

        // We have to update the project tree model
        MZmineProjectImpl project = (MZmineProjectImpl) MZmineCore
                .getProjectManager().getCurrentProject();
        PeakListTreeModel treeModel = project.getPeakListTreeModel();
        treeModel.removeObject(row);

        updateMaxIntensity();
    }

    /**
     * @see io.github.mzmine.datamodel.PeakList#removeRow(io.github.mzmine.datamodel.PeakListRow)
     */
    @Override
    public void removeRow(int rowNum) {
        removeRow(peakListRows.get(rowNum));
    }

    private void updateMaxIntensity() {
        maxDataPointIntensity = 0;
        mzRange = null;
        rtRange = null;
        for (PeakListRow peakListRow : peakListRows) {
            if (peakListRow.getDataPointMaxIntensity() > maxDataPointIntensity)
                maxDataPointIntensity = peakListRow.getDataPointMaxIntensity();

            if (mzRange == null) {
                mzRange = Range.singleton(peakListRow.getAverageMZ());
                rtRange = Range.singleton(peakListRow.getAverageRT());
            } else {
                mzRange = mzRange
                        .span(Range.singleton(peakListRow.getAverageMZ()));
                rtRange = rtRange
                        .span(Range.singleton(peakListRow.getAverageRT()));
            }
        }
    }

    @Override
    public Stream<PeakListRow> stream() {
        return peakListRows.stream();
    }

    @Override
    public Stream<PeakListRow> parallelStream() {
        return peakListRows.parallelStream();
    }

    /**
     * @see io.github.mzmine.datamodel.PeakList#getPeakRowNum(io.github.mzmine.datamodel.Feature)
     */
    @Override
    public int getPeakRowNum(Feature peak) {

        PeakListRow rows[] = getRows();

        for (int i = 0; i < rows.length; i++) {
            if (rows[i].hasPeak(peak))
                return i;
        }

        return -1;
    }

    /**
     * @see io.github.mzmine.datamodel.PeakList#getDataPointMaxIntensity()
     */
    @Override
    public double getDataPointMaxIntensity() {
        return maxDataPointIntensity;
    }

    @Override
    public boolean hasRawDataFile(RawDataFile hasFile) {
        return Arrays.asList(dataFiles).contains(hasFile);
    }

    @Override
    public PeakListRow getPeakRow(Feature peak) {
        PeakListRow rows[] = getRows();

        for (int i = 0; i < rows.length; i++) {
            if (rows[i].hasPeak(peak))
                return rows[i];
        }

        return null;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void addDescriptionOfAppliedTask(
            PeakListAppliedMethod appliedMethod) {
        descriptionOfAppliedTasks.add(appliedMethod);
    }

    @Override
    public PeakListAppliedMethod[] getAppliedMethods() {
        return descriptionOfAppliedTasks.toArray(new PeakListAppliedMethod[0]);
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String date) {
        this.dateCreated = date;
    }

    @Override
    public Range<Double> getRowsMZRange() {
        updateMaxIntensity(); // Update range before returning value
        return mzRange;
    }

    @Override
    public Range<Double> getRowsRTRange() {
        updateMaxIntensity(); // Update range before returning value
        return rtRange;
    }

    @Override
    public PeakListRow findRowByID(int id) {
        return stream().filter(r -> r.getID() == id).findFirst().orElse(null);
    }
}
