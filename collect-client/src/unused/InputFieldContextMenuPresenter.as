package org.openforis.collect.presenter
{
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
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.ObjectUtil;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * @author S. Ricci
	 */
	[Deprecated]
	public class InputFieldContextMenuPresenter extends AbstractPresenter {
		
		private static const BLANK_ON_FORM_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.blankOnForm"));
		private static const DASH_ON_FORM_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.dashOnForm"));
		private static const ILLEGIBLE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.illegible"));
		private static const EDIT_REMARKS_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.editRemarks"), true);
		private static const REPLACE_BLANKS_WITH_STAR_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.replaceBlanksWithStar"), true);
		private static const REPLACE_BLANKS_WITH_DASH_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.replaceBlanksWithDash"));
		private static const DELETE_ATTRIBUTE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.deleteAttribute"), true);
		private static const DELETE_ENTITY_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.deleteEntity"), true);
		private static const CONFIRM_ERROR_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveError"), true);
		private static const APPROVE_MISSING_VALUE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveMissingValue"), true);
		private static const APPROVE_MISSING_VALUES_IN_ROW_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveMissingValuesInRow"), true);
		private static const MENU_ITEMS:Array = [
			BLANK_ON_FORM_MENU_ITEM, 
			DASH_ON_FORM_MENU_ITEM, 
			ILLEGIBLE_MENU_ITEM, 
			EDIT_REMARKS_MENU_ITEM, 
			REPLACE_BLANKS_WITH_DASH_MENU_ITEM,
			REPLACE_BLANKS_WITH_STAR_MENU_ITEM, 
			DELETE_ATTRIBUTE_MENU_ITEM, 
			DELETE_ENTITY_MENU_ITEM, 
			CONFIRM_ERROR_MENU_ITEM, 
			APPROVE_MISSING_VALUE_MENU_ITEM
		];
		private static var remarksPopUpPresenter:RemarksPopUpPresenter;
		private static var _lastInputField:InputField;
		private var _inputField:InputField;

		{
			initStatics();
		}
		
		public function InputFieldContextMenuPresenter(inputField:InputField) {
			_inputField = inputField;
			super();
			initContextMenu();
		}
		
		private static function initStatics():void {
			//init context menu items' event listener
			var item:ContextMenuItem;
			for each (item in MENU_ITEMS)  {
				item.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, menuItemSelectHandler);
			}
			//init remarks popup presenter
			remarksPopUpPresenter = new RemarksPopUpPresenter();
		}
		
		public function initContextMenu():void {
			if(Application.activeRecord != null) {
				var step:CollectRecord$Step = Application.activeRecord.step;
				var items:Array = new Array();
				
				addValueItems(items, step);
				
				addRowItems(items, step);
				var contextMenu:ContextMenu = new ContextMenu();
				contextMenu.customItems = items;
				contextMenu.hideBuiltInItems();
				_inputField.contextMenu = contextMenu;
			} else {
				_inputField.contextMenu = null;
			}
		}
		
		private function addValueItems(currentItems:Array, step:CollectRecord$Step):void {
			if(_inputField.isEmpty()) {
				switch(step) {
					case CollectRecord$Step.ENTRY:
						currentItems.push(
							BLANK_ON_FORM_MENU_ITEM,
							DASH_ON_FORM_MENU_ITEM,
							ILLEGIBLE_MENU_ITEM
						);
						break;
					case CollectRecord$Step.CLEANSING:
						currentItems.push(APPROVE_MISSING_VALUE_MENU_ITEM);
						break;
				}
			} else {
				if(step == CollectRecord$Step.ENTRY) {
					var hasErrors:Boolean = hasErrors();
					if(hasErrors) {
						var hasConfirmedError:Boolean = hasConfirmedError()
						if(! hasConfirmedError) {
							currentItems.push(CONFIRM_ERROR_MENU_ITEM);
						}
					}
				}
			}
			currentItems.push(EDIT_REMARKS_MENU_ITEM);
		}
		
		private function hasErrors():Boolean {
			if(_inputField.attributeDefinition.multiple) {
				var attributes:IList = ObjectUtil.getValue(_inputField, "attributes") as IList;
				for each (var a:AttributeProxy in attributes) {
					if(a.hasErrors()) {
						return true;
					}
				}
			} else if(_inputField.attribute != null && _inputField.attribute.hasErrors()) {
				return true;
			}
			return false;
		}
		
		private function hasConfirmedError():Boolean {
			var result:Boolean = false;
			var field:FieldProxy;
			if(_inputField.attributeDefinition.multiple) {
				var attributes:IList = ObjectUtil.getValue(_inputField, "attributes") as IList;
				for each (var a:AttributeProxy in attributes) {
					for each (var f:FieldProxy in a.fields) {
						if(f.symbol != FieldSymbol.CONFIRMED) {
							return false;
						}
					}
				}
				return true;
			} else if(_inputField.attribute != null && _inputField.attribute.hasErrors()) {
				field = _inputField.attribute.getField(_inputField.fieldIndex);
				if(field.symbol == FieldSymbol.CONFIRMED) {
					return true;
				}
			}
			return false;
		}
		
		private function addRowItems(currentItems:Array, step:CollectRecord$Step):void {
			var def:AttributeDefinitionProxy = _inputField.attributeDefinition;
			if(def.multiple && ! (def is CodeAttributeDefinitionProxy)) {
				currentItems.push(DELETE_ATTRIBUTE_MENU_ITEM);
			} else if(def.parentLayout == UIUtil.LAYOUT_TABLE) {
				var entityDef:EntityDefinitionProxy = def.parent;
				if(entityDef != null && entityDef.multiple) {
					switch(step) {
						case CollectRecord$Step.ENTRY:
							currentItems.push(
								REPLACE_BLANKS_WITH_STAR_MENU_ITEM,
								REPLACE_BLANKS_WITH_DASH_MENU_ITEM 
							);
							break;
						case CollectRecord$Step.CLEANSING:
							currentItems.push(APPROVE_MISSING_VALUES_IN_ROW_MENU_ITEM);
					}
					if( !entityDef.enumerable) {
						currentItems.push(DELETE_ENTITY_MENU_ITEM);
					}
				}
			}			
		}
		
		public static function menuItemSelectHandler(event:ContextMenuEvent):void {
			var inputField:InputField = event.contextMenuOwner as InputField;
			var attrDefn:AttributeDefinitionProxy = inputField.attributeDefinition;
			var parentEntity:EntityProxy = inputField.parentEntity;
			var parentEntityDefn:EntityDefinitionProxy = attrDefn.parent;
			_lastInputField = inputField;
			switch(event.target) {
				case BLANK_ON_FORM_MENU_ITEM:
					inputField.applySymbol(FieldSymbol.BLANK_ON_FORM);
					break;
				case DASH_ON_FORM_MENU_ITEM:
					inputField.applySymbol(FieldSymbol.DASH_ON_FORM);
					break;
				case ILLEGIBLE_MENU_ITEM:
					inputField.applySymbol(FieldSymbol.ILLEGIBLE);
					break;
				case EDIT_REMARKS_MENU_ITEM:
					remarksPopUpPresenter.openPopUp(inputField, true);
					break;
				case REPLACE_BLANKS_WITH_DASH_MENU_ITEM:
					setSymbolInBlankChildren(parentEntity, parentEntityDefn, FieldSymbol.DASH_ON_FORM);
					break;
				case REPLACE_BLANKS_WITH_STAR_MENU_ITEM:
					setSymbolInBlankChildren(parentEntity, parentEntityDefn, FieldSymbol.BLANK_ON_FORM);
					break;
				case DELETE_ATTRIBUTE_MENU_ITEM:
					AlertUtil.showConfirm("global.confirmDelete", [inputField.attributeDefinition.getLabelText()], "global.confirmAlertTitle", 
						performDeleteAttribute);
					break;
				case DELETE_ENTITY_MENU_ITEM:
					AlertUtil.showConfirm("edit.confirmDeleteEntity", null, "global.confirmAlertTitle", performDeleteEntity);
					break;
				case CONFIRM_ERROR_MENU_ITEM:
					inputField.applySymbol(FieldSymbol.CONFIRMED);
					break
				case APPROVE_MISSING_VALUE_MENU_ITEM:
					inputField.applySymbol(FieldSymbol.CONFIRMED);
					break
				case APPROVE_MISSING_VALUES_IN_ROW_MENU_ITEM:
					setSymbolInBlankChildren(parentEntity, parentEntityDefn, FieldSymbol.CONFIRMED);
					break;
			}
		}
		
		protected static function performDeleteAttribute():void {
			var attr:AttributeProxy = _lastInputField.attribute;
			performDeleteNode(attr);
		}
		
		protected static function performDeleteEntity():void {
			var entity:EntityProxy = _lastInputField.parentEntity;
			performDeleteNode(entity);
		}
		
		protected static function performDeleteNode(node:NodeProxy):void {
			var o:UpdateRequestOperation = new UpdateRequestOperation();
			o.method = UpdateRequestOperation$Method.DELETE;
			o.parentEntityId = node.parentId;
			o.nodeName = node.name;
			o.nodeId = node.id;
			var req:UpdateRequest = new UpdateRequest(o);
			ClientFactory.dataClient.updateActiveRecord(req, null, null, faultHandler);
		}
		
		protected static function setSymbolInBlankChildren(entity:EntityProxy, entityDefn:EntityDefinitionProxy, symbol:FieldSymbol):void {
			var operations:ArrayCollection = new ArrayCollection();
			var req:UpdateRequest = new UpdateRequest();
			var updatedFields:ArrayCollection = new ArrayCollection();
			appendApplySymbolInChildrenOperations(operations, updatedFields, entity, entityDefn, symbol);
			req.operations = operations;
			if(CollectionUtil.isNotEmpty(req.operations)) {
				var token:UpdateRequestToken = new UpdateRequestToken(UpdateRequestToken.UPDATE_SYMBOL);
				token.updatedFields = updatedFields;
				token.symbol = symbol;
				ClientFactory.dataClient.updateActiveRecord(req, token, null, faultHandler);
			}
		}
		
		protected static function appendApplySymbolInChildrenOperations(operations:ArrayCollection, updatedFields:ArrayCollection, 
																		entity:EntityProxy, entityDefn:EntityDefinitionProxy, symbol:FieldSymbol):void {
			var children:IList = entity.getChildren();
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
							operations.addItem(o);
							updatedFields.addItem(field);
						}
					}
				} else if(child is EntityProxy) {
					var childEntiy:EntityProxy = EntityProxy(child);
					var childEntiyDefn:EntityDefinitionProxy = EntityDefinitionProxy(entityDefn.getChildDefinition(child.name));
					if(! childEntiyDefn.multiple) {
						appendApplySymbolInChildrenOperations(operations, updatedFields, childEntiy, childEntiyDefn, symbol);
					}
				}
			}
		}

	}
}