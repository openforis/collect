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
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.detail.CollectFormItem;
	import org.openforis.collect.ui.component.detail.EntityDataGroupItemRenderer;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.AlertUtil;

	public class ContextMenuBuilder {
		
		private static const BLANK_ON_FORM_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.blankOnForm"));
		
		private static const DASH_ON_FORM_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.dashOnForm"));
		
		private static const ILLEGIBLE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.illegible"));
		
		private static const EDIT_REMARKS_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.editRemarks"));
		
		private static const REPLACE_BLANKS_WITH_DASH_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.replaceBlanksWithDash"));
		
		private static const REPLACE_BLANKS_WITH_STAR_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.replaceBlanksWithStar"));
		
		private static const DELETE_ATTRIBUTE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.deleteAttribute"));
		
		private static const DELETE_ENTITY_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.deleteEntity"));
		
		private static const APPROVE_ERROR_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveError"));
		
		private static const APPROVE_MISSING_VALUE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveMissingValue"));
		
		private static const APPROVE_MISSING_VALUES_IN_ROW_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveMissingValuesInRow"));
		
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
		
		{
			//init context menu items' event listener
			private static var item:ContextMenuItem;
			for each (item in all)  {
				item.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, menuItemSelectHandler);
			}
		}
		
		private static var currentInputField:InputField;

		
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
						//_remarksPopUpPresenter.openPopUp(field, false, contextMouseClickGlobalPoint);
						break;
					case REPLACE_BLANKS_WITH_DASH_MENU_ITEM:
						setReasonBlankInChildren(parentEntity, AttributeSymbol.DASH_ON_FORM);
						break;
					case REPLACE_BLANKS_WITH_STAR_MENU_ITEM:
						setReasonBlankInChildren(parentEntity, AttributeSymbol.BLANK_ON_FORM);
						break;
					case DELETE_ATTRIBUTE_MENU_ITEM:
						AlertUtil.showConfirm("edit.confirmDeleteAttribute", null, null, performDeleteAttribute);
						break;
				}
			}
		}
		
		protected static function performDeleteAttribute():void {
			var name:String = currentInputField.attributeDefinition.name;
			var req:UpdateRequest = new UpdateRequest();
			var def:AttributeDefinitionProxy = currentInputField.attributeDefinition;
			req.parentEntityId = currentInputField.parentEntity.id;
			req.nodeName = def.name;
			req.nodeId = currentInputField.attribute.id;
			req.method = UpdateRequest$Method.DELETE;
			
			var responder:AsyncResponder = new AsyncResponder(updateFieldResultHandler, null);
			ClientFactory.dataClient.updateActiveRecord(responder, req);
		}
		
		public static function setReasonBlankInChildren(entity:EntityProxy, symbol:AttributeSymbol):void {
			var children:ArrayCollection = entity.getChildren() as ArrayCollection;
			for each (var child:NodeProxy in children) {
				if(child is AttributeProxy) {
					var a:AttributeProxy = AttributeProxy(child);
					if(a.empty) {
						changeSymbol(entity, a.id, a.name, symbol);
					}
				}
			}
		}
		
		public static function changeSymbol(parentEntity:EntityProxy, attributeId:Number, attributeName:String, symbol:AttributeSymbol, remarks:String = null):void {
			var req:UpdateRequest = new UpdateRequest();
			req.parentEntityId = parentEntity.id;
			req.nodeName = attributeName;
			req.symbol = symbol;
			req.remarks = remarks;
			if(! isNaN(attributeId)) {
				req.nodeId = attributeId;
				req.method = UpdateRequest$Method.UPDATE;
			} else {
				req.method = UpdateRequest$Method.ADD;
			}
			var responder:AsyncResponder = new AsyncResponder(updateFieldResultHandler, null);
			ClientFactory.dataClient.updateActiveRecord(responder, req);
		}

		protected static function updateFieldResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			Application.activeRecord.update(result);
			EventDispatcherFactory.getEventDispatcher().dispatchEvent(new ApplicationEvent(ApplicationEvent.MODEL_CHANGED));
		}
		

	}
}