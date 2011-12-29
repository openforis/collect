package org.openforis.collect.presenter {
	/**
	 * 
	 * @author Mino Togna
	 * */
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.ui.view.RootEntitySelectionView;
	import org.openforis.idm.metamodel.Schema;
	
	public class RootEntitySelectionPresenter extends AbstractPresenter {
		
		private var _view:RootEntitySelectionView;
		
		public function RootEntitySelectionPresenter(view:RootEntitySelectionView) {
			this._view = view;
			super();
		}
		
		override internal function initEventListeners():void{
			eventDispatcher.addEventListener(ApplicationEvent.SCHEMA_LOADED, schemaLoadedHandler);
			_view.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
		}
		
		protected function schemaLoadedHandler(event:ApplicationEvent):void {
			_view.rootEntitiesDataGroup.dataProvider = (event.result as Schema).rootEntityDefinitions;
		}
		
		protected function rootEntitySelectedHandler(event:UIEvent):void {
			var selectedItem:Object = event.obj;
			//TODO
		}
		
	}
}