package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	import flash.utils.Timer;
	import flash.utils.setTimeout;
	
	import mx.binding.utils.BindingUtils;
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.events.PropertyChangeEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;
	
	import spark.events.IndexChangeEvent;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class EntityFormPresenter extends AbstractPresenter {
		
		protected var _view:EntityFormContainer;
		private var _keyTextChangeWatchers:Array;
		
		public function EntityFormPresenter(view:EntityFormContainer) {
			_view = view;
			_keyTextChangeWatchers = new Array();

			super();
			
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			ChangeWatcher.watch(_view, "parentEntity", parentEntityChangeHandler);
			
			if(_view.entityDefinition != null && _view.entityDefinition.multiple && _view.entityDefinition.parent != null) {
				_view.addSection.addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
				_view.addSection.addButton.addEventListener(FocusEvent.FOCUS_IN, buttonFocusInHandler);
				_view.addSection.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
				_view.addSection.deleteButton.addEventListener(FocusEvent.FOCUS_IN, buttonFocusInHandler);
				_view.addSection.dropDownList.addEventListener(IndexChangeEvent.CHANGE, dropDownListChangeHandler);
			}
		}
		
		protected function parentEntityChangeHandler(event:Event):void {
			updateView();
		}
		
		protected function updateView():void {
			if(_view.entityDefinition != null) {
				var entities:IList = getEntities();
				if (CollectionUtil.isNotEmpty(entities) ) {
					if ( _view.entityDefinition.parent == null ) {
						_view.entity = entities.getItemAt(0) as EntityProxy;
						_view.internalContainer.visible = true;
					} else {
						_view.entities = EntityProxy.sortEntitiesByKey(entities);
						if ( _view.entityDefinition.multiple && _view.entityDefinition.layout == UIUtil.LAYOUT_FORM ) {
							selectEntity(null);
							selectFirstTab();
						} else {
							_view.internalContainer.visible = true;
						}
					}
				}
			}
		}
		
		protected function getEntities():IList {
			var entities:IList = null;
			if(_view.parentEntity != null && _view.entityDefinition != null) {
				entities = _view.parentEntity.getChildren(_view.entityDefinition.name);
			}
			return entities;
		}
		
		protected function updateViewEntities():void {
			var selectedEntity:* = _view.addSection.dropDownList.selectedItem;
			var entities:IList = getEntities();
			_view.entities = EntityProxy.sortEntitiesByKey(entities);
			initEntitiesKeyTextChangeWatchers();
			_view.addSection.dropDownList.selectedItem = selectedEntity;
		}
		
		protected function initEntitiesKeyTextChangeWatchers():void {
			for each (var cw:ChangeWatcher in _keyTextChangeWatchers) { 
				cw.unwatch();
			}
			_keyTextChangeWatchers = new Array();
			for each (var entity:EntityProxy in _view.entities) {
				var watcher:ChangeWatcher = ChangeWatcher.watch(entity, "keyText", entityKeyTextChangeHandler);
				_keyTextChangeWatchers.push(watcher);
			}
		}
		
		protected function entityKeyTextChangeHandler(event:PropertyChangeEvent):void {
			updateViewEntities();
		}
		
		protected function buttonFocusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function addButtonClickHandler(event:MouseEvent):void {
			var o:UpdateRequestOperation = new UpdateRequestOperation();
			o.method = UpdateRequestOperation$Method.ADD;
			o.parentEntityId = _view.parentEntity.id;
			o.nodeName = _view.entityDefinition.name;
			var req:UpdateRequest = new UpdateRequest(o);
			ClientFactory.dataClient.updateActiveRecord(req, addResultHandler, faultHandler);
		}
		
		protected function deleteButtonClickHandler(event:MouseEvent):void {
			AlertUtil.showConfirm("global.confirmDelete", [_view.entityDefinition.getLabelText()], 
				"global.confirmAlertTitle", performDeletion);
		}
		
		protected function performDeletion():void {
			var o:UpdateRequestOperation = new UpdateRequestOperation();
			o.method = UpdateRequestOperation$Method.DELETE;
			o.parentEntityId = _view.parentEntity.id;
			o.nodeName = _view.entity.name;
			o.nodeId = _view.entity.id;
			var req:UpdateRequest = new UpdateRequest(o);
			ClientFactory.dataClient.updateActiveRecord(req, deleteResultHandler, faultHandler);
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			updateViewEntities();
			//select the inserted entity
			_view.addSection.callLater(function():void {
				var entities:IList = getEntities();
				var lastEntity:EntityProxy = entities.getItemAt(entities.length - 1) as EntityProxy;
				selectEntity(lastEntity);
			});
		}
		
		protected function deleteResultHandler(event:ResultEvent, token:Object = null):void {
			var responses:IList = IList(event.result);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = responses;
			eventDispatcher.dispatchEvent(appEvt);
			selectEntity(null);
			updateViewEntities();
		}
		
		protected function dropDownListChangeHandler(event:IndexChangeEvent):void {
			var entity:EntityProxy = _view.addSection.dropDownList.selectedItem as EntityProxy;
			selectEntity(entity);
		}
		
		protected function selectEntity(entity:EntityProxy):void {
			_view.addSection.dropDownList.selectedItem = entity;
			_view.entity = entity;
			if(entity != null) {
				selectFirstTab();
				if(_view.internalContainer.visible) {
					//internal container already visible, call programmatically the showEffect
					_view.showFormEffect.play([_view.internalContainer]);
				} else {
					_view.internalContainer.visible = true;
				}
			} else if(_view.entityDefinition == null || _view.entityDefinition.multiple) {
				_view.internalContainer.visible = false;
			}
		}
		
		protected function selectFirstTab():void {
			_view.resetScrollbars();
			if(_view.tabBar != null) {
				_view.tabBar.selectedIndex = 0;
			}
		}
	}
}