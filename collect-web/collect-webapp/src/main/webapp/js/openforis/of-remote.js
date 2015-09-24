OF.Remote = function() {};

/**
 * Loads a HTML resource and pass the loaded content as an element to the onSuccess function.
 */
OF.Remote.loadHtml = function(pageUrl, onSuccess, onError) {
	$.ajax({
		url: pageUrl,
		dataType: "html",
		cache: false
	}).done(function(response) {
		var content = $(response);
		onSuccess(content);
	}).error( function() {
		onError.apply(this, arguments);
	});
};
