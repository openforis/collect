package org.openforis.collect.presenter
{
	import flash.events.Event;
	
	import mx.binding.utils.BindingUtils;
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	import mx.controls.Alert;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.NodeEvent;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.SchemaProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.remoting.service.UpdateResponse;
	import org.openforis.collect.ui.component.detail.CollectFormItem;
	import org.openforis.collect.ui.component.detail.RelevanceDisplayManager;
	import org.openforis.collect.ui.component.detail.ValidationDisplayManager;
	import org.openforis.collect.ui.component.input.FormItemContextMenu;
	import org.openforis.collect.util.CollectionUtil;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class FormItemPresenter extends AbstractPresenter {
		
		protected var _view:CollectFormItem;
		protected var _validationDisplayManager:ValidationDisplayManager;
		protected var _relevanceDisplayManager:RelevanceDisplayManager;
		private var _contextMenu:FormItemContextMenu;
		
		{
			eventDispatcher.addEventListener(NodeEvent.MOVE, nodeMoveHandler);
		}
		
		public function FormItemPresenter(view:CollectFormItem) {
			_view = view;
			_relevanceDisplayManager = new RelevanceDisplayManager(view);
			updateRelevanceDisplayManager();
			_contextMenu = new FormItemContextMenu(view);
			super();
			
			updateView();
		}
		
		private static function nodeMoveHandler(event:NodeEvent):void {
			var schema:SchemaProxy = Application.activeSurvey.schema;
			var record:RecordProxy = Application.activeRecord;
			var node:NodeProxy = event.node;
			var index:int = event.index;
			var parent:EntityProxy = EntityProxy(record.getNode(node.parentId));
			var nodeId:int = node.id;
			var nodeDefn:NodeDefinitionProxy = schema.getDefinitionById(node.definitionId);
			var nodeName:String = nodeDefn.name;
			var attributes:IList = parent.getChildren(nodeName);
			CollectionUtil.moveItem(attributes, node, index);
			
			var responder:IResponder = new AsyncResponder(moveResultHandler, faultHandler); 
			ClientFactory.dataClient.moveNode(responder, nodeId, index);
		}
		
		private static function moveResultHandler(event:ResultEvent, token:Object = null):void {
			//do nothing
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			eventDispatcher.addEventListener(ApplicationEvent.RECORD_SAVED, recordSavedHandler);
			eventDispatcher.addEventListener(ApplicationEvent.ASK_FOR_SUBMIT, askForSubmitHandler);
			ChangeWatcher.watch(_view, "parentEntity", parentEntityChangeHandler);
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(_view.parentEntity != null) {
				var responses:IList = IList(event.result);
				for each (var response:UpdateResponse in responses) {
					if(response.nodeId == _view.parentEntity.id) {
						updateValidationDisplayManager();
						updateRelevanceDisplayManager();
						_contextMenu.updateItems();
						break;
					}
				}
			}
		}
		
		protected function recordSavedHandler(event:ApplicationEvent):void {
			updateValidationDisplayManager();
		}
		
		protected function askForSubmitHandler(event:ApplicationEvent):void {
			updateValidationDisplayManager();
		}
		
		protected function parentEntityChangeHandler(event:Event):void {
			updateView();
		}
		
		protected function updateView():void {
			updateRelevanceDisplayManager();
			updateValidationDisplayManager();
			_contextMenu.updateItems();
		}
		
		protected function initValidationDisplayManager():void {
			_validationDisplayManager = new ValidationDisplayManager(_view, _view);
		}
		
		protected function updateRelevanceDisplayManager():void {
			
		}
		
		protected function updateValidationDisplayManager():void {
			if(_validationDisplayManager == null) {
				initValidationDisplayManager();
			}
		}
		
		protected function get validationDisplayManager():ValidationDisplayManager {
			return _validationDisplayManager;
		}
		
		protected function get relevanceDisplayManager():RelevanceDisplayManager {
			return _relevanceDisplayManager;
		}
	}
}