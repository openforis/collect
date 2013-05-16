/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class CodeValueFKColumn extends DataColumn {
	
	private String defaultCodeValue;

	CodeValueFKColumn(String name, CodeAttributeDefinition defn, Path relPath, String defaultCodeValue) {
		super(name, Types.BIGINT, "bigint", defn, relPath, null, true);
		this.defaultCodeValue = defaultCodeValue;
	}

	@Override
	public Object extractValue(Node<?> context) {
		NodeDefinition defn = getNodeDefinition();
		if ( defn instanceof CodeAttributeDefinition ) {
			Node<?> valNode = super.extractValueNode(context);
			if ( valNode != null && valNode instanceof CodeAttribute ) {
				return extractValue((CodeAttribute) valNode);
			} else if ( defaultCodeValue != null ) {
				ModelVersion version = context.getRecord().getVersion();
				return getDefaultCodeId((CodeAttributeDefinition) defn, version);
			}
		}
		return null;
	}

	protected Object extractValue(CodeAttribute valNode) {
		ModelVersion version = valNode.getRecord().getVersion();
		CodeAttributeDefinition defn = valNode.getDefinition();
		Field<String> codeField = valNode.getCodeField();
		String code = codeField.getValue();
		if ( StringUtils.isBlank(code) ) {
			if ( defaultCodeValue == null ) {
				return null;
			} else {
				return getDefaultCodeId(defn, version);
			}
		} else {
			CodeListItem codeListItem = valNode.getCodeListItem();
			if ( codeListItem == null ) {
				if ( code.equals(defaultCodeValue)) {
					return getDefaultCodeId(defn, version);
				} else {
					return null;
				}
			} else {
				return codeListItem.getId();
			}
		}
	}

	protected Integer getDefaultCodeId(CodeAttributeDefinition defn,
			ModelVersion version) {
		CodeList list = defn.getList();
		int levelIdx = defn.getCodeListLevel();
		CodeListItem defaultCodeItem = list.getItem(defaultCodeValue, levelIdx, version);
		return defaultCodeItem == null ? -1: defaultCodeItem.getId();
	}

}
