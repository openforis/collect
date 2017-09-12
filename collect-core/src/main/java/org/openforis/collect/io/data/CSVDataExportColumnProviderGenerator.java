package org.openforis.collect.io.data;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.io.data.csv.AutomaticColumnProvider;
import org.openforis.collect.io.data.csv.CSVDataExportParameters;
import org.openforis.collect.io.data.csv.ColumnProvider;
import org.openforis.collect.io.data.csv.ColumnProviderChain;
import org.openforis.collect.io.data.csv.ColumnProviders;
import org.openforis.collect.io.data.csv.DataTransformation;
import org.openforis.collect.io.data.csv.NodePositionColumnProvider;
import org.openforis.collect.io.data.csv.PivotExpressionColumnProvider;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 *
 */
public class CSVDataExportColumnProviderGenerator {
	
	private CollectSurvey survey;
	private CSVDataExportParameters configuration;
	
	public CSVDataExportColumnProviderGenerator(CollectSurvey survey, CSVDataExportParameters configuration) {
		this.survey = survey;
		this.configuration = configuration;
	}

	public DataTransformation generateDataTransformation(int entityDefId) throws InvalidExpressionException {
		Schema schema = survey.getSchema();
		EntityDefinition entityDefn = (EntityDefinition) schema.getDefinitionById(entityDefId);
		
		ColumnProvider provider = generateColumnProviderChain(entityDefn);
		String axisPath = entityDefn.getPath();
		return new DataTransformation(axisPath, provider);
	}

	public ColumnProviderChain generateColumnProviderChain(EntityDefinition entityDefn) {
		List<ColumnProvider> columnProviders = new ArrayList<ColumnProvider>();
		
		//entity children columns
		AutomaticColumnProvider entityColumnProvider = createEntityColumnProvider(entityDefn);

		//ancestor columns
		columnProviders.addAll(createAncestorsColumnsProvider(entityDefn));
		
		//position column
		if ( isPositionColumnRequired(entityDefn) ) {
			columnProviders.add(createPositionColumnProvider(entityDefn));
		}
		columnProviders.add(entityColumnProvider);
		
		//create data transformation
		ColumnProviderChain provider = new ColumnProviderChain(configuration, columnProviders);
		return provider;
	}
	
	protected AutomaticColumnProvider createEntityColumnProvider(EntityDefinition entityDefn) {
		AutomaticColumnProvider entityColumnProvider = new AutomaticColumnProvider(configuration, "", entityDefn, null);
		return entityColumnProvider;
	}
	
	private List<ColumnProvider> createAncestorsColumnsProvider(EntityDefinition entityDefn) {
		List<ColumnProvider> columnProviders = new ArrayList<ColumnProvider>();
		EntityDefinition ancestorDefn = (EntityDefinition) entityDefn.getParentDefinition();
		while ( ancestorDefn != null ) {
			if (ancestorDefn.isMultiple()) {
				ColumnProvider parentKeysColumnsProvider = createAncestorColumnProvider(entityDefn, ancestorDefn);
				columnProviders.add(0, parentKeysColumnsProvider);
			}
			ancestorDefn = ancestorDefn.getParentEntityDefinition();
		}
		return columnProviders;
	}
	
	private ColumnProvider createAncestorColumnProvider(EntityDefinition contextEntityDefn, EntityDefinition ancestorEntityDefn) {
		List<ColumnProvider> providers = new ArrayList<ColumnProvider>();
		if ( configuration.isIncludeAllAncestorAttributes() ) {
			AutomaticColumnProvider ancestorEntityColumnProvider = new AutomaticColumnProvider(configuration, 
					ColumnProviders.generateHeadingPrefix(ancestorEntityDefn, configuration) + 
					configuration.getFieldHeadingSeparator(), ancestorEntityDefn);
			providers.add(0, ancestorEntityColumnProvider);
		} else {
			//include only key attributes
			List<AttributeDefinition> keyAttrDefns = ancestorEntityDefn.getKeyAttributeDefinitions();
			for (AttributeDefinition keyDefn : keyAttrDefns) {
				String relativePath = contextEntityDefn.getRelativePath(ancestorEntityDefn);
				
				ColumnProvider keyColumnProvider = ColumnProviders.createAttributeProvider(configuration, keyDefn);
				String headingPrefix = configuration.isIncludeGroupingLabels() ?
						ColumnProviders.generateHeadingPrefix(keyDefn.getParentEntityDefinition(), configuration) + configuration.getFieldHeadingSeparator()
						: "";
				PivotExpressionColumnProvider columnProvider = new PivotExpressionColumnProvider(configuration, relativePath, headingPrefix, keyColumnProvider);
				providers.add(columnProvider);
			}
			if ( isPositionColumnRequired(ancestorEntityDefn) ) {
				String relativePath = contextEntityDefn.getRelativePath(ancestorEntityDefn);
				ColumnProvider positionColumnProvider = createPositionColumnProvider(ancestorEntityDefn);
				PivotExpressionColumnProvider columnProvider = new PivotExpressionColumnProvider(configuration, relativePath, "", positionColumnProvider);
				providers.add(columnProvider);
			}
		}
		return new ColumnProviderChain(configuration, providers);
	}

	private boolean isPositionColumnRequired(EntityDefinition entityDefn) {
		return entityDefn.getParentDefinition() != null && entityDefn.isMultiple() && entityDefn.getKeyAttributeDefinitions().isEmpty();
	}
	
	private ColumnProvider createPositionColumnProvider(EntityDefinition entityDefn) {
		String columnName = calculatePositionColumnName(entityDefn);
		return new NodePositionColumnProvider(columnName);
	}
	
	private String calculatePositionColumnName(EntityDefinition nodeDefn) {
		return "_" + nodeDefn.getName() + "_position";
	}

}