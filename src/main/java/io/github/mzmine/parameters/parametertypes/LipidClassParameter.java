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

import java.util.ArrayList;
import java.util.Collection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import io.github.mzmine.parameters.UserParameter;
import io.github.mzmine.util.CollectionUtils;

/**
 * 
 * @author Ansgar Korf (ansgar.korf@uni-muenster.de)
 */
public class LipidClassParameter<ValueType>
        implements UserParameter<ValueType[], LipidClassComponent> {

    private String name, description;
    private ValueType choices[], values[];
    private int minNumber;

    /**
     * We need the choices parameter non-null even when the length may be 0. We
     * need it to determine the class of the ValueType.
     */
    public LipidClassParameter(String name, String description,
            ValueType choices[]) {
        this(name, description, choices, null, 1);
    }

    public LipidClassParameter(String name, String description,
            ValueType choices[], ValueType values[]) {
        this(name, description, choices, values, 1);
    }

    public LipidClassParameter(String name, String description,
            ValueType choices[], ValueType values[], int minNumber) {

        assert choices != null;

        this.name = name;
        this.description = description;
        this.choices = choices;
        this.values = values;
        this.minNumber = minNumber;
    }

    /**
     * @see io.github.mzmine.data.Parameter#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public void setChoices(ValueType choices[]) {
        this.choices = choices;
    }

    public ValueType[] getChoices() {
        return choices;
    }

    /**
     * @see io.github.mzmine.data.Parameter#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public LipidClassComponent createEditingComponent() {
        return new LipidClassComponent(choices);
    }

    @Override
    public ValueType[] getValue() {
        return values;
    }

    @Override
    public void setValue(ValueType[] values) {
        this.values = values;
    }

    @Override
    public LipidClassParameter<ValueType> cloneParameter() {
        LipidClassParameter<ValueType> copy = new LipidClassParameter<ValueType>(
                name, description, choices, values);
        copy.setValue(this.getValue());
        return copy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValueFromComponent(LipidClassComponent component) {
        Object componentValue[] = component.getValue();
        Class<ValueType> arrayType = (Class<ValueType>) this.choices.getClass()
                .getComponentType();
        this.values = CollectionUtils.changeArrayType(componentValue,
                arrayType);
    }

    @Override
    public void setValueToComponent(LipidClassComponent component,
            ValueType[] newValue) {
        component.setValue(newValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadValueFromXML(Element xmlElement) {
        NodeList items = xmlElement.getElementsByTagName("item");
        ArrayList<ValueType> newValues = new ArrayList<ValueType>();
        for (int i = 0; i < items.getLength(); i++) {
            String itemString = items.item(i).getTextContent();
            for (int j = 0; j < choices.length; j++) {
                if (choices[j].toString().equals(itemString)) {
                    newValues.add(choices[j]);
                }
            }
        }
        Class<ValueType> arrayType = (Class<ValueType>) this.choices.getClass()
                .getComponentType();
        Object newArray[] = newValues.toArray();
        this.values = CollectionUtils.changeArrayType(newArray, arrayType);
    }

    @Override
    public void saveValueToXML(Element xmlElement) {
        if (values == null)
            return;
        Document parentDocument = xmlElement.getOwnerDocument();
        for (ValueType item : values) {
            Element newElement = parentDocument.createElement("item");
            newElement.setTextContent(item.toString());
            xmlElement.appendChild(newElement);
        }
    }

    @Override
    public boolean checkValue(Collection<String> errorMessages) {
        if (values == null) {
            errorMessages.add(name + " is not set properly");
            return false;
        }
        if (values.length < minNumber) {
            errorMessages.add("At least " + minNumber
                    + " option(s) must be selected for " + name);
            return false;
        }
        return true;
    }

}
