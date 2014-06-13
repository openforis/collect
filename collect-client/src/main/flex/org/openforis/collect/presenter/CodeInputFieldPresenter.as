package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.ui.Keyboard;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.core.FlexGlobals;
	import mx.core.IVisualElement;
	import mx.core.UIComponent;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.managers.IFocusManagerComponent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CodeListDialog;
	import org.openforis.collect.ui.component.input.CodeListDialogItemRenderer;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.ui.component.input.codelist.CodeListAllowedValuesPreviewDialog;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.ObjectUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
	import spark.components.CheckBox;
	
	/**
	 * 
	 * @author S. Ricci
	 * @author M. Togna
	 * */
	public class CodeInputFieldPresenter extends InputFieldPresenter {
		
		private static var _popUp:CodeListDialog;
		private static var _popUpOpened:Boolean;
		private static var _allowedValuesPreviewPopUp:CodeListAllowedValuesPreviewDialog;
		private static var _allowedValuesPreviewPopUpOpened:Boolean;
		private static var _lastLoadCodesAsyncToken:AsyncToken;
		
		private var _view:CodeInputField;
		private var _items:IList;
		
		{
			FlexGlobals.topLevelApplication.stage.addEventListener(MouseEvent.CLICK, globalClickHandler);
		}
		
		public static function globalClickHandler(event:MouseEvent):void {
			//if popup is opened and user clicks outside of it, close it
			var target:UIComponent = event.target as UIComponent;
			if ( _popUpOpened && target != null ) {
				var codeInputField:CodeInputField = _popUp.codeInputField;
				if ( ! UIUtil.hasStyleName(target, "openCodeListPopUpButton") 
					&& ! UIUtil.isDescendantOf(_popUp, target) 
					//&& target != codeInputField.textInput 
					) {
					closePopupHandler(null, false);
				}
			}
			if ( _allowedValuesPreviewPopUpOpened && target != null ) {
				if ( ! UIUtil.isDescendantOf(_allowedValuesPreviewPopUp, target) ) {
					closeAllowedValuesPreviewPopUp();
				}
			}
		}
		
		public function CodeInputFieldPresenter(view:CodeInputField) {
			_view = view;
			_view.fieldIndex = -1;
			initViewState();
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.openImage.addEventListener(MouseEvent.CLICK, openImageClickHandler);
			_view.openImage.addEventListener(KeyboardEvent.KEY_DOWN, openImageKeyDownHandler);
			_view.openImage.addEventListener(FocusEvent.KEY_FOCUS_CHANGE, preventDefaultHandler);
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
		internal static function closePopupHandler(event:Event = null, setFocusOnInputField:Boolean = true):void {
			if ( _popUpOpened ) {
				if ( setFocusOnInputField ) {
					var inputField:CodeInputField = _popUp.codeInputField;
					inputField.textInput.setFocus();
				}
				_popUpOpened = false;
				PopUpManager.removePopUp(_popUp);
			}
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
				_popUp.addEventListener(CloseEvent.CLOSE, popUpApplyHandler);
				_popUp.cancelLoading.addEventListener(MouseEvent.CLICK, cancelLoadingHandler);
				_popUp.addEventListener("apply", popUpApplyHandler);
				_popUp.addEventListener(KeyboardEvent.KEY_DOWN, popUpKeyDownHandler);
				//_popUp.cancelButton.addEventListener(MouseEvent.CLICK, closePopupHandler);
				//_popUp.addEventListener("selectionChange", popupItemSelectionChangeHandler);
			}
			_popUpOpened = true;
			_popUp.codeInputField = _view;
			_popUp.editable = Application.activeRecordEditable;
			_popUp.multiple = _view.attributeDefinition.multiple;
			_popUp.title = _view.attributeDefinition.getInstanceOrHeadingLabelText();
			
			PopUpManager.addPopUp(_popUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			
			_popUp.setFocus();
			
			_popUp.currentState = CodeListDialog.STATE_LOADING;

			PopUpManager.centerPopUp(_popUp);
			
			loadCodes(loadListDialogDataResultHandler);
		}
		
		protected function openAllowedValuesPreviewPopUp():void {
			if( _allowedValuesPreviewPopUp == null) {
				_allowedValuesPreviewPopUp = new CodeListAllowedValuesPreviewDialog();
				_allowedValuesPreviewPopUp.addEventListener(FlexEvent.STATE_CHANGE_COMPLETE, stateChangeHandler);
				
				//popup height not calculated properly immediately after state change
				//use of timer helps to align it better to the input field
				var alignmentTimer:Timer = new Timer(100, 1);
				alignmentTimer.addEventListener(TimerEvent.TIMER_COMPLETE, function(event:Event):void {
					PopUpUtil.alignToField(_allowedValuesPreviewPopUp, _allowedValuesPreviewPopUp.codeInputField.textInput, PopUpUtil.POSITION_ABOVE, PopUpUtil.VERTICAL_ALIGN_TOP, PopUpUtil.HORIZONTAL_ALIGN_LEFT);
				});
				
				function stateChangeHandler(event:Event):void {
					alignmentTimer.reset();
					alignmentTimer.start();
				}
			}
			_allowedValuesPreviewPopUp.codeInputField = _view;
			_allowedValuesPreviewPopUp.currentState = CodeListAllowedValuesPreviewDialog.STATE_LOADING;
			
			if ( ! _allowedValuesPreviewPopUpOpened ) {
				PopUpManager.addPopUp(_allowedValuesPreviewPopUp, FlexGlobals.topLevelApplication as DisplayObject, false);
				_allowedValuesPreviewPopUpOpened = true;
				_view.textInput.setFocus();
			}
	
			function loadCodesResultHandler(event:ResultEvent, token:Object = null):void {
				var data:IList = event.result as IList;
				_allowedValuesPreviewPopUp.items = data;
				_allowedValuesPreviewPopUp.currentState = CodeListAllowedValuesPreviewDialog.STATE_DEFAULT;
			}

			loadCodes(loadCodesResultHandler);
		}
		
		protected static function closeAllowedValuesPreviewPopUp():void {
			if ( _allowedValuesPreviewPopUpOpened ) {
				_allowedValuesPreviewPopUpOpened = false;
				PopUpManager.removePopUp(_allowedValuesPreviewPopUp);
			}
		}
		
		protected function loadCodes(resultHandler:Function):void {
			var codeAttributeDef:CodeAttributeDefinitionProxy = _view.attributeDefinition as CodeAttributeDefinitionProxy;
			var attribute:String = codeAttributeDef.name;
			var parentEntityId:int = _view.parentEntity.id;
			var responder:IResponder = new AsyncResponder(resultHandler, faultHandler);
			_lastLoadCodesAsyncToken = dataClient.findAssignableCodeListItems(responder, parentEntityId, attribute);
		}
		
		protected function loadListDialogDataResultHandler(event:ResultEvent, token:Object = null):void {
			var data:IList = event.result as IList;
			
			var selectedItems:ArrayCollection;
			var notSelectedItems:ArrayCollection;
			if ( data != null ) {
				selectedItems = new ArrayCollection();
				for each (var item:CodeListItemProxy in data) {
					if (item.selected) {
						selectedItems.addItem(item);
					}
				}
				notSelectedItems = new ArrayCollection(data.toArray());
				notSelectedItems.filterFunction = function(item:CodeListItemProxy):Boolean {
					return ! (item.selected);
				}
				notSelectedItems.refresh();
			} else {
				selectedItems = notSelectedItems = new ArrayCollection();
			}
			_popUp.items = data;
			_popUp.selectedItems = selectedItems;
			_popUp.notSelectedItems = notSelectedItems;
			
			var codeAttributeDef:CodeAttributeDefinitionProxy = _view.attributeDefinition as CodeAttributeDefinitionProxy;
			if ( codeAttributeDef.allowValuesSorting ) {
				_popUp.currentState = CodeListDialog.STATE_VALUES_SORTING_ALLOWED;
			} else {
				_popUp.currentState = CodeListDialog.STATE_DEFAULT;
			}
			
			PopUpManager.centerPopUp(_popUp);
			
			_popUp.setFocus();
		}

		protected function popUpKeyDownHandler(event:KeyboardEvent):void {
			if (event.keyCode == Keyboard.ESCAPE) {
				closePopupHandler();
			}
		}
		
		protected function popupItemSelectionChangeHandler(event:Event):void {
			var selectedItems:IList = _popUp.selectedItems;
			applySelection(selectedItems);
		}

		protected static function applySelection(selectedItems:IList):void {
			var parts:Array = new Array();
			for each (var item:CodeListItemProxy in selectedItems ) { 
				var codeStr:String = StringUtil.concat(": ", item.code, item.qualifier);
				parts.push(codeStr);
			}
			var inputFieldText:String = StringUtil.concat(", ", parts);
			TextInput(_popUp.codeInputField.textInput).text = inputFieldText;
			_popUp.codeInputField.presenter.updateValue();
		}
		
		protected static function popUpApplyHandler(event:Event):void {
			var selectedItems:IList = _popUp.selectedItems;
			applySelection(selectedItems);
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
			if ( ! CodeAttributeDefinitionProxy(_view.attributeDefinition).external && _view.attribute != null &&
					_view.attributeDefinition.parentLayout == UIUtil.LAYOUT_FORM) {
				var codes:Array = [];
				var attribute:AttributeProxy = _view.attribute;
				var code:String = attribute.getField(0).value as String;
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
		
		override protected function keyDownHandler(event:KeyboardEvent):void {
			if ( event.keyCode == Keyboard.TAB && ! event.shiftKey ) {
				preventDefaultHandler(event);
				closeAllowedValuesPreviewPopUp();
				_view.openImage.setFocus();
				//dispatchFocusInEvent();
			} else {
				super.keyDownHandler(event);
			}
		}

		protected function openImageKeyDownHandler(event:KeyboardEvent):void {
			if ( event.keyCode == Keyboard.TAB && event.shiftKey ) {
				preventDefaultHandler(event);
				
				_view.textInput.setFocus();
			} else {
				super.keyDownHandler(event);
			}
		}
		
		override protected function focusInHandler(event:FocusEvent):void {
			super.focusInHandler(event);
			var attrDefn:CodeAttributeDefinitionProxy = CodeAttributeDefinitionProxy(_view.attributeDefinition);
			if ( attrDefn.showAllowedValuesPreview && ! _popUpOpened && ! _allowedValuesPreviewPopUpOpened && ! attrDefn.external ) {
				openAllowedValuesPreviewPopUp();
			}
		}
		
		override protected function focusOutHandler(event:FocusEvent):void {
			super.focusOutHandler(event);
			
			var focussedElement:IFocusManagerComponent = FlexGlobals.topLevelApplication.focusManager.getFocus();
			if ( _allowedValuesPreviewPopUpOpened && _view == _allowedValuesPreviewPopUp.codeInputField ) {
				closeAllowedValuesPreviewPopUp();
			}
		}

		override protected function moveFocusOnNextField(horizontalMove:Boolean, offset:int):Boolean {
			closeAllowedValuesPreviewPopUp();
			return super.moveFocusOnNextField(horizontalMove, offset);
		}
		
	}
}
