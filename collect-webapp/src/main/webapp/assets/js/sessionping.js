(function ($, window, document) {
	"use strict";

	var methods,
			_local,
			timeout = null,
			defaults = {
				time: 60 * 1000, // time in milliseconds
				address: "api/session/ping"
			},
			settings;

	methods = {
		init: function (input) {
			settings = $.extend(true, {}, defaults, input);

			if (!settings.address) {
				throw new Error("No url address was specified.");
			}

			if (!settings.onError) {
				settings.onError = function (err) { throw new Error(err); };
			} else if (typeof (settings.onError) !== 'function') {
				throw new Error('The onError element provided is required to be a function');
			}

			// Call the request method
			_local.request.call(this);
		},

		methods: function () {
			return methods;
		}
	};

	_local = {
		request: function () {
			// Clears the timer set with setTimeout()
			if ( timeout ) {
				clearTimeout(timeout);
			}

			var editingRecord = typeof OPENFORIS != "undefined" && OPENFORIS.isEditingRecord() ? true: false;
			
			var request = $.ajax({
				url: settings.address,
				type: 'GET',
				data: {
					editing: editingRecord,
					time: new Date().getTime()
				},
				contentType: 'application/json; charset=utf-8'
			}).done(function() {
				timeout = setTimeout(_local.request, settings.time);
			}).fail(function (err) {
				settings.onError(err);
			});
		}
	};

	$.sessionPing = function () {
		return methods.init.apply(this, arguments);
	};

})(jQuery, window, document);

$.sessionPing();