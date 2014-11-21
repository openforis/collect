package org.openforis.collect.presenter
{
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
	import mx.core.UIComponent;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.managers.IFocusManagerComponent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.ui.component.input.CodeListDialog;
	import org.openforis.collect.ui.component.input.TextCodeInputField;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.ui.component.input.codelist.CodeListAllowedValuesPreviewDialog;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * @author S. Ricci
	 */
	public class TextCodeInputFieldPresenter extends CodeInputFieldPresenter {
		
		private static var _popUp:CodeListDialog;
		private static var _popUpOpened:Boolean;
		private static var _allowedValuesPreviewPopUp:CodeListAllowedValuesPreviewDialog;
		private static var _allowedValuesPreviewPopUpOpened:Boolean;
		
		private var _items:IList;
		
		{
			FlexGlobals.topLevelApplication.stage.addEventListener(MouseEvent.MOUSE_DOWN, globalMouseDownHandler);
		}
		
		public static function globalMouseDownHandler(event:MouseEvent):void {
			//if popup is opened and user clicks outside of it, close it
			var target:DisplayObject = event.target as DisplayObject;
			if ( target != null ) {
				var codeInputField:TextCodeInputField;
				if ( _popUpOpened ) {
					codeInputField = _popUp.codeInputField;
					if ( ! ( ( target is UIComponent && UIUtil.hasStyleName(UIComponent(target), "openCodeListPopUpButton") ) 
						|| UIUtil.isDescendantOf(_popUp, target) ) 
						//&& target != codeInputField.textInput 
					) {
						popUpCloseHandler(null, false);
					}
				}
				if ( _allowedValuesPreviewPopUpOpened ) {
					codeInputField = _allowedValuesPreviewPopUp.codeInputField;
					var clickIsInsideAllowedValuesPopUp:Boolean = UIUtil.isDescendantOf(_allowedValuesPreviewPopUp, target);
					var clickIsInsideCodeInputField:Boolean = UIUtil.isDescendantOf(codeInputField.textInput, target);
					if ( ! ( clickIsInsideAllowedValuesPopUp || clickIsInsideCodeInputField ) ) {
						closeAllowedValuesPreviewPopUp();
					}
				}
			}
		}

		public function TextCodeInputFieldPresenter(view:TextCodeInputField) {
			super(view);
		}
		
		private function get view():TextCodeInputField {
			return TextCodeInputField(_view);
		}
		
		override public function init():void {
			super.init();
			initViewState();
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.openImage.addEventListener(MouseEvent.CLICK, openImageClickHandler);
			view.openImage.addEventListener(KeyboardEvent.KEY_DOWN, openImageKeyDownHandler);
			view.openImage.addEventListener(FocusEvent.KEY_FOCUS_CHANGE, preventDefaultHandler);
		}
		
		protected function initViewState():void {
			if(view.attributeDefinition.parentLayout == UIUtil.LAYOUT_TABLE) {
				view.currentState = TextCodeInputField.STATE_DEFAULT;
			} else {
				view.currentState = TextCodeInputField.STATE_DESCRIPTION_VISIBLE;
			}
		}
		
		override protected function updateView():void {
			super.updateView();
			updateDescription();
		}
		
		/**
		 * Close the popup
		 * */
		internal static function popUpCloseHandler(event:Event = null, setFocusOnInputField:Boolean = true):void {
			if ( _popUpOpened ) {
				if ( setFocusOnInputField ) {
					var inputField:TextCodeInputField = _popUp.codeInputField;
					inputField.textInput.setFocus();
				}
				_popUpOpened = false;
				PopUpManager.removePopUp(_popUp);
				resetPopUp();
			}
		}
		
		internal static function cancelLoadingHandler(event:Event):void {
			//todo cancel async request
			//if(_lastLoadCodesAsyncToken != null) {
			//}
			popUpCloseHandler();
		}
		
		/**
		 * Open the popup
		 * */
		protected function openImageClickHandler(event:Event):void {
			openCodeListDialog(view);
		}
		
		protected static function openCodeListDialog(view:TextCodeInputField):void {
			if(_popUp == null) {
				_popUp = new CodeListDialog();
				_popUp.addEventListener(CloseEvent.CLOSE, popUpCloseHandler);
				_popUp.cancelLoading.addEventListener(MouseEvent.CLICK, cancelLoadingHandler);
				_popUp.addEventListener("apply", popUpApplyHandler);
				_popUp.addEventListener(KeyboardEvent.KEY_DOWN, popUpKeyDownHandler);
				
				function popUpKeyDownHandler(event:KeyboardEvent):void {
					if (event.keyCode == Keyboard.ESCAPE) {
						popUpCloseHandler();
					}
				}
			}
			_popUpOpened = true;
			_popUp.codeInputField = view;
			_popUp.editable = view.editable;
			_popUp.multiple = view.attributeDefinition.multiple;
			_popUp.title = view.attributeDefinition.getInstanceOrHeadingLabelText();
			
			PopUpManager.addPopUp(_popUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			
			_popUp.setFocus();
			
			_popUp.currentState = CodeListDialog.STATE_LOADING;

			PopUpManager.centerPopUp(_popUp);
			
			CodeInputFieldPresenter(view.presenter).loadCodes(view, loadListDialogDataResultHandler);
		}
		
		private static function resetPopUp():void {
			_popUp.items = null;
			_popUp.selectedItems = null;
			_popUp.notSelectedItems = null;
		}
		
		protected static function openAllowedValuesPreviewPopUp(view:TextCodeInputField):void {
			if( _allowedValuesPreviewPopUp == null) {
				_allowedValuesPreviewPopUp = new CodeListAllowedValuesPreviewDialog();
				_allowedValuesPreviewPopUp.addEventListener(FlexEvent.STATE_CHANGE_COMPLETE, stateChangeHandler);
				
				//popup height not calculated properly immediately after state change
				//use of timer helps to align it better to the input field
				var alignmentTimer:Timer = new Timer(100, 1);
				alignmentTimer.addEventListener(TimerEvent.TIMER_COMPLETE, function(event:Event):void {
					PopUpUtil.alignToField(_allowedValuesPreviewPopUp, _allowedValuesPreviewPopUp.codeInputField.textInput, 
						PopUpUtil.POSITION_ABOVE, PopUpUtil.VERTICAL_ALIGN_TOP, PopUpUtil.HORIZONTAL_ALIGN_LEFT);
				});
				
				function stateChangeHandler(event:Event):void {
					alignmentTimer.reset();
					alignmentTimer.start();
				}
			}
			_allowedValuesPreviewPopUp.codeInputField = view;
			_allowedValuesPreviewPopUp.currentState = CodeListAllowedValuesPreviewDialog.STATE_LOADING;
			
			if ( ! _allowedValuesPreviewPopUpOpened ) {
				//FlexGlobals.topLevelApplication as DisplayObject
				PopUpManager.addPopUp(_allowedValuesPreviewPopUp, view.textInput, false);
				_allowedValuesPreviewPopUpOpened = true;
				view.textInput.setFocus();
			}
			
			function loadCodesResultHandler(event:ResultEvent, token:Object = null):void {
				var data:IList = event.result as IList;
				_allowedValuesPreviewPopUp.items = data;
				_allowedValuesPreviewPopUp.currentState = CodeListAllowedValuesPreviewDialog.STATE_DEFAULT;
			}
			
			CodeInputFieldPresenter(view.presenter).loadCodes(view, loadCodesResultHandler);
		}
		
		protected static function closeAllowedValuesPreviewPopUp():void {
			if ( _allowedValuesPreviewPopUpOpened ) {
				_allowedValuesPreviewPopUpOpened = false;
				PopUpManager.removePopUp(_allowedValuesPreviewPopUp);
			}
		}
		
		protected static function loadListDialogDataResultHandler(event:ResultEvent, token:Object = null):void {
			var data:IList = event.result as IList;
			
			var selectedItems:ArrayCollection;
			var notSelectedItems:ArrayCollection;
			if ( data == null ) {
				selectedItems = notSelectedItems = new ArrayCollection();
			} else {
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
			}
			_popUp.items = data;
			_popUp.selectedItems = selectedItems;
			_popUp.notSelectedItems = notSelectedItems;
			
			var codeAttributeDef:CodeAttributeDefinitionProxy = _popUp.codeInputField.attributeDefinition as CodeAttributeDefinitionProxy;
			if ( codeAttributeDef.allowValuesSorting ) {
				_popUp.currentState = CodeListDialog.STATE_VALUES_SORTING_ALLOWED;
			} else {
				_popUp.currentState = CodeListDialog.STATE_DEFAULT;
			}
			
			PopUpManager.centerPopUp(_popUp);
			
			_popUp.setFocus();
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
		
		protected static function popUpApplyHandler(event:Event, setFocusOnInputField:Boolean = true):void {
			var selectedItems:IList = _popUp.selectedItems;
			applySelection(selectedItems);
			popUpCloseHandler(null, setFocusOnInputField);
		}
		
		protected function updateDescription():void {
			view.description = "";
			if ( ! CodeAttributeDefinitionProxy(view.attributeDefinition).external && view.attribute != null &&
				view.attributeDefinition.parentLayout == UIUtil.LAYOUT_FORM) {
				var codes:Array = [];
				var attribute:AttributeProxy = view.attribute;
				var code:String = attribute.getField(0).value as String;
				if( StringUtil.isNotBlank(code)) {
					codes.push(code);
				}
				if(ArrayUtil.isNotEmpty(codes)) {
					var parentEntityId:int = view.parentEntity.id;
					var name:String = view.attributeDefinition.name;
					var responder:IResponder = new AsyncResponder(findItemsResultHandler, faultHandler);
					
					dataClient.getCodeListItems(responder, parentEntityId, name, codes);
				}
			}
		}
		
		protected function findItemsResultHandler(event:ResultEvent, token:Object = null):void {
			_items = event.result as IList;
			view.description = getDescription();
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
				if ( view.openImage.visible ) {
					view.openImage.setFocus();
				} else {
					super.keyDownHandler(event);
				}
			} else {
				super.keyDownHandler(event);
			}
		}
		
		protected function openImageKeyDownHandler(event:KeyboardEvent):void {
			if ( event.keyCode == Keyboard.TAB && event.shiftKey ) {
				preventDefaultHandler(event);
				
				view.textInput.setFocus();
			} else {
				super.keyDownHandler(event);
			}
		}
		
		override protected function focusInHandler(event:FocusEvent):void {
			super.focusInHandler(event);
			var attrDefn:CodeAttributeDefinitionProxy = CodeAttributeDefinitionProxy(view.attributeDefinition);
			if ( attrDefn.showAllowedValuesPreview && ! _popUpOpened && ! _allowedValuesPreviewPopUpOpened && ! attrDefn.external ) {
				openAllowedValuesPreviewPopUp(view);
			}
		}
		
		override protected function focusOutHandler(event:FocusEvent):void {
			super.focusOutHandler(event);
			var focussedElement:IFocusManagerComponent = FlexGlobals.topLevelApplication.focusManager.getFocus();
			if ( _allowedValuesPreviewPopUpOpened && view == _allowedValuesPreviewPopUp.codeInputField ) {
				//closeAllowedValuesPreviewPopUp();
			}
		}
		
		override protected function moveFocusOnNextField(horizontalMove:Boolean, offset:int):Boolean {
			closeAllowedValuesPreviewPopUp();
			return super.moveFocusOnNextField(horizontalMove, offset);
		}
		
	}
}