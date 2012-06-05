package org.openforis.collect.ui.component.checkBoxTreeClasses {

	import flash.events.MouseEvent;
	import flash.xml.*;
	
	import mx.collections.*;
	import mx.containers.VBox;
	import mx.controls.CheckBox;
	import mx.controls.Image;
	import mx.controls.Spacer;
	import mx.controls.Tree;
	import mx.controls.listClasses.*;
	import mx.controls.treeClasses.*;
	
	import org.openforis.collect.util.ObjectUtil;
	

	public class CheckTreeRenderer extends TreeItemRenderer	{
		
		protected var myCheckBox:CheckBox;
		static public var STATE_SCHRODINGER:String = "schrodinger";
		static public var STATE_CHECKED:String = "checked";
		static public var STATE_UNCHECKED:String = "unchecked";
	    
        public function CheckTreeRenderer () {
			super();
			mouseEnabled = false;
			
			this.disclosureIcon = null;
			this.icon = null;
			
			this.setStyle("verticalAlign", "middle")
		}
		
		private function toggleParents (item:Object, tree:Tree, state:String):void {
			if (item == null){
				return;
			}
			else{
				item.state = state;
				toggleParents(tree.getParentItem(item), tree, getState (tree, tree.getParentItem(item)));
			}
		}
		
		private function toggleChildren (item:Object, tree:Tree, state:String):void{
			if (item == null){
				return;
			}
			else{
				item.state = state;
				var treeData:ITreeDataDescriptor = tree.dataDescriptor;
				if (treeData.hasChildren(item)){
					var children:ICollectionView = treeData.getChildren (item);
					var cursor:IViewCursor = children.createCursor();
					while (!cursor.afterLast){
						toggleChildren(cursor.current, tree, state);
						cursor.moveNext();
					}
				}
			}
		}
		
		private function getState(tree:Tree, parent:Object):String{
			var noChecks:int = 0;
			var noCats:int = 0;
			var noUnChecks:int = 0;
			if (parent != null)	{
				var treeData:ITreeDataDescriptor = tree.dataDescriptor;
				var cursor:IViewCursor = treeData.getChildren(parent).createCursor();
				while (!cursor.afterLast){
					if (cursor.current.state == STATE_CHECKED){
						noChecks++;
					}
					else if (cursor.current.state == STATE_UNCHECKED){
						noUnChecks++
					}
					else {
						noCats++;
					}
					cursor.moveNext();
				}
			}
			if ((noChecks > 0 && noUnChecks > 0) || (noCats > 0)) {
				return STATE_SCHRODINGER;
			}
			else if (noChecks > 0) {
				return STATE_CHECKED;
			}
			else {
				return STATE_UNCHECKED;
			}
		}
		
		private function checkBoxToggleHandler(event:MouseEvent):void {
			
			if (data) {
				var myListData:TreeListData = TreeListData(this.listData);
				var selectedNode:Object = myListData.item;
				var tree:Tree = Tree(myListData.owner);
				var toggle:Boolean = myCheckBox.selected;
				if (toggle) {
					toggleChildren(data, tree, STATE_CHECKED);
				}
				else {
					toggleChildren(data, tree, STATE_UNCHECKED);
				}
				var parent:Object = tree.getParentItem (data);
				toggleParents (parent, tree, getState (tree, parent));
			}
		}
		
		private function imageToggleHandler(event:MouseEvent):void {
			myCheckBox.selected = !myCheckBox.selected;
			checkBoxToggleHandler(event);
		}
		
		override protected function createChildren():void {
			super.createChildren();
			myCheckBox = new CheckBox;
			myCheckBox.setStyle( "verticalAlign", "middle" );
			myCheckBox.addEventListener( MouseEvent.CLICK, checkBoxToggleHandler );			
			addChild(myCheckBox);
			
			myCheckBox.setStyle("paddingTop", 15);
	    }	

		private function setCheckState (checkBox:CheckBox, value:Object, state:String):void	{
			if (state == STATE_CHECKED)	{
				checkBox.selected = true;
			}
			else if (state == STATE_UNCHECKED){
				checkBox.selected = false;
			}
			else if (state == STATE_SCHRODINGER){
				checkBox.selected = false;
			}
		}
		
		override public function set data(value:Object):void {
			super.data = value;
			
			var _tree:Tree = Tree(this.parent.parent);
			setCheckState (myCheckBox, value, value.state);
			if(ObjectUtil.getValue(TreeListData(super.listData).item, "isBranch") == 'true') {
		    	_tree.setStyle("defaultLeafIcon", null);
			}
	    }

	   override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
			super.updateDisplayList(unscaledWidth, unscaledHeight);
	        if(super.data){
			    if (super.icon != null){
				    myCheckBox.x = super.icon.x;
				    myCheckBox.y = 2;
				    super.icon.x = myCheckBox.x + myCheckBox.width + 17;
				    super.label.x = super.icon.x + super.icon.width + 3;
					myCheckBox.setStyle("borderColor", 0x666666);
				}
				else{
				    myCheckBox.x = super.label.x;
				    myCheckBox.y = 2;
				    super.label.x = myCheckBox.x + myCheckBox.width + 17;
					myCheckBox.setStyle("borderColor", 0x666666);
				}
			    if (data.state == STATE_SCHRODINGER){
					myCheckBox.setStyle("borderColor", 0xFF4545);
			    }
			    else{
					myCheckBox.setStyle("borderColor", 0x666666);
			    }
			}
	    }
	}
	
}