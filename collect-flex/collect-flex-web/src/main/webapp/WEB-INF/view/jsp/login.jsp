<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>

<html>
  <head>
  	<meta http-equiv="Pragma" content="no-cache">
  	<script type="text/javascript" src="script/jquery-1.6.2.min.js"></script>
    <script type="text/javascript" src="script/sessionping.js"></script>
    <style type="text/css" media="screen"> 
		html, body	{ height:100%; }
		body {
			font-family: verdana, serif, monospace;
			font-size: 1.0em;
			margin:0 5 5 5;			
			padding:0 10 10 10;
			overflow: hidden;
			text-align:center;
		    background-color: #ffffff;
		}
		fieldset {
			width: 80%;
			padding-top: 10px;
		}
		legend {
			font-weight: bold;
			font-size: 0.8em;
		}
		.login td {
			font-size: small;
			font-family: verdana, serif, monospace;
			line-height: 1em;
			font-size: 0.7em;
			padding-top: 10;
		}
		input.button {
			font-family: verdana, serif, monospace;
			line-height: 1em;
			font-size: 1em;
			padding: 3 8 5 8;
			background-color: #DDDDDD;
    		border: 1px outset #DDDDDD;
    		color: #000000;
    		border-style: outset;
		}

		.login input {
			height: 20;
			border: 1px solid #DDDDDD;
			font-family: verdana, serif, monospace;
			line-height: 1em;
			font-size: 1em;
		}
		
		div.error {
			font-weight: bold;
			font-size: 0.8em;
			color: red;
		}
		
		div.warn {
			font-weight: bold;
			font-size: 0.8em;
			color: blue;
		}
	</style>
    <title>OpenForis Collect</title>
  </head>

  <body onload="document.f.j_username.focus();">
    <table width="100%"  height="100%">
    	<tr height="102">
    		<td colspan="3" align="center">
    			<img alt="Open Foris Collect" src="images/banner.jpg">
    		</td>
    	</tr>
    	<tr height="50">
    		<td colspan="3" align="center">
<!-- 			    <h3>OpenForis Collect</h3> -->
			    <%-- this form-login-page form is also used as the
			         form-error-page to ask for a login again.
			         --%>
		         <c:if test="${not empty param.session_expired}">
			      <div class="warn">
			        Your session has expired.<br/>
			      </div>
			    </c:if>
			    <c:if test="${not empty param.login_error}">
			      <div class="error">
			        Your login attempt was not successful, try again.<br/><br/>
			        Reason: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>.
			      </div>
			    </c:if>
    		</td>
    	</tr>
    	<tr height="30%">
    		<td width="33%">&nbsp;</td>
    		<td width="33%" style="vertical-align: middle;">
    		<div style="margin-left:auto; margin-right:auto; width: 100%">
					      <table class="login"  width="100%" align="center" style="vertical-align: top; height: 100">
    			<form name="f" action="<c:url value='j_spring_security_check'/>" method="POST">
					      <tr>
					      	<td colspan="2" align="center"><strong style="font-size: 13px; ">Please Log In</strong></td>
					      </tr>
					        <tr>
					        	<td width="50%" align="right">User:</td>
					        	<td width="50%" align="left"><input type='text' name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td>
					        </tr>
					        <tr>
					        	<td width="50%" align="right">Password:</td>
					        	<td width="50%" align="left"><input type='password' name='j_password'></td>
					        </tr>
					        <!-- tr><td><input type="checkbox" name="_spring_security_remember_me"></td><td>Don't ask for my password for two weeks</td></tr-->
					
					        <tr>
					        	<td colspan='2' width="100%" style="text-align: center;">
					        		<input name="submit" type="submit" class="button" value="Login" />
<!-- 					        		<input name="reset" type="reset" class="button" value="Reset"/> -->
				        		</td>
			        		</tr>
			    </form>
					      </table>
			    </div>
    		</td>
    		<td width="33%">&nbsp;</td>
    	</tr>
    	<tr>
    		<td colspan="3" align="center">
    			<img  src="images/footer.jpg">
    		</td>
    	</tr>
    </table>
  </body>
</html>
