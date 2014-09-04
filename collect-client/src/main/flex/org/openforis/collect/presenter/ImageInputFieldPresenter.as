package org.openforis.collect.presenter
{
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	import flash.ui.Keyboard;
	
	import org.openforis.collect.event.ImageLoaderEvent;
	import org.openforis.collect.ui.component.input.FileInputField;
	import org.openforis.collect.ui.component.input.ImageInputField;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class ImageInputFieldPresenter extends FileInputFieldPresenter {
		
		public function ImageInputFieldPresenter(view:ImageInputField) {
			super(view);
		}
		
		private function get view():ImageInputField {
			return ImageInputField(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			
			view.imagePreview.addEventListener(ImageLoaderEvent.IMAGE_CLICK, imagePreviewClickHandler);
			view.browseButton.addEventListener(KeyboardEvent.KEY_DOWN, browseButtonKeyDownHandler);
		}
		
		protected function browseButtonKeyDownHandler(event:KeyboardEvent):void	{
			if ( event.keyCode == Keyboard.TAB ) {
				UIUtil.moveFocus(event.shiftKey);
			}
		}
		
		override protected function updateView():void {
			super.updateView();
		}
		
		override protected function updatePreview():void {
			if ( view.currentState == FileInputField.STATE_FILE_UPLOADED ) {
				var imageDownloadUrlRequest:URLRequest = getDownloadUrlRequest();
				view.imagePreview.load(imageDownloadUrlRequest);
			}			
		}
		
		protected function imagePreviewClickHandler(event:ImageLoaderEvent):void {
			super.downloadClickHandler(null);
		}
		
	}
}