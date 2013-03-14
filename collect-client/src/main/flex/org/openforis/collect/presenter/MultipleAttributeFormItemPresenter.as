package org.openforis.collect.presenter
{
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.collections.IList;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.remoting.service.UpdateResponse;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class MultipleAttributeFormItemPresenter extends FormItemPresenter {
		
		public function MultipleAttributeFormItemPresenter(view:MultipleAttributeFormItem) {
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			view.addButton.addEventListener(MouseEvent.CLICK, addButtonClickHandler);
			view.addButton.addEventListener(FocusEvent.FOCUS_IN, addButtonFocusInHandler);
			eventDispatcher.addEventListener(InputFieldEvent.VISITED, inputFieldVisitedHandler);
		}
		
		private function get view():MultipleAttributeFormItem {
			return MultipleAttributeFormItem(_view);
		}
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			super.updateResponseReceivedHandler(event);
			if(_view.parentEntity != null) {
				var responses:IList = IList(event.result);
				for each (var response:UpdateResponse in responses) {
					if(response.nodeId == _view.parentEntity.id) {
						updateValidationDisplayManager();
						updateRelevanceDisplayManager();
						break;
					}
				}
			}
		}
		
		protected function inputFieldVisitedHandler(event:InputFieldEvent):void {
			var inputField:InputField = event.inputField;
			if(inputField != null && inputField.parentEntity != null && inputField.attributeDefinition == view.attributeDefinition) {
				updateValidationDisplayManager();
			}
		}
		
		override protected function updateView():void {
			super.updateView();
			if(view.dataGroup != null && view.parentEntity != null) {
				var attributes:IList = getAttributes();
				view.dataGroup.dataProvider = attributes;
			}
		}

		protected function getAttributes():IList {
			if(view.dataGroup != null && view.parentEntity != null) {
				var name:String = view.attributeDefinition.name;
				var attributes:IList = view.parentEntity.getChildren(name);
				return attributes;
			} else {
				return null;
			}
		}
		
		protected function addButtonFocusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function addButtonClickHandler(event:MouseEvent):void {
			var attributes:IList = getAttributes();
			var maxCount:Number = view.attributeDefinition.maxCount
			if(isNaN(maxCount) || CollectionUtil.isEmpty(attributes) || attributes.length < maxCount) {
				var o:UpdateRequestOperation = new UpdateRequestOperation();
				o.method = UpdateRequestOperation$Method.ADD;
				o.parentEntityId = view.parentEntity.id;
				o.nodeName = view.attributeDefinition.name;
				var req:UpdateRequest = new UpdateRequest(o);
				ClientFactory.dataClient.updateActiveRecord(req, addResultHandler, faultHandler);
			} else {
				var labelText:String = view.attributeDefinition.getInstanceOrHeadingLabelText();
				AlertUtil.showError("edit.maxCountExceed", [maxCount, labelText]);
			}	
		}
		
		protected function addResultHandler(event:ResultEvent, token:Object = null):void {
			view.callLater(function():void {
				UIUtil.ensureElementIsVisible(view.addButton);
			});
		}
		
		override protected function initValidationDisplayManager():void {
			super.initValidationDisplayManager();
			_validationDisplayManager.showMinMaxCountErrors = true;
			if(view.attributeDefinition != null) {
				updateValidationDisplayManager();
			}
		}
		
		override protected function updateRelevanceDisplayManager():void {
			_relevanceDisplayManager.displayNodeRelevance(view.parentEntity, view.attributeDefinition);
		}
		
		override protected function updateValidationDisplayManager():void {
			super.updateValidationDisplayManager();
			if(_view.parentEntity != null) {
				var attributeName:String = view.attributeDefinition.name;
				var visited:Boolean = _view.parentEntity.isErrorOnChildVisible(attributeName);
				var active:Boolean = visited;
				if(active) {
					_validationDisplayManager.active = true;
					_validationDisplayManager.displayMinMaxCountValidationErrors(view.parentEntity, view.attributeDefinition);
				} else {
					_validationDisplayManager.active = false;
					_validationDisplayManager.reset();
				}
			}
		}

	}
}