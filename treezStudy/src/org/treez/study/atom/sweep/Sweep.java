package org.treez.study.atom.sweep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.treez.core.adaptable.FocusChangingRefreshable;
import org.treez.core.atom.attribute.attributeContainer.AttributeRoot;
import org.treez.core.atom.attribute.attributeContainer.Page;
import org.treez.core.atom.attribute.attributeContainer.section.Section;
import org.treez.core.atom.attribute.checkBox.CheckBox;
import org.treez.core.atom.attribute.fileSystem.FilePath;
import org.treez.core.atom.attribute.modelPath.ModelPathSelectionType;
import org.treez.core.atom.attribute.text.TextField;
import org.treez.core.atom.base.AbstractAtom;
import org.treez.core.atom.variablefield.VariableField;
import org.treez.core.treeview.TreeViewerRefreshable;
import org.treez.core.treeview.action.AddChildAtomTreeViewerAction;
import org.treez.core.utils.Utils;
import org.treez.data.database.sqlite.SqLiteDatabase;
import org.treez.data.output.OutputAtom;
import org.treez.model.input.HashMapModelInput;
import org.treez.model.input.ModelInput;
import org.treez.model.interfaces.Model;
import org.treez.model.output.ModelOutput;
import org.treez.study.Activator;
import org.treez.study.atom.AbstractParameterVariation;
import org.treez.study.atom.range.AbstractVariableRange;
import org.treez.study.atom.range.BooleanVariableRange;
import org.treez.study.atom.range.DirectoryPathVariableRange;
import org.treez.study.atom.range.DoubleVariableRange;
import org.treez.study.atom.range.FilePathVariableRange;
import org.treez.study.atom.range.IntegerVariableRange;
import org.treez.study.atom.range.QuantityVariableRange;
import org.treez.study.atom.range.StringItemVariableRange;
import org.treez.study.atom.range.StringVariableRange;

/**
 * Represents a parameter sweep with a maximum of two parameters
 */
@SuppressWarnings({ "checkstyle:visibilitymodifier", "checkstyle:classfanoutcomplexity" })
public class Sweep extends AbstractParameterVariation {

	private static final Logger LOG = Logger.getLogger(Sweep.class);

	//#region CONSTRUCTORS

	public Sweep(String name) {
		super(name);
		createSweepModel();
	}

	//#end region

	//#region METHODS

	private void createSweepModel() {
		// root, page and section
		AttributeRoot root = new AttributeRoot("root");
		Page dataPage = root.createPage("data", "   Data   ");

		String relativeHelpContextId = "sweep";
		String absoluteHelpContextId = Activator.getInstance().getAbsoluteHelpContextId(relativeHelpContextId);

		Section sweepSection = dataPage.createSection("sweep", absoluteHelpContextId);
		sweepSection.createSectionAction("action", "Run sweep", () -> execute(treeViewRefreshable));

		//studyId
		TextField studyIdField = sweepSection.createTextField(studyId, this, "");
		studyIdField.setLabel("Id");

		//description
		TextField descriptionField = sweepSection.createTextField(studyDescription, this);
		descriptionField.setLabel("Description");

		//choose selection type and entry atom
		ModelPathSelectionType selectionType = ModelPathSelectionType.FLAT;
		AbstractAtom<?> modelEntryPoint = this;

		//model to run
		String modelToRunDefaultValue = "";
		sweepSection
				.createModelPath(modelToRunModelPath, this, modelToRunDefaultValue, Model.class, selectionType,
						modelEntryPoint, false)
				.setLabel("Model to run");

		//source model
		String sourceModelDefaultValue = "";
		sweepSection
				.createModelPath(sourceModelPath, this, sourceModelDefaultValue, Model.class, selectionType,
						modelEntryPoint, false)
				.setLabel("Variable source model (provides variables)");

		//study info
		Section studyInfoSection = dataPage.createSection("studyInfo", absoluteHelpContextId);
		studyInfoSection.setLabel("Export study info");

		//export study info check box
		CheckBox exportStudy = studyInfoSection.createCheckBox(exportStudyInfo, this, true);
		exportStudy.setLabel("Export study information");

		//export sweep info path
		FilePath filePath = studyInfoSection.createFilePath(exportStudyInfoPath, this,
				"Target file path for study information", "");
		filePath.setValidatePath(false);
		filePath.addModificationConsumer("updateEnabledState", () -> {
			boolean exportSweepInfoEnabled = exportStudyInfo.get();
			filePath.setEnabled(exportSweepInfoEnabled);
		}

		);

		setModel(root);
	}

