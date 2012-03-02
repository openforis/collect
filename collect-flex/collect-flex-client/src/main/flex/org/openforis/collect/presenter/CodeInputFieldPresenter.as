package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.core.FlexGlobals;
	import mx.events.CloseEvent;
	import mx.events.CollectionEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CodeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
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
		
		public function CodeInputFieldPresenter(view:CodeInputField) {
			_view = view;
			_view.fieldIndex = -1;
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.openImage.addEventListener(MouseEvent.CLICK, openImageClickHandler);
			
			ChangeWatcher.watch(_view, "attributes", attributesChangeHandler);
		}
		
		protected function attributesChangeHandler(event:Event):void {
			if(! (event is CollectionEvent) && _view.attributes != null) {
				_view.attributes.addEventListener(CollectionEvent.COLLECTION_CHANGE, attributesChangeHandler);
			}
			updateView();
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
			dataClient.findAssignableCodeListItems(new AsyncResponder(loadListDialogDataResultHandler, faultHandler), parentEntityId, attribute);
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
			_popUp.codeInputField.applyValue();
			closePopupHandler();
		}
		
		override protected function valueToText():String {
			if(_view.attributeDefinition != null) {
				if(_view.attributeDefinition.multiple) {
					if(CollectionUtil.isNotEmpty(_view.attributes)) {
						var firstAttribute:AttributeProxy = _view.attributes.getItemAt(0) as AttributeProxy;
						var field:FieldProxy = firstAttribute.getField(0);
						if(field.symbol != null) {
							var shortCut:String = getShortCutForReasonBlank(field.symbol);
							if(shortCut != null) {
								return shortCut;
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
					var shortCut:String = getShortCutForReasonBlank(field.symbol);
					if(shortCut != null) {
						return shortCut;
					}
				}
				var value:CodeProxy = attribute.value as CodeProxy;
				if(value != null) {
					var text:String = value.toString();
					return text;
				}
			}
			return "";
		}
		
		override protected function updateView():void {
			super.updateView();
			updateDescription();
		}
		
		override public function applyValue():void {
			if(_view.attributeDefinition.multiple) {
				var text:String = textToRequestValue();
				var operations:ArrayCollection = new ArrayCollection();
				var o:UpdateRequestOperation;
				for each (var a:AttributeProxy in _view.attributes) {
					o = getUpdateRequestOperation(UpdateRequestOperation$Method.DELETE, a.id);
					operations.addItem(o);
				}
				var remarks:String = getRemarks();
				if(text != null) {
					var parts:Array = text.split(",");
					if(parts.length == 1 && isShortCutForReasonBlank(text)) {
						var symbol:FieldSymbol = parseShortCutForReasonBlank(text);
						o = getUpdateRequestOperation(UpdateRequestOperation$Method.ADD, NaN, null, symbol, remarks);
						operations.addItem(o);
					} else {
						for each (var part:String in parts) {
							var trimmedPart:String = StringUtil.trim(part);
							if(StringUtil.isNotBlank(trimmedPart)) {
								o = getUpdateRequestOperation(UpdateRequestOperation$Method.ADD, NaN, trimmedPart, null, remarks);
								operations.addItem(o);
							}
						}
					}
				} else if(StringUtil.isNotBlank(remarks)) {
					o = getUpdateRequestOperation(UpdateRequestOperation$Method.ADD, NaN, null, null, remarks);
					operations.addItem(o);
				}
				var req:UpdateRequest = new UpdateRequest();
				req.operations = operations;
				dataClient.updateActiveRecord(updateResponder, req);
			} else {
				super.applyValue();
			}
		}
		
		override public function applySymbolAndRemarks(symbol:FieldSymbol, remarks:String):void {
			if(_view.attributeDefinition.multiple) {
				var operations:ArrayCollection = new ArrayCollection();
				for each (var a:AttributeProxy in _view.attributes) {
					var value:String = codeAttributeToText(a);
					var o:UpdateRequestOperation = getUpdateRequestOperation(UpdateRequestOperation$Method.UPDATE, a.id, value, symbol, remarks);
					operations.addItem(o);
				}
				var req:UpdateRequest = new UpdateRequest();
				req.operations = operations;
				dataClient.updateActiveRecord(updateResponder, req);
			} else {
				super.applySymbolAndRemarks(symbol, remarks);
			}
		}
		
		override public function applySymbol(symbol:FieldSymbol):void {
			if(_view.attributeDefinition.multiple) {
				var operations:ArrayCollection = new ArrayCollection();
				var remarks:String = getRemarks();
				for each (var a:AttributeProxy in _view.attributes) {
					var value:String = codeAttributeToText(a);
					var o:UpdateRequestOperation = getUpdateRequestOperation(UpdateRequestOperation$Method.UPDATE, 
						a.id, value, symbol, remarks);
					operations.addItem(o);
				}
				var req:UpdateRequest = new UpdateRequest();
				req.operations = operations;
				dataClient.updateActiveRecord(updateResponder, req);
			} else {
				super.applySymbol(symbol);
			}
		}
		
		override protected function getRemarks():String {
			if(_view.attributeDefinition.multiple) {
				if(CollectionUtil.isNotEmpty(_view.attributes)) {
					var a:AttributeProxy = AttributeProxy(_view.attributes.getItemAt(0));
					var field:FieldProxy = FieldProxy(a.fields[0]);
					return field.remarks;
				}
			} else {
				return super.getRemarks();
			}
			return null;
		}
		
		protected function updateDescription():void {
			_view.description = "";
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
					
					dataClient.getCodeListItems(responder, parentEntityId, name, codes);
				}
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
