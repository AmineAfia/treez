package org.treez.core.atom.attribute;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.treez.core.Activator;
import org.treez.core.adaptable.Refreshable;
import org.treez.core.atom.attribute.base.parent.AbstractAttributeContainerAtom;
import org.treez.core.atom.base.annotation.IsParameter;

/**
 * An item example
 */
public class CheckBoxEnableTarget extends AbstractAttributeContainerAtom {

	//#region ATTRIBUTES

	@IsParameter(defaultValue = "true")
	private Boolean value;

	@IsParameter(defaultValue = "")
	private String targetPath; //e.g. "root.properties.mytext

	//#end region

	//#region CONSTRUCTORS

	/**
	 * @param enableValue
	 *            the boolean value for which the target is enabled
	 * @param targetPath
	 *            the model path to the target whose enabled state is controlled
	 */
	public CheckBoxEnableTarget(String name, Boolean enableValue,
			String targetPath) {
		super(name);
		setValue(enableValue);
		setTargetPath(targetPath);
	}

	/**
	 * Copy constructor
	 */
	private CheckBoxEnableTarget(
			CheckBoxEnableTarget checkBoxEnableTargetToCopy) {
		super(checkBoxEnableTargetToCopy);
		value = checkBoxEnableTargetToCopy.value;
		targetPath = checkBoxEnableTargetToCopy.targetPath;
	}

	//#end region

	//#region METHODS

	//#region COPY

	@Override
	public CheckBoxEnableTarget copy() {
		return new CheckBoxEnableTarget(this);
	}

	//#end region

	/**
	 * Provides an image to represent this atom
	 */
	@Override
	public Image provideImage() {
		return Activator.getImage("switch.png");
	}

	@Override
	public void createAtomControl(Composite parent,
			Refreshable treeViewerRefreshable) {

	}

	//#end region

	//#region ACCESSORS

	private void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public Boolean getValue() {
		return value;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

	//#end region

}
