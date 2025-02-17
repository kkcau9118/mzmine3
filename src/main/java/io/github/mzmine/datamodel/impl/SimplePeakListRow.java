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

import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

import io.github.mzmine.datamodel.Feature;
import io.github.mzmine.datamodel.IsotopePattern;
import io.github.mzmine.datamodel.PeakIdentity;
import io.github.mzmine.datamodel.PeakInformation;
import io.github.mzmine.datamodel.PeakListRow;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.util.PeakSorter;
import io.github.mzmine.util.SortingDirection;
import io.github.mzmine.util.SortingProperty;

/**
 * Implementation of PeakListRow
 */
public class SimplePeakListRow implements PeakListRow {

    // faster than Hashtable
    private ConcurrentHashMap<RawDataFile, Feature> peaks;
    private Feature preferredPeak;
    private List<PeakIdentity> identities;
    private PeakIdentity preferredIdentity;
    private String comment;
    private PeakInformation information;
    private int myID;
    private double maxDataPointIntensity = 0;

    /**
     * These variables are used for caching the average values, so we don't need
     * to calculate them again and again
     */
    private double averageRT, averageMZ, averageHeight, averageArea;
    private int rowCharge;

    public SimplePeakListRow(int myID) {
        this.myID = myID;
        peaks = new ConcurrentHashMap<RawDataFile, Feature>();
        identities = new Vector<PeakIdentity>();
        information = null;
        preferredPeak = null;
    }

    /**
     * @see io.github.mzmine.datamodel.PeakListRow#getID()
     */
    @Override
    public int getID() {
        return myID;
    }

    /**
     * Return peaks assigned to this row
     */
    @Override
    public Feature[] getPeaks() {
        return peaks.values().toArray(new Feature[0]);
    }

    @Override
    public void removePeak(RawDataFile file) {
        this.peaks.remove(file);
        calculateAverageValues();
    }

    /**
     * Returns opened raw data files with a peak on this row
     */
    @Override
    public RawDataFile[] getRawDataFiles() {
        return peaks.keySet().toArray(new RawDataFile[0]);
    }

    /**
     * Returns peak for given raw data file
     */
    @Override
    public Feature getPeak(RawDataFile rawData) {
        return peaks.get(rawData);
    }

    @Override
    public synchronized void addPeak(RawDataFile rawData, Feature peak) {
        if (peak == null)
            throw new IllegalArgumentException(
                    "Cannot add null feature to a feature list row");

        // ConcurrentHashMap is already synchronized
        peaks.put(rawData, peak);

        if (peak.getRawDataPointsIntensityRange()
                .upperEndpoint() > maxDataPointIntensity)
            maxDataPointIntensity = peak.getRawDataPointsIntensityRange()
                    .upperEndpoint();
        calculateAverageValues();
    }

    @Override
    public double getAverageMZ() {
        return averageMZ;
    }

    @Override
    public double getAverageRT() {
        return averageRT;
    }

    @Override
    public double getAverageHeight() {
        return averageHeight;
    }

    @Override
    public double getAverageArea() {
        return averageArea;
    }

    @Override
    public int getRowCharge() {
        return rowCharge;
    }

    private synchronized void calculateAverageValues() {
        double rtSum = 0, mzSum = 0, heightSum = 0, areaSum = 0;
        int charge = 0;
        HashSet<Integer> chargeArr = new HashSet<Integer>();
        Enumeration<Feature> peakEnum = peaks.elements();
        while (peakEnum.hasMoreElements()) {
            Feature p = peakEnum.nextElement();
            rtSum += p.getRT();
            mzSum += p.getMZ();
            heightSum += p.getHeight();
            areaSum += p.getArea();
            if (p.getCharge() > 0) {
                chargeArr.add(p.getCharge());
                charge = p.getCharge();
            }
        }
        averageRT = rtSum / peaks.size();
        averageMZ = mzSum / peaks.size();
        averageHeight = heightSum / peaks.size();
        averageArea = areaSum / peaks.size();
        if (chargeArr.size() < 2) {
            rowCharge = charge;
        } else {
            rowCharge = 0;
        }
    }

    /**
     * Returns number of peaks assigned to this row
     */
    @Override
    public int getNumberOfPeaks() {
        return peaks.size();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        Format mzFormat = MZmineCore.getConfiguration().getMZFormat();
        Format timeFormat = MZmineCore.getConfiguration().getRTFormat();
        buf.append("#" + myID + " ");
        buf.append(mzFormat.format(getAverageMZ()));
        buf.append(" m/z @");
        buf.append(timeFormat.format(getAverageRT()));
        if (preferredIdentity != null)
            buf.append(" " + preferredIdentity.getName());
        if ((comment != null) && (comment.length() > 0))
            buf.append(" (" + comment + ")");
        return buf.toString();
    }

    /**
     * @see io.github.mzmine.datamodel.PeakListRow#getComment()
     */
    @Override
    public String getComment() {
        return comment;
    }

    /**
     * @see io.github.mzmine.datamodel.PeakListRow#setComment(java.lang.String)
     */
    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @see io.github.mzmine.datamodel.PeakListRow#setAverageMZ(java.lang.String)
     */
    @Override
    public void setAverageMZ(double mz) {
        this.averageMZ = mz;
    }

