package org.openforis.collect.util 
{
	import flash.display.DisplayObject;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	import mx.core.FlexGlobals;
	import mx.core.UIComponent;

	/**
	 * @author S. Ricci
	 */
	public class PopUpUtil {
		
		
		static public const POSITION_RIGHT:String = "right";
		static public const POSITION_TOP:String = "top";
		static public const POSITION_BOTTOM:String = "bottom";
		static public const POSITION_LEFT:String = "left";
		
		static public const VERTICAL_ALIGN_TOP:String = "top";
		static public const VERTICAL_ALIGN_BOTTOM:String = "bottom";
		static public const VERTICAL_ALIGN_MIDDLE:String = "middle";
		
		static public const HORIZONTAL_ALIGN_LEFT:String = "left";
		static public const HORIZONTAL_ALIGN_RIGHT:String = "right";
		static public const HORIZONTAL_ALIGN_CENTER:String = "center";
		
		
		static public function centerPopUp(parent:DisplayObject, popup:UIComponent):void {
			var pt:Point = new Point(0, 0);
			pt = parent.localToGlobal(pt); // Convert refAnchor's local 0,0 into global coordinate
			//pt = popup.globalToLocal(pt); // Convert the result into local coordinate of myPop
			var popupX:Number = Math.round((parent.width - popup.width) / 2) + pt.x;
			var popupY:Number = Math.round((parent.height - popup.height) / 2) + pt.y;
			popup.move(popupX, popupY);
		}
		
		static public function alignPopUpToField(popUp:DisplayObject, inputField:DisplayObject, 
				position:String = "right", verticalAlign:String = "middle", horizontalAlign:String = "left"):void {
			//auto positioning tooltip (first on right, then below, otherwiese on top) 
			var componentBounds:Rectangle = inputField.getBounds(inputField.stage);
			
			var x:Number, y:Number;
			switch(position) {
				case POSITION_RIGHT:
					x = componentBounds.x + inputField.width;
					switch(verticalAlign) {
						case VERTICAL_ALIGN_MIDDLE:
							y = componentBounds.y + (inputField.height - popUp.height) / 2;
							break;
						case VERTICAL_ALIGN_TOP:
							y = componentBounds.y + inputField.height - popUp.height;
							break;
						case VERTICAL_ALIGN_BOTTOM:
							y = componentBounds.y;
							break;
					}
					break;
				case POSITION_LEFT:
					x = componentBounds.x - popUp.width;
					switch(verticalAlign) {
						case VERTICAL_ALIGN_MIDDLE:
							y = componentBounds.y + (inputField.height - popUp.height) / 2;
							break;
						case VERTICAL_ALIGN_TOP:
							y = componentBounds.y + inputField.height - popUp.height;
							break;
						case VERTICAL_ALIGN_BOTTOM:
							y = componentBounds.y;
							break;
					}
					break;
				case POSITION_BOTTOM:
					switch(horizontalAlign) {
						case HORIZONTAL_ALIGN_LEFT:
							x = componentBounds.x;
							break;
						case HORIZONTAL_ALIGN_RIGHT:
							x = componentBounds.x + inputField.width - popUp.width;
							break;
						case HORIZONTAL_ALIGN_CENTER:
							x = componentBounds.x + (inputField.width - popUp.width) / 2;
							break;
					}
					y = componentBounds.y + inputField.height;
					break;
				case POSITION_TOP:
					switch(horizontalAlign) {
						case HORIZONTAL_ALIGN_LEFT:
							x = componentBounds.x;
							break;
						case HORIZONTAL_ALIGN_RIGHT:
							x = componentBounds.x + inputField.width - popUp.width;
							break;
						case HORIZONTAL_ALIGN_CENTER:
							x = componentBounds.x + (inputField.width - popUp.width) / 2;
							break;
					}
					y = componentBounds.y - (inputField.height + popUp.height);
					break;
			}
			popUp.x = x;
			popUp.y = y;
			
			adjustPopUpAlignment(popUp);
		}
		
		static public function alignPopUpToMousePoint(popUp:DisplayObject, xOffset:Number = NaN, yOffset:Number = NaN):void {
			var alignmentPoint:Point = new Point(FlexGlobals.topLevelApplication.mouseX, FlexGlobals.topLevelApplication.mouseY);
			if(! isNaN(xOffset))
				alignmentPoint.x += xOffset;
			if(! isNaN(yOffset))
				alignmentPoint.y += yOffset;
			
			alignPop(popUp, alignmentPoint);
		}
		
		static public function alignPop(popUp:DisplayObject, alignmentPoint:Point):void {
			popUp.x = alignmentPoint.x;
			popUp.y = alignmentPoint.y;
			
			adjustPopUpAlignment(popUp);
		}

		static public function adjustPopUpAlignment(popUp:DisplayObject):void {
			var adjustedCoordinates:Point = getAdjustedCoordinatesOfPopUp(popUp);
			
			popUp.x = adjustedCoordinates.x;
			popUp.y = adjustedCoordinates.y;
		}
		
		static public function getAdjustedCoordinatesOfPopUp(popUp:DisplayObject, startingPoint:Point = null):Point {
			if(startingPoint == null) {
				startingPoint = new Point(popUp.x, popUp.y);
			}
			var x:Number = startingPoint.x;
			var y:Number = startingPoint.y;
			
			var screenHeight:Number = FlexGlobals.topLevelApplication.screen.height;
			var screenWidth:Number = FlexGlobals.topLevelApplication.screen.width;
			
			if(x + popUp.width > screenWidth) {
				x = screenWidth - popUp.width;
			} else if(x < 0) {
				x = 0;
			}
			if(y + popUp.height > screenHeight) {
				y = screenHeight - popUp.height;
			} else if(y < 0) {
				y = 0;
			}
			
			return new Point(x, y);
		}
		
	}
}