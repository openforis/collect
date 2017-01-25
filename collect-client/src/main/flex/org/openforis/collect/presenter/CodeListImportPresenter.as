package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.CodeListClient;
	import org.openforis.collect.client.CodeListImportClient;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.io.parsing.CSVFileOptions;
	import org.openforis.collect.io.parsing.CSVFileSeparator;
	import org.openforis.collect.io.parsing.CSVFileTextDelimiter;
	import org.openforis.collect.io.parsing.FileCharset;
	import org.openforis.collect.ui.view.CodeListImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.NavigationUtil;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 * */
	public class CodeListImportPresenter extends AbstractReferenceDataImportPresenter {
		
		private static const MAX_SUMMARIES_PER_PAGE:int = 20;
		
		private var _codeListClient:CodeListClient;
		private var _codeListImportClient:CodeListImportClient;
		
		public function CodeListImportPresenter(view:CodeListImportView) {
			super(view, new MessageKeys());

			_codeListClient = ClientFactory.codeListClient;
			_codeListImportClient = ClientFactory.codeListImportClient;

			view.importFileFormatInfo = Message.get(messageKeys.IMPORT_FILE_FORMAT_INFO);
		}
		
		private function get view():CodeListImportView {
			return CodeListImportView(_view);
		}
		
		public function get messageKeys():MessageKeys {
			return MessageKeys(_messageKeys);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.browseButton.addEventListener(MouseEvent.CLICK, browseButtonClickHandler);
			view.downloadExampleButton.addEventListener(MouseEvent.CLICK, downloadExampleButtonClickHandler);
		}
		
		override protected function loadInitialData():void {
			updateStatus();
		}
		
		override protected function loadSummaries():void {
			//no summaries
		}
		
		override protected function performProcessStart():void {
			var responder:AsyncResponder = new AsyncResponder(startResultHandler, faultHandler);
			var csvFileOptions:CSVFileOptions = null;
			if (view.charsetDropDownList != null) {
				csvFileOptions = new CSVFileOptions();
				csvFileOptions.charset = FileCharset.valueOf(view.charsetDropDownList.selectedItem.name);
				csvFileOptions.separator = CSVFileSeparator.valueOf(view.separatorDropDownList.selectedItem.name);
				csvFileOptions.textDelimiter = CSVFileTextDelimiter.valueOf(view.textDelimiterDropDownList.selectedItem.name);
			}
			_codeListImportClient.start(responder, view.codeListId, _uploadedTempFileName, csvFileOptions);
		}
		
		override protected function performImportCancel():void {
			var responder:AsyncResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			_codeListImportClient.cancel(responder);
		}
		
		override protected function performCancelThenClose():void {
			var responder:AsyncResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			_codeListImportClient.cancel(responder);
			
			function cancelResultHandler(event:ResultEvent, token:Object = null):void {
				closePopUp();
			}
		}
		
		override protected function updateStatus():void {
			_codeListImportClient.getStatus(_getStatusResponder);
		}
		
		override protected function resetView():void {
			super.resetView();
			view.sourceFileTextInput.text = null;
		}
		
		override protected function fileReferenceSelectHandler(event:Event):void {
			view.sourceFileTextInput.text = _fileReference.name;
		}
		
		override protected function importButtonClickHandler(event:MouseEvent):void {
			if ( validateImportForm() ) {
				var responder:IResponder = new AsyncResponder(successHandler, faultHandler);
				_codeListClient.isEditedSurveyCodeListEmpty(responder, view.codeListId);
				
				function successHandler(event:ResultEvent, token:Object = null):void {
					if ( event.result ) {
						startUpload();
					} else {
						AlertUtil.showConfirm(_messageKeys.CONFIRM_IMPORT, null, 
							_messageKeys.CONFIRM_IMPORT_TITLE,
							startUpload);
					}
				}
			}
		}
		
		protected function browseButtonClickHandler(event:MouseEvent):void {
			browseFileToImport();
		}
		
		protected function downloadExampleButtonClickHandler(event:MouseEvent):void {
			NavigationUtil.openInNewWindow(ApplicationConstants.CODE_LIST_IMPORT_EXAMPLE_DOWNLOAD_URL);
		}
		
		protected function validateImportForm():Boolean {
			if ( StringUtil.isBlank(view.sourceFileTextInput.text) ) {
				AlertUtil.showMessage(messageKeys.FILE_NOT_SELECTED, null, messageKeys.IMPORT_POPUP_TITLE);
				return false;
			}
			return true;
		}
		
	}
}
import org.openforis.collect.presenter.ReferenceDataImportMessageKeys;

class MessageKeys extends ReferenceDataImportMessageKeys {

	override public function get CONFIRM_IMPORT():String {
		return "codeListImport.confirmImport.message";
	}
	
	override public function get ERROR_MISSING_REQUIRED_COLUMNS():String {
		return "codeListImport.parsingError.missing_required_columns.message";
	}
	
	public function get FILE_NOT_SELECTED():String {
		return "codeListImport.fileNotSelected";
	}
	
	public function get IMPORT_POPUP_TITLE():String {
		return "codeListImport.title";
	}
	
	public function get IMPORT_FILE_FORMAT_INFO():String {
		return "codeListImport.importFileFormatInfo";
	}
}
