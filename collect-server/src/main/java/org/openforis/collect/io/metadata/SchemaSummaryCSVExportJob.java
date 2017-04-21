package org.openforis.collect.io.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.io.csv.CsvWriter;
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

	private static final String[] HEADERS = new String[] {"id", "path", "type", "attribute_type", "label", 
			"always_relevant", "relevant_when", "always_required", "required_when", "validation_rules"};
	//input
	private CollectSurvey survey;
	private String labelLanguage;
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
				final CsvWriter csvWriter = new CsvWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8")), ',', '"');
				try {
					csvWriter.writeHeaders(HEADERS);
					
					Schema schema = survey.getSchema();
					schema.traverse(new NodeDefinitionVisitor() {
						@Override
						public void visit(NodeDefinition nodeDefn) {
							csvWriter.writeNext(new String[] {
									Integer.toString(nodeDefn.getId()), 
									nodeDefn.getPath(),
									nodeDefn instanceof EntityDefinition ? "entity": "attribute",
									nodeDefn instanceof AttributeDefinition ? AttributeType.valueOf((AttributeDefinition) nodeDefn).getLabel(): "",
									nodeDefn.getLabel(Type.INSTANCE, labelLanguage),
									String.valueOf(nodeDefn.isAlwaysRelevant()),
									nodeDefn.isAlwaysRelevant() ? "" : nodeDefn.getRelevantExpression(),
									String.valueOf(nodeDefn.isAlwaysRequired()),
									nodeDefn.isAlwaysRequired() ? "" : nodeDefn.getMinCountExpression(),
									extractValidationRules(nodeDefn)
								});
						}
					});
				} finally {
					IOUtils.closeQuietly(csvWriter);
				}
			}
		};
		addTask(task);
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
	
	public void setLabelLanguage(String labelLanguage) {
		this.labelLanguage = labelLanguage;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
}