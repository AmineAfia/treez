package org.treez.results.atom.contour;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.graphics.Image;
import org.treez.core.adaptable.FocusChangingRefreshable;
import org.treez.core.adaptable.Refreshable;
import org.treez.core.atom.graphics.GraphicsPropertiesPageFactory;
import org.treez.core.treeview.TreeViewerRefreshable;
import org.treez.javafxd3.d3.D3;
import org.treez.javafxd3.d3.core.Selection;
import org.treez.javafxd3.d3.scales.QuantitativeScale;
import org.treez.results.Activator;
import org.treez.results.atom.axis.Axis;
import org.treez.results.atom.graphicspage.GraphicsPropertiesPage;
import org.treez.results.atom.legend.LegendContributor;

@SuppressWarnings("checkstyle:visibilitymodifier")
public class Contour extends GraphicsPropertiesPage implements LegendContributor {

	//#region ATTRIBUTES

	public Data data;

	public Fill fill;

	public Lines lines;

	//public Label label;
	//public ErrorBar errorBar;

	private Selection contourSelection;

	//#end region

	//#region CONSTRUCTORS

	public Contour(String name) {
		super(name);
	}

	//#end region

	//#region METHODS

	@Override
	protected void createPropertyPageFactories() {

		data = new Data();
		propertyPageFactories.add(data);

		fill = new Fill();
		propertyPageFactories.add(fill);

		lines = new Lines();
		propertyPageFactories.add(lines);

		//label = new Label();
		//errorBar = new ErrorBar();

	}

	@Override
	public Image provideImage() {
		return Activator.getImage("contour.png");
	}

	@Override
	protected List<Object> extendContextMenuActions(List<Object> actions, TreeViewerRefreshable treeViewer) {
		// no actions available right now
		return actions;
	}

	@Override
	public void execute(FocusChangingRefreshable refreshable) {
		treeViewRefreshable = refreshable;
	}

	@Override
	public Selection plotWithD3(
			D3 d3,
			Selection graphSelection,
			Selection graphRectSelection,
			FocusChangingRefreshable refreshable) {
		Objects.requireNonNull(d3);
		this.treeViewRefreshable = refreshable;

		graphSelection //
				.select("#" + name) //
				.remove();

		contourSelection = graphSelection //
				.insert("g", ".axis") //
				.attr("class", "contour") //
				.onMouseClick(this);
		bindNameToId(contourSelection);

		updatePlotWithD3(d3);

		return contourSelection;
	}

	@Override
	public void updatePlotWithD3(D3 d3) {
		plotPageModels(d3);
	}

	private void plotPageModels(D3 d3) {
		for (GraphicsPropertiesPageFactory pageModel : propertyPageFactories) {
			contourSelection = pageModel.plotWithD3(d3, contourSelection, null, this);
		}
	}

	@Override
	public void addLegendContributors(List<LegendContributor> legendContributors) {
		if (providesLegendEntry()) {
			legendContributors.add(this);
		}
	}

	@Override
	public boolean providesLegendEntry() {
		return !getLegendText().isEmpty();
	}

	@Override
	public String getLegendText() {
		return data.legendText.get();
	}

	@Override
	public Selection createLegendSymbolGroup(
			D3 d3,
			Selection parentSelection,
			int symbolLengthInPx,
			Refreshable refreshable) {
		Selection symbolSelection = parentSelection //
				.append("rect") //
				.classed("bar-legend-entry-symbol", true);

		this.fill.formatLegendSymbol(symbolSelection, symbolLengthInPx);
		this.lines.formatLegendSymbolLine(symbolSelection, refreshable);

		return symbolSelection;
	}

	public String getBarDataString() {

		List<Object> lengthDataValues = getLengthData();
		List<Object> positionDataValues = getPositionData();

		int lengthSize = lengthDataValues.size();
		int positionSize = positionDataValues.size();
		boolean sizesAreOk = lengthSize == positionSize;
		if (!sizesAreOk) {
			String message = "The length and position data has to be of equal size but size of length data is "
					+ lengthSize + " and size of position data is " + positionSize;
			throw new IllegalStateException(message);
		}

		List<String> rowList = new java.util.ArrayList<>();
		for (int rowIndex = 0; rowIndex < lengthSize; rowIndex++) {
			Object lengthDatum = lengthDataValues.get(rowIndex);
			Double length = Double.parseDouble(lengthDatum.toString());

			Object positionDatum = positionDataValues.get(rowIndex);
			Double position = Double.parseDouble(positionDatum.toString());

			String rowString = "[" + length + "," + position + "]";
			rowList.add(rowString);
		}
		String dataString = "[" + String.join(",", rowList) + "]";
		return dataString;
	}

	public int getPositionSize() {
		List<Object> positionDataValues = getPositionData();
		return positionDataValues.size();
	}

	public double getSmallestPositionDistance() {

		double smallestDistance = Double.MAX_VALUE;

		List<Object> positionDataValues = getPositionData();
		int positionSize = positionDataValues.size();

		if (positionSize > 1) {
			for (int positionIndex = 1; positionIndex < positionSize; positionIndex++) {
				Object leftPositionObj = positionDataValues.get(positionIndex - 1);
				Double leftPosition = Double.parseDouble(leftPositionObj.toString());

				Object rightPositionObj = positionDataValues.get(positionIndex);
				Double rightPosition = Double.parseDouble(rightPositionObj.toString());

				double distance = Math.abs(rightPosition - leftPosition);
				if (distance < smallestDistance) {
					smallestDistance = distance;
				}
			}
			return smallestDistance;
		} else {
			return 0;
		}

	}

	public QuantitativeScale<?> getXScale() {
		Axis xAxisAtom = getXAxis();
		if (xAxisAtom == null) {
			return null;
		}
		QuantitativeScale<?> scale = (QuantitativeScale<?>) xAxisAtom.getScale();
		return scale;
	}

	public QuantitativeScale<?> getYScale() {
		Axis yAxisAtom = getYAxis();
		if (yAxisAtom == null) {
			return null;
		}
		QuantitativeScale<?> scale = (QuantitativeScale<?>) yAxisAtom.getScale();
		return scale;
	}

	private Axis getXAxis() {
		String xAxisPath = data.xAxis.get();
		if (xAxisPath == null || xAxisPath.isEmpty()) {
			return null;
		}
		Axis xAxisAtom = getChildFromRoot(xAxisPath);
		return xAxisAtom;
	}

	private Axis getYAxis() {
		String yAxisPath = data.yAxis.get();
		if (yAxisPath == null || yAxisPath.isEmpty()) {
			return null;
		}
		Axis yAxisAtom = getChildFromRoot(yAxisPath);
		return yAxisAtom;
	}

	private List<Object> getLengthData() {
		String xDataPath = data.barLengths.get();
		if (xDataPath.isEmpty()) {
			return new ArrayList<>();
		}
		org.treez.data.column.Column xDataColumn = getChildFromRoot(xDataPath);
		List<Object> xDataValues = xDataColumn.getValues();
		return xDataValues;
	}

	private List<Object> getPositionData() {
		String yDataPath = data.barPositions.get();
		if (yDataPath.isEmpty()) {
			return new ArrayList<>();
		}
		org.treez.data.column.Column yDataColumn = getChildFromRoot(yDataPath);
		List<Object> yDataValues = yDataColumn.getValues();
		return yDataValues;
	}

	//#end region

}
