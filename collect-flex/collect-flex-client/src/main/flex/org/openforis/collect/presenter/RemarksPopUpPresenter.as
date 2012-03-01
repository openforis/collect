package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	import flash.ui.Keyboard;
	
	import mx.events.FlexEvent;
	import mx.events.FlexMouseEvent;
	import mx.managers.PopUpManager;
	
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.RemarksPopUp;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.components.RadioButton;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class RemarksPopUpPresenter {
		
		private var lastSelectedRadioButton:RadioButton = null;
		private var popUpOpened:Boolean = false;
		
		private var view:RemarksPopUp;
		private var _inputField:InputField;
		
		[Bindable]
		private var _showReasonBlank:Boolean = true;
		
		public function RemarksPopUpPresenter() {
		}
		
		protected function initPopUp():void {
			//init event listeners
			view.reasonBlankGroup.addEventListener(FlexEvent.CREATION_COMPLETE, reasonBlankGroupCreationCompleteHandler);
			
			view.blankOnFormRadioButton.addEventListener(MouseEvent.CLICK, radioButtonClickHandler);
			view.dashOnFormRadioButton.addEventListener(MouseEvent.CLICK, radioButtonClickHandler);
			view.illegibleRadioButton.addEventListener(MouseEvent.CLICK, radioButtonClickHandler);
			view.blankOnFormRadioButton.addEventListener(KeyboardEvent.KEY_DOWN, radioButtonsKeyDownHandler);
			view.dashOnFormRadioButton.addEventListener(KeyboardEvent.KEY_DOWN, radioButtonsKeyDownHandler);
			view.illegibleRadioButton.addEventListener(KeyboardEvent.KEY_DOWN, radioButtonsKeyDownHandler);
			view.remarksTextArea.addEventListener(KeyboardEvent.KEY_DOWN, remarksTextAreaKeyDownHandler);
			view.okButton.addEventListener(MouseEvent.CLICK, okButtonClickHandler);
			view.addEventListener(FlexMouseEvent.MOUSE_DOWN_OUTSIDE, mouseDownOutsideHandler);
		}
		
		protected function radioButtonClickHandler(event:MouseEvent):void {
			//allows the deselection of a selected radio button
			var radioButton:RadioButton = event.target as RadioButton;
			if(lastSelectedRadioButton == radioButton) {
				radioButton.selected = false;
				lastSelectedRadioButton = null;
			} else {
				lastSelectedRadioButton = radioButton;	
			}
		}
		
		protected function remarksTextAreaKeyDownHandler(event:KeyboardEvent):void {
			switch(event.keyCode) {
				case Keyboard.TAB:
					//save();
					break;
				case Keyboard.ESCAPE:
					hidePopUp();
					break;
			}
		}
		
		protected function mouseDownOutsideHandler(event:FlexMouseEvent):void {
			okButtonClickHandler();
		}
		
		protected function updateView():void {
			view.currentState = calculateCurrentState();
		}
		
		protected function calculateCurrentState():String {
			if(_showReasonBlank) {
				return "canSpecifyReasonBlank";
			} else {
				return "default";
			}
		}
		
		public function openPopUp(inputField:InputField, alignToField:Boolean = false, alignmentPoint:Point = null):void {
			var firstOpen:Boolean = (view == null);
			if(firstOpen) {
				view = new RemarksPopUp();
			}
			_showReasonBlank = _inputField != null && (_inputField.canApplyReasonBlank() ||	_inputField.hasBlankReasonSpecified());
			_inputField = inputField;

			if(! popUpOpened) {
				PopUpManager.addPopUp(view, inputField);
				
				if(firstOpen) {
					//init popup only after rendering to avoid null pointer exception accessing null objects
					initPopUp();
				}
			}
			setValuesInView();

			var alignmentPoint:Point;
			if(alignToField) {
				PopUpUtil.alignToField(view, inputField.validationStateDisplay, PopUpUtil.POSITION_RIGHT, PopUpUtil.VERTICAL_ALIGN_BOTTOM);
			} else if(alignmentPoint) {
				PopUpUtil.alignToPoint(view, alignmentPoint);
			} else {
				//align popup to mouse pointer
				PopUpUtil.alignToMousePoint(view, -10, -10);
			}
			
			popUpOpened = true;
		}
		
		protected function setValuesInView():void {
			var remarks:String = null;
			var symbolToSelect:FieldSymbol = null;
			if(_inputField != null && _inputField.attribute != null) {
				var fieldIndex:int = 0;
				if(_inputField.fieldIndex > 0) {
					fieldIndex = _inputField.fieldIndex;
				}
				var field:FieldProxy = _inputField.attribute.getField(fieldIndex);
				remarks = field.remarks;
				var symbol:FieldSymbol = field.symbol;
				if(symbol != null) {
					switch(symbol) {
						case FieldSymbol.BLANK_ON_FORM:
						case FieldSymbol.DASH_ON_FORM:
						case FieldSymbol.ILLEGIBLE:
							symbolToSelect = symbol;
							break;
					}
				}
			}
			if(_showReasonBlank) {
				view.currentState = RemarksPopUp.STATE_CAN_SPECIFY_REASON_BLANK;
			} else {
				view.currentState = RemarksPopUp.STATE_DEFAULT;
			}
			view.remarksTextArea.text = remarks;
			view.radioButtonGroup.selectedValue = symbolToSelect;
			setFocusOnFirstField();
		}
		
		public function hidePopUp():void {
			PopUpManager.removePopUp(view);
			popUpOpened = false;
			if(_inputField.textInput != null) {
				_inputField.textInput.setFocus();
			}
		}
		
		protected function okButtonClickHandler(event:Event = null):void {
			var symbol:FieldSymbol = view.radioButtonGroup.selectedValue as FieldSymbol;
			var remarks:String = StringUtil.trim(view.remarksTextArea.text);
			_inputField.applySymbolAndRemarks(symbol, remarks);
			hidePopUp();
		}
		
		protected function cancelHandler(event:Event):void {
			hidePopUp();
		}
		
		protected function radioButtonsKeyDownHandler(event:KeyboardEvent):void {
			switch(event.keyCode) {
				case Keyboard.ENTER:
					okButtonClickHandler();
					break;
				case Keyboard.ESCAPE:
					cancelHandler(null);
			}
			/*
			var symbol:AbstractValue$Symbol = null;
			switch(event.charCode) {
				case 42: //asterisk (*)
					symbol = AbstractValue$Symbol.BLANK_ON_FORM;
					break;
				case 45: //dash (-)
					symbol = AbstractValue$Symbol.DASH_ON_FORM;
					break;
				case 63: //question mark (?)
					symbol = AbstractValue$Symbol.ILLEGIBLE;
					break;
			}
			*/
			//if(symbol != null) {
				/*
				radioButtonGroup.selectedValue = symbol;
				radioButtonGroup.selection.setFocus();
				*/
			//}
		}
		
		protected function reasonBlankGroupCreationCompleteHandler(event:FlexEvent):void {
			setFocusOnFirstField();
		}
		
		public function setFocusOnFirstField():void {
			if(_showReasonBlank && view.blankOnFormRadioButton != null) {
				view.blankOnFormRadioButton.setFocus();
			} else if(view.remarksGroup != null) {
				view.remarksTextArea.setFocus();
			}
		}
	}
}