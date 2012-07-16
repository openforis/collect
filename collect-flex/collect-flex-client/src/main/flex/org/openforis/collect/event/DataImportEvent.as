package org.openforis.collect.event
{
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.remoting.service.dataImport.DataImportSummaryItemProxy;
	import org.openforis.collect.remoting.service.dataImport.DataImportSummaryProxy;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class DataImportEvent extends UIEvent {

		public static const SHOW_IMPORT_WARNINGS:String = "showImportWarnings";
		
		private var _summaryItem:DataImportSummaryItemProxy;
		
		public function DataImportEvent(type:String, summaryItem:DataImportSummaryItemProxy, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
			_summaryItem = summaryItem;
		}
		
		public function get summaryItem():DataImportSummaryItemProxy {
			return _summaryItem;
		}

		public function set summaryItem(value:DataImportSummaryItemProxy):void {
			_summaryItem = value;
		}

	}
}