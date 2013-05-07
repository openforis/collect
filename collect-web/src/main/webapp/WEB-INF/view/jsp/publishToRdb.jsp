<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <title>Survey Publish to RDB</title>
        <meta http-equiv="cache-control" content="no-store, no-cache, must-revalidate" />
		<meta http-equiv="Pragma" content="no-store, no-cache" />
		<meta http-equiv="Expires" content="0" />
		<meta name="google" value="notranslate" />         
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />		
        <script type="text/javascript" src="script/jquery-1.6.2.min.js"></script>
        <script type="text/javascript" src="script/sessionping.js"></script>
    </head>
    <body>
        <h1>Publish survey data into a relational database:</h1>
        <form method="post" action="submitPublishToRdb">
        	
            <label>Survey Name:</label>
            <input type="text" name="survey" />
            <label>Root Entity Name:</label>
            <input type="text" name="root_entity" />
            <label>Target Schema:</label>
            <input type="text" name="schema" />
<!--             <label>Target URL:</label> -->
<!--             <input type="text" name="url" /> -->
<!--             <label>Target User:</label> -->
<!--             <input type="text" name="user" /> -->
<!--             <label>Target Password:</label> -->
<!--             <input type="text" name="pass" /> -->
            
            <input type="submit"/>
        </form>
    </body>
</html>