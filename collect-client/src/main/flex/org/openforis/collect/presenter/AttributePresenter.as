package org.openforis.collect.presenter {
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	import mx.core.UIComponent;
	import mx.events.PropertyChangeEvent;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.AttributeChangeProxy;
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
		
		protected var _view:AttributeItemRenderer;
		private var _validationDisplayManager:ValidationDisplayManager;
		private var _updating:Boolean = false;
		
		public function AttributePresenter(view:AttributeItemRenderer) {
			_view = view;
			var inputField:InputField = _view.getElementAt(0) as InputField;
			if(inputField != null) {
				ChangeWatcher.watch(inputField, "visited", fieldVisitedHandler);
				ChangeWatcher.watch(inputField, "updating", fieldUpdatingChangeHandler);
			}
			super();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			eventDispatcher.addEventListener(ApplicationEvent.RECORD_SAVED, recordSavedHandler);
			eventDispatcher.addEventListener(ApplicationEvent.ASK_FOR_SUBMIT, askForSubmitHandler);
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
			ChangeWatcher.watch(_view, "attributes", attributesChangeHandler);
		}
		
		protected function initValidationDisplayManager():void {
			var inputField:InputField = _view.getElementAt(0) as InputField;
			var validationStateDisplay:UIComponent = inputField != null ? inputField.validationStateDisplay: _view;
			var validationToolTipTrigger:UIComponent = validationStateDisplay;
			_validationDisplayManager = new ValidationDisplayManager(validationToolTipTrigger, validationStateDisplay);
			var attrDefn:AttributeDefinitionProxy = _view.attributeDefinition;
			_validationDisplayManager.showMinMaxCountErrors = ! attrDefn.multiple || attrDefn is CodeAttributeDefinitionProxy;
			if(_view.attribute != null) {
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
			if(_view.parentEntity != null && _view.attribute != null || _view.attributes != null) {
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
					if (_view.attribute != null && _view.attribute.id == attrResp.nodeId ||
					 	_view.attributes != null && CollectionUtil.containsItemWith(_view.attributes, "id", attrResp.nodeId) ) {
						return true;
					}
				}
			}
			return false;
		}
		
		protected function parentEntityUpdated(changeSet:NodeChangeSetProxy):Boolean {
			for each (var change:NodeChangeProxy in changeSet.changes) {
				if ( change is EntityChangeProxy && EntityChangeProxy(change).nodeId == _view.parentEntity.id ) {
					return true;
				}
			}
			return false;
		}
		
		protected function fieldVisitedHandler(event:PropertyChangeEvent):void {
			if(event.newValue == true && (_view.attribute != null || _view.attributeDefinition.multiple && _view.attributes != null)) {
				var attributeName:String = _view.attributeDefinition.name;
				_view.parentEntity.showErrorsOnChild(attributeName);
			}
			updateValidationDisplayManager();
		}
		
		protected function fieldUpdatingChangeHandler(event:PropertyChangeEvent):void {
			_updating = event.newValue;
			updateValidationDisplayManager();
		}
		
		protected function attributeChangeHandler(event:PropertyChangeEvent):void {
			_view.visited = false;
			updateView();
		}
		
		protected function attributesChangeHandler(event:PropertyChangeEvent):void {
			_view.visited = false;
			updateView();
		}
		
		protected function updateValidationDisplayManager():void {
			if(_view.parentEntity != null) {
				if(_validationDisplayManager == null) {
					initValidationDisplayManager();
				}
				var attributeName:String = _view.attributeDefinition.name;
				var visited:Boolean = _view.parentEntity.isErrorOnChildVisible(attributeName);
				var active:Boolean = visited && !_updating && (_view.attribute != null || _view.attributes != null);
				if(active) {
					_validationDisplayManager.active = true;
					if (_view.attribute != null ) {
						_validationDisplayManager.displayAttributeValidation(_view.parentEntity, _view.attributeDefinition, _view.attribute);
					} else {
						_validationDisplayManager.displayAttributesValidation(_view.parentEntity, _view.attributeDefinition);
					}
				} else {
					_validationDisplayManager.active = false;
					_validationDisplayManager.reset();
				}
			}
		}
		
		protected function isErrorConfirmed():Boolean {
			var result:Boolean = false;
			if ( _view.parentEntity != null ) {
				if ( _view.attribute != null ) {
					result = _view.attribute.errorConfirmed;
				} else if ( CollectionUtil.isNotEmpty(_view.attributes) ) {
					for each (var a:AttributeProxy in _view.attributes) {
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
			if ( _view.parentEntity != null && _view.attributeDefinition != null ) {
				var attributeName:String = _view.attributeDefinition.name;
				//to do
			}
			return result;
		}
		*/
		
		protected function updateView():void {
			//var errorConfirmed:Boolean = isErrorConfirmed();
			//_view.approved = errorConfirmed;
			
			updateValidationDisplayManager();
		}

	}
}
