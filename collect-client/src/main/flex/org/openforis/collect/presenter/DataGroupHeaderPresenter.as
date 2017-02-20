package org.openforis.collect.presenter
{
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.events.PropertyChangeEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.model.proxy.AttributeChangeProxy;
	import org.openforis.collect.model.proxy.EntityChangeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeChangeProxy;
	import org.openforis.collect.model.proxy.NodeChangeSetProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.ui.component.datagroup.DataGroupHeader;
	import org.openforis.collect.util.CollectionUtil;

	/**
	 * @author S. Ricci
	 * 
	 */
	public class DataGroupHeaderPresenter extends AbstractPresenter {
		
		public function DataGroupHeaderPresenter(view:DataGroupHeader) {
			super(view);
			initNodeDefinitions();
			updateChildrenVisibility();
		}
		
		private function get view():DataGroupHeader {
			return DataGroupHeader(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			ChangeWatcher.watch(view, "parentEntity", parentEntityChangeHandler);
		}
		
		override protected function initBroadcastEventListeners():void {
			super.initBroadcastEventListeners();
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
		}
		
		override protected function removeBroadcastEventListeners():void {
			super.removeBroadcastEventListeners();
			eventDispatcher.removeEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
		}
		
		protected function parentEntityChangeHandler(event:PropertyChangeEvent):void {
			updateChildrenVisibility();
		}
		
		protected function initNodeDefinitions():void {
			var temp:IList = null;
			if(view.entityDefinition != null) {
				temp = view.entityDefinition.getDefinitionsInVersion(view.modelVersion);
			}
			view.nodeDefinitions = temp;
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(view.parentEntity != null && view.entityDefinition.hasHideableDefinitions() ) {
				var survey:SurveyProxy = Application.activeSurvey;
				var record:RecordProxy = Application.activeRecord;
				var changeSet:NodeChangeSetProxy = NodeChangeSetProxy(event.result);
				for each (var change:NodeChangeProxy in changeSet.changes) {
					var node:NodeProxy = record.getNode(change.nodeId);
					if ( node != null && view.parentEntity.isAncestorOf(node) ) {
						if ( change is EntityChangeProxy ) {
							updateChildrenVisibility();
							break;
						} else if ( change is AttributeChangeProxy ) {
							var attributeChange:AttributeChangeProxy = AttributeChangeProxy(change);
							var hideableDefinitions:IList = view.entityDefinition.hideableDefinitions;
							if ( hideableDefinitions.length > 0 && CollectionUtil.contains(hideableDefinitions, node.definition) ) {
								updateChildVisibility(node.definition);
							}
						}
					}
				}
			}
		}
		
		private function updateChildrenVisibility():void {
			if ( view.parentEntity != null ) {
				var childDefinitionsInVersion:IList = getChildDefinitionsInVersion();
				var visibilityByChildIndex:ArrayCollection = new ArrayCollection();
				CollectionUtil.fill(visibilityByChildIndex, true, childDefinitionsInVersion.length);
				view.visibilityByChildIndex = visibilityByChildIndex;
				var hideableDefinitions:IList = view.entityDefinition.hideableDefinitions;
				if ( hideableDefinitions.length > 0 ) {
					for ( var idx:int = 0; idx < childDefinitionsInVersion.length; idx ++) {
						var nodeDefn:NodeDefinitionProxy = NodeDefinitionProxy(childDefinitionsInVersion.getItemAt(idx));
						updateChildVisibility(nodeDefn);
					}
				}
			}
		}
		
		protected function updateChildVisibility(nodeDefn:NodeDefinitionProxy):void {
			var hideableDefinitions:IList = view.entityDefinition.hideableDefinitions;
			var entities:IList = view.parentEntity.getChildren(view.entityDefinition);
			var visible:Boolean;
			if ( CollectionUtil.contains(hideableDefinitions, nodeDefn, "id") ) {
				if ( entities.length > 0 ) {
					var entity:EntityProxy = EntityProxy(entities.getItemAt(0));
					var allCousinsNotRelevantAndEmpty:Boolean = true;
					var cousins:IList = entity.getDescendantCousins(nodeDefn);
					for each (var cousin:NodeProxy in cousins) {
						if ( cousin.relevant || ! cousin.empty ) {
							allCousinsNotRelevantAndEmpty = false;
							break;
						}
					}
					visible = ! allCousinsNotRelevantAndEmpty;
				} else {
					visible = false;
				}
			} else {
				visible = true;
			}
			var childDefinitionsInVersion:IList = getChildDefinitionsInVersion();
			var idx:int = childDefinitionsInVersion.getItemIndex(nodeDefn);
			view.visibilityByChildIndex.setItemAt(visible, idx);
		}
		
		protected function getChildDefinitionsInVersion():IList {
			return view.entityDefinition.getNestedDefinitionsInVersion(view.modelVersion);
		}
	}
}