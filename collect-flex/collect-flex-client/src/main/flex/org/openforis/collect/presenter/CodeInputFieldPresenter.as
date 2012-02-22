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
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CodeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CodeListDialog;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * @author M. Togna
	 * */
	public class CodeInputFieldPresenter extends InputFieldPresenter {
		
		private static var _popUp:CodeListDialog;
		private var _view:CodeInputField;
		private var _items:IList;
		
		public function CodeInputFieldPresenter(inputField:CodeInputField) {
			this._view = inputField;
			
			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.openImage.addEventListener(MouseEvent.CLICK, openImageClickHandler);
			
			ChangeWatcher.watch(_view, "attributes", attributeChangeHandler);
		}
		
		/**
		 * Close the popup
		 * */
		internal static function closePopupHandler(event:Event = null):void {
			PopUpManager.removePopUp(_popUp);
		}
		
		/**
		 * Open the popup
		 * */
		protected function openImageClickHandler(event:Event):void {
			if(_popUp == null) {
				_popUp = new CodeListDialog();
				_popUp.addEventListener(CloseEvent.CLOSE, closePopupHandler);
				_popUp.cancelButton.addEventListener(MouseEvent.CLICK, closePopupHandler);
				_popUp.applyButton.addEventListener(MouseEvent.CLICK, applyButtonClickHandler);
			}
			PopUpManager.addPopUp(_popUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(_popUp);
			_popUp.multiple = _view.attributeDefinition.multiple;
			_popUp.maxSpecified = _view.attributeDefinition.maxCount;
			_popUp.title = _view.attributeDefinition.getLabelText();
			_popUp.codeInputField = _view;
			
			loadCodes();
		}
		
		protected function loadCodes():void {
			_popUp.currentState = "loading";
			
			var codeAttributeDef:CodeAttributeDefinitionProxy = _view.attributeDefinition as CodeAttributeDefinitionProxy;
			var attribute:String = codeAttributeDef.name;
			var parentEntityId:int = _view.parentEntity.id;
			ClientFactory.dataClient.findAssignableCodeListItems(new AsyncResponder(loadListDialogDataResultHandler, faultHandler), parentEntityId, attribute);
		}
		
		protected function loadListDialogDataResultHandler(event:ResultEvent, token:Object = null):void {
			var data:IList = event.result as IList;
			_popUp.dataGroup.dataProvider = data;
			_popUp.currentState = "default";
		}

		protected static function applyButtonClickHandler(event:MouseEvent):void {
			var items:IList = _popUp.dataGroup.dataProvider;
			var parts:Array = new Array();
			for each (var item:CodeListItemProxy in items) { 
				if(item.selected) {
					var codeStr:String = StringUtil.concat(": ", item.code, item.qualifier);
					parts.push(codeStr);
				}
			}
			var codesStr:String = StringUtil.concat(", ", parts);
			TextInput(_popUp.codeInputField.textInput).text = codesStr;
			_popUp.codeInputField.presenter.applyChanges();
			closePopupHandler();
		}
		
		override protected function getTextValue():String {
			if(_view.attributeDefinition != null) {
				if(_view.attributeDefinition.multiple) {
					if(CollectionUtil.isNotEmpty(_view.attributes)) {
						var firstAttribute:AttributeProxy = _view.attributes.getItemAt(0) as AttributeProxy;
						var field:FieldProxy = firstAttribute.getField(0);
						if(field.symbol != null) {
							var shortKey:String = InputFieldPresenter.getReasonBlankShortKey(field.symbol);
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
				var field:FieldProxy = attribute.getField(0);
				if(field.symbol != null) {
					var shortKey:String = InputFieldPresenter.getReasonBlankShortKey(field.symbol);
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
			updateDescription();
		}
		
		override public function applyChanges():void {
			var req:UpdateRequest = new UpdateRequest();
			var def:AttributeDefinitionProxy = _view.attributeDefinition;
			req.parentEntityId = _view.parentEntity.id;
			req.nodeName = def.name;
			req.value = createRequestValue();
			req.fieldIndex = NaN; //ignore field index, update the entire code or list of codes
			if(_view.attribute != null || (CollectionUtil.isNotEmpty(_view.attributes))) {
				if(! def.multiple) {
					req.nodeId = _view.attribute.id;
				}
				req.method = UpdateRequest$Method.UPDATE;
			} else {
				req.method = UpdateRequest$Method.ADD;
			}
			var responder:AsyncResponder = new AsyncResponder(updateResultHandler, updateFaultHandler);
			ClientFactory.dataClient.updateActiveRecord(responder, req);
		}
		
		protected function updateDescription():void {
			if(_view.attribute != null || _view.attributes != null) {
				var codes:Array = [];
				var attribute:AttributeProxy;
				if(_view.attributeDefinition.multiple) {
					for each(attribute in _view.attributes) {
						if( attribute.value != null && StringUtil.isNotBlank(attribute.value.code)) {
							codes.push(attribute.value.code);
						}
					}
				} else {
					attribute = _view.attribute;
					if(attribute != null && attribute.value != null && StringUtil.isNotBlank(attribute.value.code)) {
						codes.push(attribute.value.code);
					}
				}
				if(ArrayUtil.isNotEmpty(codes)) {
					var parentEntityId:int = _view.parentEntity.id;
					var name:String = _view.attributeDefinition.name;
					var responder:IResponder = new AsyncResponder(findItemsResultHandler, faultHandler);
					
					ClientFactory.dataClient.getCodeListItems(responder, parentEntityId, name, codes);
				}
			} else {
				_view.description = "";
			}
		}
		
		protected function findItemsResultHandler(event:ResultEvent, token:Object = null):void {
			_items = event.result as IList;
			_view.description = getDescription();
		}
		
		protected function getDescription():String {
			var description:String = null;
			if(CollectionUtil.isNotEmpty(_items)) {
				var parts:Array = new Array();
				for each (var item:CodeListItemProxy in _items) {
					parts.push(item.getLabelText());
				}
				description = StringUtil.concat("\n", parts);
			}
			return description;
		}
	}
}
