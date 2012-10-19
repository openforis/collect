package org.openforis.collect.presenter
{
	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	
	import org.openforis.collect.event.ImageLoaderEvent;
	import org.openforis.collect.ui.component.input.FileInputField;
	import org.openforis.collect.ui.component.input.ImageInputField;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class ImageInputFieldPresenter extends FileInputFieldPresenter {
		
		private var _view:ImageInputField;
		
		public function ImageInputFieldPresenter(view:ImageInputField) {
			_view = view;
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.imagePreview.addEventListener(ImageLoaderEvent.IMAGE_CLICK, imagePreviewClickHandler);
		}
		
		override protected function updateView():void {
			super.updateView();
			
			if ( _view.currentState == FileInputField.STATE_FILE_UPLOADED ) {
				var imageDownloadUrlRequest:URLRequest = getDownloadUrlRequest();
				_view.imagePreview.load(imageDownloadUrlRequest);
			} else {
				
			}
		}
		
		protected function imagePreviewClickHandler(event:ImageLoaderEvent):void {
			super.downloadClickHandler(null);
		}
		
	}
}