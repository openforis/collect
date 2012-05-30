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
	
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.metamodel.proxy.FileAttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.FileInputField;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.StringUtil;
	
	import spark.formatters.NumberFormatter;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class FileInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:FileInputField;
		
		private var fileReference:FileReference;
		
		public function FileInputFieldPresenter(inputField:FileInputField) {
			_view = inputField;
			fileReference = new FileReference();
			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			fileReference.addEventListener(Event.COMPLETE, fileReferenceCompleteHandler);
			fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			fileReference.addEventListener(Event.OPEN, fileReferenceOpenHandler);
			fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			_view.downloadButton.addEventListener(MouseEvent.CLICK, downloadClickHandler);
			_view.removeButton.addEventListener(MouseEvent.CLICK, removeClickHandler);
			
			initFileFilter();
		}
		
		private function initFileFilter():void {
			var attrDefn:FileAttributeDefinitionProxy = FileAttributeDefinitionProxy(_view.attributeDefinition);
			var extensions:IList = attrDefn.extensions
			var extensionsAdapted:Array = new Array();
			for each(var extension:String in extensions) {
				extensionsAdapted.push("*." + extension);
			}
			var allowedFileExtensionsDescription:String = extensionsAdapted.join(", ");
			var fileFilter:FileFilter = new FileFilter(allowedFileExtensionsDescription, extensionsAdapted.join("; "));
			
			//init maxFileSizeDescription
			var maxSize:Number = attrDefn.maxSize;
			var numberFormatter:spark.formatters.NumberFormatter = new NumberFormatter();
			numberFormatter.fractionalDigits = 0;
			
			var maxSizeDescription:String = numberFormatter.format(maxSize);
		}
		
		override protected function updateView():void {
			//update view according to attribute (generic text value)
			super.updateView();
			
			if ( _view.attribute != null && ! _view.attribute.empty ) {
				_view.currentState = FileInputField.STATE_FILE_UPLOADED;
			} else {
				_view.currentState = FileInputField.STATE_DEFAULT;
			}
		}
		
		protected function fileReferenceUploadCompleteDataHandler(event:DataEvent):void {
			
		}
		
		protected function fileReferenceOpenHandler(event:Event):void {
			
		}
		
		protected function fileReferenceCompleteHandler(event:Event):void {
			
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
			
			var url:String = ApplicationConstants.FILEUPLOAD_URL;
			//workaround for firefox/chrome flahplayer bug
			url +=";jsessionid=" + Application.sessionId;
			
			var request:URLRequest = new URLRequest(url);
			//request paramters
			request.method = URLRequestMethod.POST;
			request.data = new URLVariables();
			//request.data.path = internalXPath;
			if(_view.attribute != null && ! _view.attribute.empty) {
				var field:FieldProxy = _view.attribute.getField(0);
				request.data.overwriteOldFile = true;
				request.data.oldFileName = field.value;
			} else {
				request.data.overwriteOldFile = false;
				request.data.oldFileName = null;
			}
			fileReference.upload(request);
		}
		
		protected function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			_view.currentState = FileInputField.STATE_DEFAULT;
			AlertUtil.showError("edit.file.error", [event.text]);
		}
		
		protected function downloadClickHandler(event:MouseEvent):void {
			var request:URLRequest = getDownloadUrlRequest();
			if(request != null) {
				navigateToURL(request, "_new");
			}
		}
		
		protected function getDownloadUrlRequest():URLRequest {
			if ( _view.attribute != null && ! _view.attribute.empty ) {
				var id:Number = _view.attribute.id;
				var request:URLRequest = new URLRequest(ApplicationConstants.FILEDOWNLOAD_URL);
				request.method = URLRequestMethod.POST;
				request.data = new URLVariables();
				request.data.id = id;
				//timestamp parameter to avoid caching
				request.data._r = new Date().getTime();
				return request;
			} else {
				return null;
			}
		}
		
		
		protected function removeClickHandler(event:MouseEvent):void {
			AlertUtil.showConfirm("edit.file.removeConfirm", null, "global.confirmTitle", performDelete);
		}
		
		protected function performDelete():void {
			var httpService:HTTPService = new HTTPService();
			httpService.addEventListener(ResultEvent.RESULT, deleteResultHandler);
			httpService.addEventListener(FaultEvent.FAULT, faultHandler);
			httpService.url = ApplicationConstants.FILEDELETE_URL;
			httpService.method = URLRequestMethod.POST;
			var id:Number = _view.attribute.id;
			var data:Object = {
				fileId: id
			};
			httpService.send(data);
		}
		
		protected function deleteResultHandler(event:ResultEvent):void {
			_view.currentState = FileInputField.STATE_DEFAULT;
		}
		
	}
}
