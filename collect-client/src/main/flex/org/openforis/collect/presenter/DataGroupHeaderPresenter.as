package org.openforis.collect.presenter
{
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
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
		
		private var _view:DataGroupHeader;
		
		public function DataGroupHeaderPresenter(view:DataGroupHeader) {
			this._view = view;
			super();
			initNodeDefinitions();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
		}
		
		protected function initNodeDefinitions():void {
			var temp:IList = null;
			if(_view.entityDefinition != null) {
				temp = _view.entityDefinition.getDefinitionsInVersion(_view.modelVersion);
			}
			_view.nodeDefinitions = temp;
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(_view.parentEntity != null && _view.entityDefinition.hasHideableDefinitions() ) {
				var survey:SurveyProxy = Application.activeSurvey;
				var record:RecordProxy = Application.activeRecord;
				var changeSet:NodeChangeSetProxy = NodeChangeSetProxy(event.result);
				for each (var change:NodeChangeProxy in changeSet.changes) {
					if ( change is EntityChangeProxy ) {
						var nodeChange:EntityChangeProxy = EntityChangeProxy(change);
						var entity:NodeProxy = record.getNode(nodeChange.nodeId);
						var entityDefn:EntityDefinitionProxy = EntityDefinitionProxy(entity.definition);
						if ( entityDefn.id == _view.entityDefinition.id || entityDefn.isDescendantOf(_view.entityDefinition) ) {
							updateChildrenVisualization();
							break;
						}
					} else if ( change is AttributeChangeProxy ) {
						var attributeChange:AttributeChangeProxy = AttributeChangeProxy(change);
						
						//TODO update children visualization when not relevant node becomes empty/not empty
					}
				}
			}
		}
		
		private function updateChildrenVisualization():void {
			var visibilityByChildIndex:ArrayCollection = new ArrayCollection();
			var hideableDefinitions:IList = _view.entityDefinition.getHideableDefinitions();
			if ( hideableDefinitions.length > 0 ) {
				var entities:IList = _view.parentEntity.getChildren(_view.entityDefinition.name);
				for each ( var nodeDefn:NodeDefinitionProxy in _view.nodeDefinitions ) {
					if ( _view.modelVersion == null || _view.modelVersion.isApplicable(nodeDefn) ) {
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
						visibilityByChildIndex.addItem(visible);
					}
				}
			} else {
				for each ( var nodeDefn:NodeDefinitionProxy in _view.nodeDefinitions ) {
					if ( _view.modelVersion == null || _view.modelVersion.isApplicable(nodeDefn) ) {
						visibilityByChildIndex.addItem(true);
					}
				}
			}
			_view.visibilityByChildIndex = visibilityByChildIndex;
		}
		
		
	}
}