    /**
     * @see io.github.mzmine.datamodel.PeakListRow#setAverageRT(java.lang.String)
     */
    @Override
    public void setAverageRT(double rt) {
        this.averageRT = rt;
    }

    /**
     * @see io.github.mzmine.datamodel.PeakListRow#addCompoundIdentity(io.github.mzmine.datamodel.PeakIdentity)
     */
    @Override
    public synchronized void addPeakIdentity(PeakIdentity identity,
            boolean preferred) {

        // Verify if exists already an identity with the same name
        for (PeakIdentity testId : identities) {
            if (testId.getName().equals(identity.getName())) {
                return;
            }
        }

        identities.add(identity);
        if ((preferredIdentity == null) || (preferred)) {
            setPreferredPeakIdentity(identity);
        }
    }

    /**
     * @see io.github.mzmine.datamodel.PeakListRow#addCompoundIdentity(io.github.mzmine.datamodel.PeakIdentity)
     */
    @Override
    public synchronized void removePeakIdentity(PeakIdentity identity) {
        identities.remove(identity);
        if (preferredIdentity == identity) {
            if (identities.size() > 0) {
                PeakIdentity[] identitiesArray = identities
                        .toArray(new PeakIdentity[0]);
                setPreferredPeakIdentity(identitiesArray[0]);
            } else
                preferredIdentity = null;
        }
    }

    /**
     * @see io.github.mzmine.datamodel.PeakListRow#getPeakIdentities()
     */
    @Override
    public PeakIdentity[] getPeakIdentities() {
        return identities.toArray(new PeakIdentity[0]);
    }

    /**
     * @see io.github.mzmine.datamodel.PeakListRow#getPreferredPeakIdentity()
     */
    @Override
    public PeakIdentity getPreferredPeakIdentity() {
        return preferredIdentity;
    }

    /**
     * @see io.github.mzmine.datamodel.PeakListRow#setPreferredPeakIdentity(io.github.mzmine.datamodel.PeakIdentity)
     */
    @Override
    public synchronized void setPreferredPeakIdentity(PeakIdentity identity) {

        if (identity == null)
            return;

        preferredIdentity = identity;

        if (!identities.contains(identity)) {
            identities.add(identity);
        }

    }

    @Override
    public void setPeakInformation(PeakInformation information) {
        this.information = information;
    }

    @Override
    public PeakInformation getPeakInformation() {
        return information;
    }

    /**
     * @see io.github.mzmine.datamodel.PeakListRow#getDataPointMaxIntensity()
     */
    @Override
    public double getDataPointMaxIntensity() {
        return maxDataPointIntensity;
    }

    @Override
    public boolean hasPeak(Feature peak) {
        return peaks.containsValue(peak);
    }

    @Override
    public boolean hasPeak(RawDataFile file) {
        return peaks.containsKey(file);
    }

    /**
     * Returns the highest isotope pattern of a peak in this row
     */
    @Override
    public IsotopePattern getBestIsotopePattern() {
        Feature peaks[] = getPeaks();
        Arrays.sort(peaks, new PeakSorter(SortingProperty.Height,
                SortingDirection.Descending));

        for (Feature peak : peaks) {
            IsotopePattern ip = peak.getIsotopePattern();
            if (ip != null)
                return ip;
        }

        return null;
    }

    /**
     * Returns the highest peak in this row
     */
    @Override
    public Feature getBestPeak() {

        Feature peaks[] = getPeaks();
        Arrays.sort(peaks, new PeakSorter(SortingProperty.Height,
                SortingDirection.Descending));
        if (peaks.length == 0)
            return null;
        return peaks[0];
    }

    @Override
    public Scan getBestFragmentation() {

        Double bestTIC = 0.0;
        Scan bestScan = null;
        for (Feature peak : this.getPeaks()) {
            Double theTIC = 0.0;
            RawDataFile rawData = peak.getDataFile();
            int bestScanNumber = peak.getMostIntenseFragmentScanNumber();
            Scan theScan = rawData.getScan(bestScanNumber);
            if (theScan != null) {
                theTIC = theScan.getTIC();
            }

            if (theTIC > bestTIC) {
                bestTIC = theTIC;
                bestScan = theScan;
            }
        }
        return bestScan;
    }

    @Override
    @Nonnull
    public Scan[] getAllMS2Fragmentations() {
        ArrayList<Scan> allMS2ScansList = new ArrayList<>();
        for (Feature peak : this.getPeaks()) {
            RawDataFile rawData = peak.getDataFile();
            int scanNumbers[] = peak.getAllMS2FragmentScanNumbers();
            if (scanNumbers != null) {
                for (int scanNumber : scanNumbers) {
                    Scan scan = rawData.getScan(scanNumber);
                    allMS2ScansList.add(scan);
                }
            }
        }

        return allMS2ScansList.toArray(new Scan[allMS2ScansList.size()]);
    }

    // DorresteinLab edit
    /**
     * set the ID number
     */

    @Override
    public void setID(int id) {
        myID = id;
        return;
    }
    // End DorresteinLab edit

    // Gauthier edit
    /**
     * Update average values
     */
    public void update() {
        this.calculateAverageValues();
    }
    // End Gauthier edit
}
// End DorresteinLab edit
