package org.openforis.collect.metamodel
{
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.metamodel.proxy.TypedLanguageSpecificTextProxy;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.ObjectUtil;

	public class TypedLanguageSpecificTextDelegator {
		
		private var texts:IList;
		
		public function TypedLanguageSpecificTextDelegator(texts:IList) {
			this.texts = texts;
		}
		
		public function getLabelsByType(type:Object):IList {
			var result:IList = new ArrayCollection();
			for each(var label:TypedLanguageSpecificTextProxy in texts) {
				if ( ObjectUtil.getValue(label, "type") == type ) {
					result.addItem(label);
				}
			}
			return result;
		}
		
		public function getLabel(type:Object):TypedLanguageSpecificTextProxy {
			if( CollectionUtil.isNotEmpty(texts) ) {
				var labelsPerType:IList = getLabelsByType(type);
				var langCode:String = Application.localeLanguageCode;
				var isDefaultLang:Boolean = langCode == Application.activeSurvey.defaultLanguageCode;
				for each(var label:TypedLanguageSpecificTextProxy in labelsPerType) {
					if ( label.language == null && isDefaultLang || label.language == langCode) {
						return label;
					}
				}
				return getDefaultLanguageLabelByType(type);
			} else {
				return null;
			}
		}
		
		public function getDefaultLanguageLabelByType(type:Object):TypedLanguageSpecificTextProxy {
			var labelsPerType:IList = getLabelsByType(type);
			var defaultLangCode:String = Application.activeSurvey.defaultLanguageCode;
			for each(var label:TypedLanguageSpecificTextProxy in labelsPerType) {
				if ( label.language == null || label.language == defaultLangCode) {
					return label;
				}
			}
			return null;
		}

	}
}