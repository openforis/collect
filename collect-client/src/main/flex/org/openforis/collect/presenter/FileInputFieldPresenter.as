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
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.FileAttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.AttributeUpdateRequestProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.model.proxy.NodeUpdateRequestProxy;
	import org.openforis.collect.remoting.service.FileWrapper;
	import org.openforis.collect.ui.component.input.FileInputField;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
	import spark.components.Label;
	import spark.formatters.NumberFormatter;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class FileInputFieldPresenter extends InputFieldPresenter {

		private var fileReference:FileReference;
		private var fileFilter:FileFilter;
		private var maxSizeDescription:String;
		
		public function FileInputFieldPresenter(inputField:FileInputField) {
			super(inputField);
			fileReference = new FileReference();
		}
		
		private function get view():FileInputField {
			return FileInputField(_view);
		}
		
		override public function init():void {
			super.init();
			initFileFilter();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			//fileReference.addEventListener(Event.COMPLETE, fileReferenceLoadComplete);
			fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			view.browseButton.addEventListener(MouseEvent.CLICK, browseClickHandler);
			view.downloadButton.addEventListener(MouseEvent.CLICK, downloadClickHandler);
			view.removeButton.addEventListener(MouseEvent.CLICK, removeClickHandler);
		}

		private function initFileFilter():void {
			var attrDefn:FileAttributeDefinitionProxy = FileAttributeDefinitionProxy(view.attributeDefinition);
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
			var fileName:String = getFileName();
			if ( StringUtil.isBlank(fileName) ) {
				view.currentState = FileInputField.STATE_DEFAULT;
			} else {
				view.currentState = FileInputField.STATE_FILE_UPLOADED;
			}
			var hasRemarks:Boolean = false;
			var remarks:String = getRemarks();
			hasRemarks = StringUtil.isNotBlank(remarks);
			view.remarksPresent = hasRemarks;
			
			updatePreview();
		}
		
		protected function updatePreview():void {
			if ( view.previewContainer is Label ) {
				var fileName:String = getFileName();
				var extension:String = fileName == null? null: fileName.substr(fileName.lastIndexOf("."));
				view.previewContainer.text = Message.get("edit.file.uploadedFileType", [extension]);
			}
		}
		
		protected function getFileName():String {
			var fileName:String = null;
			if ( view.attribute != null ) {
				var nameField:FieldProxy = view.attribute.getField(0);
				fileName = nameField.value as String;
			}
			return fileName;
		}
		
		override protected function setFocusHandler(event:InputFieldEvent):void {
			if ( view.browseButton != null && view.attribute != null && 
				view.attribute.id == event.attributeId && 
				view.fieldIndex == event.fieldIdx ) {
				view.browseButton.setFocus();
			}
		}
		
		protected function fileReferenceUploadCompleteDataHandler(event:DataEvent):void {
			//upload completed, get data from response
			var fileWrapper:FileWrapper = new FileWrapper();
			fileWrapper.filePath = event.data;
			fileWrapper.originalFileName = fileReference.name;

			var request:NodeUpdateRequestProxy = createFileUpdateRequestOperation(fileWrapper);
			sendUpdateRequest(request);
		}
		
		protected function fileReferenceProgressHandler(event:ProgressEvent):void {
			view.uploadProgressBar.setProgress(event.bytesLoaded, event.bytesTotal);
		}
		
		protected function fileReferenceSelectHandler(event:Event):void {
			var attrDefn:FileAttributeDefinitionProxy = FileAttributeDefinitionProxy(view.attributeDefinition);
			var maxSize:Number = attrDefn.maxSize;
			if(fileReference.size > maxSize) {
				var numberFormatter:spark.formatters.NumberFormatter = new NumberFormatter();
				numberFormatter.fractionalDigits = 0;
				var maxSizeFormatted:String = numberFormatter.format(maxSize);
				var sizeFormatted:String = numberFormatter.format(fileReference.size);
				AlertUtil.showError("edit.file.error.sizeExceedsMaximum", [sizeFormatted, maxSizeFormatted]);
			} else {
				view.currentState = FileInputField.STATE_UPLOADING;
				
				var request:URLRequest = new URLRequest(ApplicationConstants.FILE_UPLOAD_URL);
				request.method = URLRequestMethod.POST;

				fileReference.upload(request, "fileData");
			}
		}
		
		protected function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			view.currentState = FileInputField.STATE_DEFAULT;
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
			if ( view.attribute != null && ! view.attribute.empty ) {
				var request:URLRequest = new URLRequest(ApplicationConstants.RECORD_FILE_DOWNLOAD_URL);
				request.method = URLRequestMethod.POST;
				var parameters:URLVariables = new URLVariables();
				parameters.nodeId = view.attribute.id;
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
			var nodeId:Number = view.attribute.id;
			var request:NodeUpdateRequestProxy = createFileUpdateRequestOperation(null);
			sendUpdateRequest(request);
		}
		
		protected function deleteResultHandler(event:ResultEvent, token:Object = null):void {
			view.currentState = FileInputField.STATE_DEFAULT;
			var fileAttribute:AttributeProxy = view.attribute;
			fileAttribute.getField(0).value = null;
		}
		
		protected function createFileUpdateRequestOperation(fileWrapper:FileWrapper):NodeUpdateRequestProxy {
			var r:AttributeUpdateRequestProxy = new AttributeUpdateRequestProxy();
			var def:AttributeDefinitionProxy = view.attributeDefinition;
			r.nodeId = view.attribute.id;
			r.value = fileWrapper;
			r.symbol = null;
			r.remarks = getRemarks();
			return r;
		}
		
	}
}
