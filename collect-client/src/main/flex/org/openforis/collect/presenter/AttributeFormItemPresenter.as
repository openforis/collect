package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	import mx.events.PropertyChangeEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.proxy.AttributeChangeProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeChangeProxy;
	import org.openforis.collect.model.proxy.NodeChangeSetProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class AttributeFormItemPresenter extends FormItemPresenter {
		
		public function AttributeFormItemPresenter(view:AttributeFormItem) {
			super(view);
		}
		
		private function get view():AttributeFormItem {
			return AttributeFormItem(_view);
		}
		
		override public function init():void {
			super.init();
			assignAttribute();
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
		}
		
		protected function focusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function focusOutHandler(event:FocusEvent):void {
		}
		
		override protected function parentEntityChangeHandler(event:PropertyChangeEvent):void {
			assignAttribute();
		}
		
		protected function attributeChangeHandler(event:Event):void {
			updateView();
		}
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			super.updateResponseReceivedHandler(event);
			if ( view.nodeDefinition.parentLayout == UIUtil.LAYOUT_TABLE && view.nodeDefinition.hideWhenNotRelevant ) {
				//current form item node is inside a table entity, update relevance display if changed node is a cousin of the current one
				var changeSet:NodeChangeSetProxy = NodeChangeSetProxy(event.result);
				for each (var change:NodeChangeProxy in changeSet.changes) {
					if ( change is AttributeChangeProxy ) {
						var changedNode:NodeProxy = Application.activeRecord.getNode(change.nodeId);
						if ( changedNode != null && changedNode.definition.id == view.nodeDefinition.id && view.parentEntity.isDescendantCousin(changedNode) ) {
							updateRelevanceDisplayManager();
							break;
						}
					}
				}
			}
		}
		
		override protected function updateRelevanceDisplayManager():void {
			super.updateRelevanceDisplayManager();
			relevanceDisplayManager.displayNodeRelevance(view.parentEntity, view.attributeDefinition);
		}
		
		override protected function updateValidationDisplayManager():void {
			//validation display managed by AttributePresenter
			return;
		}

		/**
		 * get the attribute (or attributes) from the parentEntity
		 */
		protected function assignAttribute():void {
			var attrDefn:AttributeDefinitionProxy = view.attributeDefinition;
			if (view.parentEntity != null && attrDefn != null) {
				if (view.attributeDefinition.multiple) {
					var attributes:IList = view.parentEntity.getChildren(attrDefn);
					view.attributes = attributes;
				} else {
					var attribute:AttributeProxy = view.parentEntity.getSingleAttribute(attrDefn);
					view.attribute = attribute;
				}
			} else {
				view.attribute = null;
				view.attributes = null;
			}
		}
		
	}
}