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

package io.github.mzmine.parameters.parametertypes;

import java.util.Collection;

import org.w3c.dom.Element;

import io.github.mzmine.parameters.UserParameter;

public class MassListParameter
        implements UserParameter<String, MassListComponent> {

    private String name, description, value;

    /**
     * This new constructor will be used if cloneParameter is called, so it's
     * possible to create mass list parameters with a different name and
     * description if needed. (e.g. if it's an optional parameter)
     * 
     * @param name1
     *            Name of the parameter.
     * @param description1
     *            Description of the parameter.
     */
    public MassListParameter(String name1, String description1) {
        this.name = name1;
        this.description = description1;
    }

    public MassListParameter() {
        this.name = "Mass list";
        this.description = "Please select a mass list name";
    }

    /**
     * @see io.github.mzmine.data.Parameter#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @see io.github.mzmine.data.Parameter#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public MassListComponent createEditingComponent() {
        return new MassListComponent();
    }

    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public MassListParameter cloneParameter() {
        MassListParameter copy = new MassListParameter(name, description);
        copy.setValue(this.getValue());
        return copy;
    }

    @Override
    public void setValueFromComponent(MassListComponent component) {
        value = component.getValue();
    }

    @Override
    public void setValueToComponent(MassListComponent component,
            String newValue) {
        component.setValue(newValue);
    }

    @Override
    public void loadValueFromXML(Element xmlElement) {
        value = xmlElement.getTextContent();
    }

    @Override
    public void saveValueToXML(Element xmlElement) {
        if (value == null)
            return;
        xmlElement.setTextContent(value);
    }

    @Override
    public boolean checkValue(Collection<String> errorMessages) {
        if ((value == null) || (value.trim().length() == 0)) {
            errorMessages.add(name + " is not set properly");
            return false;
        }
        return true;
    }

}
