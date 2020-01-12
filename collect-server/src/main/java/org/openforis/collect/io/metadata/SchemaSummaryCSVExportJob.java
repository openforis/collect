package org.openforis.collect.io.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.designer.metamodel.AttributeType;
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
				FileOutputStream out = new FileOutputStream(outputFile);
				final FlatDataWriter csvWriter = new ExcelFlatValuesWriter(out);
				try {
					writeHeaders(csvWriter);
					
					Schema schema = survey.getSchema();
					schema.traverse(new NodeDefinitionVisitor() {
						@Override
						public void visit(NodeDefinition nodeDefn) {
							List<String> values = new ArrayList<String>();
							values.addAll(Arrays.asList(
									Integer.toString(nodeDefn.getId()), 
									nodeDefn.getPath(),
									nodeDefn instanceof EntityDefinition ? "entity": "attribute",
									nodeDefn instanceof AttributeDefinition ? AttributeType.valueOf((AttributeDefinition) nodeDefn).getLabel(): ""
							));
							// Instance labels
							for (String lang : survey.getLanguages())
								values.add(nodeDefn.getLabel(Type.INSTANCE, lang));
							// Reporting labels
							for (String lang : survey.getLanguages())
								values.add(nodeDefn.getLabel(Type.REPORTING, lang));
							
							values.addAll(Arrays.asList(
									String.valueOf(nodeDefn.isAlwaysRelevant()),
									nodeDefn.isAlwaysRelevant() ? "" : nodeDefn.getRelevantExpression(),
									String.valueOf(nodeDefn.isAlwaysRequired()),
									nodeDefn.isAlwaysRequired() ? "" : nodeDefn.getMinCountExpression(),
									extractValidationRules(nodeDefn)
							));
							csvWriter.writeNext(values);
						}
					});
				} finally {
					IOUtils.closeQuietly(csvWriter);
				}
			}

			
		};
		addTask(task);
	}
	
	private void writeHeaders(final FlatDataWriter valuesWriter) {
		List<String> headers = new ArrayList<String>();
		// Generic info
		headers.addAll(Arrays.asList("id", "path", "type", "attribute_type"));
		// Instance labels
		for (String lang : survey.getLanguages())
			headers.add("label_" + lang);
		// Reporting labels
		for (String lang : survey.getLanguages())
			headers.add("label_reporting_" + lang);
		// Expressions
		headers.addAll(Arrays.asList("always_relevant", "relevant_when", "always_required", "required_when", "validation_rules"));
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
	
	public File getOutputFile() {
		return outputFile;
	}
	
}