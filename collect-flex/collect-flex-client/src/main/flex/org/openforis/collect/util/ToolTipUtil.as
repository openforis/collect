package org.openforis.collect.util {
	import flash.display.DisplayObject;
	import flash.geom.Rectangle;
	
	import mx.controls.ToolTip;
	import mx.core.IToolTip;
	import mx.core.IUIComponent;
	import mx.managers.ToolTipManager;
	
	import org.openforis.collect.i18n.Message;
	
	/**
	 * @author M. Togna
	 * @author S. Ricci
	 * */
	public final class ToolTipUtil {
		
		public static const STYLE_NAME_ERROR:String = "error";
		public static const STYLE_NAME_WARNING:String = "warning";
		
		public static function create(object:DisplayObject, errorMsg:String, styleName:String = STYLE_NAME_ERROR):ToolTip {
			var pt:Rectangle = object.stage.getBounds(object);
			var yPos:Number = -(pt.y);
			var xPos:Number = -(pt.x);

			if(styleName == STYLE_NAME_ERROR) {
				errorMsg = Message.get("edit.validationToolTip.error", [errorMsg]);
			} else if(styleName == STYLE_NAME_WARNING) {
				errorMsg = Message.get("edit.validationToolTip.warning", [errorMsg]);
			}
			var toolTip:ToolTip = ToolTipManager.createToolTip(errorMsg, xPos + object.width, yPos, null, object as IUIComponent) as ToolTip;
			toolTip.styleName = styleName;
			
			var position:String;
			if(PopUpUtil.exceedViewport(toolTip)) {
				position = PopUpUtil.alignToField(toolTip, object, PopUpUtil.POSITION_RIGHT);
			} else {
				position = PopUpUtil.POSITION_RIGHT;
			}
			var borderStyle:String = getBorderStyle(position);
			toolTip.setStyle("borderStyle", borderStyle);
			return toolTip;
		}
		
		public static function destroy(toolTip:IToolTip):void{
			if(toolTip != null){
				ToolTipManager.destroyToolTip(toolTip);
			}
		}
		
		private static function getBorderStyle(position:String):String {
			switch(position) {
				case PopUpUtil.POSITION_LEFT:
					return "errorTipLeft";
				case PopUpUtil.POSITION_ABOVE:
					return "errorTipAbove";
				case PopUpUtil.POSITION_BELOW:
					return "errorTipBelow";
				case PopUpUtil.POSITION_RIGHT:
				default:
					return "errorTipRight";
					
			}
		}
		
	}
}