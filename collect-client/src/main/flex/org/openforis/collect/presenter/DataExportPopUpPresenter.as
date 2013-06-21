package org.openforis.collect.presenter {

	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.controls.Alert;
	import mx.controls.Tree;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.manager.dataexport.proxy.DataExportStatusProxy;
	import org.openforis.collect.manager.process.ProcessStatus$Step;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.NodeItem;
	import org.openforis.collect.ui.component.DataExportPopUp;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	
	import spark.components.DropDownList;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class DataExportPopUpPresenter extends PopUpPresenter {
		
		private static const PROGRESS_DELAY:int = 2000;
		private static const ALL_STEPS_ITEM:Object = {label: Message.get('global.allItemsLabel')};
		
		private var _cancelResponder:IResponder;
		private var _exportResponder:IResponder;
		private var _getStateResponder:IResponder;
		private var _progressTimer:Timer;
		private var _type:String;
		private var _state:DataExportStatusProxy;
		private var _firstOpen:Boolean = true;
		
		public function DataExportPopUpPresenter(view:DataExportPopUp) {
			this._exportResponder = new AsyncResponder(exportResultHandler, faultHandler);
			this._cancelResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			this._getStateResponder = new AsyncResponder(getStateResultHandler, faultHandler);
			super(view);
			
			initView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
			DataExportPopUp(_view).closeButton1.addEventListener(MouseEvent.CLICK, closeHandler);
			DataExportPopUp(_view).typeGroup.addEventListener(Event.CHANGE, typeChangeHandler);
			DataExportPopUp(_view).exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
			DataExportPopUp(_view).cancelExportButton.addEventListener(MouseEvent.CLICK, cancelExportButtonClickHandler);
			DataExportPopUp(_view).downloadButton.addEventListener(MouseEvent.CLICK, downloadButtonClickHandler);
			DataExportPopUp(_view).closeButton3.addEventListener(MouseEvent.CLICK, closeHandler);
		}
		
		override protected function closeHandler(event:Event = null):void {
			if ( _state != null && _state.step == ProcessStatus$Step.RUN ) {
				AlertUtil.showMessage("export.cannotClosePopUp");
			} else {
				PopUpManager.removePopUp(_view);
			}
		}
		
		internal function rootEntitySelectedHandler(event:UIEvent):void {
			initView();
		}
		
		protected function typeChangeHandler(event:Event):void {
			var type:String = DataExportPopUp(_view).typeGroup.selectedValue as String;
			_type = type;
			initStepsDropDown();
			switch ( type ) {
				case "full":
					_view.currentState = DataExportPopUp.STATE_PARAMETERS_SELECTION;
					break;
				case "partial":
					_view.currentState = DataExportPopUp.STATE_PARTIAL_EXPORT_PARAMETERS_SELECTION;
					break;
			}
		}
			
		protected function exportButtonClickHandler(event:MouseEvent):void {
			var rootEntity:String = Application.activeRootEntity.name;
			var step:Object;
			var stepNumber:Number;
			switch ( _type ) {
				case "partial":
					var selectedEntity:NodeItem = DataExportPopUp(_view).rootTree.selectedItem as NodeItem;
					if ( selectedEntity == null ) {
						AlertUtil.showMessage("export.selectAnEntity");
					} else {
						step = DataExportPopUp(_view).stepDropDownList.selectedItem;
						if ( step == null ) {
							AlertUtil.showError("export.error.selectStep");
						} else {
							stepNumber = Application.getRecordStepNumber(CollectRecord$Step(step));
							var entityId:int = selectedEntity.id;
							ClientFactory.dataExportClient.export(_exportResponder, rootEntity, stepNumber, entityId);
							
							_view.currentState = DataExportPopUp.STATE_EXPORTING;
							DataExportPopUp(_view).progressBar.setProgress(0, 0);
						}
					}
					break;
				case "full":
					step = DataExportPopUp(_view).stepDropDownList.selectedItem;
					var stepNums:Array;
					if ( step != ALL_STEPS_ITEM ) {
						stepNumber = Application.getRecordStepNumber(CollectRecord$Step(step));
						stepNums = [stepNumber];
					} else {
						stepNums = [1, 2, 3];
					}
					ClientFactory.dataExportClient.fullExport(_exportResponder, rootEntity, stepNums);
					_view.currentState = DataExportPopUp.STATE_EXPORTING;
					DataExportPopUp(_view).progressBar.setProgress(0, 0);
					break;
			}
		}
		
		protected function downloadButtonClickHandler(event:MouseEvent):void {
			var url:String = ApplicationConstants.DOWNLOAD_EXPORTED_DATA_URL;
			navigateToURL(new URLRequest(url), "_new");
		}
		
		protected function cancelExportButtonClickHandler(event:MouseEvent):void {
			ClientFactory.dataExportClient.cancel(_cancelResponder);
		}
		
		protected function exportResultHandler(event:ResultEvent, token:Object = null):void {
			_state = event.result as DataExportStatusProxy;
			updateView();
		}
		
		protected function startProgressTimer():void {
			if ( _progressTimer == null ) {
				_progressTimer = new Timer(PROGRESS_DELAY);
				_progressTimer.addEventListener(TimerEvent.TIMER, progressTimerHandler);
			}
			_progressTimer.start();
		}
		
		protected function stopProgressTimer():void {
			if ( _progressTimer != null ) {
				_progressTimer.stop();
				_progressTimer = null;
			}
		}
		
		protected function progressTimerHandler(event:TimerEvent):void {
			updateExportState();
		}
		
		protected function updateExportState():void {
			ClientFactory.dataExportClient.getState(_getStateResponder);
		}
		
		protected function cancelResultHandler(event:ResultEvent, token:Object = null):void {
			resetView();
		}
		
		protected function getStateResultHandler(event:ResultEvent, token:Object = null):void {
			_state = event.result as DataExportStatusProxy;
			updateView();
		}
		
		protected function updateView():void {
			if(_state != null) {
				var processed:int = _state.processed;
				var step:ProcessStatus$Step = _state.step;
				if ( step == ProcessStatus$Step.RUN && processed <= _state.total ) {
					_view.currentState = DataExportPopUp.STATE_EXPORTING;
					DataExportPopUp(_view).progressBar.setProgress(processed, _state.total);
					var progressText:String;
					if ( _state.total == 0 ) {
						progressText = Message.get("export.processing");
					} else {
						progressText = Message.get("export.progressLabel", [processed, _state.total]);
					}
					DataExportPopUp(_view).progressLabel.text = progressText;
					if ( _progressTimer == null ) {
						startProgressTimer();
					}
				} else if ( !_firstOpen && step == ProcessStatus$Step.COMPLETE ) {
					_view.currentState = DataExportPopUp.STATE_COMPLETE;
					stopProgressTimer();
				} else {
					if ( !_firstOpen ) {
						if ( step == ProcessStatus$Step.ERROR ) {
							AlertUtil.showError("export.error");
							resetView();
						} else if ( step == ProcessStatus$Step.CANCEL ) {
							AlertUtil.showError("export.cancelled");
							resetView();
						} else {
							//process starting in a while...
							startProgressTimer();
						}
					} else {
						resetView();
					}
				}
			} else {
				resetView();
			}
			_firstOpen = false;
		}
		
		protected function resetView():void {
			_state = null;
			_view.currentState = DataExportPopUp.STATE_TYPE_SELECTION;
			DataExportPopUp(_view).typeGroup.selection = null;
			stopProgressTimer();
		}
		
		protected function initView():void {
			initEntitiesTree();
			initStepsDropDown();
			//try to see if there is an export still running
			updateExportState();
		}
		
		protected function initEntitiesTree():void {
			var rootEntity:EntityDefinitionProxy = Application.activeRootEntity;
			var rootNodeItem:NodeItem = NodeItem.fromNodeDef(rootEntity, true, false, false);
			var tree:Tree = DataExportPopUp(_view).rootTree;
			tree.dataProvider = new ArrayCollection([rootNodeItem]);
			tree.callLater(function():void {
				tree.expandItem(rootNodeItem, true);
			});
		}
		
		protected function initStepsDropDown():void {
			var steps:IList = new ArrayCollection(CollectRecord$Step.constants);
			if ( _type == "full" ) {
				steps.addItemAt(ALL_STEPS_ITEM, 0);
			}
			var stepDropDownList:DropDownList = DataExportPopUp(_view).stepDropDownList;
			stepDropDownList.dataProvider = steps;
			stepDropDownList.callLater(function():void {
				if ( _type == "full" ) {
					stepDropDownList.selectedIndex = 0;
				}
			});
		}
		
	}
}