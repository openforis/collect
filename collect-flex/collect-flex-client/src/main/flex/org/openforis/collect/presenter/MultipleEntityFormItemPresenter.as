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
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
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
		
		override protected function modelChangedHandler(event:ApplicationEvent):void {
			if(view.dataGroup.dataProvider == null) {
				updateView();
			}
		}
		
		override protected function updateView():void {
			if(view.dataGroup != null && view.parentEntity != null) {
				var name:String = view.entityDefinition.name;
				var entities:IList = view.parentEntity.getChildren(name);
				view.dataGroup.dataProvider = entities;
			}
		}

		protected function addButtonFocusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function addButtonClickHandler(event:MouseEvent):void {
			var req:UpdateRequest = new UpdateRequest();
			req.method = UpdateRequest$Method.ADD;
			req.parentNodeId = view.parentEntity.id;
			req.nodeName = view.entityDefinition.name;
			ClientFactory.dataClient.updateActiveRecord(new AsyncResponder(addResultHandler, faultHandler, null), req);
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			Application.activeRecord.update(result);
			eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.MODEL_CHANGED));
			
			view.callLater(function():void {
				UIUtil.ensureElementIsVisible(view.addButton);
			});
		}
		
	}
}