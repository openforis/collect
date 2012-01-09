package org.openforis.collect.util {
	
	import org.granite.collections.BasicMap;
	import org.openforis.collect.metamodel.proxy.CodeListProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.LanguageSpecificTextProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.NodeLabelProxy;
	import org.openforis.collect.metamodel.proxy.NodeLabelProxy$Type;
	import org.openforis.collect.metamodel.proxy.PromptProxy;
	import org.openforis.collect.metamodel.proxy.PromptProxy$Type;
	import org.openforis.collect.metamodel.proxy.SchemaProxy;
	import org.openforis.collect.metamodel.proxy.SpatialReferenceSystemProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.metamodel.proxy.UnitProxy;

	public class ModelClassInitializer {
		

		public static function init():void {
			var array:Array = [
				BasicMap, 
				
				CodeListProxy,
				EntityDefinitionProxy,
				LanguageSpecificTextProxy,
				ModelVersionProxy,
				NodeLabelProxy,
				NodeLabelProxy$Type,
				PromptProxy,
				PromptProxy$Type,
				SchemaProxy,
				SpatialReferenceSystemProxy,
				SurveyProxy,
				UnitProxy
				
			];
		}
		
	}
}