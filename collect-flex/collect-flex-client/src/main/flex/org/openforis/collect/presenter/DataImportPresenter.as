package org.openforis.collect.presenter {
	
	import flash.events.DataEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.MouseEvent;
	import flash.events.ProgressEvent;
	import flash.net.FileReference;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.URLVariables;
	
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataImportClient;
	import org.openforis.collect.remoting.service.dataImport.DataImportState;
	import org.openforis.collect.ui.view.DataImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 * */
	public class DataImportPresenter extends AbstractPresenter {
		
		private var _view:DataImportView;
		
		public var _fileReference:FileReference;
		
		private var _dataImportClient:DataImportClient;
		
		public function DataImportPresenter(view:DataImportView) {
			this._view = view;
			_fileReference = new FileReference();
			_dataImportClient = ClientFactory.dataImportClient;
			super();
		}
		
		override internal function initEventListeners():void {
			_fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			_fileReference.addEventListener(Event.COMPLETE, fileReferenceCompleteHandler);
			_fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			_fileReference.addEventListener(Event.OPEN, fileReferenceOpenHandler);
			_fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			_fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			_view.uploadButton.addEventListener(MouseEvent.CLICK, uploadButtonClickHandler);
		}
		
		protected function fileReferenceUploadCompleteDataHandler(event:DataEvent):void {
			var responder:AsyncResponder = new AsyncResponder(initProcessResultHandler, faultHandler);
			var surveyName:String = _view.surveyNameTextInput.text;
			var rootEntityName:String = _view.rootEntityNameTextInput.text;
			_dataImportClient.initProcess(responder, surveyName, rootEntityName);
		}
		
		protected function fileReferenceOpenHandler(event:Event):void {
			
		}
		
		protected function fileReferenceCompleteHandler(event:Event):void {
			
		}
		
		protected function fileReferenceProgressHandler(event:ProgressEvent):void {
			_view.progressBar.setProgress(event.bytesLoaded, event.bytesTotal);
		}
		
		protected function fileReferenceSelectHandler(event:Event):void {
			//_view.currentState = STATE_UPLOADING;
			
			var url:String = ApplicationConstants.DATA_IMPORT_UPLOAD_URL;
			//workaround for firefox/chrome flahplayer bug
			url +=";jsessionid=" + Application.sessionId;
			
			var request:URLRequest = new URLRequest(url);
			//request paramters
			request.method = URLRequestMethod.POST;
			var parameters:URLVariables = new URLVariables();
			parameters.surveyName = _view.surveyNameTextInput.text;
			parameters.rootEntityName = _view.rootEntityNameTextInput.text;
			request.data = parameters;
			_fileReference.upload(request);
		}
		
		protected function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			AlertUtil.showError("dataImport.file.error", [event.text]);
		}
		
		protected function uploadButtonClickHandler(event:MouseEvent):void {
			_fileReference.browse();
		}
		
		protected function initProcessResultHandler(event:ResultEvent):void {
			var state:DataImportState = event.result as DataImportState;
			AlertUtil.showConfirm("dataImport.confirmStart", [], null, initProcessConfirmHandler);
		}
		
		protected function startImportResultHandler(event:ResultEvent):void {
			
		}
		
		protected function initProcessConfirmHandler():void {
			var responder:AsyncResponder = new AsyncResponder(startImportResultHandler, faultHandler);
			_dataImportClient.startImport(responder);
		}
	}
}