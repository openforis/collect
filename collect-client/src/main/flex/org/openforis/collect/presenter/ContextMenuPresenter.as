package org.openforis.collect.presenter
{
	import flash.external.ExternalInterface;
	import flash.geom.Point;
	
	import mx.controls.Menu;
	import mx.core.FlexGlobals;
	import mx.events.MenuEvent;
	import mx.resources.ResourceManager;
	
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.Phase;
	import org.openforis.collect.presenter.input.RemarksPopUpPresenter;
	import org.openforis.collect.ui.component.datagroup.DataGroupItemRenderer;
	import org.openforis.collect.ui.component.detail.input.InputField;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.PopUpUtil;

	/**
	 * @author S. Ricci
	 */
	public class ContextMenuPresenter extends AbstractPresenter
	{
		private static const BLANK_ON_FORM_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.blankOnForm"), action: "blankOnForm"};
		
		private static const DASH_ON_FORM_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.dashOnForm"), action: "dashOnForm"};
		
		private static const ILLEGIBLE_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.illegible"), action: "illegible"};
		
		private static const EDIT_REMARKS_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.editRemarks"), action: "editRemarks"};
		
		private static const REPLACE_BLANKS_WITH_DASH_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.replaceBlanksWithDash"), action: "replaceBlanksWithDash"};
		
		private static const REPLACE_BLANKS_WITH_STAR_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.replaceBlanksWithStar"), action: "replaceBlanksWithStar"};
		
		private static const DELETE_ROW_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.deleteRow"), action: "deleteRow"};
		
		private static const APPROVE_ERROR_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.approveError"), action: "approveError"};
		
		private static const APPROVE_MISSING_VALUE_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.approveMissingValue"), action: "approveMissingValue"};
		
		private static const APPROVE_MISSING_VALUES_IN_ROW_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.approveMissingValuesInRow"), action: "approveMissingValuesInRow"};
		
		public static var lastMouseOverInputField:InputField;
		
		public static var lastMouseOverDataGroupRow:DataGroupItemRenderer;
		
		private static var contextInputField:InputField;
		
		private static var contextDataGroupRow:DataGroupItemRenderer;
		
		private static var contextMouseClickGlobalPoint:Point;
		
		private var _remarksPopUpPresenter:RemarksPopUpPresenter;
		
		private var contextMenu:Menu;
		
		public function ContextMenuPresenter(view:collect) {
			super();
			
			contextMenu = new Menu();
			contextMenu.variableRowHeight = true;
			contextMenu.styleName = "contextMenu";
			contextMenu.addEventListener(MenuEvent.ITEM_CLICK, contextMenuItemClickHandler);
			
			initExternalInterface();
			
			_remarksPopUpPresenter = new RemarksPopUpPresenter();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			eventDispatcher.addEventListener(InputFieldEvent.INPUT_FIELD_MOUSE_OVER, inputFieldMouseOverHandler);
			eventDispatcher.addEventListener(InputFieldEvent.INPUT_FIELD_MOUSE_OUT, inputFieldMouseOutHandler);
		}
		
		private function initExternalInterface():void {
			if(ExternalInterface.available) {
				ExternalInterface.addCallback("isEditingItem", isEditingItem);
				ExternalInterface.addCallback("openContextMenu", openContextMenu);
			}
		}
		
		//called from External Interface (javascript)
		public function isEditingItem():Boolean {
			return true;
			//return ! (AbstractPresenter.serverOffline) && _view.isEditingItem();
		}
		
		public function openContextMenu():Boolean {
			contextMouseClickGlobalPoint = new Point(FlexGlobals.topLevelApplication.mouseX, FlexGlobals.topLevelApplication.mouseY);
			contextMouseClickGlobalPoint = FlexGlobals.topLevelApplication.localToGlobal(contextMouseClickGlobalPoint);
			
			//init menu data provider
			var menuDataProvider:Array = [];
			//var currentPhaseCode:String = (FlexGlobals.topLevelApplication as collect).selectedPhaseCode;
			var currentPhase:Phase = Phase.DATA_ENTRY;
			if(lastMouseOverInputField) {
				//add items related to blank infos (if the field is empty)
				//if(currentPhase == Phase.DATA_ENTRY && lastMouseOverInputField.isEmpty()) {
				if(currentPhase == Phase.DATA_ENTRY) {
					menuDataProvider.push(BLANK_ON_FORM_MENU_ITEM);
					menuDataProvider.push(DASH_ON_FORM_MENU_ITEM);
					menuDataProvider.push(ILLEGIBLE_MENU_ITEM);
				}
				menuDataProvider.push(EDIT_REMARKS_MENU_ITEM);
				//save a link to the last mouse over item before variable is updated on mouse out
				contextInputField = lastMouseOverInputField;
				
				//add approve error item
				//if(currentPhaseCode == Phase.DATA_ENTRY && lastMouseOverInputField.hasError()	&& ! lastMouseOverInputField.isApprovedError()) {
				if(currentPhase == Phase.DATA_ENTRY) {
					//check that the error is not a "notReasonBlankSpecified" error
					/*
					var modelValidator:ModelValidator = ModelValidator.getInstance();
					var validationResults:Array = modelValidator.getValidationResult(lastMouseOverInputField.internalXPath);
					if(ArrayUtil.isEmpty(validationResults)) {
						//try to get validation results using subElement (like for coordinates)
						var subElementName:String = lastMouseOverInputField.hasOwnProperty("subElementName") ? lastMouseOverInputField["subElementName"]: null;
						if(StringUtil.isNotBlank(subElementName)) {
							validationResults = modelValidator.getValidationResult(lastMouseOverInputField.internalXPath + "/" + subElementName + "[1]");
						}
					}
					if(ArrayUtil.isNotEmpty(validationResults)) {
						var validationResult:ValidationResult = validationResults[0] as ValidationResult;
						if(! (validationResult.reasonBlankNotSpecifiedError || validationResult.minSpecifiedError)) {
							menuDataProvider.push(APPROVE_ERROR_MENU_ITEM);
						}
					}
					*/
				} else if(currentPhase == Phase.DATA_CLEANSING /*&& ! lastMouseOverInputField.isApprovedMissingValue() && lastMouseOverInputField.hasNoValue()*/) {
					//add approve missing value item
					menuDataProvider.push(APPROVE_MISSING_VALUE_MENU_ITEM);
				}
			}
			
			//add items related to data group rows (multiple entities)
			if(lastMouseOverDataGroupRow) {
				if(currentPhase == Phase.DATA_ENTRY) {
					if(menuDataProvider.length > 0) {
						menuDataProvider.push({type: "separator"});
					}
					menuDataProvider.push(REPLACE_BLANKS_WITH_DASH_MENU_ITEM);
					menuDataProvider.push(REPLACE_BLANKS_WITH_STAR_MENU_ITEM);
				}
				
				//add delete row menu item only if can delete row and dataGroup contains more than 1 row
				var dataGroup:Object = lastMouseOverDataGroupRow.parent as Object;
				if(lastMouseOverDataGroupRow["canDelete"] 
					//&& dataGroup != null && dataGroup.dataProvider != null && dataGroup.dataProvider.length > 1
				) {
					menuDataProvider.push({type: "separator"});
					menuDataProvider.push(DELETE_ROW_MENU_ITEM);
				}
				//save a link to the last mouse over item before variable is updated on mouse out
				contextDataGroupRow = lastMouseOverDataGroupRow;
			}
			
			if(menuDataProvider.length > 0) {
				contextMenu.hide();
				
				Menu.popUpMenu(contextMenu, null, menuDataProvider);
				
				var alignmentPt:Point = new Point(contextMouseClickGlobalPoint.x + 5, contextMouseClickGlobalPoint.y - 10);
				alignmentPt = PopUpUtil.getAdjustedCoordinatesOfPopUp(contextMenu, alignmentPt);
				
				contextMenu.show(alignmentPt.x, alignmentPt.y);
				
				if(contextInputField != null) {
					//contextInputField.hideToolTip();
				}
				
				return true;
			} else {
				return false;
			}
		}
		
		protected function inputFieldMouseOverHandler(event:InputFieldEvent):void {
			lastMouseOverInputField = event.inputField;
		}
		
		protected function inputFieldMouseOutHandler(event:InputFieldEvent):void {
			lastMouseOverInputField = null;
		}
		
		private function contextMenuItemClickHandler(event:MenuEvent):void {
			switch(event.item) {
				/*
				case BLANK_ON_FORM_MENU_ITEM:
					contextInputField.changeReasonBlankInfo(FieldExtraInfo.BLANK_ON_FORM_CODE);
					break;
				case DASH_ON_FORM_MENU_ITEM:
					contextInputField.changeReasonBlankInfo(FieldExtraInfo.DASH_CODE);
					break;
				case ILLEGIBLE_MENU_ITEM:
					contextInputField.changeReasonBlankInfo(FieldExtraInfo.ILLEGIBLE_CODE);
					break;
				*/
				case EDIT_REMARKS_MENU_ITEM:
					_remarksPopUpPresenter.openPopUp(contextInputField, false, contextMouseClickGlobalPoint);
					break;
				/*
				case APPROVE_ERROR_MENU_ITEM:
					contextInputField.approveError();
					break;
				case APPROVE_MISSING_VALUE_MENU_ITEM:
					contextInputField.approveMissingValue();
					break;
				case APPROVE_MISSING_VALUES_IN_ROW_MENU_ITEM:
					contextInputField.approveMissingValue();
					break;
				case DELETE_ROW_MENU_ITEM:
					//var message:String = isLastRemainingRowSelected() ? "Delete the last row?": "Delete this row?";
					var message:String = "Delete this row?";
					ConfirmUtil.showConfirm(message, "Confirm", doDeleteRow);
					break;
				case REPLACE_BLANKS_WITH_STAR_MENU_ITEM:
					if(contextDataGroupRow) {
						contextDataGroupRow.replaceBlankOnRowWithReasonBlankInfo(contextDataGroupRow.itemIndex, FieldExtraInfo.BLANK_ON_FORM_CODE);
					}
					break;
				case REPLACE_BLANKS_WITH_DASH_MENU_ITEM:
					if(contextDataGroupRow) {
						contextDataGroupRow.replaceBlankOnRowWithReasonBlankInfo(contextDataGroupRow.itemIndex, FieldExtraInfo.DASH_CODE);
					}
					break;
				*/
			}
			/*
			function doDeleteRow():void {
				if(contextDataGroupRow) {
					(contextDataGroupRow.parent as OpenForisDataGroup).deleteItemAt(contextDataGroupRow.itemIndex);
				}
			}
			*/
		}
		
		
	}
}