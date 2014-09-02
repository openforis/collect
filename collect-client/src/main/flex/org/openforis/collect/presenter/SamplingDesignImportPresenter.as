package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.navigateToURL;
	
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.SamplingDesignClient;
	import org.openforis.collect.client.SamplingDesignImportClient;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.proxy.SamplingDesignSummariesProxy;
	import org.openforis.collect.ui.view.SamplingDesignImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.NavigationUtil;
	
	import spark.components.gridClasses.GridColumn;
	
	/**
	 * 
	 * @author Ricci, Stefano
	 * 
	 * */
	public class SamplingDesignImportPresenter extends AbstractReferenceDataImportPresenter {
		
		private static const MAX_SUMMARIES_PER_PAGE:int = 20;
		private static const FIXED_COLUMNS_COUNT:int = 6;
		
		private var _samplingDesignClient:SamplingDesignClient;
		private var _samplingDesignImportClient:SamplingDesignImportClient;
		
		public function SamplingDesignImportPresenter(view:SamplingDesignImportView) {
			super(view, new MessageKeys());
			_samplingDesignClient = ClientFactory.samplingDesignClient;
			_samplingDesignImportClient = ClientFactory.samplingDesignImportClient;
			view.importFileFormatInfo = Message.get(messageKeys.IMPORT_FILE_FORMAT_INFO);
			view.spatialReferenceSystems = Application.activeSurvey.spatialReferenceSystems;
		}
		
		private function get view():SamplingDesignImportView {
			return SamplingDesignImportView(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.downloadExampleButton.addEventListener(MouseEvent.CLICK, downloadExampleButtonClickHandler);
		}
		
		private function initInfoColumns(infoAttributes:IList):void {
			var columns:IList = view.summaryDataGrid.columns;
			for(var i:int = columns.length - 1; i >= FIXED_COLUMNS_COUNT; i-- ) {
				columns.removeItemAt(i);
			}
			for (var index:int = 0; index < infoAttributes.length; index ++) {
				var attr:String = String(infoAttributes.getItemAt(index));
				var col:GridColumn = new GridColumn();
				col.dataField = "info_" + (index + 1);
				col.headerText = attr;
				col.labelFunction = view.infoLabelFunction;
				col.width = 70;
				columns.addItem(col);
			}
		}
		
		private function get messageKeys():MessageKeys {
			return MessageKeys(_messageKeys);
		}
		
		override protected function checkCanImport():Boolean {
			var result:Boolean = checkIsWork();
			return result;
		}
		
		protected function checkIsWork():Boolean {
			if ( view.work ) {
				return true;
			} else {
				AlertUtil.showMessage(messageKeys.SAVE_SURVEY_BEFORE_EDIT);
				return false;
			}
		}

		override protected function performSummariesLoad(offset:int = 0, recordsPerPage:int = MAX_SUMMARIES_PER_PAGE):void {
			var surveyId:int = view.surveyId;
			var work:Boolean = view.work;
			var responder:IResponder = new AsyncResponder(loadSummariesResultHandler, faultHandler);
			if ( work ) {
				_samplingDesignClient.loadBySurveyWork(responder, surveyId, offset, recordsPerPage);
			} else {
				_samplingDesignClient.loadBySurvey(responder, surveyId, offset, recordsPerPage);
			}
		}
		
		override protected function loadSummariesResultHandler(event:ResultEvent, token:Object=null):void {
			var result:SamplingDesignSummariesProxy = event.result as SamplingDesignSummariesProxy;
			initInfoColumns(result.infoAttributes);
			view.summaryContainer.selectedIndex = result.totalCount > 0 ? 1: 0;
			view.summaryDataGrid.dataProvider = result.records;
			view.paginationBar.totalRecords = result.totalCount;
		}
		
		override protected function performProcessStart():void {
			var responder:AsyncResponder = new AsyncResponder(startResultHandler, faultHandler);
			var surveyId:int = view.surveyId;
			var work:Boolean = view.work;
			_samplingDesignImportClient.start(responder, _uploadedTempFileName, surveyId, work, true);
		}
		
		override protected function performImportCancel():void {
			var responder:AsyncResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			_samplingDesignImportClient.cancel(responder);
		}
		
		override protected function performCancelThenClose():void {
			var responder:AsyncResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			_samplingDesignImportClient.cancel(responder);
			
			function cancelResultHandler(event:ResultEvent, token:Object = null):void {
				closePopUp();
			}
		}
		
		override protected function updateStatus():void {
			_samplingDesignImportClient.getStatus(_getStatusResponder);
		}
		
		override protected function exportButtonClickHandler(event:MouseEvent):void {
			var request:URLRequest = getExportUrlRequest();
			if(request != null) {
				navigateToURL(request, "_new");
			}
		}
		
		override protected function fileReferenceSelectHandler(event:Event):void {
			if ( view.summaryContainer.selectedIndex == 0 ) {
				startUpload();
			} else {
				super.fileReferenceSelectHandler(event);
			}
		}
		
		protected function getExportUrlRequest():URLRequest {
			var url:String = ApplicationConstants.getSamplingDesignExportUrl(view.surveyId, view.work);
			var request:URLRequest = new URLRequest(url);
			request.method = URLRequestMethod.GET;
			return request;
		}
		
		protected function downloadExampleButtonClickHandler(event:MouseEvent):void {
			NavigationUtil.openInNewWindow(ApplicationConstants.SAMPLING_DESIGN_IMPORT_EXAMPLE_DOWNLOAD_URL);
		}
	}
}
import org.openforis.collect.presenter.ReferenceDataImportMessageKeys;

class MessageKeys extends ReferenceDataImportMessageKeys {
	/*
	override public function get CONFIRM_CLOSE_TITLE():String {
		return "samplingDesignImport.confirmClose.title";
	}
	*/
	public function get SAVE_SURVEY_BEFORE_EDIT():String {
		return "samplingDesignImport.saveSurveyBeforeEdit";
	}
	
	public function get SRS_NOT_SELECTED():String {
		return "samplingDesignImport.srsNotSelected";
	}
	
	public function get FILE_NOT_SELECTED():String {
		return "samplingDesignImport.fileNotSelected";
	}
	
	public function get IMPORT_POPUP_TITLE():String {
		return "samplingDesignImport.importPopUpTitle";
	}

	public function get IMPORT_FILE_FORMAT_INFO():String {
		return "samplingDesignImport.importFileFormatInfo";
	}

}
