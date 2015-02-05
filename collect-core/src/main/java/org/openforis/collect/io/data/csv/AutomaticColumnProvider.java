package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class AutomaticColumnProvider extends ColumnProviderChain {
	
//	private static final Log LOG = LogFactory.getLog(AutomaticColumnProvider.class);
//	private static final String MULTIPLE_ATTRIBUTE_VALUES_DELIMITER = ", ";
	
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
					createAttributeProviders(config, (AttributeDefinition) childDefn, cols);
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

	private static void createAttributeProviders(CSVExportConfiguration config, AttributeDefinition defn, List<ColumnProvider> cols) {
		String name = defn.getName();
		ColumnProvider columnProvider;
//		if ( defn.isMultiple() && ! (defn instanceof CodeAttributeDefinition) ) {
//			columnProvider = new MultipleAttributeColumnProvider(defn, MULTIPLE_ATTRIBUTE_VALUES_DELIMITER, name);
//		} else 
		if ( defn instanceof CodeAttributeDefinition ) {
			columnProvider = new CodeColumnProvider(config, (CodeAttributeDefinition) defn);
		} else if(defn instanceof CoordinateAttributeDefinition){
			columnProvider = new CoordinateColumnProvider(config, (CoordinateAttributeDefinition) defn);
		} else if(defn instanceof DateAttributeDefinition) {
			columnProvider = new DateColumnProvider(config, (DateAttributeDefinition) defn);
		} else if(defn instanceof NumberAttributeDefinition){
			columnProvider = new NumberColumnProvider(config, (NumberAttributeDefinition) defn);
		} else if(defn instanceof RangeAttributeDefinition){
			columnProvider = new RangeColumnProvider(config, (RangeAttributeDefinition) defn);
		} else if(defn instanceof TaxonAttributeDefinition){
			columnProvider = new TaxonColumnProvider(config, (TaxonAttributeDefinition) defn);
		} else if(defn instanceof TimeAttributeDefinition){
			columnProvider = new TimeColumnProvider(config, (TimeAttributeDefinition) defn);
		} else {
			columnProvider = new SingleFieldAttributeColumnProvider(config, defn, name);
		}
		cols.add(columnProvider);
	}
}
