package org.treez.core.atom.variablefield;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.treez.core.Activator;
import org.treez.core.adaptable.FocusChangingRefreshable;
import org.treez.core.atom.attribute.base.AbstractStringAttributeAtom;
import org.treez.core.atom.attribute.fileSystem.FilePath;
import org.treez.core.atom.variablelist.AbstractVariableListField;
import org.treez.core.atom.variablelist.StringVariableListField;

public class FilePathVariableField extends FilePath implements VariableField<FilePathVariableField, String> {

	//#region CONSTRUCTORS
	public FilePathVariableField(String name) {
		super(name);
		this.setShowEnabledCheckBox(true);
	}

	/**
	 * Copy constructor
	 */
	private FilePathVariableField(FilePathVariableField atomToCopy) {
		super(atomToCopy);
	}

	//#end region

	//#region METHODS

	@Override
	public FilePathVariableField getThis() {
		return this;
	}

	@Override
	public FilePathVariableField copy() {
		return new FilePathVariableField(this);
	}

	@Override
	public Image provideImage() {
		Image baseImage = Activator.getImage("filePathVariable.png");
		Image image;
		if (isEnabled()) {
			image = Activator.getOverlayImageStatic(baseImage, "enabledDecoration.png");
		} else {
			image = Activator.getOverlayImageStatic(baseImage, "disabledDecoration.png");
		}
		return image;
	}

	@Override
	public AbstractStringAttributeAtom<FilePath> createAttributeAtomControl(
			Composite parent,
			FocusChangingRefreshable treeViewerRefreshable) {
		this.treeViewRefreshable = treeViewerRefreshable;
		super.createAttributeAtomControl(parent, treeViewerRefreshable);

		return this;
	}

	@Override
	public AbstractVariableListField<?, String> createVariableListField() {
		//TODO: Solve the return type problem to use the FilePathList class in variablelist
		//		FilePathList listField = new FilePathList(name);
		//		String currentValue = get();
		//		listField.setValue(currentValue);
		//
		//		return listField;

		StringVariableListField listField = new StringVariableListField(name);
		List<String> valueList = new ArrayList<>();
		String currentValue = get();
		valueList.add(currentValue);
		listField.set(valueList);

		return listField;
	}

	//#end region

	//#region ACCESSORS

	@Override
	public FilePathVariableField setEnabled(boolean state) {
		super.setEnabled(state);
		return getThis();
	}

	@Override
	public FilePathVariableField setLabel(String label) {
		super.setLabel(label);
		return getThis();
	}

	@Override
	public String getValueString() {
		String value = this.get();
		return value;
	}

	@Override
	public FilePathVariableField setValueString(String valueString) {
		this.set(valueString);
		return getThis();
	}

	@Override
	public FilePathVariableField setBackgroundColor(Color color) {
		super.setBackgroundColor(color);
		return getThis();
	}

	//#end region

}