	/**
	 * Executes the sweep
	 */
	@Override
	public void execute(FocusChangingRefreshable refreshable) {
		String jobTitle = "Sweep '" + getName() + "'";
		runNonUiJob(jobTitle, (monitor) -> {
			runStudy(refreshable, monitor);
		});

	}

	/**
	 * Runs the study
	 */
	@Override
	public void runStudy(FocusChangingRefreshable refreshable, IProgressMonitor monitor) {
		Objects.requireNonNull(monitor, "You need to pass a valid IProgressMonitor that is not null.");
		this.treeViewRefreshable = refreshable;

		String startMessage = "Executing sweep '" + getName() + "'";
		LOG.info(startMessage);

		//create ModelInput generator
		String sweepModelPath = Sweep.this.createTreeNodeAdaption().getTreePath();
		SweepModelInputGenerator inputGenerator = new SweepModelInputGenerator(sweepModelPath);

		//get variable ranges
		List<AbstractVariableRange<?>> variableRanges = inputGenerator.getEnabledVariableRanges(this);
		LOG.info("Number of variable ranges: " + variableRanges.size());

		//check if all variable ranges reference enabled variables
		boolean allReferencedVariablesAreActive = checkIfAllREferencedVariablesAreActive(variableRanges);
		if (allReferencedVariablesAreActive) {
			doRunStudy(refreshable, monitor, inputGenerator, variableRanges);
		}

	}

	private void doRunStudy(
			FocusChangingRefreshable refreshable,
			IProgressMonitor monitor,
			SweepModelInputGenerator inputGenerator,
			List<AbstractVariableRange<?>> variableRanges) {
		//get total number of simulations
		int numberOfSimulations = inputGenerator.getNumberOfSimulations(variableRanges);
		LOG.info("Number of total simulations: " + numberOfSimulations);

		//initialize progress monitor
		monitor.beginTask("", numberOfSimulations);

		//reset job index to 1
		HashMapModelInput.resetIdCounter();

		//create model inputs
		List<ModelInput> modelInputs = inputGenerator.createModelInputs(variableRanges);

		//exports study info if the corresponding option is enabled
		if (exportStudyInfo.get()) {
			exportStudyInfo(variableRanges, modelInputs, numberOfSimulations);
		}

		//prepare result structure
		prepareResultStructure();
		refresh();

		//get sweep output atom
		String sweepOutputAtomPath = getStudyOutputAtomPath();
		AbstractAtom<?> sweepOutputAtom = this.getChildFromRoot(sweepOutputAtomPath);

		//remove all old children if they exist
		sweepOutputAtom.removeAllChildren();

		//execute target model for all model inputs
		executeTargetModel(refreshable, monitor, numberOfSimulations, modelInputs, sweepOutputAtom);

		//inform progress monitor to be done
		monitor.setTaskName("=>Finished!");

		//show end message
		logAndShowSweepEndMessage();
		LOG.info("The sweep outout is located at " + sweepOutputAtomPath);
		monitor.done();
	}

