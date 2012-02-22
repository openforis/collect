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
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class MultipleEntityFormItemPresenter extends EntityFormItemPresenter {
		
		public function MultipleEntityFormItemPresenter(view:MultipleEntityFormItem) {
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			view.addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
			view.addButton.addEventListener(FocusEvent.FOCUS_IN, addButtonFocusInHandler);
		}
		
		private function get view():MultipleEntityFormItem {
			return MultipleEntityFormItem(_view);
		}
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			var list:IList = event.result as IList;
			for each(var node:NodeProxy in list) {
				if(view.parentEntity != null && view.entityDefinition != null 
					&& node.parentId == view.parentEntity.id 
					&& view.entityDefinition.name == node.name) {
					updateView();
				}
			}
		}
		
		override protected function updateView():void {
			if(view.entityDefinition != null 
					&& view.parentEntity != null 
					&& view.modelVersion != null) {
				var entities:IList = getEntities();
				view.dataGroup.dataProvider = entities;
			} else {
				view.dataGroup.dataProvider = null;
			}
		}
		
		protected function getEntities():IList {
			var name:String = view.entityDefinition.name;
			var entities:IList = view.parentEntity.getChildren(name);
			return entities;
		}

		protected function addButtonFocusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function addButtonClickHandler(event:MouseEvent):void {
			var entities:IList = getEntities();
			var maxCount:Number = view.entityDefinition.maxCount
			if(isNaN(maxCount) || CollectionUtil.isEmpty(entities) || entities.length < maxCount) {
				var req:UpdateRequest = new UpdateRequest();
				req.method = UpdateRequest$Method.ADD;
				req.parentEntityId = view.parentEntity.id;
				req.nodeName = view.entityDefinition.name;
				ClientFactory.dataClient.updateActiveRecord(new AsyncResponder(addResultHandler, faultHandler, null), req);
			} else {
				var labelText:String = view.entityDefinition.getLabelText();
				AlertUtil.showError("edit.maxCountExceed", [maxCount, labelText]);
			}
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			Application.activeRecord.update(result);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = result;
			eventDispatcher.dispatchEvent(appEvt);
			
			view.callLater(function():void {
				UIUtil.ensureElementIsVisible(view.addButton);
			});
		}
		
	}
}