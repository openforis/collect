package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class AutomaticColumnProvider extends ColumnProviderChain {
	
//	private static final Log LOG = LogFactory.getLog(AutomaticColumnProvider.class);
	
	public AutomaticColumnProvider(CSVExportConfiguration config, EntityDefinition entityDefinition) {
		this(config, entityDefinition, null);
	}
	
	public AutomaticColumnProvider(CSVExportConfiguration config, EntityDefinition entityDefinition, List<String> exclusions) {
		this(config, "", entityDefinition, exclusions);
	}

	public AutomaticColumnProvider(CSVExportConfiguration config, String headingPrefix, EntityDefinition entityDefinition) {
		this(config, headingPrefix, entityDefinition, null);
	}
	
	public AutomaticColumnProvider(CSVExportConfiguration config, String headingPrefix, EntityDefinition entityDefinition, List<String> exclusions) {
		super(config, headingPrefix, createProviders(config, entityDefinition, exclusions));
	}
	
	private static List<ColumnProvider> createProviders(CSVExportConfiguration config, EntityDefinition rowDefn, List<String> exclusions) {
		List<ColumnProvider> cols = new ArrayList<ColumnProvider>();
		CollectSurvey survey = (CollectSurvey) rowDefn.getSurvey();
		CollectAnnotations surveyAnnotations = survey.getAnnotations();
		List<NodeDefinition> childDefinitions = rowDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefinitions) {
			if (includeChild(exclusions, childDefn)) {
				if (childDefn instanceof EntityDefinition) {
					createEntityProviders(config, (EntityDefinition) childDefn, cols);
				} else if (childDefn instanceof AttributeDefinition && surveyAnnotations.isIncludedInDataExport(childDefn) ) {
					cols.add(ColumnProviders.createAttributeProvider(config, (AttributeDefinition) childDefn));
				}
			}
		}
		return cols;
	}
	
	private static boolean includeChild(List<String> exclusions, NodeDefinition childDefn) {
		return exclusions == null || !exclusions.contains(childDefn.getName());
	}
	
	private static void createEntityProviders(CSVExportConfiguration config, EntityDefinition defn, List<ColumnProvider> cols) {
		String name = defn.getName();
		if ( defn.isMultiple() ) {
			if ( defn.isEnumerable() && config.isIncludeEnumeratedEntities() ) {
				EnumerableEntityColumnProvider p = new EnumerableEntityColumnProvider(config, defn);
				cols.add(p);
			}
		} else {
			ColumnProvider p = new AutomaticColumnProvider(config, defn);
			List<ColumnProvider> childCols = Arrays.asList(p);
			String pivotExpression = name;
			String headingPrefix = name + "_";
			PivotExpressionColumnProvider col = new PivotExpressionColumnProvider(config, pivotExpression, headingPrefix, childCols);
			cols.add(col);
		}
	}

	
}
