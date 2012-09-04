package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import flash.display.DisplayObject;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ListCollectionView;
	import mx.core.FlexGlobals;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.model.NodeItem;
	import org.openforis.collect.ui.view.DataExportView;


	public class DataExportPresenter extends AbstractPresenter {
		
		private var _view:DataExportView;
		private var _cancelResponder:IResponder;
		private var _exportResponder:IResponder;
		private var _getStateResponder:IResponder;
		
		public function DataExportPresenter(view:DataExportView) {
			this._view = view;
			this._exportResponder = new AsyncResponder(exportResultHandler, faultHandler);
			this._cancelResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			this._getStateResponder = new AsyncResponder(getStateResultHandler, faultHandler);
			
			super();
			
			if(Application.activeRootEntity != null) {
				initView(Application.activeRootEntity);
			}
		}
		
		override internal function initEventListeners():void {
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
			_view.exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
		}
		
		protected function cancelButtonClickHandler(event:MouseEvent):void {
			ClientFactory.dataExportClient.cancel(_cancelResponder);
		}
		
		protected function exportButtonClickHandler(event:MouseEvent):void {
			var rootEntity:String = Application.activeRootEntity.name;
			var selectedEntity:NodeItem = _view.rootTree.selectedItem as NodeItem;
			var stepNumber:int = 1;
			var entityId:int = selectedEntity.id;
			ClientFactory.dataExportClient.export(_exportResponder, rootEntity, entityId, stepNumber);


			PopUpManager.addPopUp(_view.progressPopUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(_view.progressPopUp);
			_view.progressBar.setProgress(0, 0);
			_view.cancelButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandler);
		}
		
		protected function exportResultHandler(event:ResultEvent, token:Object = null):void {
			
		}
		
		protected function cancelResultHandler(event:ResultEvent, token:Object = null):void {
			
		}
		
		protected function getStateResultHandler(event:ResultEvent, token:Object = null):void {
			
		}
		
		internal function rootEntitySelectedHandler(event:UIEvent):void {
			var rootEntityDef:EntityDefinitionProxy = event.obj as EntityDefinitionProxy;
			
			initView(rootEntityDef);
		}
		
		internal function initView(rootEntityDef:EntityDefinitionProxy):void {
			var rootNodeItem:NodeItem = NodeItem.fromNodeDef(rootEntityDef, true, false);
			_view.rootTree.dataProvider = new ArrayCollection([rootNodeItem]);
			
			var attributesTreeRootNodeItem:NodeItem = NodeItem.fromNodeDef(rootEntityDef, true, true);
			_view.attributesTree.dataProvider = new ArrayCollection([attributesTreeRootNodeItem]);
		}
		
	}
}