	private void executeTargetModel(
			FocusChangingRefreshable refreshable,
			IProgressMonitor monitor,
			int numberOfSimulations,
			List<ModelInput> modelInputs,
			AbstractAtom<?> sweepOutputAtom) {
		int counter = 1;
		Model model = getModelToRun();
		long startTime = System.currentTimeMillis();
		for (ModelInput modelInput : modelInputs) {

			//allows to cancel the sweep if a user clicks the cancel button at the progress monitor window
			if (!monitor.isCanceled()) {
				logModelStartMessage(counter, startTime, numberOfSimulations);

				//create subtask and sub monitor for progress monitor
				monitor.setTaskName("=>Simulation #" + counter);

				SubProgressMonitor subMonitor = new SubProgressMonitor(
						monitor,
						1,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);

				//execute model
				ModelOutput modelOutput = model.runModel(modelInput, refreshable, subMonitor);

				//post process model output
				AbstractAtom<?> modelOutputAtom = modelOutput.getOutputAtom();
				String modelOutputName = getName() + "OutputId" + modelInput.getJobId();
				modelOutputAtom.setName(modelOutputName);
				sweepOutputAtom.addChild(modelOutputAtom);

				counter++;
			}
		}

		refresh();

	}

	/**
	 * Checks if the variables that are references by the given variable ranges are active. If not an error message is
	 * shown to the user;
	 *
	 * @param variableRanges
	 * @return
	 */
	private boolean checkIfAllREferencedVariablesAreActive(List<AbstractVariableRange<?>> variableRanges) {
		List<String> inactiveVariables = new ArrayList<>();
		for (AbstractVariableRange<?> variableRange : variableRanges) {
			String variableModelPath = variableRange.getSourceVariableModelPath();
			VariableField<?, ?> variableField;
			try {
				variableField = this.getChildFromRoot(variableModelPath);
			} catch (IllegalArgumentException exception) {
				String message = "Could not find atom '" + variableModelPath + "'.";
				Utils.showErrorMessage(message);
				return false;
			}

			boolean isEnabled = variableField.isEnabled();
			if (!isEnabled) {
				inactiveVariables.add(variableModelPath);
			}
		}

		if (inactiveVariables.isEmpty()) {
			return true;
		} else {
			String message = "Found disabled variable(s):\n" + String.join("\n", inactiveVariables)
					+ "Please enable the variable(s) or disable the corresponding range(s).";
			Utils.showErrorMessage(message);
			return false;
		}

	}

	/**
	 * Creates a text file with some information about the (Sweep) study and saves it at the exportStudyInfoPath
	 *
	 * @param variableRanges
	 * @param numberOfSimulations
	 */
	private void exportStudyInfo(
			List<AbstractVariableRange<?>> variableRanges,
			List<ModelInput> modelInputs,
			int numberOfSimulations) {

		String filePath = exportStudyInfoPath.get();

		if (filePath.isEmpty()) {
			LOG.warn("Export of study info is enabled but no file (e.g. c:/studyInfo.txt) is specified. ");
			return;
		}

		boolean isTextFile = filePath.endsWith(".txt");
		if (isTextFile) {
			exportStudyInfoToTextFile(variableRanges, numberOfSimulations, filePath);
			return;
		}

		boolean isSqLiteFile = filePath.endsWith(".sqlite");
		if (isSqLiteFile) {
			exportStudyInfoToSqLiteDatabase(variableRanges, modelInputs, filePath);
			return;
		}

		String message = "Could not export study info due to unknown file format of file path '" + filePath + "'";
		throw new IllegalStateException(message);

	}

	private void exportStudyInfoToSqLiteDatabase(
			List<AbstractVariableRange<?>> variableRanges,
			List<ModelInput> modelInputs,
			String filePath) {

		SqLiteDatabase database = new SqLiteDatabase(filePath);
		writeStudyInfo(variableRanges, database);
		writeJobInfo(modelInputs, database);

	}

	private void writeStudyInfo(List<AbstractVariableRange<?>> variableRanges, SqLiteDatabase database) {
		String studyInfoTableName = "study_info";
		createStudyInfoTableIfNotExists(database, studyInfoTableName);
		deleteOldEntriesForStudyIfExist(database, studyInfoTableName);

		for (AbstractVariableRange<?> range : variableRanges) {
			String variablePath = range.getSourceVariableModelPath();
			List<?> rangeValues = range.getRange();
			for (Object value : rangeValues) {
				String query = "INSERT INTO '" + studyInfoTableName + "' VALUES(null, '" + studyId + "', '"
						+ variablePath + "','" + value + "')";
				database.execute(query);
			}
		}
	}

