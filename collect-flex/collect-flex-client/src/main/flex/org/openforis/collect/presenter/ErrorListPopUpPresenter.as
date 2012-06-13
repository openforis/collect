package org.openforis.collect.presenter
{
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.SchemaProxy;
	import org.openforis.collect.model.Queue;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.ui.component.ErrorListPopUp;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.idm.metamodel.validation.ValidationResultFlag;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class ErrorListPopUpPresenter extends PopUpPresenter {
		
		public function ErrorListPopUpPresenter(view:ErrorListPopUp) {
			super(view);
			ErrorListPopUp(_view).closeButton.visible = false;
			initListDataProvider();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			ErrorListPopUp(_view).okButton.addEventListener(MouseEvent.CLICK, okButtonClickHandler);
		}
		
		protected function okButtonClickHandler(event:MouseEvent):void {
			ErrorListPopUp(_view).dispatchEvent(new CloseEvent(CloseEvent.CLOSE));
		}
		
		protected function initListDataProvider():void {
			var dataProvider:ArrayCollection = new ArrayCollection();
			var record:RecordProxy = Application.activeRecord;
			var rootEntity:EntityProxy = record.rootEntity;
			
			var queue:Queue = new Queue();
			queue.push(rootEntity);
			var message:String, label:String, item:Object;
			while ( ! queue.isEmpty() ) {
				var node:NodeProxy = NodeProxy(queue.pop());
				var messages:Array = null;
				if ( node is AttributeProxy ) {
					var attribute:AttributeProxy = AttributeProxy(node);
					if ( attribute.hasErrors() ) {
						item = createAttributeDataGridItem(attribute);
						dataProvider.addItem(item);
					}
				} else {
					var entity:EntityProxy = EntityProxy(node);
					var items:IList = createEntityDataGridItems(entity);
					if ( items.length > 0 ) {
						dataProvider.addAll(items);
					}
					var children:IList = entity.getChildren();
					queue.pushAll(children.toArray());
				}
			}
			ErrorListPopUp(_view).dataGrid.dataProvider = dataProvider;
		}
		
		protected function createAttributeDataGridItem(attribute:AttributeProxy):Object {
			var messages:Array = attribute.validationResults.validationMessages;
			var message:String = StringUtil.concat(", ", messages);
			var label:String = createNodeLabel(attribute);
			var item:Object = {label:label, message: message};
			return item;
		}
		
		protected function createEntityDataGridItems(entity:EntityProxy):IList {
			var result:IList = new ArrayCollection();
			var schema:SchemaProxy = Application.activeSurvey.schema;
			var entityDefn:EntityDefinitionProxy = EntityDefinitionProxy(schema.getDefinitionById(entity.definitionId));
			var childDefinitions:ListCollectionView = entityDefn.childDefinitions;
			var messages:Array
			for each (var childDefn:NodeDefinitionProxy in childDefinitions) {
				var childName:String = childDefn.name;
				var minCountValid:ValidationResultFlag = entity.childrenMinCountValidationMap.get(childName);
				var maxCountValid:ValidationResultFlag = entity.childrenMaxCountValidationMap.get(childName);
				if(minCountValid == ValidationResultFlag.ERROR || maxCountValid == ValidationResultFlag.ERROR) {
					if ( minCountValid == ValidationResultFlag.ERROR ) {
						messages = [Message.get("edit.validation.minCount", [childDefn.minCount > 0 ? childDefn.minCount: 1])];
					} else {
						messages = [Message.get("edit.validation.maxCount", [childDefn.maxCount > 0 ? childDefn.maxCount: 1])];
					}
					var message:String = StringUtil.concat(", ", messages);
					var label:String = (entity.parent != null ? createNodeLabel(entity) + " / ": "") + childDefn.getLabelText();
					var item:Object = {label:label, message: message};
					result.addItem(item);
				}
			}
			return result;
		}
		
		protected function createNodeLabel(node:NodeProxy):String {
			var defn:NodeDefinitionProxy = Application.activeSurvey.schema.getDefinitionById(node.definitionId);
			var label:String = defn.getLabelText();
			if ( defn is EntityDefinitionProxy ) {
				var keyText:String = EntityProxy(node).keyText;
				if ( StringUtil.isBlank(keyText) ) {
					label += "[" + (node.index + 1) + "]";
				} else {
					label += " " + keyText;
				}
			}
			if ( node.parent == null || node.parent.parent == null ) {
				return label;
			} else {
				return createNodeLabel(node.parent) + " / " + label;
			}
		}
	}
}