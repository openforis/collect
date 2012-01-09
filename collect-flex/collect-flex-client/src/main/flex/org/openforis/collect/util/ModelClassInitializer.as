package org.openforis.collect.util {
	
	import org.granite.collections.BasicMap;
	import org.openforis.collect.metamodel.proxy.LanguageSpecificTextProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;

	public class ModelClassInitializer {
		

		public static function init():void {
			var array:Array = [
				BasicMap, 
				
				SurveyProxy,
				ModelVersionProxy,
				LanguageSpecificTextProxy
				
				
				
			];
		}
		
	}
}