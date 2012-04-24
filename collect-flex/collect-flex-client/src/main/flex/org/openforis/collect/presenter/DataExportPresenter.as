package org.openforis.collect.presenter {
	/**
	 * 
	 * @author S. Ricci
	 * */
	import mx.collections.ArrayCollection;
	import mx.collections.ListCollectionView;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.model.NodeItem;
	import org.openforis.collect.ui.view.DataExportView;


	public class DataExportPresenter extends AbstractPresenter {
		
		private var _view:DataExportView;

		public function DataExportPresenter(view:DataExportView) {
			this._view = view;
			super();
			
			if(Application.activeRootEntity != null) {
				initView(Application.activeRootEntity);
			}
		}

		override internal function initEventListeners():void {
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
			
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