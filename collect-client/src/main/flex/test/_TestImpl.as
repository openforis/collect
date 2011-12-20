package test {
    

  import mx.events.PropertyChangeEvent;
    
  import flash.events.EventDispatcher;    
  import mx.core.IUID;
  import mx.utils.UIDUtil;
    
  /* [ExcludeClass] */
  public class _TestImpl extends flash.events.EventDispatcher
    implements mx.core.IUID, 
               test.Test {
  
    /* Constructor */
    public function _TestImpl():void {
      super();
    }
    
    // implementors of IUID must have a uid property
    private var _uid:String;
    
    [Transient]
    [Bindable(event="propertyChange")] 
    public function get uid():String {
      // If the uid hasn't been assigned a value, just create a new one.
      if (_uid == null) {
        _uid = mx.utils.UIDUtil.createUID();
      }
      return _uid;
    }

    public function set uid(value:String):void {
      const previous:String = _uid;
      if (previous != value) {
        _uid = value;
        dispatchEvent(
          mx.events.PropertyChangeEvent.createUpdateEvent(
            this, "uid", previous, value
          )
        );            
      }
    }
    
      
    /* Property "name" */
    private var _name:String;
    
    [Bindable(event="propertyChange")]
	public function get name():String {
      return _name;
    }
    public function set name(value:String):void {
      const previous:String = this._name;
      if (previous != value) {
        _name = value;
        const ev:mx.events.PropertyChangeEvent = mx.events.PropertyChangeEvent.createUpdateEvent(
          this, "name", previous, _name
        );
        dispatchEvent(ev);
      }
    }
    
    /* Property "surname" */
    private var _surname:String;
    
    [Bindable(event="propertyChange")]
	public function get surname():String {
      return _surname;
    }
    public function set surname(value:String):void {
      const previous:String = this._surname;
      if (previous != value) {
        _surname = value;
        const ev:mx.events.PropertyChangeEvent = mx.events.PropertyChangeEvent.createUpdateEvent(
          this, "surname", previous, _surname
        );
        dispatchEvent(ev);
      }
    }
    
  }
  
}
      