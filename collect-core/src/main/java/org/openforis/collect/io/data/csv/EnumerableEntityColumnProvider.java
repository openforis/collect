package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 */
public class EnumerableEntityColumnProvider extends ColumnProviderChain {

	// TODO Support multiple code keys 
	// TODO Check that def is right type!
	// TODO Check that list is not lookup
	// TODO support hierarchical lists!
	
	public EnumerableEntityColumnProvider(CSVDataExportParameters config, EntityDefinition defn) {
		super(config, defn, createProviders(config, defn));
	}

	private static List<ColumnProvider> createProviders(CSVDataExportParameters config, EntityDefinition defn) {
		List<ColumnProvider> providers = new ArrayList<ColumnProvider>();
		List<AttributeDefinition> keyDefs = defn.getKeyAttributeDefinitions();
		CodeAttributeDefinition keyDef = (CodeAttributeDefinition) keyDefs.get(0);
		CodeList codeList = keyDef.getList();
		SurveyContext context = defn.getSurvey().getContext();
		CodeListService codeListService = context.getCodeListService();
		List<CodeListItem> items = codeListService.loadRootItems(codeList);
		for (CodeListItem item : items) {
			String code = item.getCode();
			String keyName = keyDef.getName();
			EnumeratedCodeItemColumnProvider p = new EnumeratedCodeItemColumnProvider(config, defn, keyName, code);
			providers.add(p);
		}
		return providers;
	}
	
	@Override
	protected String generateHeadingPrefix() {
		if (entityDefinition == null) {
			throw new IllegalStateException("Entity definition not specified for enumerable entity column provider");
		}
		return ColumnProviders.generateHeadingPrefix(entityDefinition, config);
	}

	private static class EnumeratedCodeItemColumnProvider extends AutomaticColumnProvider {

		private EntityDefinition entityDefinition;
		private String keyName;
		private String code;

		public EnumeratedCodeItemColumnProvider(CSVDataExportParameters config, EntityDefinition defn, String keyName, String code) {
			super(config, code + "_", defn);
			this.entityDefinition = defn;
			this.keyName = keyName;
			this.code = code;
		}
		
		@Override
		public List<Object> extractValues(Node<?> axis) {
			Entity parentEntity = (Entity) axis;
			List<Node<?>> children = parentEntity.getChildren(entityDefinition);
			for (Node<?> child : children) {
				Entity childEntity = (Entity) child;
				Code childCode = (Code) childEntity.getValue(keyName, 0);
				if(childCode != null ){
					String codeVal = childCode.getCode();
					if ( code.equals(codeVal) ) {
						return super.extractValues(childEntity);
					}
				}
			}
			// not found; return empty array
			return super.emptyValues();
		}
	}
}
