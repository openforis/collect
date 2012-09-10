(function ($, window, document) {
	"use strict";

	var methods,
			_local,
			timeout,
			defaults = {
				time: 60 * 1000, // time in miliseconds
				address: window.location.href
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
			clearTimeout(timeout);

			var request = $.ajax({
				url: settings.address,
				type: 'POST',
				data: {},
				contentType: 'application/json; charset=utf-8'
			});

			request.success(function () {
				timeout = setTimeout(_local.request, settings.time);
			});

			request.error(function (err) {
				settings.onError(err);
			});
		}
	};

	$.sessionPing = function () {
		return methods.init.apply(this, arguments);
	};

})(jQuery, window, document);

$.sessionPing();