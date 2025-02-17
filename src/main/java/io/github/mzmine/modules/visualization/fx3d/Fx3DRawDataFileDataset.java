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
package io.github.mzmine.modules.visualization.fx3d;

import java.util.logging.Logger;

import io.github.mzmine.datamodel.RawDataFile;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * @author akshaj This class represents the dataset of a Raw data file.
 */
public class Fx3DRawDataFileDataset extends Fx3DAbstractDataset {

    private static final int SIZE = 500;
    private static float AMPLIFI = 130;
    private int rtResolution;
    private int mzResolution;
    private MeshView meshView = new MeshView();
    private static final Logger LOG = Logger
            .getLogger(Fx3DRawDataFileDataset.class.getName());
    private TriangleMesh mesh;
    private int[][] peakListIndices;
    private float[][] intensityValues;
    private double maxBinnedIntensity;
    private float maxIntensityValue = Float.NEGATIVE_INFINITY;

    public Fx3DRawDataFileDataset(RawDataFile dataFile,
            float[][] intensityValues, int rtResolution, int mzResolution,
            double maxBinnedIntensity, String fileName, Color peakColor) {
        super(dataFile, fileName, peakColor);
        this.intensityValues = intensityValues;
        this.rtResolution = rtResolution;
        this.mzResolution = mzResolution;
        this.maxBinnedIntensity = maxBinnedIntensity;
        mesh = new TriangleMesh();

        peakListIndices = new int[rtResolution][mzResolution];
        float factorX = (float) SIZE / rtResolution;
        float factorZ = (float) SIZE / mzResolution;

        for (int i = 0; i < rtResolution; i++) {
            for (int j = 0; j < mzResolution; j++) {
                if (maxIntensityValue < intensityValues[i][j]) {
                    maxIntensityValue = intensityValues[i][j];
                }
            }
        }

        for (int x = 0; x < rtResolution; x++) {
            for (int z = 0; z < mzResolution; z++) {
                mesh.getPoints().addAll((float) x * factorX,
                        -intensityValues[x][z] * AMPLIFI, (float) z * factorZ);
                if (intensityValues[x][z] > 0.022 * maxIntensityValue) {
                    peakListIndices[x][z] = 1;
                }
            }
        }

        int rtLength = rtResolution;
        int mzLength = mzResolution;
        float rtTotal = rtLength;
        float mzTotal = mzLength;

        for (float x = 0; x < rtLength - 1; x++) {
            for (float y = 0; y < mzLength - 1; y++) {

                float x0 = x / rtTotal;
                float y0 = y / mzTotal;
                float x1 = (x + 1) / rtTotal;
                float y1 = (y + 1) / mzTotal;

                mesh.getTexCoords().addAll( //
                        x0, y0, // 0, top-left
                        x0, y1, // 1, bottom-left
                        x1, y0, // 2, top-right
                        x1, y1 // 3, bottom-right
                );
            }
        }

        // faces
        for (int x = 0; x < rtLength - 1; x++) {
            for (int z = 0; z < mzLength - 1; z++) {

                int tl = x * mzLength + z; // top-left
                int bl = x * mzLength + z + 1; // bottom-left
                int tr = (x + 1) * mzLength + z; // top-right
                int br = (x + 1) * mzLength + z + 1; // bottom-right

                int offset = (x * (mzLength - 1) + z) * 8 / 2; // div 2 because
                                                               // we have u AND
                                                               // v in the list

                // working
                mesh.getFaces().addAll(bl, offset + 1, tl, offset + 0, tr,
                        offset + 2);
                mesh.getFaces().addAll(tr, offset + 2, br, offset + 3, bl,
                        offset + 1);

            }
        }
        setNodeColor(peakColor);
        meshView.setMesh(mesh);
        meshView.setCullFace(CullFace.NONE);
        meshView.setDrawMode(DrawMode.FILL);
        meshView.setDepthTest(DepthTest.ENABLE);
        LOG.finest("Plot mesh is ready.");
    }

    /*
     * Sets the peak color
     * 
     * @see io.github.mzmine.modules.visualization.fx3d.Fx3DAbstractDataset#
     * setNodeColor (javafx.scene.paint.Color)
     */
    public void setNodeColor(Color peakColor) {
        int width = rtResolution;
        int height = mzResolution;
        WritableImage wr = new WritableImage(width, height);
        PixelWriter pw = wr.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                float value = intensityValues[x][y];
                double gray = normalizeValue(value, 0, maxIntensityValue, 0.,
                        1.);
                gray = clamp(gray, 0, 1);

                if (0 <= gray && gray <= 0.1) {
                    Color color = Color.GREY.interpolate(peakColor, gray);
                    pw.setColor(x, y, color);
                } else {
                    if (peakListIndices[x][y] == 1) {
                        Color color = peakColor;
                        pw.setColor(x, y, color);
                        if (x - 1 >= 0 && y - 1 >= 0) {
                            pw.setColor(x - 1, y - 1, color);
                            pw.setColor(x, y - 1, color);
                            pw.setColor(x - 1, y, color);
                        }
                    }
                }
            }
        }
        Image diffuseMap = wr;
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(diffuseMap);
        meshView.setMaterial(material);
    }

    private static double normalizeValue(double value, double min, double max,
            double newMin, double newMax) {

        return (value - min) * (newMax - newMin) / (max - min) + newMin;

    }

    private static double clamp(double value, double min, double max) {

        if (Double.compare(value, min) < 0)
            return min;

        if (Double.compare(value, max) > 0)
            return max;

        return value;
    }

    /*
     * Normalizes each data plot when the maxIntensity of the 3D plot changes.
     * 
     * @see io.github.mzmine.modules.visualization.fx3d.Fx3DAbstractDataset#
     * normalize( double)
     */
    public void normalize(double maxOfAllBinnedIntensities) {
        float factorX = (float) SIZE / rtResolution;
        float factorZ = (float) SIZE / mzResolution;
        float factorY = (float) ((float) maxBinnedIntensity
                / maxOfAllBinnedIntensities);
        mesh.getPoints().clear();
        for (int x = 0; x < rtResolution; x++) {
            for (int z = 0; z < mzResolution; z++) {
                mesh.getPoints().addAll((float) x * factorX,
                        -intensityValues[x][z] * AMPLIFI * factorY,
                        (float) z * factorZ);
                if (intensityValues[x][z] > 0.022 * maxIntensityValue) {
                    peakListIndices[x][z] = 1;
                }
            }
        }
    }

    public double getMaxBinnedIntensity() {
        return maxBinnedIntensity;
    }

    public Node getNode() {
        return meshView;
    }

    @Override
    public RawDataFile getFile() {
        return getDataFile();
    }

}
