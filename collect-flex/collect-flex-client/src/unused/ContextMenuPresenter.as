package org.openforis.collect.presenter
{
	import flash.external.ExternalInterface;
	import flash.geom.Point;
	
	import mx.collections.IList;
	import mx.controls.Menu;
	import mx.core.FlexGlobals;
	import mx.events.MenuEvent;
	import mx.events.Request;
	import mx.resources.ResourceManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.model.Phase;
	import org.openforis.collect.model.proxy.AttributeSymbol;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.datagroup.DataGroupItemRenderer;
	import org.openforis.collect.ui.component.detail.EntityDataGroupItemRenderer;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.AlertUtil;
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
		
		private static const DELETE_ATTRIBUTE_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.deleteAttribute"), action: "deleteAttribute"};
		
		private static const DELETE_ENTITY_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.deleteEntity"), action: "deleteEntity"};
		
		private static const APPROVE_ERROR_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.approveError"), action: "approveError"};
		
		private static const APPROVE_MISSING_VALUE_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.approveMissingValue"), action: "approveMissingValue"};
		
		private static const APPROVE_MISSING_VALUES_IN_ROW_MENU_ITEM:Object = {label: Message.get("edit.contextMenu.approveMissingValuesInRow"), action: "approveMissingValuesInRow"};
		
		public static var lastMouseOverInputField:InputField;
		
		public static var lastMouseOverDataGroupRow:EntityDataGroupItemRenderer;
		
		private static var contextInputField:InputField;
		
		private static var contextDataGroupRow:EntityDataGroupItemRenderer;
		
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
			
			eventDispatcher.addEventListener(UIEvent.ENTITY_MOUSE_OVER, entityMouseOverHandler);
			eventDispatcher.addEventListener(UIEvent.ENTITY_MOUSE_OUT, entityMouseOutHandler);
			
		}
		
		private function initExternalInterface():void {
			if(ExternalInterface.available) {
				ExternalInterface.addCallback("openContextMenu", openContextMenu);
			}
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
				if(currentPhase == Phase.DATA_ENTRY && lastMouseOverInputField.isEmpty()) {
					menuDataProvider.push(BLANK_ON_FORM_MENU_ITEM);
					menuDataProvider.push(DASH_ON_FORM_MENU_ITEM);
					menuDataProvider.push(ILLEGIBLE_MENU_ITEM);
				}
				menuDataProvider.push(EDIT_REMARKS_MENU_ITEM);
				
				if(lastMouseOverInputField.attributeDefinition != null && lastMouseOverInputField.attributeDefinition.multiple) {
					menuDataProvider.push({type: "separator"});
					menuDataProvider.push(DELETE_ATTRIBUTE_MENU_ITEM);
				}
				
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
				
				menuDataProvider.push({type: "separator"});
				menuDataProvider.push(DELETE_ENTITY_MENU_ITEM);

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
		
		protected function entityMouseOverHandler(event:InputFieldEvent):void {
			lastMouseOverDataGroupRow = event.obj as EntityDataGroupItemRenderer;
		}
		
		protected function entityMouseOutHandler(event:InputFieldEvent):void {
			lastMouseOverDataGroupRow = null;
		}
		
		private function contextMenuItemClickHandler(event:MenuEvent):void {
			switch(event.item) {
				case BLANK_ON_FORM_MENU_ITEM:
					contextInputField.changeSymbol(AttributeSymbol.BLANK_ON_FORM);
					break;
				case DASH_ON_FORM_MENU_ITEM:
					contextInputField.changeSymbol(AttributeSymbol.DASH_ON_FORM);
					break;
				case ILLEGIBLE_MENU_ITEM:
					contextInputField.changeSymbol(AttributeSymbol.ILLEGIBLE);
					break;
				case EDIT_REMARKS_MENU_ITEM:
					_remarksPopUpPresenter.openPopUp(contextInputField, false, contextMouseClickGlobalPoint);
					break;
				case DELETE_ATTRIBUTE_MENU_ITEM:
					AlertUtil.showConfirm("edit.confirmDeleteAttribute", null, null, performDeleteAttribute);
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
			
		}
		
		protected function performDeleteAttribute():void {
			if(contextInputField) {
				var name:String = contextInputField.attributeDefinition.name;
				var req:UpdateRequest = new UpdateRequest();
				var def:AttributeDefinitionProxy = contextInputField.attributeDefinition;
				req.parentEntityId = contextInputField.parentEntity.id;
				req.nodeName = def.name;
				req.nodeId = contextInputField.attribute.id;
				req.method = UpdateRequest$Method.DELETE;
				
				var responder:AsyncResponder = new AsyncResponder(deleteAttributeResultHandler, faultHandler);
				ClientFactory.dataClient.updateActiveRecord(responder, req);
			}
		}
		
		protected function deleteAttributeResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			Application.activeRecord.update(result);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = result;
			eventDispatcher.dispatchEvent(appEvt);
		}
		
	}
}