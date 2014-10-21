<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:idml="http://www.openforis.org/idml/3.0"
    xmlns:ui="http://www.openforis.org/collect/3.0/ui"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0"
    xsi:schemaLocation="http://www.openforis.org/idml/3.0 https://raw.github.com/openforis/idm/reengineering/idm-api/src/main/resources/idml3.xsd">
    
    <xsl:template match="/">
        <html>
            <head>
            	<style type="text/css">
					.idmlMainHeading {
					    font-size: 20px;
						color: black;
						text-align: center;
					}
					.idmlProjectName {
					    font-size: 24px;
					    color: #5588DD;
					    text-align: center;
						font-weight: bold;
					}
					h1 {
					    font-size: 26px;
						color: black;
					}
					h2 {
					    font-size: 20px;
					    color: #5588DD;
					}
					h3 {
					    font-size: 18px;    
					    color: #5588DD;
					}
					h4 {
					    font-size: 16px;
					    color: #5588DD;
					}
					h1, h2, h3, h4, h5 {
					    font-family: 'Times New Roman', Times, serif;
					    padding: 0;
					    margin: 8px 0 0 0;
					}
					table {
					    border-collapse: collapse;    
					}
					th, td {
					    padding: 2px;
					    vertical-align: top;
					    text-align: left;
					}
					th {
					    font-family: Arial, sans-serif;
						font-size: 11px;
					}
					td {
					    font-family: Calibri, Verdana, Arial, sans-serif;
					    font-size: 12px;
					    border: 1px solid #CCC;
					}
					
					.idmlListName {
					    font-family: 'Courier New', courier, fixed, monospace;
					    font-size: .9em;
					}
					.idmlEntityName, .idmlAttrName {
					    font-family: 'Courier New', courier, fixed, monospace;
					    font-size: .7em;
					    white-space: nowrap;
					}
					.idmlEntityName {
					    font-weight: bold;
					}
					.idmlLabel {
					    
					}
            	</style>                
            </head>
            <body>
                <xsl:apply-templates select="idml:survey"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="idml:survey">
        <div class="idmlProjectName">
            <xsl:value-of select="idml:project"/>
        </div>
        <div class="idmlMainHeading">Inventory Data Model</div>
        <xsl:apply-templates select="idml:codeLists"/>
        <xsl:apply-templates select="idml:schema"/>
    </xsl:template>

    <!--  -->
    <xsl:template match="idml:codeLists">
        <h1>Code lists</h1>
        <xsl:apply-templates select="idml:list"/>
    </xsl:template>

    <xsl:template match="idml:list">
        <h2>
            <xsl:call-template name="list-heading"/>
        </h2>
        <span class="idmlListName"><xsl:value-of select="@name"/></span>
        <xsl:apply-templates select="idml:items"/>
    </xsl:template>

    <xsl:template match="idml:items">
        <xsl:call-template name="list-level"/>
    </xsl:template>

    <xsl:template name="list-level">
        <table>
            <thead>
                <tr>
                    <th>Code</th>
                    <th>Label(s)</th>
                    <th>Description(s)</th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="idml:item">                    
                    <tr>
                        <td>
                            <xsl:if test="idml:item">
                                <xsl:attribute name="rowspan">2</xsl:attribute>
                            </xsl:if>
                            <xsl:value-of select="idml:code"/>
                        </td>
                        <td>
                            <xsl:apply-templates select="idml:label"/>
                        </td>
                        <td>
                            <xsl:apply-templates select="idml:description"/>
                        </td>
                    </tr>
                    <xsl:if test="idml:item">
                        <tr>
                            <td colspan="2">
                                <xsl:call-template name="list-level"/>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>
   
    <xsl:template name="list-heading">
        <xsl:choose>
            <xsl:when test="idml:label[@type='list']/text()">
                <xsl:value-of select="idml:label[@type='list']"/>
            </xsl:when>
            <xsl:when test="idml:label[@type='item']/text()">
                <xsl:value-of select="idml:label[@type='item']"/>
            </xsl:when>
            <xsl:otherwise>
                Unnamed list
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- -->
    <xsl:template match="idml:schema">
        <h1>Schema</h1>
        <table>
            <thead>
                <th>Name</th>
                <th>Type</th>
                <th>Subtype</th>
                <th>Label(s)</th>
                <th>Prompt(s)</th>
                <th>Description(s)</th>
                <th>Default(s)</th>
            </thead>
            <tbody>
                <xsl:apply-templates select="*">
                    <xsl:with-param name="level">0</xsl:with-param>
                </xsl:apply-templates>
            </tbody>
        </table>
    </xsl:template>

    <xsl:template match="idml:entity|idml:code|idml:text|idml:date|idml:time|idml:taxon|idml:coordinate|idml:number|idml:range|idml:file|idml:boolean">
        <xsl:param name="level"/>
        <tr>
            <td>
                <xsl:choose>
                    <xsl:when test="name()='entity'">
                        <xsl:attribute name="class">idmlEntityName</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="class">idmlAttrName</xsl:attribute>                        
                    </xsl:otherwise>
                </xsl:choose>
                
                <xsl:call-template name="indent">
                    <xsl:with-param name="count" select="$level * 2"/>
                </xsl:call-template>
                <xsl:value-of select="@name"/>
            </td>
            <td>
                <xsl:value-of select="name()"/>
            </td>
            <td>
                <xsl:value-of select="@type"/>
            </td>
            <td>
                <xsl:apply-templates select="idml:label"/>
            </td>
            <td>
                <xsl:apply-templates select="idml:prompt"/>                
            </td>
            <td>
                <xsl:apply-templates select="idml:description"/>                
            </td>
            <td>
                <xsl:apply-templates select="idml:default"/>
            </td>
        </tr>
        <xsl:apply-templates select="idml:entity|idml:code|idml:text|idml:date|idml:time|idml:taxon|idml:coordinate|idml:number|idml:range|idml:file|idml:boolean">
            <xsl:with-param name="level" select="$level+1"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="idml:default">
        <xsl:value-of select="@value"/><xsl:value-of select="@expr"/>
    </xsl:template>
    
    <!-- Shared templates -->
    <xsl:template match="idml:label|idml:prompt|idml:description">
        <xsl:value-of select="."/>
        <xsl:if test="@xml:lang">
            <span class="idmlLang">&#160;[<xsl:value-of select="@xml:lang"/>]</span>
        </xsl:if>
        &#160;
    </xsl:template>
    
    <xsl:template name="indent">
        <xsl:param name="count"/>
        <xsl:if test="$count > 0">&#160;<xsl:call-template name="indent"><xsl:with-param name="count" select="$count - 1"/></xsl:call-template></xsl:if>
    </xsl:template>
    
    <xsl:template match="idml:*"/>
</xsl:stylesheet>
