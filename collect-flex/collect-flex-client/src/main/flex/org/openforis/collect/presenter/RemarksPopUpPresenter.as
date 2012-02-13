package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	import flash.ui.Keyboard;
	
	import mx.events.FlexEvent;
	import mx.events.FlexMouseEvent;
	import mx.managers.PopUpManager;
	
	import org.openforis.collect.event.RemarksPopUpEvent;
	import org.openforis.collect.model.proxy.AttributeSymbol;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.RemarksPopUp;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.components.RadioButton;
	
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
			//popUp.showReasonBlank = inputField.canShowReasonBlankOnPopUp() && PhaseUtil.currentPhaseCode == PhaseUtil.DATA_ENTRY_CODE;
			
			if(! popUpOpened) {
				this._inputField = inputField;
				PopUpManager.addPopUp(view, inputField);
				
				if(firstOpen) {
					//init popup only after rendering to avoid null pointer exception accessing null objects
					initPopUp();
				}
			}
			setValuesInView();

			var alignmentPoint:Point;
			if(alignToField) {
				PopUpUtil.alignPopUpToField(view, inputField, PopUpUtil.POSITION_RIGHT, PopUpUtil.VERTICAL_ALIGN_BOTTOM);
			} else if(alignmentPoint) {
				PopUpUtil.alignPop(view, alignmentPoint);
			} else {
				//align popup to mouse pointer
				PopUpUtil.alignPopUpToMousePoint(view, -10, -10);
			}
			
			popUpOpened = true;
		}
		
		protected function setValuesInView():void {
			var remarks:String = null;
			var symbolToSelect:AttributeSymbol = null;
			if(_inputField != null && _inputField.attribute != null) {
				remarks = _inputField.attribute.remarks;
				var symbol:AttributeSymbol = _inputField.attribute.symbol;
				if(symbol != null) {
					switch(symbol) {
						case AttributeSymbol.BLANK_ON_FORM:
						case AttributeSymbol.DASH_ON_FORM:
						case AttributeSymbol.ILLEGIBLE:
							symbolToSelect = symbol;
							break;
					}
				}
			}
			view.currentState = _inputField != null && _inputField.isEmpty() ? 
				RemarksPopUp.STATE_CAN_SPECIFY_REASON_BLANK: RemarksPopUp.STATE_DEFAULT;
			view.remarksTextArea.text = remarks;
			view.radioButtonGroup.selectedValue = symbolToSelect;
		}
		
		public function hidePopUp():void {
			PopUpManager.removePopUp(view);
			popUpOpened = false;
		}
		
		protected function okButtonClickHandler(event:Event = null):void {
			var symbol:AttributeSymbol = view.radioButtonGroup.selectedValue as AttributeSymbol;
			var remarks:String = StringUtil.trim(view.remarksTextArea.text);
			_inputField.changeSymbol(symbol, remarks);
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
			if(_showReasonBlank) {
				if(view.blankOnFormRadioButton) {
					view.blankOnFormRadioButton.setFocus();
				}
			} else {
				if(view.remarksGroup) {
					view.remarksTextArea.setFocus();
				}
			}
		}
	}
}