	private void deleteOldEntriesForStudyIfExist(SqLiteDatabase database, String tableName) {
		String query = "DELETE FROM '" + tableName + "' WHERE study = '" + studyId + "';";
		database.execute(query);
	}

	private static void createStudyInfoTableIfNotExists(SqLiteDatabase database, String tableName) {
		String query = "CREATE TABLE IF NOT EXISTS '" + tableName
				+ "' (id INTEGER PRIMARY KEY NOT NULL, study TEXT, variable TEXT, value TEXT);";
		database.execute(query);
	}

	private void writeJobInfo(List<ModelInput> modelInputs, SqLiteDatabase database) {
		String jobInfoTableName = "job_info";
		createJobInfoTableIfNotExists(database, jobInfoTableName);
		deleteOldEntriesForStudyIfExist(database, jobInfoTableName);
		for (ModelInput modelInput : modelInputs) {
			String jobId = modelInput.getJobId();
			List<String> variablePaths = modelInput.getAllVariableModelPaths();
			for (String variablePath : variablePaths) {
				Object value = modelInput.getVariableValue(variablePath);
				String query = "INSERT INTO '" + jobInfoTableName + "' VALUES(null, '" + studyId + "', '" + jobId
						+ "', '" + variablePath + "','" + value + "')";
				database.execute(query);
			}
		}
	}

	private static void createJobInfoTableIfNotExists(SqLiteDatabase database, String tableName) {
		String query = "CREATE TABLE IF NOT EXISTS '" + tableName
				+ "' (id INTEGER PRIMARY KEY NOT NULL, study TEXT, job TEXT, variable TEXT, value TEXT);";
		database.execute(query);
	}

	private static void exportStudyInfoToTextFile(
			List<AbstractVariableRange<?>> variableRanges,
			int numberOfSimulations,
			String filePath) {
		String studyInfo = "---------- SweepInfo ----------\r\n\r\n" + "Total number of simulations:\r\n"
				+ numberOfSimulations + "\r\n\r\n" + "Variable model paths and values:\r\n\r\n";

		for (AbstractVariableRange<?> range : variableRanges) {
			String variablePath = range.getSourceVariableModelPath();
			studyInfo += variablePath + "\r\n";
			List<?> rangeValues = range.getRange();
			for (Object value : rangeValues) {
				studyInfo += value.toString() + "\r\n";
			}
			studyInfo += "\r\n";
		}

		File file = new File(filePath);

		try {
			FileUtils.writeStringToFile(file, studyInfo);
		} catch (IOException exception) {
			String message = "The specified exportStudyInfoPath '" + filePath
					+ "' is not valid. Export of study info is skipped.";
			LOG.error(message);
		}
	}

	/**
	 * Creates the result structure if it does not yet exist to have a place in the tree where the sweep result can be
	 * put. The sweep results will not be a child of the Sweep put a child of for example root.results.data.sweepOutput
	 */
	private void prepareResultStructure() {
		createResultsAtomIfNotExists();
		createDataAtomIfNotExists();
		createSweepOutputAtomIfNotExists();
		this.refresh();
	}

	/**
	 * Creates the sweep output atom if it does not yet exist
	 */
	private void createSweepOutputAtomIfNotExists() {
		String dataAtomPath = createOutputDataAtomPath();
		String sweepOutputAtomName = createStudyOutputAtomName();
		String sweepPutputAtomPath = getStudyOutputAtomPath();
		boolean sweepOutputAtomExists = this.rootHasChild(sweepPutputAtomPath);
		if (!sweepOutputAtomExists) {
			OutputAtom sweepOutputAtom = new OutputAtom(sweepOutputAtomName, provideImage());
			AbstractAtom<?> data = this.getChildFromRoot(dataAtomPath);
			data.addChild(sweepOutputAtom);
			LOG.info("Created " + sweepPutputAtomPath + " for sweep output.");
		}

	}

