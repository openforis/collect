<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>

<%@ page import="org.springframework.security.core.Authentication" %>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page pageEncoding="UTF-8" %>

<html>
  <head>
    <title>Exit User</title>
  </head>

  <body>
    <h1>Exit User</h1>

    <c:if test="${not empty param.login_error}">
      <font color="red">
        Your 'Exit User' attempt was not successful, try again.<br/><br/>
        Reason: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>
      </font>
    </c:if>

    <form action="<c:url value='j_spring_security_exit_user'/>" method="POST">
      <table>
        <tr><td>Current User:</td><td>

<%
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) { %>

        <%= auth.getPrincipal().toString() %>

 <% } %>
         </td></tr>
        <tr><td colspan='2'><input name="exit" type="submit" value="Exit"></td></tr>
      </table>
    </form>
  </body>
</html>
