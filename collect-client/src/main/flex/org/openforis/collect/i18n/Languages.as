package org.openforis.collect.i18n {
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.resources.IResourceBundle;
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.model.LanguageItem;

	/**
	 * @author S. Ricci
	 * */
	public class Languages {
		
		public static const LANGUAGE_CODES_RESOURCE:String = "language_codes_iso_639_3";
		
		public static function getLanguageCodes(locale:String = null):ArrayCollection {
			var result:ArrayCollection  = new ArrayCollection();
			var languagesMap:Object = getLanguagesMap(locale);
			for (var langCode:String in languagesMap) {
				var label:String = languagesMap[langCode];
				var item:LanguageItem = new LanguageItem(langCode, label);
				result.addItem(item);
			}
			var sort:Sort = new Sort();
			sort.fields = [new SortField("code", true)];
			result.sort = sort;
			result.refresh();
			return result;
		}
		
		public static function getLanguageLabel(code:String, locale:String = null):String {
			var languagesMap:Object = getLanguagesMap(locale);
			var label:String = languagesMap[code];
			return label;
		}
		
		public static function getCode(label:String, locale:String = null):String {
			var items:ArrayCollection = getLanguageCodes(locale);
			for each (var item:LanguageItem in items)  {
				if ( item.label.toLowerCase() == label.toLowerCase() ) {
					return item.code;
				}
			}
			return null;
		}

		protected static function getLanguagesMap(locale:String = null):Object {
			if ( locale == null ) {
				locale = Application.localeString;
			}
			var rm:IResourceManager = ResourceManager.getInstance();
			var resource:IResourceBundle = rm.getResourceBundle(locale, LANGUAGE_CODES_RESOURCE);
			var content:Object = resource.content;
			return content;
		}
		
	}
}