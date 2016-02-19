package org.openforis.collect.ui.component
{
	
	import mx.containers.HBox;
	import mx.controls.ProgressBar;
	
	import org.openforis.collect.util.ObjectUtil;
	
	public class ProgressBarItemRenderer extends HBox {
		
		private var pb:ProgressBar;
		private var _maximum:int = 100;
		private var _toolTipFunction:Function = null;
		private var _valuePropertyName:String = "value";
		
		public function ProgressBarItemRenderer():void {
			this.setStyle("verticalAlign","middle");
			this.minWidth = 0;
			this.minHeight = 0;
			this.pb = createProgressBar();
			//Add ProgressBar as child
			addChild(pb);
		}
		
		private function createProgressBar():ProgressBar {
			var pb:ProgressBar = new ProgressBar();
			//Set some layout things
			pb.mode = "manual";
			pb.minWidth = 20;
			pb.minHeight = 5;
			pb.percentWidth = 100;
			pb.percentHeight = 100;
			pb.labelPlacement = "center";
			pb.label = "";
			return pb;
		}
		
		override public function set data(value:Object):void {
			super.data = value;
		}   
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			var value:Number = ObjectUtil.getValue(data, _valuePropertyName);
			pb.setProgress(value, maximum);
			if (_toolTipFunction != null) {
				pb.toolTip = _toolTipFunction(data);
			} else {
				pb.toolTip = pb.value + "%";
			}
		}
		
		public function get valuePropertyName():String {
			return _valuePropertyName;
		}
		
		public function set valuePropertyName(value:String):void {
			this._valuePropertyName = value;
		}
		
		public function get maximum():int {
			return _maximum;
		}
		
		public function set maximum(value:int):void {
			this._maximum = value;
		}
		
		public function get toolTipFunction():Function {
			return _toolTipFunction;
		}
		
		public function set toolTipFunction(value:Function):void {
			this._toolTipFunction = value;
		}
	}
}