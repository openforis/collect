package org.openforis.collect.presenter
{
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.remoting.service.UpdateResponse;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.ui.component.detail.ValidationDisplayManager;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class MultipleEntityFormItemPresenter extends EntityFormItemPresenter {
		
		private var _validationDisplayManager:ValidationDisplayManager;
		
		public function MultipleEntityFormItemPresenter(view:MultipleEntityFormItem) {
			_validationDisplayManager = new ValidationDisplayManager(view, view);
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
			var response:UpdateResponse = UpdateResponse(event.result);
			for each(var node:NodeProxy in response.addedNodes) {
				if(view.parentEntity != null && view.entityDefinition != null 
					&& node.parentId == view.parentEntity.id 
					&& node.name == view.entityDefinition.name) {
					updateView();
				}
			}
		}
		
		override protected function updateView():void {
			super.updateView();
			if(view.entityDefinition != null
					&& view.entityDefinition.multiple
					&& view.parentEntity != null 
					&& view.modelVersion != null) {
				var entities:IList = getEntities();
				view.dataGroup.dataProvider = entities;
			} else {
				view.dataGroup.dataProvider = null;
			}
			/*
			//TODO if parent has min count validation errors for this element, init validationDisplayManager
			_validationDisplayManager.displayStyleName = ValidationDisplayManager.STYLE_NAME_ERROR;
			_validationDisplayManager.toolTipMessage = "Error";
			_validationDisplayManager.toolTipStyleName = ToolTipUtil.STYLE_NAME_ERROR;
			_validationDisplayManager.init();
			*/
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
				var o:UpdateRequestOperation = new UpdateRequestOperation();
				o.method = UpdateRequestOperation$Method.ADD;
				o.parentEntityId = view.parentEntity.id;
				o.nodeName = view.entityDefinition.name;
				var req:UpdateRequest = new UpdateRequest(o);
				ClientFactory.dataClient.updateActiveRecord(new AsyncResponder(addResultHandler, faultHandler, null), req);
			} else {
				var labelText:String = view.entityDefinition.getLabelText();
				AlertUtil.showError("edit.maxCountExceed", [maxCount, labelText]);
			}
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			var result:UpdateResponse = UpdateResponse(event.result);
			Application.activeRecord.update(result);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = result;
			eventDispatcher.dispatchEvent(appEvt);
			
			view.callLater(function():void {
				if(view.scroller != null && view.scroller.verticalScrollBar != null) {
					view.scroller.verticalScrollBar.value = view.scroller.verticalScrollBar.maximum;
				}
				UIUtil.ensureElementIsVisible(view.addButton);
			});
		}
		/*
		override protected function initNodeDefinitions(event:Event=null):void {
			super.initNodeDefinitions(event);
			initConstraintLayout(event);
		}
		
		protected function initConstraintLayout(event:Event = null):void {
			var constraintLayout:ConstraintLayout = new ConstraintLayout();
			var constraintColumns:Vector.<ConstraintColumn> = new Vector.<ConstraintColumn>();
			if(CollectionUtil.isNotEmpty(view.nodeDefinitions)) {
				for(var index:int = 0; index < view.nodeDefinitions.length; index ++) {
					var defn:NodeDefinitionProxy = view.nodeDefinitions[index] as NodeDefinitionProxy; 
					var constraintColumn:ConstraintColumn = new ConstraintColumn();
					constraintColumns.push(constraintColumn);
				}
			}
			constraintLayout.constraintColumns = constraintColumns;
			view.constraintLayout = constraintLayout;
		}
		*/
	}
}