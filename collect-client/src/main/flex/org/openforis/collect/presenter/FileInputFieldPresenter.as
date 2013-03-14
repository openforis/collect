package org.openforis.collect.presenter {
	import flash.events.DataEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.MouseEvent;
	import flash.events.ProgressEvent;
	import flash.net.FileFilter;
	import flash.net.FileReference;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.URLVariables;
	import flash.net.navigateToURL;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.FileAttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.model.proxy.FileProxy;
	import org.openforis.collect.remoting.service.FileWrapper;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.ui.component.input.FileInputField;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.formatters.NumberFormatter;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class FileInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:FileInputField;
		
		private var fileReference:FileReference;
		private var fileFilter:FileFilter;
		private var maxSizeDescription:String;
		
		public function FileInputFieldPresenter(inputField:FileInputField) {
			_view = inputField;
			fileReference = new FileReference();
			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			fileReference.addEventListener(Event.COMPLETE, fileReferenceLoadComplete);
			fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			_view.browseButton.addEventListener(MouseEvent.CLICK, browseClickHandler);
			_view.downloadButton.addEventListener(MouseEvent.CLICK, downloadClickHandler);
			_view.removeButton.addEventListener(MouseEvent.CLICK, removeClickHandler);
			
			initFileFilter();
		}
		
		private function initFileFilter():void {
			var attrDefn:FileAttributeDefinitionProxy = FileAttributeDefinitionProxy(_view.attributeDefinition);
			var extensions:IList = attrDefn.extensions;
			if ( CollectionUtil.isEmpty(extensions) || 
					extensions.length == 1 && StringUtil.isBlank(extensions.getItemAt(0) as String) ) {
				extensions = new ArrayCollection(["*"]);
			}
			var extensionsAdapted:Array = new Array();
			for each(var extension:String in extensions) {
				extensionsAdapted.push("*." + extension);
			}
			var allowedFileExtensionsDescription:String = extensionsAdapted.join(", ");
			fileFilter = new FileFilter(allowedFileExtensionsDescription, extensionsAdapted.join("; "));
			
			//init maxFileSizeDescription
			var maxSize:Number = attrDefn.maxSize;
			var numberFormatter:spark.formatters.NumberFormatter = new NumberFormatter();
			numberFormatter.fractionalDigits = 0;
			
			maxSizeDescription = numberFormatter.format(maxSize);
		}
		
		override protected function updateView():void {
			super.updateView();
			var fileName:String = null;
			if ( _view.attribute != null ) {
				var nameField:FieldProxy = _view.attribute.getField(0);
				fileName = nameField.value as String;
			}
			if ( StringUtil.isBlank(fileName) ) {
				_view.currentState = FileInputField.STATE_DEFAULT;
			} else {
				_view.currentState = FileInputField.STATE_FILE_UPLOADED;
			}
			var hasRemarks:Boolean = false;
			var remarks:String = getRemarks();
			hasRemarks = StringUtil.isNotBlank(remarks);
			_view.remarksPresent = hasRemarks;
		}
		
		protected function fileReferenceUploadCompleteDataHandler(event:DataEvent):void {
			//upload completed, get data from response
			var fileName:String = event.data as String;
			var fileAttribute:AttributeProxy = _view.attribute;
			fileAttribute.getField(0).value = fileName;
			_view.currentState = FileInputField.STATE_FILE_UPLOADED;
		}
		
		protected function fileReferenceProgressHandler(event:ProgressEvent):void {
			_view.uploadProgressBar.setProgress(event.bytesLoaded, event.bytesTotal);
		}
		
		protected function fileReferenceSelectHandler(event:Event):void {
			var attrDefn:FileAttributeDefinitionProxy = FileAttributeDefinitionProxy(_view.attributeDefinition);
			var maxSize:Number = attrDefn.maxSize;
			if(fileReference.size > maxSize) {
				var numberFormatter:spark.formatters.NumberFormatter = new NumberFormatter();
				numberFormatter.fractionalDigits = 0;
				var maxSizeFormatted:String = numberFormatter.format(maxSize);
				var sizeFormatted:String = numberFormatter.format(fileReference.size);
				AlertUtil.showError("edit.file.error.sizeExceedsMaximum", [sizeFormatted, maxSizeFormatted]);
				return;
			}
			_view.currentState = FileInputField.STATE_UPLOADING;
			
			/*
			var url:String = ApplicationConstants.MODEL_FILE_UPLOAD_URL;
			//workaround for firefox/chrome flahplayer bug
			url +=";jsessionid=" + Application.sessionId;
			
			var request:URLRequest = new URLRequest(url);
			//request paramters
			request.method = URLRequestMethod.POST;
			var parameters:URLVariables = new URLVariables();
			parameters.sessionId = Application.sessionId;
			parameters.surveyId = Application.activeSurvey.id;
			parameters.recordId = Application.activeRecord.id;
			parameters.nodeDefnId = attrDefn.id;
			parameters.nodeId = _view.attribute.id;
			request.data = parameters;
			//request.data.path = internalXPath;
			fileReference.upload(request);
			*/
			fileReference.load();
		}
		
		protected function fileReferenceLoadComplete(event:Event):void {
			var fileWrapper:FileWrapper = new FileWrapper();
			fileWrapper.data = fileReference.data;
			fileWrapper.fileName = fileReference.name;
			var nodeId:Number = _view.attribute.id;
			var updateReq:UpdateRequestOperation = createFileUpdateRequestOperation(fileWrapper);
			sendUpdateRequest(updateReq);
		}
		
		protected function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			_view.currentState = FileInputField.STATE_DEFAULT;
			AlertUtil.showError("edit.file.error", [event.text]);
		}
		
		protected function browseClickHandler(event:MouseEvent):void {
			fileReference.browse([fileFilter]);
		}
		
		protected function downloadClickHandler(event:MouseEvent):void {
			var request:URLRequest = getDownloadUrlRequest();
			if(request != null) {
				navigateToURL(request, "_new");
			}
		}
		
		protected function getDownloadUrlRequest():URLRequest {
			if ( _view.attribute != null && ! _view.attribute.empty ) {
				var request:URLRequest = new URLRequest(ApplicationConstants.RECORD_FILE_DOWNLOAD_URL);
				request.method = URLRequestMethod.POST;
				var parameters:URLVariables = new URLVariables();
				parameters.nodeId = _view.attribute.id;
				request.data = parameters;
				//timestamp parameter to avoid caching
				request.data._r = new Date().getTime();
				return request;
			} else {
				return null;
			}
		}
		
		protected function removeClickHandler(event:MouseEvent):void {
			AlertUtil.showConfirm("edit.file.deleteConfirm", null, "global.deleteTitle", performDelete);
		}
		
		protected function performDelete():void {
			var nodeId:Number = _view.attribute.id;
			var updateReq:UpdateRequestOperation = createFileUpdateRequestOperation(null);
			sendUpdateRequest(updateReq);

			//var responder:IResponder = new AsyncResponder(deleteResultHandler, faultHandler);
			//ClientFactory.recordFileClient.deleteFile(responder, nodeId);
			/*
			var httpService:HTTPService = new HTTPService();
			httpService.addEventListener(ResultEvent.RESULT, deleteResultHandler);
			httpService.addEventListener(FaultEvent.FAULT, faultHandler);
			httpService.url = ApplicationConstants.RECORD_FILE_DELETE_URL;
			httpService.method = URLRequestMethod.POST;
			var nodeId:Number = _view.attribute.id;
			var data:Object = {
				fileId: nodeId
			};
			httpService.send(data);
			*/
		}
		
		protected function deleteResultHandler(event:ResultEvent, token:Object = null):void {
			_view.currentState = FileInputField.STATE_DEFAULT;
			var fileAttribute:AttributeProxy = _view.attribute;
			fileAttribute.getField(0).value = null;
		}
		
		protected function createFileUpdateRequestOperation(fileWrapper:FileWrapper):UpdateRequestOperation {
			var o:UpdateRequestOperation = new UpdateRequestOperation();
			var def:AttributeDefinitionProxy = _view.attributeDefinition;
			o.method = UpdateRequestOperation$Method.UPDATE;
			o.parentEntityId = _view.parentEntity.id;
			o.nodeName = def.name;
			o.nodeId = _view.attribute.id;
			o.fieldIndex = -1;
			o.value = fileWrapper;
			o.symbol = null;
			o.remarks = getRemarks();
			return o;
		}
		
	}
}
