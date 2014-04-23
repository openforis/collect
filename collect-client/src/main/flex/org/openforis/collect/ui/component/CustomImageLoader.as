package org.openforis.collect.ui.component
{
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.model.Logo;
	import org.openforis.collect.model.LogoPosition;
	import org.openforis.collect.util.AlertUtil;
	
	import spark.components.Image;

	/**
	 * @author S. Ricci
	 */
	public class CustomImageLoader {
		
		private var imageEl:Image;
		private var position:LogoPosition;
		private var defaultImage:Class;
		private var errorMessageKey:String;
		
		private var _logo:Logo;
		
		public function CustomImageLoader(imageEl:Image, position:LogoPosition, defaultImage:Class = null, errorMessageKey = null) {
			this.imageEl = imageEl;
			this.position = position;
			this.defaultImage = defaultImage;
			this.errorMessageKey = errorMessageKey;
		}
		
		public function load():void {
			ClientFactory.logoClient.loadLogo(new AsyncResponder(loadResultHandler, loadFaultHandler), position);
		}
		
		private function loadResultHandler(event:ResultEvent, token:Object = null):void {
			_logo = event.result as Logo;
			if ( _logo == null ) {
				imageEl.source = defaultImage;
			} else {
				imageEl.source = _logo.image;
			}
		}
		
		private function loadFaultHandler(event:FaultEvent, token:Object = null):void {
			if ( errorMessageKey == null ) {
				imageEl.source = defaultImage;
			} else {
				AlertUtil.showError(errorMessageKey, [event.message]);
			}
		}
		
		public function get logo():Logo {
			return _logo;
		}
		
	}
}