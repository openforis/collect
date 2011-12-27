package org.openforis.collect.event.input
{
	import org.openforis.collect.event.InputFieldEvent;
	
	public class TaxonInputFieldEvent extends InputFieldEvent
	{
		public static const TAXON_SEARCH_POPUP_CLOSE:String = "taxonSearchPopUpClose";
		public static const TAXON_SEARCH_POPUP_SEARCH_TEXT_CHANGE:String = "taxonSearchPopUpSearchTextChange";
		public static const TAXON_AUTOCOMPLETE_POPUP_CLOSE:String = "taxonAutocompletePopUpClose";
		public static const TAXON_SELECT:String = "taxonSelect";
		
		public var taxon:Object;
		
		public function TaxonInputFieldEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
	}
}