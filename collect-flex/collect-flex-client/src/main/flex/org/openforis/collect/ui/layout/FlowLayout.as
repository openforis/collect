package org.openforis.collect.ui.layout
{
	import mx.core.ILayoutElement;
	
	import spark.layouts.supportClasses.LayoutBase;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 * */
	public class FlowLayout extends LayoutBase {
		
		override public function updateDisplayList(width:Number, height:Number):void {
			super.updateDisplayList(width,height);
			
			var currentElement:ILayoutElement;
			var elementHeight:Number;
			var elementWidth:Number;
			var rowHeight:Number = 0;
			var verticalPosition:Number = 0;
			var horizontalPosition:Number = 0;
			
			if (!target) return;
			
			var childrenCount:int = target.numElements;
			
			for(var i:int = 0; i< childrenCount; i++) {    
				currentElement = target.getElementAt(i);
				
				//Reset element size to retrieve its original one
				currentElement.setLayoutBoundsSize(NaN,NaN);
				
				elementHeight = currentElement.getPreferredBoundsHeight();
				elementWidth = currentElement.getPreferredBoundsWidth();
				
				//Last element in a row
				if(horizontalPosition + elementWidth > width ) {     
					horizontalPosition = 0;
					verticalPosition += rowHeight;
					rowHeight = 0;
				}
				
				rowHeight = Math.max(elementHeight, rowHeight);
				
				//Set element position
				currentElement.setLayoutBoundsPosition(horizontalPosition, verticalPosition);
				
				horizontalPosition += elementWidth;
			}
			
			//Invalidate targetr measuredHeight, if it is incorrect
			if(target.measuredHeight != verticalPosition + rowHeight) {
				target.invalidateSize();    
			}
		}
		
		override public function measure():void {
			super.measure();
			if (!target) return;
			
			var childrenCount:int = target.numElements;
			
			var currentElement:ILayoutElement;
			var maxWidth:Number = 0;
			var maxHeight:Number = 0;
			var elementMaxX:Number;
			var elementMaxY:Number;
			
			for(var i:int = 0; i< childrenCount; i++) {
				currentElement = target.getElementAt(i);
				elementMaxX = currentElement.getLayoutBoundsX() + currentElement.getPreferredBoundsWidth();
				elementMaxY = currentElement.getLayoutBoundsY() + currentElement.getPreferredBoundsHeight();
				if(maxWidth < elementMaxX) maxWidth = elementMaxX;
				if(maxHeight < elementMaxY) maxHeight = elementMaxY;
			}
			target.measuredWidth = maxWidth;
			target.measuredHeight = maxHeight;
		}
	}
}