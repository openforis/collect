package org.openforis.collect.event
{
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.remoting.service.dataimport.DataImportSummaryItemProxy;
	import org.openforis.collect.remoting.service.dataimport.FileUnmarshallingErrorProxy;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class DataImportEvent extends UIEvent {

		public static const SHOW_IMPORT_WARNINGS:String = "showImportWarnings";
		public static const SHOW_SKIPPED_FILE_ERRORS:String = "showSkippedFileErrors";
		
		private var _summaryItem:DataImportSummaryItemProxy;
		private var _fileUnmarshallingError:FileUnmarshallingErrorProxy;
		
		public function DataImportEvent(type:String, summaryItem:DataImportSummaryItemProxy = null, fileUnmarshallingError:FileUnmarshallingErrorProxy = null, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
			_summaryItem = summaryItem;
			_fileUnmarshallingError = fileUnmarshallingError;
		}
		
		public function get summaryItem():DataImportSummaryItemProxy {
			return _summaryItem;
		}

		public function set summaryItem(value:DataImportSummaryItemProxy):void {
			_summaryItem = value;
		}

		public function get fileUnmarshallingError():FileUnmarshallingErrorProxy {
			return _fileUnmarshallingError;
		}

		public function set fileUnmarshallingError(value:FileUnmarshallingErrorProxy):void {
			_fileUnmarshallingError = value;
		}


	}
}