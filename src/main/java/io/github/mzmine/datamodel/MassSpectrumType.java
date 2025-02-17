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

package io.github.mzmine.datamodel;

/**
 * Defines a type of the mass spectrum. For exact definition of different
 * spectra types, see Deutsch, E. W. (2012). File Formats Commonly Used in Mass
 * Spectrometry Proteomics. Molecular & Cellular Proteomics, 11(12), 1612–1621.
 * doi:10.1074/mcp.R112.019695
 */
public enum MassSpectrumType {

    /**
     * Continuous (profile) mass spectrum. Continuous stream of connected data
     * points forms a spectrum consisting of individual peaks. Peaks represent
     * detected ions. Each peak consists of multiple data points.
     */
    PROFILE,

    /**
     * Thresholded mass spectrum = same as profile, but data points below
     * certain intensity threshold are removed.
     */
    THRESHOLDED,

    /**
     * Centroided mass spectrum = discrete data points, one for each detected
     * ion.
     */
    CENTROIDED;

}
