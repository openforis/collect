package org.openforis.collect.datacleansing.form;

import java.util.List;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.datacleansing.DataErrorReportItem;
import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.model.CollectRecord;
import org.openforis.commons.web.PersistedObjectForm;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorReportItemForm extends PersistedObjectForm<DataErrorReportItem> {

	private String key1;
	private String key2;
	private String key3;
	private String key4;
	private String key5;
	private String nodePath;
	private String attributeValue;
	
	public DataErrorReportItemForm() {
		super();
	}

	public DataErrorReportItemForm(DataErrorReportItem obj) {
		super(obj);
		DataErrorReport report = obj.getReport();
		CollectRecord record = obj.getRecord();
		DataErrorQuery query = report.getQuery();
		DataErrorType type = query.getType();
		nodePath = obj.extractNodePath();
		Value val = obj.extractAttributeValue();
		attributeValue = val == null ? null : val.toString();
		if (record != null) {
			List<String> keyValues = record.getRootEntityKeyValues();
			for (int i = 0; i < keyValues.size() && i < 5; i++) {
				String keyVal = keyValues.get(i);
				setKeyValue(i + 1, keyVal);
			}
		}
	}

	private void setKeyValue(int position, String keyVal) {
		switch (position) {
		case 1: 
			key1 = keyVal;
			break;
		case 2: 
			key2 = keyVal;
			break;
		case 3: 
			key3 = keyVal;
			break;
		case 4: 
			key4 = keyVal;
			break;
		case 5: 
			key5 = keyVal;
			break;
		}
	}

	public String getKey1() {
		return key1;
	}

	public String getKey2() {
		return key2;
	}

	public String getKey3() {
		return key3;
	}

	public String getKey4() {
		return key4;
	}

	public String getKey5() {
		return key5;
	}
	
	public String getNodePath() {
		return nodePath;
	}
	
	public String getAttributeValue() {
		return attributeValue;
	}
	
}
