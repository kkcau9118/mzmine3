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

package io.github.mzmine.modules.io.xmlexport;

import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.BooleanParameter;
import io.github.mzmine.parameters.parametertypes.filenames.FileNameParameter;
import io.github.mzmine.parameters.parametertypes.selectors.PeakListsParameter;

public class XMLExportParameters extends SimpleParameterSet {

    public static final PeakListsParameter peakLists = new PeakListsParameter(
            1);

    public static final FileNameParameter filename = new FileNameParameter(
            "Filename",
            "Use pattern \"{}\" in the file name to substitute with feature list name. "
                    + "(i.e. \"blah{}blah.mpl\" would become \"blahSourcePeakListNameblah.mpl\"). "
                    + "If the file already exists, it will be overwritten.",
            "mpl");

    public static final BooleanParameter compression = new BooleanParameter(
            "Compressed file", "Generates a compressed file (.zip)");

    public XMLExportParameters() {
        super(new Parameter[] { peakLists, filename, compression });
    }
}
