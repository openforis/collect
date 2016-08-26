package org.openforis.collect.manager;

import org.openforis.collect.Collect;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.versioning.Version;
import org.openforis.commons.versioning.Version.Significance;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.NumericRangeAttribute;

/**
 * Converts record's data to the current version
 * 
 * @author S. Ricci
 *
 */
public class RecordConverter {

	public static final Version PREVIOUS_TO_APPLICATION_VERSION_STORAGE_VERSION = new Version("3.9.0");
	private static final Version UNIT_STORAGE_CHANGE_VERSION = new Version("3.9.0");

	public void convertToLatestVersion(CollectRecord record) {
		if (record.getApplicationVersion().compareTo(UNIT_STORAGE_CHANGE_VERSION, Significance.MINOR) <= 0) {
			convertToLatestUnitStorage(record);
		}
		record.setApplicationVersion(Collect.VERSION);
	}
	
	/**
	 * This is a workaround: it avoids to refer to the old unit field to get data
	 * 
	 * TODO remove this conversion or apply only for records stored using version prior to 3.0 Alpha 5
	 * 
	 * @param survey
	 * @param rootEntity
	 */
	protected void convertToLatestUnitStorage(CollectRecord record) {
		final CollectSurvey survey = (CollectSurvey) record.getSurvey();
		Entity rootEntity = record.getRootEntity();
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if ( node instanceof NumberAttribute<?, ?> || node instanceof NumericRangeAttribute<?, ?>) {
					Field<String> unitNameField;
					Field<Integer> unitField;
					if ( node instanceof NumberAttribute<?, ?> ) {
						unitNameField = ((NumberAttribute<?, ?>) node).getUnitNameField();
						unitField = ((NumberAttribute<?, ?>) node).getUnitField();
					} else {
						unitNameField = ((NumericRangeAttribute<?, ?>) node).getUnitNameField();
						unitField = ((NumericRangeAttribute<?, ?>) node).getUnitField();
					}
					if ( unitNameField.hasData() ) {
						moveDataToNewUnitField(survey, unitNameField, unitField);
					}
				}
			}
		});
	}
	
	protected void moveDataToNewUnitField(CollectSurvey survey, Field<String> unitNameField, Field<Integer> unitField) throws RecordConversionException {
		unitField.setRemarks(unitNameField.getRemarks());
		unitField.setSymbol(unitNameField.getSymbol());
		unitField.getState().set(unitNameField.getState().intValue());
		String unitName = unitNameField.getValue();
		if ( unitName != null ) {
			Unit unit = survey.getUnit(unitName);
			if ( unit != null ) {
				unitField.setValue(unit.getId());
			} else {
				throw new RecordConversionException("Cannot find unit with name: " + unitName);
			}
		}
		unitNameField.getState().set(0);
		unitNameField.clear();
	}
	
}
