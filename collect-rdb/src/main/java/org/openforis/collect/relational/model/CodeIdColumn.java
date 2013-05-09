/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.relational.DatabaseExporterConfig;
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
public class CodeIdColumn extends DataColumn {

	CodeIdColumn(String name, CodeAttributeDefinition defn, Path relPath) {
		super(name, Types.BIGINT, "bigint", defn, relPath, null, true);
	}

	@Override
	public Object extractValue(DatabaseExporterConfig config, Node<?> context) {
		NodeDefinition defn = getNodeDefinition();
		if ( defn instanceof CodeAttributeDefinition ) {
			Node<?> valNode = super.extractValueNode(context);
			if ( valNode != null && valNode instanceof CodeAttribute ) {
				return extractValue(config, (CodeAttribute) valNode);
			} else if ( config.getDefaultCode() != null ) {
				ModelVersion version = context.getRecord().getVersion();
				return getDefaultCodeId(config, (CodeAttributeDefinition) defn, version);
			}
		}
		return null;
	}

	protected Object extractValue(DatabaseExporterConfig config,
			CodeAttribute valNode) {
		ModelVersion version = valNode.getRecord().getVersion();
		CodeAttributeDefinition defn = valNode.getDefinition();
		Field<String> codeField = valNode.getCodeField();
		String code = codeField.getValue();
		String defaultCode = config.getDefaultCode();
		if ( StringUtils.isBlank(code) ) {
			if ( defaultCode == null ) {
				return null;
			} else {
				return getDefaultCodeId(config, defn, version);
			}
		} else {
			CodeListItem codeListItem = valNode.getCodeListItem();
			if ( codeListItem == null ) {
				if ( code.equals(defaultCode)) {
					return getDefaultCodeId(config, defn, version);
				} else {
					return null;
				}
			} else {
				return codeListItem.getId();
			}
		}
	}

	protected Integer getDefaultCodeId(DatabaseExporterConfig config, CodeAttributeDefinition defn,
			ModelVersion version) {
		CodeList list = defn.getList();
		int levelIdx = defn.getCodeListLevel();
		String defaultCode = config.getDefaultCode();
		CodeListItem defaultCodeItem = list.getItem(defaultCode, levelIdx, version);
		return defaultCodeItem == null ? -1: defaultCodeItem.getId();
	}

}
