package org.openforis.collect.presenter.input
{
	import flash.events.MouseEvent;
	
	import mx.events.FlexEvent;
	import mx.rpc.AsyncToken;
	
	import org.openforis.collect.event.input.RemarksPopUpEvent;
	import org.openforis.collect.ui.component.detail.input.RemarksPopUp;
	
	import spark.components.RadioButton;
	
	public class RemarksPopUpPresenter {
	{
		private var lastSelectedRadioButton:RadioButton = null;
		private var popUpOpened:Boolean = false;
		
		private static var popUp:RemarksPopUp;
		//init static variables
		{
			//init extra info popup
			popUp = new RemarksPopUp();
			popUp.addEventListener(RemarksPopUpEvent.SAVE, saveHandler);
			popUp.addEventListener(RemarksPopUpEvent.CANCEL, cancelHandler);
		}

		public function RemarksPopUpPresenter(popUp:RemarksPopUp) {
			this.popUp = popUp;
		}
		
		
		protected function radioButtonClickHandler(event:MouseEvent):void {
			var radioButton:RadioButton = event.target as RadioButton;
			if(lastSelectedRadioButton == radioButton) {
				radioButton.selected = false;
				lastSelectedRadioButton = null;
			} else {
				lastSelectedRadioButton = radioButton;	
			}AsyncToken
		}
		
		public static function openPopUp(inputField:OpenForisInputField, alignToField:Boolean = false, 
										 showErrorBlankReasonMissing:Boolean = false, alignmentPoint:Point = null):void {
			popUp.reasonBlankGroup.addEventListener(FlexEvent.CREATION_COMPLETE, reasonBlankGroupCreationCompleteHandler);
			
			
		}
			extraInfoPopUp.reset();
			extraInfoPopUp.inputField = inputField;
			extraInfoPopUp.fieldExtraInfo = inputField.extraInfo;
			extraInfoPopUp.showErrorReasonBlankMissing = showErrorBlankReasonMissing;
			extraInfoPopUp.showReasonBlank = inputField.canShowReasonBlankOnPopUp() && PhaseUtil.currentPhaseCode == PhaseUtil.DATA_ENTRY_CODE;
			
			if(! extraInfoPopUpOpened) {
				PopUpManager.addPopUp(extraInfoPopUp, inputField);
			}
			inputField.callLater(function():void {extraInfoPopUp.setFocusOnFirstField();});
			
			var alignmentPoint:Point;
			if(alignToField) {
				PopUpUtil.alignPopUpToField(extraInfoPopUp, inputField, PopUpUtil.POSITION_RIGHT, PopUpUtil.VERTICAL_ALIGN_BOTTOM);
			} else if(alignmentPoint) {
				PopUpUtil.alignPop(extraInfoPopUp, alignmentPoint);
			} else {
				//align popup to mouse pointer
				PopUpUtil.alignPopUpToMousePoint(extraInfoPopUp, -10, -10);
			}
			
			popUpOpened = true;
		}
		
		public static function hidePopUp():void {
			PopUpManager.removePopUp(extraInfoPopUp);
			popUpOpened = false;
		}
		
		private static function saveHandler(event:DialogEvent = null):void {
			
			var extraInfo:FieldExtraInfo = event.data as FieldExtraInfo;
			inputField.changeExtraInfo(extraInfo);
			ExtraInfoPopUpManager.hidePopUp();
		}
		
		private static function cancelHandler(event:DialogEvent):void {
			hidePopUp();
		}
		
		protected function radioButtonsKeyDownHandler(event:KeyboardEvent):void {
			switch(event.keyCode) {
				case Keyboard.ENTER:
					saveHander();
					break;
				case Keyboard.ESCAPE:
					cancelHandler(null);
			}
			var reasonBlankCode:String = null;
			switch(event.charCode) {
				case 42: //asterisk (*)
					reasonBlankCode = FieldExtraInfo.BLANK_ON_FORM_CODE;
					break;
				case 45: //minus (-)
					reasonBlankCode = FieldExtraInfo.DASH_CODE;
					break;
				case 63: //question mark (?)
					reasonBlankCode = FieldExtraInfo.ILLEGIBLE_CODE;
					break;
			}
			if(reasonBlankCode != null) {
				radioButtonGroup.selectedValue = reasonBlankCode;
				radioButtonGroup.selection.setFocus();
			}
		}
		
		protected function reasonBlankGroupCreationCompleteHandler(event:FlexEvent):void {
			setFocusOnFirstField();
		}
		
		public function setFocusOnFirstField():void {
			if(_showReasonBlank) {
				if(blankOnFormRadioButton) {
					blankOnFormRadioButton.setFocus();
				}
			} else {
				if(remarksGroup) {
					remarksTextArea.setFocus();
				}
			}
		}
	}
}