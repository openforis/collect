package org.openforis.collect.io.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.designer.metamodel.AttributeTypeUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.io.excel.ExcelFlatValuesWriter;
import org.openforis.commons.io.flat.FlatDataWriter;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.validation.Check;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaSummaryCSVExportJob extends Job {

	//input
	private CollectSurvey survey;
	private boolean onlyLabels;
	//output
	private File outputFile;
	
	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		outputFile = File.createTempFile("collect_schema_export", ".csv");
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		Task task = new Task() {
			@Override
			protected void execute() throws Throwable {
				try (
					FileOutputStream out = new FileOutputStream(outputFile); 
					final FlatDataWriter csvWriter = new ExcelFlatValuesWriter(out)) {
					writeHeaders(csvWriter);
					
					Schema schema = survey.getSchema();
					schema.traverse(new NodeDefinitionVisitor() {
						@Override
						public void visit(NodeDefinition nodeDefn) {
							List<String> values = new ArrayList<String>();
							values.add(Integer.toString(nodeDefn.getId()));
							if (!onlyLabels) {
								values.addAll(Arrays.asList(
										nodeDefn.getPath(),
										nodeDefn instanceof EntityDefinition ? "entity": "attribute",
										nodeDefn instanceof AttributeDefinition ? AttributeTypeUtils.getLabel((AttributeDefinition) nodeDefn): ""
								));
							}
							// Instance labels
							for (String lang : survey.getLanguages())
								values.add(nodeDefn.getLabel(Type.INSTANCE, lang));
							// Descriptions (tooltip text)
							for (String lang : survey.getLanguages())
								values.add(nodeDefn.getDescription(lang));
							// Reporting labels
							for (String lang : survey.getLanguages())
								values.add(nodeDefn.getLabel(Type.REPORTING, lang));
							
							if (!onlyLabels) {
								// Expressions
								values.addAll(Arrays.asList(
										String.valueOf(nodeDefn.isAlwaysRelevant()),
										nodeDefn.isAlwaysRelevant() ? "" : nodeDefn.getRelevantExpression(),
										String.valueOf(nodeDefn.isAlwaysRequired()),
										nodeDefn.isAlwaysRequired() ? "" : nodeDefn.extractRequiredExpression(),
										extractValidationRules(nodeDefn)
								));
							}
							csvWriter.writeNext(values);
						}
					});
				}
			}
		};
		addTask(task);
	}
	
	private void writeHeaders(final FlatDataWriter valuesWriter) {
		List<String> headers = new ArrayList<String>();
		headers.add("id");
		if (!onlyLabels) {
			// Generic node info
			headers.addAll(Arrays.asList("path", "type", "attribute_type"));
		}
		// Instance labels
		for (String lang : survey.getLanguages())
			headers.add("label_" + lang);
		// Descriptions (tooltip text)
		for (String lang : survey.getLanguages())
			headers.add("description_" + lang);
		// Reporting labels
		for (String lang : survey.getLanguages())
			headers.add("label_reporting_" + lang);
		
		if (!onlyLabels) {
			// Expressions
			headers.addAll(Arrays.asList("always_relevant", "relevant_when", "always_required", "required_when", "validation_rules"));
		}
		valuesWriter.writeHeaders(headers);
	}
	
	private String extractValidationRules(NodeDefinition nodeDefn) {
		if (! (nodeDefn instanceof AttributeDefinition)) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		List<Check<?>> checks = ((AttributeDefinition) nodeDefn).getChecks();
		for (int i = 0; i < checks.size(); i++) {
			Check<?> check = checks.get(i);
			sb.append(check.toString());
			if (i < checks.size() - 1) {
				sb.append('\n');
			}
		}
		return sb.toString();
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public void setOnlyLabels(boolean onlyLabels) {
		this.onlyLabels = onlyLabels;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
}