package org.openforis.collect.util {
	
	import org.granite.collections.BasicMap;
	import org.openforis.collect.metamodel.proxy.LanguageSpecificTextProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.SpatialReferenceSystemProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.metamodel.proxy.UnitProxy;

	public class ModelClassInitializer {
		

		public static function init():void {
			var array:Array = [
				BasicMap, 
				
				SurveyProxy,
				ModelVersionProxy,
				SpatialReferenceSystemProxy,
				UnitProxy,
				LanguageSpecificTextProxy
				
				
				
			];
		}
		
	}
}