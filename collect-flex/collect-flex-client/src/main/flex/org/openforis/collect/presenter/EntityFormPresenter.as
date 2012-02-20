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
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
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
			var req:UpdateRequest = new UpdateRequest();
			req.method = UpdateRequest$Method.ADD;
			req.parentEntityId = _view.parentEntity.id;
			req.nodeName = _view.entityDefinition.name;
			ClientFactory.dataClient.updateActiveRecord(new AsyncResponder(addResultHandler, faultHandler, null), req);
		}
		
		protected function deleteButtonClickHandler(event:MouseEvent):void {
			AlertUtil.showConfirm("global.confirmDelete", [_view.entityDefinition.getLabelText()], 
				"global.confirmAlertTitle", performDeletion);
		}
		
		protected function performDeletion():void {
			var req:UpdateRequest = new UpdateRequest();
			req.method = UpdateRequest$Method.DELETE;
			req.parentEntityId = _view.parentEntity.id;
			req.nodeId = _view.entity.id;
			ClientFactory.dataClient.updateActiveRecord(new AsyncResponder(deleteResultHandler, faultHandler, null), req);
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			Application.activeRecord.update(result);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = result;
			eventDispatcher.dispatchEvent(appEvt);
			//select the inserted entity
			_view.callLater(function():void {
				var entities:IList = getEntities();
				var lastEntity:EntityProxy = entities.getItemAt(entities.length -1) as EntityProxy; 
				selectEntity(lastEntity);
			});
		}
		
		protected function deleteResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			Application.activeRecord.update(result);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = result;
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
				
				//todo reset entity form scrollers, set first tab
			} else {
				_view.internalContainer.visible = true;
			}
		}
		
		
	}
}