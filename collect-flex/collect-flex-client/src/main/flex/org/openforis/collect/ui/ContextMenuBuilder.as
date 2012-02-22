package org.openforis.collect.ui
{
	import flash.display.DisplayObjectContainer;
	import flash.display.InteractiveObject;
	import flash.events.ContextMenuEvent;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.controls.Alert;
	import mx.messaging.management.Attribute;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.AttributeSymbol;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.presenter.RemarksPopUpPresenter;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.detail.CollectFormItem;
	import org.openforis.collect.ui.component.detail.EntityDataGroupItemRenderer;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.RemarksPopUp;
	import org.openforis.collect.util.AlertUtil;

	public class ContextMenuBuilder {
		
		private static const BLANK_ON_FORM_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.blankOnForm"));
		
		private static const DASH_ON_FORM_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.dashOnForm"));
		
		private static const ILLEGIBLE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.illegible"));
		
		private static const EDIT_REMARKS_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.editRemarks"), true);
		
		private static const REPLACE_BLANKS_WITH_DASH_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.replaceBlanksWithDash"), true);
		
		private static const REPLACE_BLANKS_WITH_STAR_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.replaceBlanksWithStar"));
		
		private static const DELETE_ATTRIBUTE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.deleteAttribute"), true);
		
		private static const DELETE_ENTITY_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.deleteEntity"), true);
		
		private static const APPROVE_ERROR_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveError"), true);
		
		private static const APPROVE_MISSING_VALUE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveMissingValue"), true);
		
		private static const APPROVE_MISSING_VALUES_IN_ROW_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveMissingValuesInRow"), true);
		
		private static const all:Array = [
			BLANK_ON_FORM_MENU_ITEM, 
			DASH_ON_FORM_MENU_ITEM, 
			ILLEGIBLE_MENU_ITEM, 
			EDIT_REMARKS_MENU_ITEM, 
			REPLACE_BLANKS_WITH_DASH_MENU_ITEM,
			REPLACE_BLANKS_WITH_STAR_MENU_ITEM, 
			DELETE_ATTRIBUTE_MENU_ITEM, 
			DELETE_ENTITY_MENU_ITEM, 
			APPROVE_ERROR_MENU_ITEM, 
			APPROVE_MISSING_VALUE_MENU_ITEM
		];
		
		private static var remarksPopUpPresenter:RemarksPopUpPresenter;
		
		private static var currentInputField:InputField;

		{
			initStatics();
		}

		private static function initStatics():void {
			//init context menu items' event listener
			var item:ContextMenuItem;
			for each (item in all)  {
				item.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, menuItemSelectHandler);
			}
			//init remarks popup presenter
			remarksPopUpPresenter = new RemarksPopUpPresenter();
		}
		
		public static function buildContextMenu(inputField:InputField):ContextMenu {
			var cm:ContextMenu = new ContextMenu();
			var items:Array = new Array();
			if(inputField.isEmpty()) {
				items.push(
					BLANK_ON_FORM_MENU_ITEM,
					DASH_ON_FORM_MENU_ITEM,
					ILLEGIBLE_MENU_ITEM
				);
			}
			items.push(EDIT_REMARKS_MENU_ITEM);
			
			var def:AttributeDefinitionProxy = inputField.attributeDefinition;
			if(def != null) {
				if(def.multiple) {
					items.push(DELETE_ATTRIBUTE_MENU_ITEM);
				}
				var entityDef:EntityDefinitionProxy = def.parent;
				if(entityDef != null && entityDef.multiple) {
					items.push(
						REPLACE_BLANKS_WITH_DASH_MENU_ITEM, 
						REPLACE_BLANKS_WITH_STAR_MENU_ITEM
					);
					if( !entityDef.enumerated) {
						items.push(DELETE_ENTITY_MENU_ITEM);
					}
				}
			}
			cm.customItems = items;
			cm.hideBuiltInItems();
			return cm;
		}
		
		public static function menuItemSelectHandler(event:ContextMenuEvent):void {
			var owner:InteractiveObject = event.contextMenuOwner;
			if(owner is InputField) {
				var field:InputField = InputField(owner);
				var parentEntity:EntityProxy = field.parentEntity;
				currentInputField = field;
				switch(event.target) {
					case BLANK_ON_FORM_MENU_ITEM:
						field.changeSymbol(AttributeSymbol.BLANK_ON_FORM);
						break;
					case DASH_ON_FORM_MENU_ITEM:
						field.changeSymbol(AttributeSymbol.DASH_ON_FORM);
						break;
					case ILLEGIBLE_MENU_ITEM:
						field.changeSymbol(AttributeSymbol.ILLEGIBLE);
						break;
					case EDIT_REMARKS_MENU_ITEM:
						remarksPopUpPresenter.openPopUp(field, true);
						break;
					case REPLACE_BLANKS_WITH_DASH_MENU_ITEM:
						setReasonBlankInChildren(parentEntity, AttributeSymbol.DASH_ON_FORM);
						break;
					case REPLACE_BLANKS_WITH_STAR_MENU_ITEM:
						setReasonBlankInChildren(parentEntity, AttributeSymbol.BLANK_ON_FORM);
						break;
					case DELETE_ATTRIBUTE_MENU_ITEM:
						AlertUtil.showConfirm("global.confirmDelete", [field.attributeDefinition.getLabelText()], "global.confirmAlertTitle", performDeleteAttribute);
						break;
					case DELETE_ENTITY_MENU_ITEM:
						AlertUtil.showConfirm("edit.confirmDeleteEntity", null, "global.confirmAlertTitle", performDeleteEntity);
						break;
				}
			}
		}
		
		protected static function performDeleteAttribute():void {
			var def:AttributeDefinitionProxy = currentInputField.attributeDefinition;
			var req:UpdateRequest = new UpdateRequest();
			req.parentEntityId = currentInputField.parentEntity.id;
			req.nodeName = def.name;
			if(currentInputField.attribute != null) {
				req.nodeId = currentInputField.attribute.id;
			}
			req.method = UpdateRequest$Method.DELETE;
			
			var responder:AsyncResponder = new AsyncResponder(updateFieldResultHandler, null);
			ClientFactory.dataClient.updateActiveRecord(responder, req);
		}
		
		protected static function performDeleteEntity():void {
			var entity:EntityProxy = currentInputField.parentEntity;
			var req:UpdateRequest = new UpdateRequest();
			req.nodeName = entity.name;
			req.nodeId = entity.id;
			req.parentEntityId = entity.parentId;
			req.method = UpdateRequest$Method.DELETE;
			
			var responder:AsyncResponder = new AsyncResponder(updateFieldResultHandler, null);
			ClientFactory.dataClient.updateActiveRecord(responder, req);
		}
		
		public static function setReasonBlankInChildren(entity:EntityProxy, symbol:AttributeSymbol):void {
			var req:UpdateRequest = new UpdateRequest();
			req.parentEntityId = entity.parentId;
			req.nodeName = entity.name;
			req.symbol = symbol;
			req.nodeId = entity.id;
			req.method = UpdateRequest$Method.UPDATE_SYMBOL;
			var responder:AsyncResponder = new AsyncResponder(updateFieldResultHandler, null);
			ClientFactory.dataClient.updateActiveRecord(responder, req);
		}
		
		protected static function updateFieldResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			Application.activeRecord.update(result);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = result;
			EventDispatcherFactory.getEventDispatcher().dispatchEvent(appEvt);
		}
		

	}
}