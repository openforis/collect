package org.openforis.collect.ui
{
	import flash.display.InteractiveObject;
	import flash.events.ContextMenuEvent;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.UpdateRequestToken;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.presenter.RemarksPopUpPresenter;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.CollectionUtil;

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
			var step:CollectRecord$Step = Application.activeRecord.step;
			var cm:ContextMenu = new ContextMenu();
			var items:Array = new Array();
			
			addValueItems(items, step, inputField);
			
			addRowItems(items, step, inputField);
			
			addApproveValueItems(items, step, inputField);
			
			cm.customItems = items;
			cm.hideBuiltInItems();
			return cm;
		}
		
		private static function addValueItems(currentItems:Array, step:CollectRecord$Step, inputField:InputField):void {
			if(inputField.isEmpty()) {
				currentItems.push(
					BLANK_ON_FORM_MENU_ITEM,
					DASH_ON_FORM_MENU_ITEM,
					ILLEGIBLE_MENU_ITEM
				);
			}
			currentItems.push(EDIT_REMARKS_MENU_ITEM);
		}
		
		private static function addRowItems(currentItems:Array, step:CollectRecord$Step, inputField:InputField):void {
			var def:AttributeDefinitionProxy = inputField.attributeDefinition;
			if(def != null && inputField.isInDataGroup) {
				if(def.multiple && ! (def is CodeAttributeDefinitionProxy)) {
					currentItems.push(DELETE_ATTRIBUTE_MENU_ITEM);
				}
				var entityDef:EntityDefinitionProxy = def.parent;
				if(entityDef != null && entityDef.multiple) {
					currentItems.push(
						REPLACE_BLANKS_WITH_DASH_MENU_ITEM, 
						REPLACE_BLANKS_WITH_STAR_MENU_ITEM
					);
					if( !entityDef.enumerated) {
						currentItems.push(DELETE_ENTITY_MENU_ITEM);
					}
				}
			}			
		}
		
		private static function addApproveValueItems(currentItems:Array, step:CollectRecord$Step, inputField:InputField):void {
			var attribute:AttributeProxy = inputField.attribute;
			if(attribute != null) {
				/*
				var state:NodeStateProxy = attribute.state;
				if(step == RecordProxy$Step.ENTRY && state != null && state.hasErrors()) {
					currentItems.push(APPROVE_ERROR_MENU_ITEM);
				}
				*/
			}
		}
		
		public static function menuItemSelectHandler(event:ContextMenuEvent):void {
			var owner:InteractiveObject = event.contextMenuOwner;
			if(owner is InputField) {
				var field:InputField = InputField(owner);
				var parentEntity:EntityProxy = field.parentEntity;
				currentInputField = field;
				switch(event.target) {
					case BLANK_ON_FORM_MENU_ITEM:
						field.applySymbol(FieldSymbol.BLANK_ON_FORM);
						break;
					case DASH_ON_FORM_MENU_ITEM:
						field.applySymbol(FieldSymbol.DASH_ON_FORM);
						break;
					case ILLEGIBLE_MENU_ITEM:
						field.applySymbol(FieldSymbol.ILLEGIBLE);
						break;
					case EDIT_REMARKS_MENU_ITEM:
						remarksPopUpPresenter.openPopUp(field, true);
						break;
					case REPLACE_BLANKS_WITH_DASH_MENU_ITEM:
						setReasonBlankInChildren(parentEntity, FieldSymbol.DASH_ON_FORM);
						break;
					case REPLACE_BLANKS_WITH_STAR_MENU_ITEM:
						setReasonBlankInChildren(parentEntity, FieldSymbol.BLANK_ON_FORM);
						break;
					case DELETE_ATTRIBUTE_MENU_ITEM:
						AlertUtil.showConfirm("global.confirmDelete", [field.attributeDefinition.getLabelText()], "global.confirmAlertTitle", performDeleteAttribute);
						break;
					case DELETE_ENTITY_MENU_ITEM:
						AlertUtil.showConfirm("edit.confirmDeleteEntity", null, "global.confirmAlertTitle", performDeleteEntity);
						break;
					case APPROVE_ERROR_MENU_ITEM:
						field.applySymbol(FieldSymbol.CONFIRMED);
						break
				}
			}
		}
		
		protected static function performDeleteAttribute():void {
			var def:AttributeDefinitionProxy = currentInputField.attributeDefinition;
			var o:UpdateRequestOperation = new UpdateRequestOperation();
			o.parentEntityId = currentInputField.parentEntity.id;
			o.nodeName = def.name;
			if(currentInputField.attribute != null) {
				o.nodeId = currentInputField.attribute.id;
			}
			o.method = UpdateRequestOperation$Method.DELETE;
			var req:UpdateRequest = new UpdateRequest(o);
			ClientFactory.dataClient.updateActiveRecord(req);
		}
		
		protected static function performDeleteEntity():void {
			var entity:EntityProxy = currentInputField.parentEntity;
			var req:UpdateRequest = new UpdateRequest();
			req.operations = new ArrayCollection();
			var o:UpdateRequestOperation = new UpdateRequestOperation();
			o.method = UpdateRequestOperation$Method.DELETE;
			o.parentEntityId = entity.parentId;
			o.nodeName = entity.name;
			o.nodeId = entity.id;
			req.operations.addItem(o);
			ClientFactory.dataClient.updateActiveRecord(req);
		}

		protected static function setReasonBlankInChildren(entity:EntityProxy, symbol:FieldSymbol):void {
			var children:IList = entity.getChildren();
			var req:UpdateRequest = new UpdateRequest();
			req.operations = new ArrayCollection();
			var updatedFields:ArrayCollection = new ArrayCollection();
			for each (var child:NodeProxy in children) {
				if(child is AttributeProxy) {
					var a:AttributeProxy = AttributeProxy(child);
					for(var index:int = 0; index < a.fields.length; index ++) {
						var field:FieldProxy = a.fields[index];
						if(field.value == null && field.symbol == null) {
							var o:UpdateRequestOperation = new UpdateRequestOperation();
							o.method = UpdateRequestOperation$Method.UPDATE;
							o.parentEntityId = entity.id;
							o.nodeName = child.name;
							o.nodeId = child.id;
							o.fieldIndex = index;
							o.remarks = field.remarks;
							o.value = field.value != null ? field.value.toString(): null;
							o.symbol = symbol;
							req.operations.addItem(o);
							updatedFields.addItem(field);
						}
					}
				}
			}
			if(CollectionUtil.isNotEmpty(req.operations)) {
				var token:UpdateRequestToken = new UpdateRequestToken(UpdateRequestToken.TYPE_UPDATE_SYMBOL);
				token.updatedFields = updatedFields;
				token.symbol = symbol;
				ClientFactory.dataClient.updateActiveRecord(req, token);
			}
		}
		
	}
}