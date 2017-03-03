<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Set"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">	
    <head>
		<title>Open Foris Collect</title>
		
		<link rel="shortcut icon" href="assets/images/favicon.ico" />
		
	    <meta http-equiv="cache-control" content="no-store, no-cache, must-revalidate" />
		<meta http-equiv="Pragma" content="no-store, no-cache" />
		<meta http-equiv="Expires" content="0" />
		<meta name="google" value="notranslate" />         
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />		

     	<style type="text/css" media="screen"> 
			html, body	{ height:100%; }
			body {
				margin:0;
				padding:0;
				overflow: hidden;
				text-align:center;
			    background-color: #ffffff;
			}
			object:focus {
				outline:none;
			}
		</style>

		<script type="text/javascript" src="script/jquery-1.6.2.min.js"></script>
		<script type="text/javascript" src="script/swfobject.js"></script>
		<script type="text/javascript" src="script/openforis.js"></script>
		<script type="text/javascript" src="script/sessionping.js"></script>
		<script type="text/javascript">
			// For version detection, set to min. required Flash Player version, or 0 (or 0.0.0), for no version detection. 
			var swfVersionStr = "10.2.0";
			// To use express install, set to playerProductInstall.swf, otherwise the empty string. 
			var xiSwfUrlStr = "flash/playerProductInstall.swf";
			
			var flashvars = {};
			//pass request parameters as flash vars
			<%
				Set<Entry<String, String[]>> parametersSet = request.getParameterMap().entrySet();
				for ( Entry<String, String[]> entry: parametersSet ) {
					String[] values = entry.getValue();
					if ( values != null && values.length > 0 ) {
						String value = values[0];
						out.write("flashvars['" + entry.getKey() + "'] = '" + value + "';\r\n");
					}
				}
			%>
			if ( ! flashvars.locale ) {
				flashvars.locale = '<%= request.getLocale().toString() %>';
			}
	        var params = {};
			// params.wmode = "opaque";
	        params.quality = "high";
	        params.bgcolor = "#ffffff";
	        params.allowscriptaccess = "sameDomain";
	        params.allowfullscreen = "true";
	        var attributes = {};
	        attributes.id = "collect";
	        attributes.name = "collect";
	        attributes.align = "middle";
	        swfobject.embedSWF(
	            "collect.swf", "flashContent", 
	            "100%", "100%", 
	            swfVersionStr, xiSwfUrlStr, 
	            flashvars, params, attributes);
	        // JavaScript enabled so display the flashContent div in case it is not replaced with a swf object.
	        swfobject.createCSS("#flashContent", "display:block;text-align:left;");
	        
	        //init OpenForis
	        OPENFORIS.init();
		</script>
    </head>
	<body>
		<!-- 
			SWFObject's dynamic embed method replaces this alternative HTML content with Flash content when enough 
           	JavaScript and Flash plug-in support is available. The div is initially hidden so that it doesn't show
           	when JavaScript is disabled.
      	-->
		<div id="flashContent">
			<p>To view this page ensure that Adobe Flash Player version 10.2.0 or greater is installed.</p>
			<script type="text/javascript"> 
		         var pageHost = ((document.location.protocol == "https:") ? "https://" : "http://"); 
		         document.write("<a href='http://www.adobe.com/go/getflashplayer'><img src='" 
			                          + pageHost + "www.adobe.com/images/shared/download_buttons/get_flash_player.gif' alt='Get Adobe Flash player' /></a>" ); 
		    </script>
		</div>
		<noscript>Please enable JavaScript in your browser.</noscript>
	</body>
</html>