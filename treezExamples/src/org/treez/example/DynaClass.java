package org.treez.example;

import org.treez.core.atom.attribute.AttributeRoot;
import org.treez.core.scripting.ModelProvider;
import org.treez.data.variable.VariableDefinition;

public class DynaClass extends ModelProvider {

	@Override
	public org.treez.core.atom.attribute.AttributeRoot createModel() {
		AttributeRoot root = new AttributeRoot("root");

		//AbstractAdjustableAtom adjustableAtom = new AbstractAdjustableAtom("adjustableAtom");
		//root.addChild(adjustableAtom);

		VariableDefinition defItem = new VariableDefinition("defItem");
		root.addChild(defItem);

		defItem.define("a", "[1, 2; 3, 55]*u.m", "");
		defItem.define("b", "a * u.s", "");
		defItem.define("c", "a/b", "");
		defItem.define("d", "a+b", "");

		return root;

	}
}
