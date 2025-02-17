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

/**
 * Simple Parameter implementation
 * 
 * 
 */
public class PercentParameter
        implements UserParameter<Double, PercentComponent> {

    private final String name, description;
    private final Double defaultValue, minValue, maxValue;
    private Double value;

    public PercentParameter(String name, String description) {
        this(name, description, null, 0.0, 1.0);
    }

    public PercentParameter(final String name, final String description,
            final Double defaultValue) {
        this(name, description, defaultValue, 0.0, 1.0);
    }

    public PercentParameter(final String name, final String description,
            final Double defaultValue, final Double minValue, Double maxValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
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
    public PercentComponent createEditingComponent() {
        return new PercentComponent();
    }

    @Override
    public void setValueFromComponent(PercentComponent component) {
        Double componentValue = component.getValue();
        if (componentValue == null)
            return;
        this.value = componentValue;
    }

    @Override
    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public PercentParameter cloneParameter() {
        PercentParameter copy = new PercentParameter(name, description,
                defaultValue, minValue, maxValue);
        copy.setValue(this.getValue());
        return copy;
    }

    @Override
    public void setValueToComponent(PercentComponent component,
            Double newValue) {
        if (newValue == null)
            return;
        component.setValue(newValue);
    }

    @Override
    /**
     * Returns the percentage value in the range 0..1
     */
    public Double getValue() {
        return value;
    }

    @Override
    public void loadValueFromXML(Element xmlElement) {
        String numString = xmlElement.getTextContent();
        if (numString.length() == 0)
            return;
        this.value = Double.parseDouble(numString);
    }

    @Override
    public void saveValueToXML(Element xmlElement) {
        if (value == null)
            return;
        xmlElement.setTextContent(value.toString());
    }

    @Override
    public boolean checkValue(Collection<String> errorMessages) {
        if (value == null) {
            errorMessages.add(name + " is not set");
            return false;
        }
        if ((value < minValue) || (value > maxValue)) {
            errorMessages.add(name + " value must be in the range "
                    + (minValue * 100) + " - " + (maxValue * 100) + "%");
            return false;
        }
        return true;
    }
}
