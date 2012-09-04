/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is: www.iwobanas.com code samples.

The Initial Developer of the Original Code is Iwo Banas.
Portions created by the Initial Developer are Copyright (C) 2009
the Initial Developer. All Rights Reserved.

Contributor(s):
*/
package org.openforis.collect.ui.container
{
	import flash.events.MouseEvent;
	
	import spark.components.Button;Button;
	import spark.components.Panel;
	
	[SkinState("collapsed")]
	/**
	 * The CollapsiblePanel class adds support for collapsing (minimizing) to the Spark Panel.
	 */
	public class CollapsiblePanel extends Panel
	{
		[SkinPart(required="false")]
		/**
		 *  The skin part that defines the appearance of the 
		 *  button responsible for collapsing/uncollapsing the panel.
		 */
		public var collapseButton:Button;
		
		/**
		 * Flag indicating whether this panel is collapsed (minimized) or not.
		 */ 
		[Bindable]
		public function get collapsed():Boolean
		{
			return _collapsed;
		}
		/**
		 * @private
		 */
		public function set collapsed(value:Boolean):void
		{
			_collapsed = value;
			invalidateSkinState();
		}
		
		/**
		 * @private
		 * Toggle collapsed state on collapseButton click event.
		 */
		protected function collapseButtonClickHandler(event:MouseEvent):void
		{
			collapsed = !collapsed;
		}
		
		/**
		 * @private
		 * storage variable for <code>collapsed</code>property.
		 * Add collapeButton click listener.
		 */
		protected var _collapsed:Boolean;
		
		override protected function partAdded(partName:String, instance:Object) : void
		{
			super.partAdded(partName, instance);
			
			if (instance == collapseButton)
			{
				Button(instance).addEventListener(MouseEvent.CLICK, collapseButtonClickHandler);
			}
		}
		
		/**
	     * @private
	     * Remove collapeButton click listener.
	     */
		override protected function partRemoved(partName:String, instance:Object) : void
		{
			if (instance == collapseButton)
			{
				Button(instance).removeEventListener(MouseEvent.CLICK, collapseButtonClickHandler);
			}
			super.partRemoved(partName, instance);
		}
		
		/**
	     *  @private
	     */
		override protected function getCurrentSkinState():String
		{
			return collapsed ? "collapsed" : super.getCurrentSkinState();
		}
	}
}