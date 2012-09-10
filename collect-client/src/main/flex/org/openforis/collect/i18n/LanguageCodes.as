package org.openforis.collect.i18n {
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.resources.IResourceBundle;
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	
	import org.openforis.collect.model.LanguageItem;

	/**
	 * @author S. Ricci
	 * */
	public class LanguageCodes {
		
		public static const LANGUAGE_CODES_RESOURCE:String = "language_codes_iso_639_3";
		
		public static function getLanguageCodes(locale:String):ArrayCollection {
			var result:ArrayCollection  = new ArrayCollection();
			var rm:IResourceManager = ResourceManager.getInstance();
			var resource:IResourceBundle = rm.getResourceBundle(locale, LANGUAGE_CODES_RESOURCE);
			var content:Object = resource.content;
			for (var langCode:String in content) {
				var label:String = content[langCode];
				var item:LanguageItem = new LanguageItem(langCode, label);
				result.addItem(item);
			}
			var sort:Sort = new Sort();
			sort.fields = [new SortField("code", true)];
			result.sort = sort;
			result.refresh();
			return result;
		} 
	}
}