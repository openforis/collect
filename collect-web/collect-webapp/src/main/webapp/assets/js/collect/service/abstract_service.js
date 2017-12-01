Collect.AbstractService = function() {
	this.contextPath = "./";
};

Collect.AbstractService.prototype.loadAll = function(onSuccess, onError) {
	this.send("", null, "GET", onSuccess, onError);
};

Collect.AbstractService.prototype.loadById = function(id, onSuccess, onError) {
	this.send("/" + id, null, "GET", onSuccess, onError);
};

Collect.AbstractService.prototype.save = function(data, onSuccess, onError) {
	this.send("", data, "POST", onSuccess, onError);
};

Collect.AbstractService.prototype.duplicate = function(itemId, onSuccess, onError) {
	this.send("/duplicate.json", itemId, "POST", onSuccess, onError);
};

Collect.AbstractService.prototype.validate = function(data, onSuccess, onError) {
	this.send("/validate.json", data, "POST", onSuccess, onError);
};

Collect.AbstractService.prototype.remove = function(id, onSuccess, onError) {
	this.send("/" + id, null, "DELETE", onSuccess, onError);
};

Collect.AbstractService.prototype.send = function(url, data, method, onSuccess, onError) {
	var $this = this;
	$.ajax({
		url: $this.contextPath + url,
		cache: false,
		method: method ? method: "GET",
		data: data ? $this.param(data) : null
	}).done(function(response, textStatus, jqXHR) {
		if (! response || ! response.hasOwnProperty("statusOk") || response.statusOk) {
			onSuccess(response);
		} else {
			if (onError) {
				onError(response);
			} else {
				var errorMessage = response.errorMessage;
				collect.error.apply(this, [jqXHR, errorMessage]);
			}
		}
	}).fail(function(jqXHR, textStatus, errorThrown) {
		if (onError) {
			onError();
		} else {
			collect.error.apply(this, [jqXHR, textStatus, errorThrown]);
		}
	});
};

/**
 * Serializes a complex object into a parameter query string.
 * Copied from jQuery param function, the only difference is that nested bean properties
 * are serialized with a different naming convention (e.g. step[1].id instead of step[1][id] 
 * as jQuery param function does.
 */
Collect.AbstractService.prototype.param = function( a ) {
	var r20 = /%20/g, 
		rbracket = /\[\]$/;
	
	function buildParams(prefix, obj, add) {
		if (jQuery.isArray(obj)) {
			// Serialize array item.
			jQuery.each(obj, function(i, v) {
				if (rbracket.test(prefix)) {
					// Treat each array item as a scalar.
					add(prefix, v);

				} else {
					//buildParams(prefix + "[" + (typeof v === "object" || jQuery.isArray(v) ? i : "") + "]", v, add);
					buildParams(prefix + "[" + i + "]", v, add);
				}
			});
		} else if (obj != null && typeof obj === "object") {
			// Serialize object item.
			for ( var name in obj) {
				buildParams(prefix + "." + name, obj[name], add);
			}
		} else {
			// Serialize scalar item.
			add(prefix, obj);
		}
	};
	
    var s = [],
      add = function( key, value ) {
        // If value is a function, invoke it and return its value
        value = value == null ? "" : jQuery.isFunction( value ) ? value() : value;
        s[ s.length ] = encodeURIComponent( key ) + "=" + encodeURIComponent( value );
      };

    // If an array was passed in, assume that it is an array of form elements.
    if ( jQuery.isArray( a ) || ( a.jquery && !jQuery.isPlainObject( a ) ) ) {
      // Serialize the form elements
      jQuery.each( a, function() {
        add( this.name, this.value );
      });

    } else {
      for ( var prefix in a ) {
        buildParams( prefix, a[ prefix ], add );
      }
    }

    // Return the resulting serialization
    return s.join( "&" ).replace( r20, "+" );
  }