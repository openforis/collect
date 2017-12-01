$(window, document, undefined).ready(function() {

	const DEFAULT_USERNAME = 'admin';
	const DEFAULT_PASSWORD = 'admin';
	
	$('input').blur(toggleInputUsedClass);

	var $ripples = $('.ripples');

	$ripples.on('click.Ripples', function(e) {

		var $this = $(this);
		var $offset = $this.parent().offset();
		var $circle = $this.find('.ripplesCircle');

		var x = e.pageX - $offset.left;
		var y = e.pageY - $offset.top;

		$circle.css({
			top : y + 'px',
			left : x + 'px'
		});

		$this.addClass('is-active');

	});

	$ripples.on('animationend webkitAnimationEnd mozAnimationEnd oanimationend MSAnimationEnd', function(e) {
		$(this).removeClass('is-active');
	});

	jQuery.i18n.properties({
		name : 'messages',
		path : 'assets/bundle/',
		mode : 'both', // We specified mode: 'both' so
						// translated values will be available
						// as JS vars/functions and as a map
		checkAvailableLanguages : true
	});

	OF.i18n.initializeAll();

	document.f.username.focus();

	//set input fields as 'used' if fields have been filled by browser autofill
	setTimeout(function() {
		$('input').each(toggleInputUsedClass);
	}, 500);
	
	if (! LOGGED_OUT) {
		checkDefaultPasswordActive();
	}
	
	function toggleInputUsedClass() {
		var $this = $(this);
		var empty = $this.val().length === 0;
		$this.toggleClass('used', !empty);
	}

	function checkDefaultPasswordActive() {
		$.ajax({
			url: "api/defaultpasswordactive"
		}).done(function(defaultPasswordActive) {
			if (defaultPasswordActive) {
				document.f.username.value = DEFAULT_USERNAME;
				document.f.password.value = DEFAULT_PASSWORD;
				document.f.submit();
			}
		});
	};
});