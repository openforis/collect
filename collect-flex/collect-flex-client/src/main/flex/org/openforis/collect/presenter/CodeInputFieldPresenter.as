package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	import mx.core.FlexGlobals;
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CodeProxy;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * @author M. Togna
	 * */
	public class CodeInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:CodeInputField;
		
		public function CodeInputFieldPresenter(inputField:CodeInputField) {
			this._view = inputField;
			
			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.openImage.addEventListener(MouseEvent.CLICK, openImageClickHandler);
			
			_view.popup.addEventListener(CloseEvent.CLOSE, closePopupHandler);
			_view.popup.cancelButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandelr);
			_view.popup.applyButton.addEventListener(MouseEvent.CLICK, applyButtonClickHandelr);
			
			ChangeWatcher.watch(_view, "attributes", attributeChangeHandler);
		}
		
		/**
		 * Close the popup
		 * */
		internal function closePopupHandler(event:CloseEvent):void {
			PopUpManager.removePopUp(_view.popup);
		}
		
		/**
		 * Open the popup
		 * */
		protected function openImageClickHandler(event:Event):void {
			PopUpManager.addPopUp(_view.popup, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(_view.popup);
			
			loadCodes();
		}
		
		protected function loadCodes():void {
			_view.popup.currentState = "loading";
			
			//call service method
			
			var codeAttributeDef:CodeAttributeDefinitionProxy = _view.attributeDefinition as CodeAttributeDefinitionProxy;
			var attribute:String = codeAttributeDef.name;
			var parentEntityId:int = _view.parentEntity.id;
			ClientFactory.dataClient.findCodeList(new AsyncResponder(loadListDialogDataResultHandler, faultHandler), parentEntityId, attribute);
		}
		
		protected function loadListDialogDataResultHandler(event:ResultEvent, token:Object = null):void {
			var data:IList = event.result as IList;
			_view.popup.dataGroup.dataProvider = data;
			_view.popup.currentState = "default";
		}

		protected function applyButtonClickHandelr(event:MouseEvent):void {
			//var data:ArrayCollection = _view.popup.dataGroup.dataProvider;
			
			//call service method
		}
		
		protected function cancelButtonClickHandelr(event:MouseEvent):void {
			PopUpManager.removePopUp(_view.popup);
		}

		override protected function get textValue():String {
			if(_view.attributeDefinition != null) {
				if(_view.attributeDefinition.multiple) {
					if(_view.attributes) {
						var parts:Array = new Array();
						for each (var attribute:AttributeProxy in _view.attributes) {
							var part:String = getText(attribute);
							parts.push(part);
						}
						var result:String = org.openforis.collect.util.StringUtil.concat(", ", parts);
						return result;
					}
				} else {
					return getText(_view.attribute);
				}
			}
			return "";
		}
		
		protected function getText(attribute:AttributeProxy):String {
			if(attribute != null) {
				var value:CodeProxy = attribute.value as CodeProxy;
				if(value != null) {
					var text:String = value.toString();
					return text;
				}
			}
			return "";
		}
		
		override public function updateView():void {
			super.updateView();
		}
		
	}
}
