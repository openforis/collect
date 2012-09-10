package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.ui.Keyboard;
	
	import mx.collections.IList;
	import mx.core.FlexGlobals;
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CodeListDialog;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * @author M. Togna
	 * */
	public class CodeInputFieldPresenter extends InputFieldPresenter {
		
		private static var _popUp:CodeListDialog;
		private static var _lastLoadCodesAsyncToken:AsyncToken;
		private var _view:CodeInputField;
		private var _items:IList;
		
		
		public function CodeInputFieldPresenter(view:CodeInputField) {
			_view = view;
			_view.fieldIndex = -1;
			initViewState();
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.openImage.addEventListener(MouseEvent.CLICK, openImageClickHandler);
		}
		
		protected function initViewState():void {
			if(_view.attributeDefinition.parentLayout == UIUtil.LAYOUT_TABLE) {
				_view.currentState = CodeInputField.STATE_DEFAULT;
			} else {
				_view.currentState = CodeInputField.STATE_DESCRIPTION_VISIBLE;
			}
		}
		
		/**
		 * Close the popup
		 * */
		internal static function closePopupHandler(event:Event = null):void {
			var inputField:CodeInputField = _popUp.codeInputField;
			PopUpManager.removePopUp(_popUp);
			inputField.textInput.setFocus();
		}
		
		internal static function cancelLoadingHandler(event:Event):void {
			if(_lastLoadCodesAsyncToken != null) {
				//todo cancel async request
			}
			closePopupHandler();
		}
		
		/**
		 * Open the popup
		 * */
		protected function openImageClickHandler(event:Event):void {
			if(_popUp == null) {
				_popUp = new CodeListDialog();
				_popUp.addEventListener(CloseEvent.CLOSE, closePopupHandler);
				_popUp.cancelLoading.addEventListener(MouseEvent.CLICK, cancelLoadingHandler);
				_popUp.cancelButton.addEventListener(MouseEvent.CLICK, closePopupHandler);
				_popUp.applyButton.addEventListener(MouseEvent.CLICK, applyButtonClickHandler);
				_popUp.addEventListener(KeyboardEvent.KEY_DOWN, popUpKeyDownHandler);
			}
			PopUpManager.addPopUp(_popUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			_popUp.editable = Application.activeRecordEditable;
			_popUp.multiple = _view.attributeDefinition.multiple;
			_popUp.maxSpecified = _view.attributeDefinition.maxCount;
			_popUp.title = _view.attributeDefinition.getLabelText();
			_popUp.codeInputField = _view;
			_popUp.setFocus();
			loadCodes();
		}
		
		protected function loadCodes():void {
			_popUp.currentState = CodeListDialog.STATE_LOADING;
			PopUpManager.centerPopUp(_popUp);
			
			var codeAttributeDef:CodeAttributeDefinitionProxy = _view.attributeDefinition as CodeAttributeDefinitionProxy;
			var attribute:String = codeAttributeDef.name;
			var parentEntityId:int = _view.parentEntity.id;
			var responder:IResponder = new AsyncResponder(loadListDialogDataResultHandler, faultHandler);
			_lastLoadCodesAsyncToken = dataClient.findAssignableCodeListItems(responder, parentEntityId, attribute);
		}
		
		protected function loadListDialogDataResultHandler(event:ResultEvent, token:Object = null):void {
			var data:IList = event.result as IList;
			_popUp.dataGroup.dataProvider = data;
			_popUp.currentState = CodeListDialog.STATE_DEFAULT;
			PopUpManager.centerPopUp(_popUp);
		}

		protected function popUpKeyDownHandler(event:KeyboardEvent):void {
			if (event.keyCode == Keyboard.ESCAPE) {
				closePopupHandler();
			}
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
			_popUp.codeInputField.presenter.updateValue();
			closePopupHandler();
		}
		
		override protected function getTextFromValue():String {
			if(_view.attributeDefinition != null) {
				return codeAttributeToText(_view.attribute);
			}
			return "";
		}
		
		protected function codeAttributeToText(attribute:AttributeProxy):String {
			if(attribute != null) {
				var field:FieldProxy = attribute.getField(0);
				if(field.symbol != null) {
					var shortCut:String = FieldProxy.getShortCutForReasonBlank(field.symbol);
					if(shortCut != null) {
						return shortCut;
					}
				}
				var code:String = field.value as String;
				var qualifierField:FieldProxy = attribute.getField(1);
				var qualifier:String = qualifierField.value as String;
				return StringUtil.concat(": ", code, qualifier);
			}
			return "";
		}
		
		override protected function updateView():void {
			super.updateView();
			updateDescription();
		}
		
		protected function updateDescription():void {
			_view.description = "";
			if(_view.attribute != null) {
				var codes:Array = [];
				var code:String;
				var attribute:AttributeProxy;
				attribute = _view.attribute;
				code = attribute.getField(0).value as String;
				if( StringUtil.isNotBlank(code)) {
					codes.push(code);
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
					var part:String = item.getLabelText();
					parts.push(part);
				}
				description = StringUtil.concat("\n", parts);
			}
			return description;
		}
	}
}
