
package test {
  

  import flash.events.IEventDispatcher;    
  import mx.core.IUID;
      
  /* [ExcludeClass] */
  public interface _Test extends flash.events.IEventDispatcher, mx.core.IUID {
      
    /* Property name */
    function get name():String;
    function set name(value:String):void;
    
    /* Property surname */
    function get surname():String;
    function set surname(value:String):void;
    
  }
  
}