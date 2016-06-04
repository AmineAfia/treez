package org.treez.results.atom.contour;

import org.treez.core.atom.attribute.AttributeRoot;
import org.treez.core.atom.attribute.Page;
import org.treez.core.atom.attribute.Section;
import org.treez.core.atom.base.AbstractAtom;
import org.treez.core.atom.graphics.GraphicsAtom;
import org.treez.core.atom.graphics.GraphicsPropertiesPageFactory;
import org.treez.core.attribute.Attribute;
import org.treez.core.attribute.Wrap;
import org.treez.javafxd3.d3.D3;
import org.treez.javafxd3.d3.core.Selection;

@SuppressWarnings("checkstyle:visibilitymodifier")
public class Lines implements GraphicsPropertiesPageFactory {

	//#region ATTRIBUTES

	public final Attribute<String> color = new Wrap<>();

	public final Attribute<Double> width = new Wrap<>();

	public final Attribute<String> style = new Wrap<>();

	public final Attribute<Double> smoothing = new Wrap<>();

	public final Attribute<String> transparency = new Wrap<>();

	public final Attribute<Boolean> hide = new Wrap<>();

	//#end region

	//#region METHODS

	@Override
	public void createPage(AttributeRoot root, AbstractAtom parent) {

		Page linePage = root.createPage("line", "   Line    ");

		Section line = linePage.createSection("line", "Line");

		line.createColorChooser(color, "color", "black");

		line.createDoubleVariableField(width, this, 1.0);

		line.createLineStyle(style, "style", "solid");

		line.createDoubleVariableField(smoothing, this, 0.0);

		line.createTextField(transparency, "transparency", "0");

		line.createCheckBox(hide, "hide");
	}

	@Override
	public Selection plotWithD3(D3 d3, Selection contourSelection, Selection rectSelection, GraphicsAtom parent) {

		return contourSelection;
	}

	//#end region

}
