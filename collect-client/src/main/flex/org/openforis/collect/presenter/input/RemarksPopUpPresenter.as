package org.openforis.collect.presenter.input
{
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	import flash.ui.Keyboard;
	
	import mx.events.FlexEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncToken;
	
	import org.openforis.collect.event.input.RemarksPopUpEvent;
	import org.openforis.collect.idm.model.impl.AbstractValue$Symbol;
	import org.openforis.collect.ui.component.detail.input.InputField;
	import org.openforis.collect.ui.component.detail.input.RemarksPopUp;
	import org.openforis.collect.util.PopUpUtil;
	
	import spark.components.RadioButton;
	
	public class RemarksPopUpPresenter {
		
		private var lastSelectedRadioButton:RadioButton = null;
		private var popUpOpened:Boolean = false;
		
		private var popUp:RemarksPopUp;

		[Bindable]
		private var _showReasonBlank:Boolean = true;
		
		public function RemarksPopUpPresenter(popUp:RemarksPopUp = null) {
			this.popUp = popUp;
			
			initPopUp();
		}
		
		protected function initPopUp():void {
			if(this.popUp != null) {
				popUp.reasonBlankGroup.addEventListener(FlexEvent.CREATION_COMPLETE, reasonBlankGroupCreationCompleteHandler);
				
				popUp.blankOnFormRadioButton.addEventListener(MouseEvent.CLICK, radioButtonClickHandler);
				popUp.dashOnFormRadioButton.addEventListener(MouseEvent.CLICK, radioButtonClickHandler);
				popUp.illegibleRadioButton.addEventListener(MouseEvent.CLICK, radioButtonClickHandler);
				popUp.blankOnFormRadioButton.addEventListener(KeyboardEvent.KEY_DOWN, radioButtonsKeyDownHandler);
				popUp.dashOnFormRadioButton.addEventListener(KeyboardEvent.KEY_DOWN, radioButtonsKeyDownHandler);
				popUp.illegibleRadioButton.addEventListener(KeyboardEvent.KEY_DOWN, radioButtonsKeyDownHandler);
				
				popUp.addEventListener(RemarksPopUpEvent.SAVE, saveHandler);
				popUp.addEventListener(RemarksPopUpEvent.CANCEL, cancelHandler);
			}
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
		
		public function openPopUp(inputField:InputField, alignToField:Boolean = false, alignmentPoint:Point = null):void {
			if(popUp == null) {
				//init popup
				popUp = new RemarksPopUp();
				initPopUp();
			}
			popUp.reset();
			//popUp.showReasonBlank = inputField.canShowReasonBlankOnPopUp() && PhaseUtil.currentPhaseCode == PhaseUtil.DATA_ENTRY_CODE;
			
			if(! popUpOpened) {
				PopUpManager.addPopUp(popUp, inputField);
			}
			//popUp.callLater(function():void {popUp.setFocusOnFirstField();});
			
			var alignmentPoint:Point;
			if(alignToField) {
				PopUpUtil.alignPopUpToField(popUp, inputField, PopUpUtil.POSITION_RIGHT, PopUpUtil.VERTICAL_ALIGN_BOTTOM);
			} else if(alignmentPoint) {
				PopUpUtil.alignPop(popUp, alignmentPoint);
			} else {
				//align popup to mouse pointer
				PopUpUtil.alignPopUpToMousePoint(popUp, -10, -10);
			}
			
			popUpOpened = true;
		}
		
		public function hidePopUp():void {
			PopUpManager.removePopUp(popUp);
			popUpOpened = false;
		}
		
		protected function saveHandler(event:Event = null):void {
			//TODO get data and store it...
			
			hidePopUp();
		}
		
		protected function cancelHandler(event:Event):void {
			hidePopUp();
		}
		
		protected function radioButtonsKeyDownHandler(event:KeyboardEvent):void {
			switch(event.keyCode) {
				case Keyboard.ENTER:
					saveHandler();
					break;
				case Keyboard.ESCAPE:
					cancelHandler(null);
			}
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
			if(symbol != null) {
				/*
				radioButtonGroup.selectedValue = symbol;
				radioButtonGroup.selection.setFocus();
				*/
			}
		}
		
		protected function reasonBlankGroupCreationCompleteHandler(event:FlexEvent):void {
			setFocusOnFirstField();
		}
		
		public function setFocusOnFirstField():void {
			if(_showReasonBlank) {
				if(popUp.blankOnFormRadioButton) {
					popUp.blankOnFormRadioButton.setFocus();
				}
			} else {
				if(popUp.remarksGroup) {
					popUp.remarksTextArea.setFocus();
				}
			}
		}
	}
}