$(window, document, undefined).ready(function() {

	$('input').blur(function() {
		var $this = $(this);
		if ($this.val())
			$this.addClass('used');
		else
			$this.removeClass('used');
	});

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

	if (! LOGGED_OUT) {
		checkDefaultPasswordActive();
	}

	function checkDefaultPasswordActive() {
		$.ajax({
			url: "api/defaultpasswordactive"
		}).done(function(defaultPasswordActive) {
			if (defaultPasswordActive) {
				document.f.username.value = "admin";
				document.f.password.value = "admin";
				document.f.submit();
			}
		});
	};
});