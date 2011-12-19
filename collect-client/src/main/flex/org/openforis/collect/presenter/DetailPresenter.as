package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.ui.UIGenerator;
	import org.openforis.collect.ui.component.DetailView;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.detail.input.DateInputField;
	import org.openforis.collect.ui.component.detail.input.InputField;
	import org.openforis.collect.ui.component.detail.input.MemoInputField;
	import org.openforis.collect.util.ArrayUtil;
	
	import spark.components.NavigatorContent;

	public class DetailPresenter extends AbstractPresenter {
		
		[Bindable]
		private var formVersions:ArrayCollection = new ArrayCollection([
			{id: "1", label: "version 1"},
			{id: "2", label: "version 2"},
			{id: "3", label: "version 3"}
		]);
		
		private var _view:DetailView;
		
		public function DetailPresenter(view:DetailView) {
			this._view = view;
			super();
		}
		
		override internal function initEventListeners():void{
			_view.formVersionsRP.dataProvider = formVersions;
			eventDispatcher.addEventListener(UIEvent.NEW_RECORD_CREATED, newRecordCreatedHandler);
			
			_view.backToListButton.addEventListener(MouseEvent.CLICK, backToListClickHandler);
		}
		
		public function addFormContainer(value:FormContainer, version:Object):void {
			//viewStack.addElement(value);
			(_view.viewStack.getChildAt(formVersions.getItemIndex(version)) as NavigatorContent).addChild(value);
		}
		
		protected function newRecordCreatedHandler(event:UIEvent):void {
			var record:Object = event.obj;
			var version:Object = ArrayUtil.getItem(formVersions, record.versionId, 'id');
			var index:int = formVersions.getItemIndex(version);
			var formContainerWrapper:NavigatorContent = (_view.formContainers as Array)[index];
			if(formContainerWrapper.numElements == 0) {
				//init form container
				var formContainer:FormContainer = null; //UIGenerator.generateDetailPageForms(
				
				//test
				formContainer = new FormContainer();
				formContainerWrapper.addElement(formContainer);
				
				formContainer.version = version;
				var entityFormContainer:EntityFormContainer = new EntityFormContainer();
				formContainer.addEntityFormContainer(entityFormContainer);
				
				entityFormContainer.label = "Cluster";
				var inputField:InputField = new DateInputField();
				entityFormContainer.addFormItem("Test", inputField);
				
				entityFormContainer = new EntityFormContainer();
				formContainer.addEntityFormContainer(entityFormContainer);
				entityFormContainer.label = "Plots";
				inputField = new MemoInputField();
				entityFormContainer.addFormItem("Test 2", inputField);
				
			}
			_view.viewStack.selectedChild = formContainerWrapper;
		}
		
		protected function backToListClickHandler(event:Event):void {
			var uiEvent:UIEvent = new UIEvent(UIEvent.BACK_TO_LIST);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
	}
}