package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.view.DetailView;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormContainer;
	import org.openforis.collect.ui.component.detail.RootEntityFormContainer;
	import org.openforis.collect.ui.component.input.DateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.MemoInputField;
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
			_view.formsContainer.formVersions = formVersions;
			eventDispatcher.addEventListener(UIEvent.NEW_RECORD_CREATED, newRecordCreatedHandler);
			
			_view.backToListButton.addEventListener(MouseEvent.CLICK, backToListClickHandler);
		}
		
		protected function newRecordCreatedHandler(event:UIEvent):void {
			var record:Object = event.obj;
			var version:Object = ArrayUtil.getItem(formVersions, record.versionId, 'id');
			_view.formsContainer.setActiveForm(version);
		}
		
		protected function backToListClickHandler(event:Event):void {
			var uiEvent:UIEvent = new UIEvent(UIEvent.BACK_TO_LIST);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
	}
}