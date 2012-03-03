package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.remoting.service.UpdateResponse;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.UIUtil;
	
	import spark.events.IndexChangeEvent;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class EntityFormPresenter extends AbstractPresenter {
		
		protected var _view:EntityFormContainer;
		
		public function EntityFormPresenter(view:EntityFormContainer) {
			_view = view;
			
			super();
			
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			ChangeWatcher.watch(_view, "parentEntity", parentEntityChangeHandler);
			
			if(_view.currentState == EntityFormContainer.STATE_WITH_TABS) {
				_view.addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
				_view.addButton.addEventListener(FocusEvent.FOCUS_IN, buttonFocusInHandler);
				_view.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
				_view.deleteButton.addEventListener(FocusEvent.FOCUS_IN, buttonFocusInHandler);
				_view.dropDownList.addEventListener(IndexChangeEvent.CHANGE, dropDownListChangeHandler);
			}
		}
		
		protected function parentEntityChangeHandler(event:Event):void {
			updateView();
		}
		
		protected function updateView():void {
			if(_view.entityDefinition != null && _view.entityDefinition.multiple) {
				var entities:IList = getEntities();
				_view.entities = entities;
				selectFirstTab();
			}
		}
		
		protected function getEntities():IList {
			var entities:IList = null;
			if(_view.parentEntity != null && _view.entityDefinition != null) {
				entities = _view.parentEntity.getChildren(_view.entityDefinition.name);
			}
			return entities;
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
			ClientFactory.dataClient.updateActiveRecord(new AsyncResponder(addResultHandler, faultHandler, null), req);
		}
		
		protected function deleteButtonClickHandler(event:MouseEvent):void {
			AlertUtil.showConfirm("global.confirmDelete", [_view.entityDefinition.getLabelText()], 
				"global.confirmAlertTitle", performDeletion);
		}
		
		protected function performDeletion():void {
			var o:UpdateRequestOperation = new UpdateRequestOperation();
			o.method = UpdateRequestOperation$Method.DELETE;
			o.parentEntityId = _view.parentEntity.id;
			o.nodeId = _view.entity.id;
			var req:UpdateRequest = new UpdateRequest(o);
			ClientFactory.dataClient.updateActiveRecord(new AsyncResponder(deleteResultHandler, faultHandler, null), req);
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			var responses:IList = IList(event.result);
			Application.activeRecord.update(responses);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = responses;
			eventDispatcher.dispatchEvent(appEvt);
			//select the inserted entity
			_view.callLater(function():void {
				var entities:IList = getEntities();
				var lastEntity:EntityProxy = entities.getItemAt(entities.length -1) as EntityProxy; 
				selectEntity(lastEntity);
			});
		}
		
		protected function deleteResultHandler(event:ResultEvent, token:Object = null):void {
			var responses:IList = IList(event.result);
			Application.activeRecord.update(responses);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = responses;
			eventDispatcher.dispatchEvent(appEvt);
		}
		
		protected function dropDownListChangeHandler(event:IndexChangeEvent):void {
			var entity:EntityProxy = _view.dropDownList.selectedItem as EntityProxy;
			selectEntity(entity);
		}
		
		protected function selectEntity(entity:EntityProxy):void {
			_view.dropDownList.selectedItem = entity;
			_view.entity = entity;
			if(_view.internalContainer.visible) {
				//internal container already visible, call programmatically the showEffect
				_view.showFormEffect.play([_view.internalContainer]);
			} else {
				_view.internalContainer.visible = true;
			}
			selectFirstTab();
		}
		
		protected function selectFirstTab():void {
			_view.resetScrollbars();
			if(_view.tabBar != null) {
				_view.tabBar.selectedIndex = 0;
			}
		}
	}
}