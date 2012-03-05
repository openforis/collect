package org.openforis.collect.model.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author G. Miceli
 */
public class AutomaticColumnProvider extends ColumnProviderChain {
	
	private static final Log LOG = LogFactory.getLog(AutomaticColumnProvider.class);
	
	public AutomaticColumnProvider(EntityDefinition entityDefinition) {
		super(createProviders(entityDefinition));
	}

	public AutomaticColumnProvider(String headingPrefix, EntityDefinition entityDefinition) {
		super(headingPrefix, createProviders(entityDefinition));
	}
	
	private static List<ColumnProvider> createProviders(EntityDefinition rowDefn) {
		List<ColumnProvider> cols = new ArrayList<ColumnProvider>();
		List<NodeDefinition> childDefinitions = rowDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefinitions) {
			if ( childDefn instanceof EntityDefinition ) {
				createEntityProviders((EntityDefinition) childDefn, cols);
			} else if ( childDefn instanceof AttributeDefinition ) {				
				createAttributeProviders((AttributeDefinition) childDefn, cols);
			}
		}
		return cols;
	}

	private static void createEntityProviders(EntityDefinition defn, List<ColumnProvider> cols) {
		String name = defn.getName();
		if ( defn.isMultiple() ) {
			if ( defn.isEnumerable() ) {
				LOG.info("Flatting enumerable multiple entity "+defn.getPath());
				EnumerableEntityColumnProvider p = new EnumerableEntityColumnProvider(defn);
				cols.add(p);
			} else {
				LOG.info("Skipping multiple entity "+defn.getPath());
			}
		} else {
			LOG.info("Flatting single entity "+defn.getPath());
			ColumnProvider p = new AutomaticColumnProvider(defn);
			List<ColumnProvider> childCols = Arrays.asList(p);
			PivotExpressionColumnProvider col = new PivotExpressionColumnProvider(name, name+"_", childCols);
			cols.add(col);
		}
	}

	private static void createAttributeProviders(AttributeDefinition defn, List<ColumnProvider> cols) {
		String name = defn.getName();
		if ( defn.isMultiple() ) {
			LOG.info("Flatting multiple attribute "+defn.getPath());
			MultipleAttributeColumnProvider col = new MultipleAttributeColumnProvider(name, ", ", name);
			cols.add(col);
		} else {
			SingleAttributeColumnProvider col = new SingleAttributeColumnProvider(name, name);
			cols.add(col);
		}
	}
}
