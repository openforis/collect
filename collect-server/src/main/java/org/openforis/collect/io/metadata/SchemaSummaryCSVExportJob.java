package org.openforis.collect.io.metadata;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.NodeLabel.Type;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaSummaryCSVExportJob extends Job {

	private CollectSurvey survey;
	private File outputFile;
	
	@Override
	protected void initInternal() throws Throwable {
		outputFile = File.createTempFile("collect_schema_export", ".csv");
		super.initInternal();
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		Task task = new Task() {
			@Override
			protected void execute() throws Throwable {
				FileOutputStream out = new FileOutputStream(outputFile);
				final CsvWriter csvWriter = new CsvWriter(out);
				try {
					csvWriter.writeHeaders(new String[] {"id", "path", "name", "type", "label"});
					
					Schema schema = survey.getSchema();
					schema.traverse(new NodeDefinitionVisitor() {
						@Override
						public void visit(NodeDefinition nodeDefn) {
							csvWriter.writeNext(new String[] {
									Integer.toString(nodeDefn.getId()), 
									nodeDefn.getPath(),
									nodeDefn.getName(),
									nodeDefn instanceof EntityDefinition ? "entity": "attribute",
									nodeDefn.getLabel(Type.INSTANCE)
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
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
}