package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.core.DragSource;
	import mx.events.DragEvent;
	import mx.managers.DragManager;
	
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.NodeEvent;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.ui.Images;
	import org.openforis.collect.ui.component.datagroup.MultipleNodesDataGroupItemRenderer;
	
	import spark.components.supportClasses.ItemRenderer;
	
	/**
	 * @author S. Ricci
	 */
	public class MultipleNodesDataGroupItemPresenter extends AbstractPresenter {
		
		private var _handCursor:int = -1;
		
		public function MultipleNodesDataGroupItemPresenter(view:MultipleNodesDataGroupItemRenderer) {
			super(view);
		}
		
		private function get view():MultipleNodesDataGroupItemRenderer {
			return MultipleNodesDataGroupItemRenderer(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			
			view.addEventListener(DragEvent.DRAG_ENTER, dragEnterHandler);
			view.addEventListener(DragEvent.DRAG_EXIT, dragExitHandler);
			view.addEventListener(DragEvent.DRAG_OVER, dragOverHandler);
			view.addEventListener(DragEvent.DRAG_DROP, dragDropHandler);
			view.addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
			view.addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
			view.dragAnchor.addEventListener(MouseEvent.MOUSE_DOWN, dragAnchorMouseDownHandler);
			view.dragAnchor.addEventListener(MouseEvent.MOUSE_MOVE, dragAnchorMouseMoveHandler);
			view.dragAnchor.addEventListener(MouseEvent.MOUSE_OVER, dragAnchorMouseOverHandler);
			view.dragAnchor.addEventListener(MouseEvent.MOUSE_OUT, dragAnchorMouseOutHandler);
		}
		
		override protected function initBroadcastEventListeners():void {
			super.initBroadcastEventListeners();
			eventDispatcher.addEventListener(InputFieldEvent.FOCUS_IN, fieldFocusInHandler);
			eventDispatcher.addEventListener(InputFieldEvent.FOCUS_OUT, fieldFocusOutHandler);
		}
		
		override protected function removeBroadcastEventListeners():void {
			super.removeBroadcastEventListeners();
			eventDispatcher.removeEventListener(InputFieldEvent.FOCUS_IN, fieldFocusInHandler);
			eventDispatcher.removeEventListener(InputFieldEvent.FOCUS_OUT, fieldFocusOutHandler);
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
			if ( isDropOnTopOfElement(view, event.localY) ) {
				indexTo = view.itemIndex;
			} else {
				indexTo = view.itemIndex + 1;
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
			if(isDropOnTopOfElement(view, event.localY)) {
				view.currentState = MultipleNodesDataGroupItemRenderer.STATE_DROPPING_TOP;
			} else {
				view.currentState = MultipleNodesDataGroupItemRenderer.STATE_DROPPING_BOTTOM;
			}
		}
		
		protected function isDropOnTopOfElement(element:DisplayObject, y:Number):Boolean {
			return y <= (element.height / 2) + 5;
		}
		
		protected function hideDropIndicator():void {
			view.currentState = MultipleNodesDataGroupItemRenderer.STATE_NORMAL;
		}
		
		protected function mouseOverHandler(event:MouseEvent):void {
			view.dragAnchor.alpha = 0.5;
		}
		
		protected function mouseOutHandler(event:MouseEvent):void {
			view.dragAnchor.alpha = 0.0;
		}
		
		protected function dragAnchorMouseOverHandler(event:MouseEvent):void {
			//draw pointer
			_handCursor = view.cursorManager.setCursor(Images.CURSOR_GRAB_OPEN, 2, -8, -8);
		}
		
		protected function dragAnchorMouseOutHandler(event:MouseEvent):void {
			if(_handCursor > 0) {
				view.cursorManager.removeCursor(_handCursor);
			}
		}
		
		protected function dragAnchorMouseDownHandler(event:MouseEvent):void {
			if(view.focusManager.getFocus() != null) {
				view.dragAnchor.setFocus();
			}
		}
		
		protected function fieldFocusOutHandler(event:InputFieldEvent):void {
		}
		
		protected function fieldFocusInHandler(event:InputFieldEvent):void {
		}
		
	}
}