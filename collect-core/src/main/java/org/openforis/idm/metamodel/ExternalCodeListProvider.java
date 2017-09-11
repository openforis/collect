/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.List;

import org.openforis.commons.collection.Visitor;
import org.openforis.idm.model.CodeAttribute;


/**
 * @author M. Togna
 * @author S. Ricci
 *
 */
public interface ExternalCodeListProvider {
	
	@Deprecated
	String getCode(CodeList list, String attribute, Object... keys);
	
	ExternalCodeListItem getItem(CodeAttribute attribute);
	
	List<ExternalCodeListItem> getRootItems(CodeList list);
	
	List<ExternalCodeListItem> getChildItems(ExternalCodeListItem item);

	void visitItems(CodeList list, Visitor<CodeListItem> visitor);
	
	void visitChildItems(ExternalCodeListItem item, Visitor<CodeListItem> visitor);

}
