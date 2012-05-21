package org.openforis.collect.presenter
{
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.collections.IList;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.ui.component.input.InputField;
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
			eventDispatcher.addEventListener(InputFieldEvent.VISITED, inputFieldVisitedHandler);
		}
		
		private function get view():MultipleEntityFormItem {
			return MultipleEntityFormItem(_view);
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
				var entities:IList = getEntities();
				view.dataGroup.dataProvider = entities;
			} else {
				view.dataGroup.dataProvider = null;
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
				ClientFactory.dataClient.updateActiveRecord(req, addResultHandler, faultHandler);
			} else {
				var labelText:String = view.entityDefinition.getLabelText();
				AlertUtil.showError("edit.maxCountExceed", [maxCount, labelText]);
			}
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			view.callLater(function():void {
				updateValidationDisplayManager();
				
				if(view.scroller != null && view.scroller.verticalScrollBar != null) {
					view.scroller.verticalScrollBar.value = view.scroller.verticalScrollBar.maximum;
				}
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
		
	}
}