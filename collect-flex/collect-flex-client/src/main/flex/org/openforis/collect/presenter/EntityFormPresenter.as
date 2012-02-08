package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
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
			
			_view.addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
			_view.addButton.addEventListener(FocusEvent.FOCUS_IN, buttonFocusInHandler);
			_view.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
			_view.deleteButton.addEventListener(FocusEvent.FOCUS_IN, buttonFocusInHandler);
			_view.dropDownList.addEventListener(IndexChangeEvent.CHANGE, dropDownListChangeHandler);
		}
		
		protected function parentEntityChangeHandler(event:Event):void {
			updateView();
		}
		
		protected function updateView():void {
			if(_view.parentEntity != null && _view.entityDefinition != null) {
				var children:IList = _view.parentEntity.getChildren(_view.entityDefinition.name);
				if(_view.entityDefinition.multiple) {
					_view.dropDownList.dataProvider = children;
					_view.internalContainer.visible = false;
				}
			}
		}

		protected function buttonFocusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function addButtonClickHandler(event:MouseEvent):void {
			var req:UpdateRequest = new UpdateRequest();
			req.method = UpdateRequest$Method.ADD;
			req.parentNodeId = _view.parentEntity.id;
			req.nodeName = _view.entityDefinition.name;
			ClientFactory.dataClient.updateActiveRecord(new AsyncResponder(addResultHandler, faultHandler, null), req);
		}
		
		protected function deleteButtonClickHandler(event:MouseEvent):void {
			//TODO
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			var newEntity:EntityProxy = result.getItemAt(0) as EntityProxy;
			_view.parentEntity.addChild(newEntity);

			selectEntity(newEntity);
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