package org.treez.core.atom.variablefield;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.treez.core.Activator;
import org.treez.core.adaptable.FocusChangingRefreshable;
import org.treez.core.atom.attribute.CheckBox;
import org.treez.core.atom.attribute.base.AbstractBooleanAttributeAtom;
import org.treez.core.atom.variablelist.AbstractVariableListField;

public class BooleanVariableField extends CheckBox implements VariableField<Boolean> {

	//#region CONSTRUCTORS

	public BooleanVariableField(String name) {
		super(name);
	}

	/**
	 * Copy constructor
	 */
	private BooleanVariableField(BooleanVariableField checkBoxToCopy) {
		super(checkBoxToCopy);
	}

	//#end region

	//#region METHODS

	@Override
	public BooleanVariableField copy() {
		return new BooleanVariableField(this);
	}

	@Override
	public Image provideImage() {
		Image baseImage = Activator.getImage("booleanVariable.png");
		Image image;
		if (isEnabled()) {
			image = Activator.getOverlayImageStatic(baseImage, "enabledDecoration.png");
		} else {
			image = Activator.getOverlayImageStatic(baseImage, "disabledDecoration.png");
		}
		return image;
	}

	@Override
	public AbstractBooleanAttributeAtom createAttributeAtomControl(
			Composite parent,
			FocusChangingRefreshable treeViewerRefreshable) {
		this.treeViewRefreshable = treeViewerRefreshable;

		super.createAttributeAtomControl(parent, treeViewerRefreshable);

		return this;
	}

	@Override
	public AbstractVariableListField<Boolean> createVariableListField() {

		throw new IllegalStateException("Not yet implemented");
	}

	//#end region

	//#region ACCESSORS

	@Override
	public String getValueString() {
		Boolean value = this.get();
		String valueString = value.toString();
		return valueString;
	}

	@SuppressWarnings("checkstyle:illegalcatch")
	@Override
	public void setValueString(String valueString) {
		try {
			Boolean value = new Boolean(valueString);
			this.set(value);
		} catch (Exception exception) {
			String message = "The string value '" + valueString + "' could not be inteprted as Boolean value";
			throw new IllegalStateException(message);
		}
	}

	//#end region

}
