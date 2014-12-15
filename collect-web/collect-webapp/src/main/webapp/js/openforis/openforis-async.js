OpenForis.Async = function() {};

/**
 * Loads a HTML resource and pass the loaded content as an element to the onSuccess function.
 */
OpenForis.Async.loadHtml = function(pageUrl, onSuccess, onError) {
	$.ajax({
		url: pageUrl,
		dataType:"html"
	}).done(function(response) {
		var content = $(response);
		onSuccess(content);
	}).error( function() {
		onError.apply(this, arguments);
	});
};
