package org.treez.example.picking;

import org.treez.core.atom.variablefield.IntegerVariableField;
import org.treez.core.data.table.TableSourceType;
import org.treez.core.scripting.ModelProvider;
import org.treez.data.table.nebula.Table;
import org.treez.data.tableSource.TableSource;
import org.treez.model.atom.Models;
import org.treez.model.atom.executable.Executable;
import org.treez.model.atom.genericInput.GenericInputModel;
import org.treez.model.atom.inputFileGenerator.InputFileGenerator;
import org.treez.model.atom.tableImport.TableImport;
import org.treez.results.atom.results.Results;
import org.treez.study.atom.Studies;
import org.treez.study.atom.picking.Picking;
import org.treez.study.atom.picking.Sample;
import org.treez.views.tree.rootAtom.Root;

public class PickingDemo extends ModelProvider {

	@SuppressWarnings({ "checkstyle:executablestatementcount", "checkstyle:javancss" })
	@Override
	public Root createModel() {

		Root root = new Root("root");

		Models models = new Models("models");
		root.addChild(models);

		//generic model
		//TODO: Why Double variables cause the following problem for simple pickings?
		// "Could not copy value of type 'java.lang.Double'. It must implement Copiable."

		GenericInputModel genericModel = new GenericInputModel("genericModel");
		models.addChild(genericModel);

		IntegerVariableField x = new IntegerVariableField("x");
		x.set(10);
		genericModel.addChild(x);

		IntegerVariableField y = new IntegerVariableField("y");
		y.set(20);
		genericModel.addChild(y);

		IntegerVariableField t = new IntegerVariableField("t");
		t.set(1);
		genericModel.addChild(t);

		String resourcePath = "D:/EclipseJava/workspace_Treez/treezExamples/src/";

		//executable
		String inputFilePath = resourcePath + "input.txt";
		String importFilePath = resourcePath + "importData.txt";

		Executable executable = models.createExecutable("executable");
		executable.executablePath.set("foo");
		executable.commandInfo.set("\"foo\"");
		executable.executionStatusInfo.set("Not yet executed");
		executable.jobIndexInfo.set("31");
		//executable.executablePath.set(resourcePath + "executable.bat");
		//executable.inputPath.set(inputFilePath);
		//executable.outputPath.set(importFilePath);
		//models.addChild(executable);

		InputFileGenerator inputFile = new InputFileGenerator("inputFileGenerator");
		inputFile.templateFilePath.set(resourcePath + "template.txt");
		inputFile.inputFilePath.set(inputFilePath);
		inputFile.nameExpression.set("<name>");
		inputFile.valueExpression.set("<value>");
		executable.addChild(inputFile);

		TableImport dataImport = executable.createTableImport("tableImport");
		dataImport.sourceType.set(TableSourceType.SQLITE);
		dataImport.linkSource.set(true);
		dataImport.inheritSourceFilePath.set(false);
		dataImport.sourceFilePath.set("D:/EclipseJava/workspace_Treez/TreezExamples/resources/example.sqlite");
		dataImport.tableName.set("example");
		dataImport.customJobId.set("31");
		dataImport.useCustomQuery.set(true);
		//dataImport.appendData.set(false);
		dataImport.customQuery.set("select * from example where id = {$jobId$}");
		dataImport.resultTableModelPath.set("root.results.data.table");
		//executable.addChild(dataImport);

		//studies------------------------------------------------------------
		Studies studies = new Studies("studies");
		root.addChild(studies);

		Picking picking0 = studies.createPicking("picking0");
		picking0.modelToRunModelPath.set("root.models.executable");
		picking0.sourceModelPath.set("root.models.genericModel");

		Sample sample0 = picking0.createSample("sample0");

		Sample sample1 = picking0.createSample("sample1");

		//results------------------------------------------------------------
		Results results = new Results("results");
		root.addChild(results);

		org.treez.results.atom.data.Data data = new org.treez.results.atom.data.Data("data");
		results.addChild(data);

		Table table = data.createTable("table");
		TableSource TableSource = table.createTableSource("TableSource");
		TableSource.sourceType.set(TableSourceType.SQLITE);
		TableSource.filePath.set("D:/EclipseJava/workspace_Treez/TreezExamples/resources/example.sqlite");
		TableSource.tableName.set("example");
		TableSource.jobId.set("30");
		TableSource.useCustomQuery.set(true);
		TableSource.customQuery.set("select * from example where id = {$jobId$}");

		//create data table with two columns---------------------------------
		//Table table = new Table("table");
		//data.addChild(table);

		return root;

	}
}
