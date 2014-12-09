package org.openforis.collect.presenter
{
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.events.CloseEvent;
	
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
			view.closeButton.visible = false;
			initListDataProvider();
		}
		
		private function get view():ErrorListPopUp {
			return ErrorListPopUp(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.okButton.addEventListener(MouseEvent.CLICK, okButtonClickHandler);
		}
		
		protected function okButtonClickHandler(event:MouseEvent):void {
			view.dispatchEvent(new CloseEvent(CloseEvent.CLOSE));
		}
		
		protected function initListDataProvider():void {
			var dataProvider:ArrayCollection = new ArrayCollection();
			var record:RecordProxy = Application.activeRecord;
			var rootEntity:EntityProxy = record.rootEntity;
			
			var queue:Queue = new Queue();
			queue.push(rootEntity);
			while ( ! queue.isEmpty() ) {
				var node:NodeProxy = NodeProxy(queue.pop());
				if ( node is AttributeProxy ) {
					var attribute:AttributeProxy = AttributeProxy(node);
					if ( attribute.hasErrors() ) {
						var item:Object = createAttributeDataGridItem(attribute);
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
			view.dataGrid.dataProvider = dataProvider;
		}
		
		protected function createAttributeDataGridItem(attribute:AttributeProxy):Object {
			var messages:IList = attribute.validationResults.validationMessages;
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
				var minCountValid:ValidationResultFlag = entity.getMinCountValidation(childDefn);
				var maxCountValid:ValidationResultFlag = entity.getMaxCountValidation(childDefn);
				if(minCountValid == ValidationResultFlag.ERROR || maxCountValid == ValidationResultFlag.ERROR) {
					if ( minCountValid == ValidationResultFlag.ERROR ) {
						var minCount:int = entity.getMinCount(childDefn);
						if ( minCount == 1 ) {
							messages = [Message.get("edit.validation.requiredField")];
						} else {
							messages = [Message.get("edit.validation.minCount", [minCount])];
						}
					} else {
						var maxCount:int = entity.getMaxCount(childDefn)
						messages = [Message.get("edit.validation.maxCount", [maxCount > 0 ? maxCount: 1])];
					}
					var message:String = StringUtil.concat(", ", messages);
					var label:String = (entity.parent != null ? createNodeLabel(entity) + " / ": "") + childDefn.getInstanceOrHeadingLabelText();
					var item:Object = {label:label, message: message};
					result.addItem(item);
				}
			}
			return result;
		}
		
		protected function createNodeLabel(node:NodeProxy):String {
			var defn:NodeDefinitionProxy = Application.activeSurvey.schema.getDefinitionById(node.definitionId);
			var label:String = defn.getInstanceOrHeadingLabelText();
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