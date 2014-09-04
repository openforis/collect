package org.openforis.collect.presenter {
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.core.UIComponent;
	import mx.events.PropertyChangeEvent;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeChangeProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityChangeProxy;
	import org.openforis.collect.model.proxy.NodeChangeProxy;
	import org.openforis.collect.model.proxy.NodeChangeSetProxy;
	import org.openforis.collect.ui.component.detail.AttributeItemRenderer;
	import org.openforis.collect.ui.component.detail.ValidationDisplayManager;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.CollectionUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class AttributePresenter extends AbstractPresenter {
		
		private var _validationDisplayManager:ValidationDisplayManager;
		private var _updating:Boolean = false;
		
		public function AttributePresenter(view:AttributeItemRenderer) {
			super(view);
		}
		
		private function get view():AttributeItemRenderer {
			return AttributeItemRenderer(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			ChangeWatcher.watch(view, "attribute", attributeChangeHandler);
			ChangeWatcher.watch(view, "attributes", attributesChangeHandler);
			
			var inputField:InputField = view.getElementAt(0) as InputField;
			if(inputField != null) {
				ChangeWatcher.watch(inputField, "visited", fieldVisitedHandler);
				ChangeWatcher.watch(inputField, "updating", fieldUpdatingChangeHandler);
			}
		}
		
		override protected function initBroadcastEventListeners():void {
			super.initBroadcastEventListeners();
			eventDispatcher.addEventListener(ApplicationEvent.RECORD_SAVED, recordSavedHandler);
			eventDispatcher.addEventListener(ApplicationEvent.ASK_FOR_SUBMIT, askForSubmitHandler);
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
		}
		
		override protected function removeBroadcastEventListeners():void {
			super.removeBroadcastEventListeners();
			eventDispatcher.removeEventListener(ApplicationEvent.RECORD_SAVED, recordSavedHandler);
			eventDispatcher.removeEventListener(ApplicationEvent.ASK_FOR_SUBMIT, askForSubmitHandler);
			eventDispatcher.removeEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
		}
		
		protected function initValidationDisplayManager():void {
			var inputField:InputField = view.getElementAt(0) as InputField;
			var validationStateDisplay:UIComponent = inputField != null ? inputField.validationStateDisplay: view;
			var validationToolTipTrigger:UIComponent = validationStateDisplay;
			_validationDisplayManager = new ValidationDisplayManager(validationToolTipTrigger, validationStateDisplay);
			var attrDefn:AttributeDefinitionProxy = view.attributeDefinition;
			_validationDisplayManager.showMinMaxCountErrors = ! attrDefn.multiple || attrDefn is CodeAttributeDefinitionProxy;
			if(view.attribute != null) {
				updateValidationDisplayManager();
			}
		}
		
		protected function recordSavedHandler(event:ApplicationEvent):void {
			updateValidationDisplayManager();
		}
		
		protected function askForSubmitHandler(event:ApplicationEvent):void {
			updateValidationDisplayManager();
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(view.parentEntity != null && view.attribute != null || view.attributes != null) {
				var changeSet:NodeChangeSetProxy = NodeChangeSetProxy(event.result);
				if ( nodeUpdated(changeSet) ) {
					updateView();
				} else if ( parentEntityUpdated(changeSet) ) {
					updateValidationDisplayManager();
				}
			}
		}
		
		protected function nodeUpdated(changeSet:NodeChangeSetProxy):Boolean {
			for each (var change:NodeChangeProxy in changeSet.changes) {
				if ( change is AttributeChangeProxy) {
					var attrResp:AttributeChangeProxy = AttributeChangeProxy(change);
					if (view.attribute != null && view.attribute.id == attrResp.nodeId ||
					 	view.attributes != null && CollectionUtil.containsItemWith(view.attributes, "id", attrResp.nodeId) ) {
						return true;
					}
				}
			}
			return false;
		}
		
		protected function parentEntityUpdated(changeSet:NodeChangeSetProxy):Boolean {
			for each (var change:NodeChangeProxy in changeSet.changes) {
				if ( change is EntityChangeProxy && EntityChangeProxy(change).nodeId == view.parentEntity.id ) {
					return true;
				}
			}
			return false;
		}
		
		protected function fieldVisitedHandler(event:PropertyChangeEvent):void {
			if(event.newValue == true && (view.attribute != null || view.attributeDefinition.multiple && view.attributes != null)) {
				var attributeName:String = view.attributeDefinition.name;
				view.parentEntity.showErrorsOnChild(attributeName);
			}
			updateValidationDisplayManager();
		}
		
		protected function fieldUpdatingChangeHandler(event:PropertyChangeEvent):void {
			_updating = event.newValue;
			updateValidationDisplayManager();
		}
		
		protected function attributeChangeHandler(event:PropertyChangeEvent):void {
			view.visited = false;
			updateView();
		}
		
		protected function attributesChangeHandler(event:PropertyChangeEvent):void {
			view.visited = false;
			updateView();
		}
		
		protected function updateValidationDisplayManager():void {
			if(view.parentEntity != null) {
				if(_validationDisplayManager == null) {
					initValidationDisplayManager();
				}
				var attributeName:String = view.attributeDefinition.name;
				var visited:Boolean = view.parentEntity.isErrorOnChildVisible(attributeName);
				var active:Boolean = visited && !_updating && (view.attribute != null || view.attributes != null);
				if(active) {
					_validationDisplayManager.active = true;
					if (view.attribute != null ) {
						_validationDisplayManager.displayAttributeValidation(view.parentEntity, view.attributeDefinition, view.attribute);
					} else {
						_validationDisplayManager.displayAttributesValidation(view.parentEntity, view.attributeDefinition);
					}
				} else {
					_validationDisplayManager.active = false;
					_validationDisplayManager.reset();
				}
			}
		}
		
		protected function isErrorConfirmed():Boolean {
			var result:Boolean = false;
			if ( view.parentEntity != null ) {
				if ( view.attribute != null ) {
					result = view.attribute.errorConfirmed;
				} else if ( CollectionUtil.isNotEmpty(view.attributes) ) {
					for each (var a:AttributeProxy in view.attributes) {
						if ( a.errorConfirmed ) {
							result = true;
							break;
						}
					}
				}
			}
			return result;
		}
		
		/*
		protected function isMissingValueApproved():Boolean {
			var result:Boolean = false;
			if ( view.parentEntity != null && view.attributeDefinition != null ) {
				var attributeName:String = view.attributeDefinition.name;
				//to do
			}
			return result;
		}
		*/
		
		protected function updateView():void {
			//var errorConfirmed:Boolean = isErrorConfirmed();
			//view.approved = errorConfirmed;
			
			updateValidationDisplayManager();
		}

	}
}
