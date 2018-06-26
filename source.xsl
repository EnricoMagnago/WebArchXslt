<?xml version="1.0"?>

<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output indent="no" omit-xml-declaration="yes"/>

<xsl:template match="/">
  <xsl:text>item_number,price&#xa;</xsl:text>
  <xsl:for-each select="catalog/product/catalog_item">
    <xsl:value-of select="item_number"/>,<xsl:value-of select="price"/><xsl:text>&#xa;</xsl:text>
  </xsl:for-each>

</xsl:template>

</xsl:transform>
