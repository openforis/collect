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
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CodeProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.util.CollectionUtil;
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
			_view.popup.cancelButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandler);
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
			var items:IList = _view.popup.dataGroup.dataProvider;
			var parts:Array = new Array();
			for each (var item:CodeListItemProxy in items) { 
				if(item.selected) {
					var codeStr:String = StringUtil.concat(": ", item.code, item.qualifier);
					parts.push(codeStr);
				}
			}
			var codesStr:String = StringUtil.concat(", ", parts);
			TextInput(_view.textInput).text = codesStr;
			PopUpManager.removePopUp(_view.popup);
			applyChanges();
		}
		
		protected function cancelButtonClickHandler(event:MouseEvent):void {
			PopUpManager.removePopUp(_view.popup);
		}

		override protected function getTextValue():String {
			if(_view.attributeDefinition != null) {
				if(_view.attributeDefinition.multiple) {
					if(CollectionUtil.isNotEmpty(_view.attributes)) {
						var firstAttribute:AttributeProxy = _view.attributes.getItemAt(0) as AttributeProxy;
						if(firstAttribute.symbol != null) {
							var shortKey:String = InputFieldPresenter.getReasonBlankShortKey(attribute.symbol);
							if(shortKey != null) {
								return shortKey;
							}
						}
						var parts:Array = new Array();
						for each (var attribute:AttributeProxy in _view.attributes) {
							var part:String = codeAttributeToText(attribute);
							parts.push(part);
						}
						var result:String = org.openforis.collect.util.StringUtil.concat(", ", parts);
						return result;
					}
				} else {
					return codeAttributeToText(_view.attribute);
				}
			}
			return "";
		}
		
		protected function codeAttributeToText(attribute:AttributeProxy):String {
			if(attribute != null) {
				if(attribute.symbol != null) {
					var shortKey:String = InputFieldPresenter.getReasonBlankShortKey(attribute.symbol);
					return shortKey;
				} else {
					var value:CodeProxy = attribute.value as CodeProxy;
					if(value != null) {
						var text:String = value.toString();
						return text;
					}
				}
			}
			return "";
		}
		
		override protected function updateView():void {
			super.updateView();
		}
		
		override protected function applyChanges(value:*=null):void {
			if(_view.parentEntity == null) {
				throw new Error("Missing parent entity for this attribute");
			}
			if(value == null) {
				value = createValue();
			}
			var req:UpdateRequest = new UpdateRequest();
			var def:AttributeDefinitionProxy = _view.attributeDefinition;
			req.parentEntityId = _view.parentEntity.id;
			req.nodeName = def.name;
			req.value = String(value);
			
			if(_view.attribute != null || (CollectionUtil.isNotEmpty(_view.attributes))) {
				if(def.multiple) {
					var firstAttr:Object = _view.attributes.getItemAt(0);
					req.nodeId = firstAttr.id;
				} else {
					req.nodeId = _view.attribute.id;
				}
				req.method = UpdateRequest$Method.UPDATE;
			} else {
				req.method = UpdateRequest$Method.ADD;
			}
			var responder:AsyncResponder = new AsyncResponder(updateResultHandler, updateFaultHandler);
			ClientFactory.dataClient.updateActiveRecord(responder, req);
		}
		
	}
}
