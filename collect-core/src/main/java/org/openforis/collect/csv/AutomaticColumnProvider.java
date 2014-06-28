package org.openforis.collect.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class AutomaticColumnProvider extends ColumnProviderChain {
	
	private static final Log LOG = LogFactory.getLog(AutomaticColumnProvider.class);
	
	public AutomaticColumnProvider(EntityDefinition entityDefinition) {
		this(entityDefinition, null);
	}
	
	public AutomaticColumnProvider(EntityDefinition entityDefinition, List<String> exclusions) {
		this("", entityDefinition, exclusions);
	}

	public AutomaticColumnProvider(String headingPrefix, EntityDefinition entityDefinition) {
		this(headingPrefix, entityDefinition, null);
	}
	
	public AutomaticColumnProvider(String headingPrefix, EntityDefinition entityDefinition, List<String> exclusions) {
		this(headingPrefix, entityDefinition, exclusions, false, false);
	}
	
	public AutomaticColumnProvider(String headingPrefix, EntityDefinition entityDefinition, List<String> exclusions, 
			boolean includeCodeItemPositionColumn, boolean includeKMLColumnForCoordinates) {
		super(headingPrefix, createProviders( entityDefinition, exclusions, includeCodeItemPositionColumn, includeKMLColumnForCoordinates));
	}
	
	private static List<ColumnProvider> createProviders(EntityDefinition rowDefn, List<String> exclusions,
			boolean includeItemPositionColumn, boolean includeKMLColumnForCoordinates) {
		List<ColumnProvider> cols = new ArrayList<ColumnProvider>();
		CollectSurvey survey = (CollectSurvey) rowDefn.getSurvey();
		CollectAnnotations surveyAnnotations = survey.getAnnotations();
		List<NodeDefinition> childDefinitions = rowDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefinitions) {
			if (includeChild(exclusions, childDefn)) {
				if (childDefn instanceof EntityDefinition) {
					createEntityProviders((EntityDefinition) childDefn, cols);
				} else if (childDefn instanceof AttributeDefinition && (
					! (childDefn instanceof CalculatedAttributeDefinition) || 
						surveyAnnotations.isIncludedInDataExport((CalculatedAttributeDefinition) childDefn) )
					) {
					createAttributeProviders((AttributeDefinition) childDefn, cols, includeKMLColumnForCoordinates, includeItemPositionColumn);
				}
			}
		}
		return cols;
	}
	
	private static boolean includeChild(List<String> exclusions, NodeDefinition childDefn) {
		return exclusions == null || !exclusions.contains(childDefn.getName());
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
			String pivotExpression = name;
			String headingPrefix = name + "_";
			PivotExpressionColumnProvider col = new PivotExpressionColumnProvider(pivotExpression, headingPrefix, childCols);
			cols.add(col);
		}
	}

	private static void createAttributeProviders(AttributeDefinition defn, List<ColumnProvider> cols, 
			boolean includeKMLColumnForCoordinates, boolean includeItemPositionColumn) {
		String name = defn.getName();
		ColumnProvider columnProvider;
		if ( defn.isMultiple() ) {
			LOG.info("Flatting multiple attribute "+defn.getPath());
			columnProvider = new MultipleAttributeColumnProvider(defn, ", ", name);
		} else if ( defn instanceof CodeAttributeDefinition ) {
			columnProvider = new CodeColumnProvider((CodeAttributeDefinition) defn, includeItemPositionColumn);
		} else if(defn instanceof CoordinateAttributeDefinition){
			columnProvider = new CoordinateColumnProvider((CoordinateAttributeDefinition) defn, includeKMLColumnForCoordinates);
		} else if(defn instanceof DateAttributeDefinition) {
			columnProvider = new DateColumnProvider((DateAttributeDefinition) defn);
		} else if(defn instanceof TaxonAttributeDefinition){
			columnProvider = new TaxonColumnProvider((TaxonAttributeDefinition) defn);
		} else if(defn instanceof TimeAttributeDefinition){
			columnProvider = new TimeColumnProvider((TimeAttributeDefinition) defn);
		} else {
			columnProvider = new SingleAttributeColumnProvider(defn, name);
		}
		cols.add(columnProvider);
	}
}
