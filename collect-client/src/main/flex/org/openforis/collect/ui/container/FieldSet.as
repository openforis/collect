////////////////////////////////////////////////////////////////////////////////
//
//  SHINYNET
//  Copyright 2011 SHINYNET
//  All Rights Reserved.
//
//  NOTICE: Shinynet permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

package org.openforis.collect.ui.container
{
	import flash.events.Event;
	
	import mx.graphics.IFill;
	import mx.graphics.IStroke;
	
	import spark.components.Label;
	import spark.components.SkinnableContainer;
	import spark.components.supportClasses.GroupBase;
	
	//--------------------------------------
	//  Styles
	//--------------------------------------
	
	/**
	 *  Alpha level of the color defined by the <code>borderColor</code> style.
	 *  
	 *  Valid values range from 0.0 to 1.0. 
	 *  
	 *  @default 1.0
	 */
	[Style(name="borderAlpha", type="Number", inherit="no")]
	
	/**
	 *  Color of the border.
	 *  
	 *  @default 0xB7BABC
	 */
	[Style(name="borderColor", type="uint", format="Color", inherit="no")]
	
	/**
	 *  Determines if the border is visible or not. 
	 *  If <code>false</code>, then no border is visible
	 *  except a border set by using the <code>borderStroke</code> property. 
	 *   
	 *  @default true
	 */
	[Style(name="borderVisible", type="Boolean", inherit="no")]
	
	/**
	 *  The stroke weight for the border. 
	 *
	 *  @default 1
	 */
	[Style(name="borderWeight", type="Number", format="Length", inherit="no")]
	
	/**
	 *  Radius of the curved corners of the border.
	 *
	 *  @default 6
	 */
	[Style(name="cornerRadius", type="Number", format="Length", inherit="no")]
	
	/**
	 * @default 6
	 */
	[Style(name="gap", type="Number", format="length", inherit="no")]
	
	/**
	 * @default 10
	 */
	[Style(name="paddingLeft", type="Number", format="length", inherit="no")]
	
	/**
	 * @default 0
	 */
	[Style(name="paddingRight", type="Number", format="length", inherit="no")]
	
	//--------------------------------------
	//  Other metadata
	//--------------------------------------
	
	/**
	 * Because this component does not define a skin for the mobile theme, It is
	 * recommended that you not use it in a mobile application. Alternatively, you
	 * can define your own mobile skin for the component. For more information,
	 * see <a href="http://help.adobe.com/en_US/flex/mobileapps/WS19f279b149e7481c698e85712b3011fe73-8000.html">Basics of mobile skinning</a>.
	 */
	[DiscouragedForProfile("mobileDevice")]
	
	/**
	 * 
	 * @author Devin
	 * 
	 */
	public class FieldSet extends SkinnableContainer
	{
		
		//--------------------------------------------------------------------------
		//
		//  Constructor
		//
		//--------------------------------------------------------------------------
		
		/**
		 * Constructor. 
		 */     
		public function FieldSet()
		{
			super();
		}
		
		//--------------------------------------------------------------------------
		//
		//  Skin parts
		//
		//--------------------------------------------------------------------------
		
		[SkinPart(required="true")]
		/**
		 * 
		 */     
		public var legendGroup:GroupBase;
		
		//----------------------------------
		//  legendDisplay
		//---------------------------------- 
		
		[SkinPart(required="true")]
		/**
		 *  The skin part that defines the appearance of the 
		 *  title text in the container.
		 */
		public var legendDisplay:Label;
		
		//--------------------------------------------------------------------------
		//
		//  Properties
		//
		//--------------------------------------------------------------------------
		
		//----------------------------------
		//  legend
		//----------------------------------
		
		/**
		 *  @private
		 */
		private var _legend:String = "";
		
		[Bindable("legendChange")]
		[Inspectable(category="General", defaultValue="")]
		
		/**
		 *  Title or caption displayed in the title bar. 
		 *
		 *  @default ""
		 */
		public function get legend():String 
		{
			return _legend;
		}
		
		/**
		 *  @private
		 */
		public function set legend(value:String):void 
		{
			if (_legend == value)
				return;
			
			_legend = value;
			invalidateSize();
			invalidateDisplayList();
			
			if (legendDisplay)
				legendDisplay.text = legend;
			
			dispatchEvent(new Event("legendChange"));
		}
		
		//--------------------------------------
		//  backgroundFill
		//--------------------------------------
		
		/**
		 * @private
		 */
		private var _backgroundFill:IFill;
		
		/**
		 *  Defines the background of the FieldSet. 
		 *  Setting this property override the <code>backgroundAlpha</code> and 
		 *  <code>backgroundColor</code> styles.
		 * 
		 *  <p>The following example uses the <code>backgroundFill</code> property
		 *  to set the background color to red:</p>
		 *
		 *  <pre>
		 *  &lt;s:FieldSet
		 *     &lt;s:backgroundFill&gt; 
		 *         &lt;s:SolidColor 
		 *             color="red" 
		 *             alpha="1"/&gt; 
		 *     &lt;/s:backgroundFill&gt; 
		 *  &lt;/s:FieldSet&gt; </pre>
		 *
		 *  @default null
		 */ 
		public function get backgroundFill():IFill
		{
			return _backgroundFill;
		}
		
		/**
		 *  @private
		 */ 
		public function set backgroundFill(value:IFill):void
		{
			if (value == _backgroundFill)
				return;
			
			_backgroundFill = value;
			
			if (skin)
				skin.invalidateDisplayList();
		}
		
		//--------------------------------------
		//  borderStroke
		//--------------------------------------
		
		/**
		 * @private 
		 */     
		private var _borderStroke:IStroke;
		
		/**
		 *  Defines the stroke of the FieldSet container. 
		 *  Setting this property overrides the <code>borderAlpha</code>, 
		 *  <code>borderColor</code>, <code>borderStyle</code>, 
		 *  and <code>borderWeight</code> styles.  
		 * 
		 *  <p>The following example sets the <code>borderStroke</code> property:</p>
		 *
		 *  <pre>
		 *  &lt;s:FieldSet
		 *     &lt;s:borderStroke&gt; 
		 *         &lt;mx:SolidColorStroke 
		 *             color="black" 
		 *             weight="3"/&gt; 
		 *     &lt;/s:borderStroke&gt; 
		 *  &lt;/s:FieldSet&gt; </pre>
		 *
		 *  @default null
		 */ 
		public function get borderStroke():IStroke
		{
			return _borderStroke;
		}
		
		/**
		 *  @private
		 */ 
		public function set borderStroke(value:IStroke):void
		{
			if (value == _borderStroke)
				return;
			
			_borderStroke = value;
			
			if (skin)
				skin.invalidateDisplayList();
		}
		
		//--------------------------------------------------------------------------
		//
		//  Overridden methods: UIComponent
		//
		//--------------------------------------------------------------------------
		
		/**
		 * @private 
		 */     
		override protected function measure():void
		{
			super.measure();
			
			if (isNaN(explicitWidth))
			{
				var cornerRadius:Number = getStyle("cornerRadius");
				var gap:Number = getStyle("gap");
				var paddingLeft:Number = getStyle("paddingLeft");
				var paddingRight:Number = getStyle("paddingRight");
				
				var titleBarWidth:Number = legendDisplay.getPreferredBoundsWidth(false);
				titleBarWidth += cornerRadius * 2;
				titleBarWidth += gap * 2;
				titleBarWidth += paddingLeft + paddingRight;
				
				measuredWidth = Math.max(super.measuredWidth, titleBarWidth);
			}
		}
		
		/**
		 * @private 
		 */     
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
		{
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			
			var cornerRadius:Number = getStyle("cornerRadius");
			var gap:Number = getStyle("gap");
			var paddingLeft:Number = getStyle("paddingLeft");
			var paddingRight:Number = getStyle("paddingRight");
			var legendWidth:Number = legendDisplay.getPreferredBoundsWidth(false);
			var legendHeight:Number = legendDisplay.getPreferredBoundsHeight(false);
			var availableWidth:Number = unscaledWidth - cornerRadius * 2;
			availableWidth -= gap * 2 + paddingLeft + paddingRight;
			
			legendGroup.width = Math.min(legendWidth, availableWidth);
			legendGroup.setLayoutBoundsPosition(cornerRadius + gap + paddingLeft, 0);
		}
		
		/**
		 *  @private 
		 *  Detected changes to cornerRadius and update as necessary.
		 */ 
		override public function styleChanged(styleProp:String):void 
		{
			var allStyles:Boolean = !styleProp || styleProp == "styleName";
			
			if (allStyles || styleProp == "cornerRadius" || styleProp == "gap" ||
				styleProp == "paddingLeft" || styleProp == "paddingRight")
			{
				invalidateSize();
			}
			
			super.styleChanged(styleProp);
		}
		
		//--------------------------------------------------------------------------
		//
		//  Overridden methods: SkinnableComponent
		//
		//--------------------------------------------------------------------------
		
		/**
		 * @inheritDoc 
		 */     
		override protected function partAdded(partName:String, instance:Object):void
		{
			super.partAdded(partName, instance);
			
			if (instance == legendDisplay)
			{
				legendDisplay.text = _legend;
			}
		}
		
	}
}