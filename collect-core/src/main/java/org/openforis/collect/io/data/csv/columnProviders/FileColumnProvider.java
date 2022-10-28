/**
 * 
 */
package org.openforis.collect.io.data.csv.columnProviders;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.io.data.csv.CSVDataExportParameters;
import org.openforis.collect.io.data.csv.Column;
import org.openforis.collect.io.data.csv.Column.DataType;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.RecordFileService;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.FileAttribute;

/**
 * @author S. Ricci
 *
 */
public class FileColumnProvider extends CompositeAttributeColumnProvider<FileAttributeDefinition> {

	private static final String FILE_CONTENT_COLUMN = "file_content";

	public FileColumnProvider(CSVDataExportParameters config, FileAttributeDefinition defn) {
		super(config, defn);
	}

	@Override
	protected String[] getFieldNames() {
		List<String> cols = new ArrayList<String>();
		cols.add(FileAttributeDefinition.FILE_NAME_FIELD);
		cols.add(FileAttributeDefinition.FILE_SIZE_FIELD);
		if (getConfig().isIncludeImages()) {
			cols.add(FILE_CONTENT_COLUMN);
		}
		return cols.toArray(new String[cols.size()]);
	}

	@Override
	protected Column generateFieldColumn(String fieldName, String suffix) {
		Column column = super.generateFieldColumn(fieldName, suffix);
		if (FileAttributeDefinition.FILE_SIZE_FIELD.equals(fieldName)) {
			column.setDataType(DataType.INTEGER);
		} else if (FILE_CONTENT_COLUMN.equals(fieldName)) {
			column.setDataType(DataType.IMAGE);
		}
		return column;
	}

	@Override
	protected Object extractValue(Attribute<?, ?> attr, String fieldName) {
		if (FILE_CONTENT_COLUMN.equals(fieldName)) {
			CollectSurveyContext ctx = attr.getSurveyContext();
			RecordFileService recordFileService = ctx.getRecordFileService();
			return recordFileService.getRepositoryFile((FileAttribute) attr);
		} else {
			return super.extractValue(attr, fieldName);
		}
	}

}
