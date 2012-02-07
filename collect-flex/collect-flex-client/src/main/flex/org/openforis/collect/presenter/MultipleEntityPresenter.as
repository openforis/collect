package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;

	public class MultipleEntityPresenter extends AbstractPresenter {
		
		public static const EMPTY_ENTITY:Object = new Object();
		
		protected var _view:MultipleEntityFormItem;
		
		public function MultipleEntityPresenter(view:MultipleEntityFormItem) {
			_view = view;
			
			super();
			
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			ChangeWatcher.watch(_view, "parentEntity", parentEntityChangeHandler);
			
			_view.addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
			_view.addButton.addEventListener(FocusEvent.FOCUS_IN, addButtonFocusInHandler);
		}
		
		protected function parentEntityChangeHandler(event:Event):void {
			updateView();
		}
		
		protected function updateView():void {
			if(_view.dataGroup != null) {
				if(_view.parentEntity != null) {
					var name:String = _view.entityDefinition.name
					var entities:IList = _view.parentEntity.getChildren(name);
					if(entities != null && entities.length > 0) {
						_view.dataGroup.dataProvider = entities;
						return;
					}
				}
				//add empty entity
				var c:ArrayCollection = new ArrayCollection();
				c.addItem(EMPTY_ENTITY);
				_view.dataGroup.dataProvider = c;
			}
		}

		protected function addButtonFocusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function addButtonClickHandler(event:MouseEvent):void {
			var entities:IList = _view.parentEntity.getChildren(_view.entityDefinition.name);
			if(CollectionUtil.isEmpty(entities)) {
				//add another entity because the first one was a fake, empty entity
				addNewEntity();
			}
			addNewEntity();
		}
		
		protected function addNewEntity():void {
			var req:UpdateRequest = new UpdateRequest();
			req.method = UpdateRequest$Method.ADD;
			req.parentNodeId = _view.parentEntity.id;
			req.nodeName = _view.entityDefinition.name;
			ClientFactory.dataClient.updateActiveRecord(new AsyncResponder(addResultHandler, faultHandler, null), req);
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			var newEntity:EntityProxy = result.getItemAt(0) as EntityProxy;
			_view.parentEntity.addChild(newEntity);
			updateView();
		}
		
	}
}