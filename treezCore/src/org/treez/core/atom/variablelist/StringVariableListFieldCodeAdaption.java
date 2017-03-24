package org.treez.core.atom.variablelist;

import java.util.List;

import org.treez.core.adaptable.CodeContainer;
import org.treez.core.atom.attribute.base.AttributeAtomCodeAdaption;
import org.treez.core.atom.base.AbstractAtom;
import org.treez.core.utils.Utils;

public class StringVariableListFieldCodeAdaption extends AttributeAtomCodeAdaption<List<String>> {

	//#region CONSTRUCTORS

	public StringVariableListFieldCodeAdaption(StringVariableListField atom) {
		super(atom);
	}

	//#end region

	//#region METHODS

	@Override
	public CodeContainer extendAttributeCodeContainerForModelParent(
			AbstractAtom<?> parentAtom,
			AbstractAtom<?> intermediateAtom,
			CodeContainer parentContainer) {

		CodeContainer extendedContainer = parentContainer;

		StringVariableListField variableListField = (StringVariableListField) atom;
		boolean hasDefaultValue = variableListField.hasDefaultValue();
		if (hasDefaultValue) {
			return extendedContainer;
		} else {

			String attributeName = variableListField.getName();

			String valueString = variableListField.getValueString();
			if (valueString != null) {
				String setterName = "set" + Utils.firstToUpperCase(attributeName) + "ValueString";
				checkIfSetterExists(parentAtom, setterName, String.class);
				extendedContainer.extendBulk("\t\t" + VARIABLE_NAME + "." + setterName + "(\"" + valueString + "\");");
			}

			return extendedContainer;
		}
	}

	//#end region

}