	/**
	 * Provides an image to represent this atom
	 */
	@Override
	public Image provideImage() {
		return Activator.getImage("sweep.png");
	}

	/**
	 * Creates the context menu actions for this atom
	 */
	@Override
	protected List<Object> extendContextMenuActions(List<Object> actions, TreeViewerRefreshable treeViewer) {

		Action addQuantityRange = new AddChildAtomTreeViewerAction(
				QuantityVariableRange.class,
				"quantityRange",
				Activator.getImage("quantityVariableRange.png"),
				this,
				treeViewer);
		actions.add(addQuantityRange);

		Action addDoubleRange = new AddChildAtomTreeViewerAction(
				DoubleVariableRange.class,
				"doubleRange",
				Activator.getImage("doubleVariableRange.png"),
				this,
				treeViewer);
		actions.add(addDoubleRange);

		Action addIntegerRange = new AddChildAtomTreeViewerAction(
				IntegerVariableRange.class,
				"integerRange",
				Activator.getImage("integerVariableRange.png"),
				this,
				treeViewer);
		actions.add(addIntegerRange);

		Action addBooleanRange = new AddChildAtomTreeViewerAction(
				BooleanVariableRange.class,
				"booleanRange",
				Activator.getImage("booleanVariableRange.png"),
				this,
				treeViewer);
		actions.add(addBooleanRange);

		Action addStringRange = new AddChildAtomTreeViewerAction(
				StringVariableRange.class,
				"stringRange",
				Activator.getImage("stringVariableRange.png"),
				this,
				treeViewer);
		actions.add(addStringRange);

		Action addStringItemRange = new AddChildAtomTreeViewerAction(
				StringItemVariableRange.class,
				"stringItemRange",
				Activator.getImage("stringItemVariableRange.png"),
				this,
				treeViewer);
		actions.add(addStringItemRange);

		Action addFilePathRange = new AddChildAtomTreeViewerAction(
				FilePathVariableRange.class,
				"filePathRange",
				Activator.getImage("filePathVariableRange.png"),
				this,
				treeViewer);
		actions.add(addFilePathRange);

		Action addDirectoryPathRange = new AddChildAtomTreeViewerAction(
				DirectoryPathVariableRange.class,
				"directoryPathRange",
				Activator.getImage("directoryPathVariableRange.png"),
				this,
				treeViewer);
		actions.add(addDirectoryPathRange);

		return actions;
	}

	//#region CREATE CHILD ATOMS

	public DoubleVariableRange createDoubleVariableRange(String name) {
		DoubleVariableRange child = new DoubleVariableRange(name);
		addChild(child);
		return child;
	}

	public IntegerVariableRange createIntegerVariableRange(String name) {
		IntegerVariableRange child = new IntegerVariableRange(name);
		addChild(child);
		return child;
	}

	public BooleanVariableRange createBooleanVariableRange(String name) {
		BooleanVariableRange child = new BooleanVariableRange(name);
		addChild(child);
		return child;
	}

	public StringVariableRange createStringVariableRange(String name) {
		StringVariableRange child = new StringVariableRange(name);
		addChild(child);
		return child;
	}

	public StringItemVariableRange createStringItemVariableRange(String name) {
		StringItemVariableRange child = new StringItemVariableRange(name);
		addChild(child);
		return child;
	}

	public FilePathVariableRange createFilePathVariableRange(String name) {
		FilePathVariableRange child = new FilePathVariableRange(name);
		addChild(child);
		return child;
	}

	public DirectoryPathVariableRange createDirectoryPathVariableRange(String name) {
		DirectoryPathVariableRange child = new DirectoryPathVariableRange(name);
		addChild(child);
		return child;
	}

	public QuantityVariableRange createQuantityVariableRange(String name) {
		QuantityVariableRange child = new QuantityVariableRange(name);
		addChild(child);
		return child;
	}

	//#end region

	//#end region

}
