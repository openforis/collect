package org.openforis.collect.ui.component
{
	
	import mx.containers.HBox;
	import mx.controls.ProgressBar;
	
	import org.openforis.collect.util.ObjectUtil;
	
	public class ProgressBarItemRenderer extends HBox {
		
		protected var progressBar:ProgressBar;
		private var _maximum:int = 100;
		private var _toolTipFunction:Function = null;
		private var _valuePropertyName:String = "value";
		
		public function ProgressBarItemRenderer():void {
			this.setStyle("verticalAlign","middle");
			this.minWidth = 0;
			this.minHeight = 0;
			this.progressBar = createProgressBar();
			//Add ProgressBar as child
			addChild(progressBar);
		}
		
		private function createProgressBar():ProgressBar {
			var progressBar:ProgressBar = new ProgressBar();
			//Set some layout things
			progressBar.mode = "manual";
			progressBar.minWidth = 20;
			progressBar.minHeight = 5;
			progressBar.percentWidth = 100;
			progressBar.percentHeight = 100;
			progressBar.labelPlacement = "center";
			progressBar.label = "";
			return progressBar;
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			var progress:Number;
			var toolTip:String;
			if (data == null) {
				progress = 0;
				toolTip = null;
			} else {
				progress = ObjectUtil.getValue(data, _valuePropertyName);
				if (_toolTipFunction != null) {
					toolTip = _toolTipFunction(data);
				} else {
					toolTip = progressBar.value + "%";
				}
			}
			setProgress(progress);
			progressBar.toolTip = toolTip;
		}
		
		protected function setProgress(value:Number):void {
			progressBar.setProgress(value, maximum);
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