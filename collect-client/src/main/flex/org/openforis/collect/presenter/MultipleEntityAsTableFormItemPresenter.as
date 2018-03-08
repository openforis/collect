package org.openforis.collect.presenter
{
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.collections.IList;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.proxy.EntityAddRequestProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeChangeProxy;
	import org.openforis.collect.model.proxy.NodeChangeSetProxy;
	import org.openforis.collect.model.proxy.NodeDeleteChangeProxy;
	import org.openforis.collect.model.proxy.NodeUpdateRequestSetProxy;
	import org.openforis.collect.ui.component.detail.MultipleEntityAsTableFormItem;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class MultipleEntityAsTableFormItemPresenter extends EntityFormItemPresenter {
		
		public function MultipleEntityAsTableFormItemPresenter(view:MultipleEntityAsTableFormItem) {
			super(view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			
			view.addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
			view.addButton.addEventListener(FocusEvent.FOCUS_IN, addButtonFocusInHandler);
		}
		
		override protected function initBroadcastEventListeners():void {
			super.initBroadcastEventListeners();
			eventDispatcher.addEventListener(InputFieldEvent.VISITED, inputFieldVisitedHandler);
		}
		
		override protected function removeBroadcastEventListeners():void {
			super.removeBroadcastEventListeners();
			eventDispatcher.removeEventListener(InputFieldEvent.VISITED, inputFieldVisitedHandler);
		}
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			super.updateResponseReceivedHandler(event);
			if(view.parentEntity != null) {
				var changeSet:NodeChangeSetProxy = NodeChangeSetProxy(event.result);
				for each (var change:NodeChangeProxy in changeSet.changes) {
					if (change is NodeDeleteChangeProxy 
						&& NodeDeleteChangeProxy(change).parentEntityId == view.parentEntity.id
						&& NodeDeleteChangeProxy(change).nodeName == view.nodeDefinition.name) {
						checkAddButtonVisibility();
					}
				}
			}
		}
		
		private function get view():MultipleEntityAsTableFormItem {
			return MultipleEntityAsTableFormItem(_view);
		}
		
		override protected function initValidationDisplayManager():void {
			super.initValidationDisplayManager();
			_validationDisplayManager.showMinMaxCountErrors = true;
		}
		
		override protected function updateView():void {
			var entities:IList = null;
			if(view.entityDefinition != null
					&& view.entityDefinition.multiple
					&& view.parentEntity != null) {
				entities = getEntities();
			}
			view.dataGroup.dataProvider = entities;
			
			checkAddButtonVisibility();

			super.updateView();
		}
		
		private function checkAddButtonVisibility():void {
			if (view.parentEntity != null) {
				var entities:IList = getEntities();
				var maxCount:int = view.parentEntity.getMaxCount(view.entityDefinition);
				view.addButton.visible = view.addButton.includeInLayout = 
					Application.activeRecordEditable 
					&& !(view.entityDefinition.enumerable && view.entityDefinition.enumerate) 
					&& (CollectionUtil.isEmpty(entities) || entities.length < maxCount);
			}
		}
		
		protected function getEntities():IList {
			var entityDef:EntityDefinitionProxy = view.entityDefinition;
			var entities:IList = null;
			if(view.parentEntity != null) {
				entities = view.parentEntity.getChildren(entityDef);
			}
			return entities;
		}

		protected function addButtonFocusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function addButtonClickHandler(event:MouseEvent):void {
			var entities:IList = getEntities();
			var maxCount:int = view.parentEntity.getMaxCount(view.entityDefinition);
			if(CollectionUtil.isEmpty(entities) || entities.length < maxCount) {
				var r:EntityAddRequestProxy = new EntityAddRequestProxy();
				r.parentEntityId = view.parentEntity.id;
				r.nodeName = view.entityDefinition.name;
				var reqSet:NodeUpdateRequestSetProxy = new NodeUpdateRequestSetProxy(r);
				ClientFactory.dataClient.updateActiveRecord(reqSet, addResultHandler, faultHandler);
			} else {
				var labelText:String = view.entityDefinition.getInstanceOrHeadingLabelText();
				AlertUtil.showError("edit.maxCountExceed", [maxCount, labelText]);
			}
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			view.callLater(function():void {
				updateValidationDisplayManager();
				
				if ( view.scroller != null && view.scroller.verticalScrollBar != null ) {
					view.scroller.verticalScrollBar.value = view.scroller.verticalScrollBar.maximum;
				}
				checkAddButtonVisibility();
				UIUtil.ensureElementIsVisible(view.addButton);
			});
		}
		
		protected function inputFieldVisitedHandler(event:InputFieldEvent):void {
			var inputField:InputField = event.inputField;
			if(inputField != null && inputField.parentEntity != null) {
				var entities:IList = getEntities();
				for each (var e:EntityProxy in entities) {
					if(e == inputField.parentEntity) {
						updateValidationDisplayManager();
						break;
					}
				}
			}
		}
		
		override protected function updateValidationDisplayManager():void {
			super.updateValidationDisplayManager();
			if(view.parentEntity != null) {
				var entityDefn:EntityDefinitionProxy = view.entityDefinition;
				var visited:Boolean = view.parentEntity.isErrorOnChildVisible(entityDefn);
				var active:Boolean = visited;
				if(active) {
					_validationDisplayManager.active = true;
					_validationDisplayManager.displayMinMaxCountValidationErrors(view.parentEntity, entityDefn);
				} else {
					_validationDisplayManager.active = false;
					_validationDisplayManager.reset();
				}
			}
		}
		
	}
}