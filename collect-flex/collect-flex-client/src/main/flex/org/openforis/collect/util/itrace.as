package org.openforis.collect.util
{
	import flash.display.Shape;
	import flash.display.Sprite;
	import flash.display.Stage;
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.text.TextField;
	import flash.text.TextFieldAutoSize;
	import flash.text.TextFormat;
	
	import mx.core.Application;
	
	/*
	Author: Inder
	Profile : http://askmeflash.com/profile/Inder
	Website :Askmeflash.com 
	Send me mail on contact@askmeflash.com for any querries suggessions 
	
	Creation Date: July 12 2010
	Functionality: Use this class to create a custom runtime tracer. 
	This will enable you to display messages on runtime, making the development easy and fast.
	You can specify color with each message to distinguish from another. Use "~" key to show hide the tracer window.
	*/
	public class itrace
	{
		private static var Background:Shape = new Shape();
		private static var bgColour:int;
		private static var myPanel:Sprite = new Sprite();
		
		private static var stage:Stage;
		private static var isInit:Boolean;
		private static var myText:TextField;
		
		// you can modify these parameters to customise
		private static var showHideKey:int =96;// key to show hide default is ~
		private static var yposition:int=0; //y position of debug pannel
		private static var panwidth:int=800; //width of window
		
		// height of window, old messages are deleted when height exceeds. Increase the height to see more messages.
		private static var panHeight:int=450; 
		
		/////////
		public static function init():void {
			stage = Application.application.stage;
			if(!isInit){
				stage.addEventListener(KeyboardEvent.KEY_UP, showHide);
				myPanel.y = yposition;
				stage.addChild(myPanel);
				myPanel.visible=false;
				myPanel.addEventListener(MouseEvent.MOUSE_DOWN,mouseDown)
				myPanel.addEventListener(MouseEvent.MOUSE_UP,mouseUp)
				isInit= true;
			}
		}
		private static function mouseDown(evt:Event):void{
			myPanel.startDrag();
		}
		private static function mouseUp(evt:Event):void{
			myPanel.stopDrag()
		}
		private static function showHide(event:KeyboardEvent):void {
			if (event.charCode == showHideKey) {
				if (myPanel.visible) {
					myPanel.visible=false;
				}else{
					myPanel.visible= true;
				}
			}
		}
		private static function updateBackground():void {
			Background.graphics.clear();
			Background.graphics.beginFill(0xffffff);
			Background.graphics.drawRect(0, 0, myText.width, myText.height+25);
			Background.graphics.endFill();
			Background.alpha=0.8;
		}
		private static var myFmt:TextFormat= new TextFormat();
		public static function msg(message:String,color1:int=10):void{
			
			if(myText==null){
				var tText:TextField = new TextField();
				tText.text="Inder's Flex Tracer / Debugger ( Use ' ~ ' key to hide and show tracer window ) Click to Drag Window";
				tText.height=25;
				tText.width=panwidth;
				tText.border=true;
				tText.background=true;
				
				tText.backgroundColor=0x006699;
				var txtFmt:TextFormat= new TextFormat
				txtFmt.size=16;
				txtFmt.leftMargin=4;
				txtFmt.color=0xffffff;
				txtFmt.font="Arial";
				txtFmt.bold=true;
				tText.setTextFormat(txtFmt);
				
				myText = new TextField();
				myText.width = panwidth;
				myText.y = tText.height;
				myText.border = true;
				myText.borderColor=0x000000
				myText.multiline=true;
				myText.wordWrap=true;
				myText.autoSize= TextFieldAutoSize.LEFT;
				
				myFmt.font="Arial";
				myFmt.leftMargin=4;
				myPanel.addChild(Background);
				myPanel.addChild(tText);
				myPanel.addChild(myText);
			}
			if(myText.height>panHeight){
				var x:String=myText.htmlText
				myText.htmlText=x.substring(x.indexOf("</P>"),x.length);
			}
			
			var color:String; 
			switch (color1){ 
				case 0:
					color="#663366";
					break;
				case 1:
					color="#001EFF";
					break;
				case 2:
					color="#ff6666";
					break;
				case 3:
					color="#6DA000";
					break;
				case 4:
					color="#FF8A00";
					break;
				case 5:
					color="#CC0000";
					break;
				case 6:
					color="#3399CC";
					break;
				case 7:
					color="#0055FF";
					break;
				case 8:
					color="#6600CC";
					break;
				case 9:
					color="#FF00FF";
				case 10:
					color="#000000"; 
			}
			myText.htmlText+= "* <font color='"+color+"'>"+message+"</font>";
			myText.setTextFormat(myFmt);
			updateBackground();
			//application not initialized yet
			if (Application.application.stage == null) return;
			init();
		}
	}
}
