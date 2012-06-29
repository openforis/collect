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
	import org.openforis.collect.model.UIConfiguration;
	import org.openforis.collect.model.UITab;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;
	
	import spark.events.IndexChangeEvent;

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
			
			eventDispatcher.addEventListener(InputFieldEvent.VISITED, inputFieldVisitedHandler);
			
			view.addSection.addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
			view.addSection.addButton.addEventListener(FocusEvent.FOCUS_IN, buttonFocusInHandler);
			view.addSection.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
			view.addSection.deleteButton.addEventListener(FocusEvent.FOCUS_IN, buttonFocusInHandler);
			view.addSection.dropDownList.addEventListener(IndexChangeEvent.CHANGE, dropDownListChangeHandler);
		}
		
		private function get view():MultipleEntityFormItem {
			return MultipleEntityFormItem(_view);
		}
		
		protected function buttonFocusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			super.updateResponseReceivedHandler(event);
		}
		
		override protected function initValidationDisplayManager():void {
			super.initValidationDisplayManager();
			_validationDisplayManager.showMinMaxCountErrors = true;
		}
		
		override protected function updateView():void {
			if(view.entityDefinition != null
					&& view.entityDefinition.multiple
					&& view.parentEntity != null 
					&& view.modelVersion != null) {
				var uiTab:UITab = UIBuilder.getUITab(view.entityDefinition);
				var uiTabs:IList = null;
				if ( uiTab != null ) {
					uiTabs = uiTab.tabs;
				}
				if(uiTabs == null) {
					view.currentState = MultipleEntityFormItem.STATE_WITHOUT_TABS;
					view.definitionsPerMainTab = UIBuilder.getDefinitionsPerMainTab(view.entityDefinition, view.modelVersion);
				} else {
					view.uiTabs = uiTabs;
					view.definitionsPerTab = UIBuilder.getDefinitionsPerEachSubTab(view.entityDefinition, view.modelVersion);
					view.currentState = MultipleEntityFormItem.STATE_WITH_TABS;
				}
				var entities:IList = getEntities();
				view.entities = entities;
				selectEntity(null);
			}
			super.updateView();
		}
		
		protected function getEntities():IList {
			var name:String = view.entityDefinition.name;
			var entities:IList = null;
			if(view.parentEntity != null) {
				entities = view.parentEntity.getChildren(name);
			}
			return entities;
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
				ClientFactory.dataClient.updateActiveRecord(req, addResultHandler, faultHandler);
			} else {
				var labelText:String = view.entityDefinition.getLabelText();
				AlertUtil.showError("edit.maxCountExceed", [maxCount, labelText]);
			}
		}
		
		protected function deleteButtonClickHandler(event:MouseEvent):void {
			AlertUtil.showConfirm("global.confirmDelete", [view.entityDefinition.getLabelText()], 
				"global.confirmAlertTitle", performDeletion);
		}
		
		protected function performDeletion():void {
			var o:UpdateRequestOperation = new UpdateRequestOperation();
			o.method = UpdateRequestOperation$Method.DELETE;
			o.parentEntityId = _view.parentEntity.id;
			o.nodeId = view.entity.id;
			var req:UpdateRequest = new UpdateRequest(o);
			ClientFactory.dataClient.updateActiveRecord(req, deleteResultHandler, faultHandler);
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
				var name:String = view.entityDefinition.name;
				var visited:Boolean = view.parentEntity.isErrorOnChildVisible(name);
				var active:Boolean = visited;
				if(active) {
					_validationDisplayManager.active = true;
					_validationDisplayManager.displayMinMaxCountValidationErrors(view.parentEntity, view.entityDefinition);
				} else {
					_validationDisplayManager.active = false;
					_validationDisplayManager.reset();
				}
			}
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			//select the inserted entity
			_view.callLater(function():void {
				var entities:IList = getEntities();
				var lastEntity:EntityProxy = entities.getItemAt(entities.length -1) as EntityProxy; 
				selectEntity(lastEntity);
			});
		}
		
		protected function deleteResultHandler(event:ResultEvent, token:Object = null):void {
			var responses:IList = IList(event.result);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = responses;
			eventDispatcher.dispatchEvent(appEvt);
		}
		
		protected function dropDownListChangeHandler(event:IndexChangeEvent):void {
			var entity:EntityProxy = view.addSection.dropDownList.selectedItem as EntityProxy;
			selectEntity(entity);
		}
		
		protected function selectEntity(entity:EntityProxy):void {
			view.selectedEntity = entity;
			view.addSection.dropDownList.selectedItem = entity;
			view.entity = entity;
			if(entity != null) {
				if(view.internalContainer.visible) {
					//internal container already visible, call programmatically the showEffect
					view.showFormEffect.play([view.internalContainer]);
				} else {
					view.internalContainer.visible = true;
				}
			} else if(view.entityDefinition == null || view.entityDefinition.multiple) {
				view.internalContainer.visible = false;
			}
		}
		
	}
}