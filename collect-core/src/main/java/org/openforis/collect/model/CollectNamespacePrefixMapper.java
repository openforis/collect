/**
 * 
 */
package org.openforis.collect.model;

import java.util.HashMap;
import java.util.Map;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;

/**
 * @author M. Togna
 *
 */
@SuppressWarnings("restriction")
public class CollectNamespacePrefixMapper extends NamespacePrefixMapper {

	private Map<String, String> namespaces;
	
	public CollectNamespacePrefixMapper() {
		namespaces = new HashMap<String, String>();
		//namespaces.put("", value)
	}

	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		// TODO Auto-generated method stub
		return null;
	}

}
