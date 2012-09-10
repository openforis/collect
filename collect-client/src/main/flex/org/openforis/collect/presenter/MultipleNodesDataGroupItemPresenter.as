package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.MouseEvent;
	
	import mx.core.DragSource;
	import mx.events.DragEvent;
	import mx.managers.DragManager;
	
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.event.NodeEvent;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.ui.Images;
	import org.openforis.collect.ui.component.datagroup.MultipleNodesDataGroupItemRenderer;
	
	import spark.components.supportClasses.ItemRenderer;
	
	/**
	 * @author S. Ricci
	 */
	public class MultipleNodesDataGroupItemPresenter extends AbstractPresenter {
		
		private var _view:MultipleNodesDataGroupItemRenderer;
		private var _handCursor:int = -1;
		
		public function MultipleNodesDataGroupItemPresenter(view:MultipleNodesDataGroupItemRenderer) {
			_view = view;
			super();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.addEventListener(DragEvent.DRAG_ENTER, dragEnterHandler);
			_view.addEventListener(DragEvent.DRAG_EXIT, dragExitHandler);
			_view.addEventListener(DragEvent.DRAG_OVER, dragOverHandler);
			_view.addEventListener(DragEvent.DRAG_DROP, dragDropHandler);
			_view.addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
			_view.addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
			_view.dragAnchor.addEventListener(MouseEvent.MOUSE_DOWN, dragAnchorMouseDownHandler);
			_view.dragAnchor.addEventListener(MouseEvent.MOUSE_MOVE, dragAnchorMouseMoveHandler);
			_view.dragAnchor.addEventListener(MouseEvent.MOUSE_OVER, dragAnchorMouseOverHandler);
			_view.dragAnchor.addEventListener(MouseEvent.MOUSE_OUT, dragAnchorMouseOutHandler);
		}
		
		protected function dragAnchorMouseMoveHandler(event:MouseEvent):void {
			// Get the drag initiator component from the event object.
			var dragInitiator:ItemRenderer = event.currentTarget.parentDocument as ItemRenderer;
			
			var ds:DragSource = new DragSource();
			
			DragManager.doDrag(dragInitiator, ds, event);
		}
		
		/**
		 * Called when the user moves the drag indicator onto the drop target.
		 */
		protected function dragEnterHandler(event:DragEvent):void {
			var dragInitiator:ItemRenderer = event.dragInitiator as ItemRenderer;
			var dropTarget:ItemRenderer = ItemRenderer(event.currentTarget);
			
			// Accept the drag if dragged attribute is relative to the same form item
			if ( dragInitiator != null && dragInitiator.parentDocument == dropTarget.parentDocument) {
				DragManager.acceptDragDrop(dropTarget);
			}
		}
		
		/**
		 * Called if the target accepts the dragged object and the user 
		 * releases the mouse button while over another ItemRenderer.
		 */ 
		protected function dragDropHandler(event:DragEvent):void {
			var dragInitiator:ItemRenderer = ItemRenderer(event.dragInitiator);
			var indexFrom:int = dragInitiator.itemIndex;
			var indexTo:int;
			if ( isDropOnTopOfElement(_view, event.localY) ) {
				indexTo = _view.itemIndex;
			} else {
				indexTo = _view.itemIndex + 1;
			}
			if ( indexFrom < indexTo ) {
				//the node in index from will be deleted and the other nodes will shift
				//so the final index has to be decreased
				indexTo--;
			}
			if ( indexFrom != indexTo ) {
				var dragNode:NodeProxy = NodeProxy(dragInitiator.data);
				var nodeEvent:NodeEvent = new NodeEvent(NodeEvent.MOVE);
				nodeEvent.node = dragNode;
				nodeEvent.index = indexTo;
				EventDispatcherFactory.getEventDispatcher().dispatchEvent(nodeEvent);
			}
			hideDropIndicator();
		}
		
		protected function dragExitHandler(event:DragEvent):void {
			hideDropIndicator();
		}
		
		protected function dragOverHandler(event:DragEvent):void {
			showDropIndicator(event);
		}
		
		protected function showDropIndicator(event:DragEvent):void {
			if(isDropOnTopOfElement(_view, event.localY)) {
				_view.currentState = "droppingTop";
			} else {
				_view.currentState = "droppingBottom";
			}
		}
		
		protected function isDropOnTopOfElement(element:DisplayObject, y:Number):Boolean {
			return y <= (element.height / 2) + 5;
		}
		
		protected function hideDropIndicator():void {
			_view.currentState = "normal";
		}
		
		protected function mouseOverHandler(event:MouseEvent):void {
			_view.dragAnchor.alpha = 0.5;
		}
		
		protected function mouseOutHandler(event:MouseEvent):void {
			_view.dragAnchor.alpha = 0.0;
		}
		
		protected function dragAnchorMouseOverHandler(event:MouseEvent):void {
			//draw pointer
			_handCursor = _view.cursorManager.setCursor(Images.CURSOR_GRAB_OPEN, 2, -8, -8);
		}
		
		protected function dragAnchorMouseOutHandler(event:MouseEvent):void {
			if(_handCursor > 0) {
				_view.cursorManager.removeCursor(_handCursor);
			}
		}
		
		protected function dragAnchorMouseDownHandler(event:MouseEvent):void {
			if(_view.focusManager.getFocus() != null) {
				_view.dragAnchor.setFocus();
			}
		}
	}
}