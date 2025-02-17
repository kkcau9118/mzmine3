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

package io.github.mzmine.modules.io.projectload.version_2_3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.MassSpectrumType;
import io.github.mzmine.datamodel.PolarityType;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.datamodel.impl.SimpleMassList;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.io.projectload.RawDataFileOpenHandler;
import io.github.mzmine.project.impl.RawDataFileImpl;
import io.github.mzmine.project.impl.StorableScan;
import io.github.mzmine.util.scans.ScanUtils;

public class RawDataFileOpenHandler_2_3 extends DefaultHandler
        implements RawDataFileOpenHandler {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private StringBuffer charBuffer;
    private RawDataFileImpl newRawDataFile;
    private int scanNumber;
    private int msLevel;
    private int[] fragmentScan;
    private int numberOfFragments;
    private double precursorMZ;
    private int precursorCharge;
    private double retentionTime;
    private MassSpectrumType spectrumType;
    private int dataPointsNumber;
    private long storageFileOffset;
    private int fragmentCount;
    private ArrayList<SimpleMassList> currentMassLists, allMassLists;

    private boolean canceled = false;

    /**
     * Extract the scan file and copies it into the temporary folder. Create a
     * new raw data file using the information from the XML raw data description
     * file
     * 
     * @param Name
     *            raw data file name
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public RawDataFile readRawDataFile(InputStream is, File scansFile)
            throws IOException, ParserConfigurationException, SAXException {

        storageFileOffset = 0;

        charBuffer = new StringBuffer();
        currentMassLists = new ArrayList<SimpleMassList>();
        allMassLists = new ArrayList<SimpleMassList>();

        newRawDataFile = (RawDataFileImpl) MZmineCore.createNewFile(null);
        newRawDataFile.openDataPointsFile(scansFile);

        // Reads the XML file (raw data description)
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(is, this);

        // Add all the mass lists after we opened the data points file, because
        // that is where the mass list data will be stored. This is a hack
        // because in MZmine 2.3 and 2.4 the mass lists were saved as XML
        // instead of being part of the data points file.
        for (SimpleMassList ml : allMassLists) {
            Scan s = ml.getScan();
            s.addMassList(ml);
        }

        RawDataFile rawDataFile = newRawDataFile.finishWriting();
        return rawDataFile;

    }

    public void cancel() {
        canceled = true;
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String lName, String qName,
            Attributes attrs) throws SAXException {

        if (canceled)
            throw new SAXException("Parsing canceled");

        // This will remove any remaining characters from previous elements
        getTextOfElement();

        if (qName.equals(RawDataElementName_2_3.QUANTITY_FRAGMENT_SCAN
                .getElementName())) {
            numberOfFragments = Integer.parseInt(attrs.getValue(
                    RawDataElementName_2_3.QUANTITY.getElementName()));
            if (numberOfFragments > 0) {
                fragmentScan = new int[numberOfFragments];
                fragmentCount = 0;
            }
        }

        if (qName.equals(RawDataElementName_2_3.MASS_LIST.getElementName())) {
            String name = attrs
                    .getValue(RawDataElementName_2_3.NAME.getElementName());
            SimpleMassList newML = new SimpleMassList(name, null, null);
            currentMassLists.add(newML);
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String sName, String qName)
            throws SAXException {

        if (canceled)
            throw new SAXException("Parsing canceled");

        // <NAME>
        if (qName.equals(RawDataElementName_2_3.NAME.getElementName())) {

            // Adds the scan file and the name to the new raw data file
            String name = getTextOfElement();
            logger.info("Loading raw data file: " + name);
            newRawDataFile.setName(name);
        }

        if (qName.equals(
                RawDataElementName_2_3.QUANTITY_SCAN.getElementName())) {
            // number of scans - actually not used for anything
            Integer.parseInt(getTextOfElement());
        }

        if (qName.equals(RawDataElementName_2_3.SCAN_ID.getElementName())) {
            scanNumber = Integer.parseInt(getTextOfElement());
        }

        if (qName.equals(RawDataElementName_2_3.MS_LEVEL.getElementName())) {
            msLevel = Integer.parseInt(getTextOfElement());
        }

        if (qName.equals(RawDataElementName_2_3.PARENT_SCAN.getElementName())) {
            Integer.parseInt(getTextOfElement());
        }

        if (qName
                .equals(RawDataElementName_2_3.PRECURSOR_MZ.getElementName())) {
            precursorMZ = Double.parseDouble(getTextOfElement());
        }

        if (qName.equals(
                RawDataElementName_2_3.PRECURSOR_CHARGE.getElementName())) {
            precursorCharge = Integer.parseInt(getTextOfElement());
        }

        if (qName.equals(
                RawDataElementName_2_3.RETENTION_TIME.getElementName())) {
            // Before MZmine 2.6 retention time was saved in seconds, but now we
            // use
            // minutes, so we need to divide by 60
            retentionTime = Double.parseDouble(getTextOfElement()) / 60d;
        }

        if (qName.equals(RawDataElementName_2_3.CENTROIDED.getElementName())) {
            boolean centroided = Boolean.parseBoolean(getTextOfElement());
            if (centroided)
                spectrumType = MassSpectrumType.CENTROIDED;
            else
                spectrumType = MassSpectrumType.PROFILE;
        }

        if (qName.equals(
                RawDataElementName_2_3.QUANTITY_DATAPOINTS.getElementName())) {
            dataPointsNumber = Integer.parseInt(getTextOfElement());
        }

        if (qName.equals(
                RawDataElementName_2_3.FRAGMENT_SCAN.getElementName())) {
            fragmentScan[fragmentCount++] = Integer
                    .parseInt(getTextOfElement());
        }

        if (qName.equals(RawDataElementName_2_3.MASS_LIST.getElementName())) {
            char encodedDataPoints[] = getTextOfElement().toCharArray();
            DataPoint dataPoints[] = ScanUtils
                    .decodeDataPointsBase64(encodedDataPoints);
            SimpleMassList newML = currentMassLists
                    .get(currentMassLists.size() - 1);
            newML.setDataPoints(dataPoints);
        }

        if (qName.equals(RawDataElementName_2_3.SCAN.getElementName())) {

            try {
                int newStorageID = 1;
                TreeMap<Integer, Long> dataPointsOffsets = newRawDataFile
                        .getDataPointsOffsets();
                TreeMap<Integer, Integer> dataPointsLengths = newRawDataFile
                        .getDataPointsLengths();
                if (!dataPointsOffsets.isEmpty())
                    newStorageID = dataPointsOffsets.lastKey().intValue() + 1;

                StorableScan storableScan = new StorableScan(newRawDataFile,
                        newStorageID, dataPointsNumber, scanNumber, msLevel,
                        retentionTime, precursorMZ, precursorCharge,
                        fragmentScan, spectrumType, PolarityType.UNKNOWN, "",
                        null);
                newRawDataFile.addScan(storableScan);

                dataPointsOffsets.put(newStorageID, storageFileOffset);
                dataPointsLengths.put(newStorageID, dataPointsNumber);

                for (SimpleMassList newML : currentMassLists) {
                    newML.setScan(storableScan);
                }
                allMassLists.addAll(currentMassLists);
                currentMassLists.clear();

            } catch (IOException e) {
                throw new SAXException(e);
            }
            storageFileOffset += dataPointsNumber * 4 * 2;

        }
    }

    /**
     * Return a string without tab an EOF characters
     * 
     * @return String element text
     */
    private String getTextOfElement() {
        String text = charBuffer.toString();
        text = text.replaceAll("[\n\r\t]+", "");
        text = text.replaceAll("^\\s+", "");
        charBuffer.delete(0, charBuffer.length());
        return text;
    }

    /**
     * characters()
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char buf[], int offset, int len)
            throws SAXException {
        charBuffer = charBuffer.append(buf, offset, len);
    }
}
