package org.treez.study.atom.probability;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.treez.core.adaptable.FocusChangingRefreshable;
import org.treez.core.atom.attribute.attributeContainer.AttributeRoot;
import org.treez.core.atom.attribute.attributeContainer.Page;
import org.treez.core.atom.attribute.attributeContainer.section.Section;
import org.treez.core.atom.attribute.checkBox.CheckBox;
import org.treez.core.atom.attribute.fileSystem.FilePath;
import org.treez.core.atom.attribute.modelPath.ModelPathSelectionType;
import org.treez.core.atom.base.AbstractAtom;
import org.treez.core.treeview.TreeViewerRefreshable;
import org.treez.core.treeview.action.AddChildAtomTreeViewerAction;
import org.treez.model.interfaces.Model;
import org.treez.study.Activator;
import org.treez.study.atom.AbstractParameterVariation;

/**
 * Represents a probability parameter variation. Each parameter is specified with a probability distribution. the
 * definition space.
 */
public class Probability extends AbstractParameterVariation {

	//#region ATTRIBUTES

	//#end region

	//#region CONSTRUCTORS

	/**
	 * Constructor
	 *
	 * @param name
	 */
	public Probability(String name) {
		super(name);
		createProbabilityModel();
	}

	//#end region

	//#region METHODS

	/**
	 * Creates the underlying model
	 */
	private void createProbabilityModel() {
		// root, page and section
		AttributeRoot root = new AttributeRoot("root");
		Page dataPage = root.createPage("data", "   Data   ");

		String relativeHelpContextId = "probability";
		String absoluteHelpContextId = Activator.getInstance().getAbsoluteHelpContextId(relativeHelpContextId);

		Section pickingSection = dataPage.createSection("probability", absoluteHelpContextId);
		pickingSection.createSectionAction("action", "Run probability", () -> execute(treeViewRefreshable));

		//choose selection type and entry atom
		ModelPathSelectionType selectionType = ModelPathSelectionType.FLAT;
		AbstractAtom<?> modelEntryPoint = this;

		//model to run
		String modelToRunDefaultValue = "";
		pickingSection
				.createModelPath(modelToRunModelPath, this, modelToRunDefaultValue, Model.class, selectionType,
						modelEntryPoint, false)
				.setLabel("Model to run");

		//variable source model
		String sourceModelDefaultValue = "";
		pickingSection
				.createModelPath(sourceModelPath, this, sourceModelDefaultValue, Model.class, selectionType,
						modelEntryPoint, false)
				.setLabel("Variable source model (provides variables)");

		//export study info check box
		CheckBox exportStudy = pickingSection.createCheckBox(exportStudyInfo, this, true);
		exportStudy.setLabel("Export study information");

		//export study info path
		FilePath filePath = pickingSection.createFilePath(exportStudyInfoPath, this,
				"Target file path for study information", "");
		filePath.setValidatePath(false);
		filePath.addModificationConsumer("updateEnabledState", () -> {
			boolean exportSweepInfoEnabled = exportStudyInfo.get();
			filePath.setEnabled(exportSweepInfoEnabled);
		});

		setModel(root);
	}

	/**
	 * Provides an image to represent this atom
	 */
	@Override
	public Image provideImage() {
		return Activator.getImage("probability.png");
	}

	/**
	 * Creates the context menu actions for this atom
	 */
	@Override
	protected List<Object> createContextMenuActions(TreeViewerRefreshable treeViewer) {

		List<Object> actions = new ArrayList<>();

		//normal assumption
		Action normalAssumptionAction = new AddChildAtomTreeViewerAction(
				NormalAssumption.class,
				"normal",
				Activator.getImage("normalAssumption.png"),
				this,
				treeViewer);
		actions.add(normalAssumptionAction);

		//equal assumption
		Action equalAssumptionAction = new AddChildAtomTreeViewerAction(
				EqualAssumption.class,
				"equal",
				Activator.getImage("equalAssumption.png"),
				this,
				treeViewer);
		actions.add(equalAssumptionAction);

		return actions;
	}

	@Override
	public void runStudy(FocusChangingRefreshable refreshable, IProgressMonitor monitor) {
		//not yet implemented
	}

	//#region CREATE CHILD ATOMS

	/**
	 * Creates a new normal assumption
	 *
	 * @param name
	 * @return
	 */
	public NormalAssumption createNormalAssumption(String name) {
		NormalAssumption assumption = new NormalAssumption(name);
		this.addChild(assumption);
		return assumption;
	}

	//#end region

	//#end region

	//#region ACCESSORS

	@Override
	public String getSourceModelPath() {
		//return sourceModelPath.getValue();
		return "not implemented";
	}

	@Override
	public String getModelToRunModelPath() {
		// TODO Auto-generated method stub
		return "not implemented";
	}

	//#end region

}
