package it.unitn.webarch.xml2csv;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Project: xml2csv
 * Created by en on 11/10/17.
 */
public class Xml2Csv{
	private final String inputXml;

	public Xml2Csv(String inputXml){
		this.inputXml = inputXml;
	}

	public void toCsvXslt(final String inputXslt, final String output){
		final TransformerFactory tf = TransformerFactory.newInstance();
		final StreamSource xsl = new StreamSource(inputXslt);
		final StreamSource xml = new StreamSource(this.inputXml);
		final StreamResult out = new StreamResult(output);
		Transformer t = null;
		try{
			t = tf.newTransformer(xsl);
		}catch(TransformerConfigurationException e){
			System.err.println("unable to create transformer:");
			e.printStackTrace();
			System.exit(1);
		}

		try{
			t.transform(xml, out);
		}catch(TransformerException e){
			System.err.println("unable to transformer:");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void toCsvXPath(final String output){
		/* open output file */
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(output, "UTF-8");
		}catch(FileNotFoundException e){
			System.err.println("unable to access/create file: " + output);
			e.printStackTrace();
			System.exit(1);
		}catch(UnsupportedEncodingException e){
			System.err.println("unknown UTF-8 encoding");
			e.printStackTrace();
			System.exit(1);
		}

		/* create document builder */
		final DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
		domFact.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try{
			builder = domFact.newDocumentBuilder();
		}catch(ParserConfigurationException e){
			System.err.println("unable to parse configuration");
			e.printStackTrace();
			System.exit(1);
		}

		/* parse input document */
		Document doc = null;
		try{
			doc = builder.parse(this.inputXml);
		}catch(SAXException e){
			e.printStackTrace();
		}catch(IOException e){
			System.err.println("unable to parse xml file: " + this.inputXml);
			e.printStackTrace();
			System.exit(1);
		}

		/* create xpath to get all catalog_item nodes */
		XPathFactory xPathFact = XPathFactory.newInstance();
		XPath xpath = xPathFact.newXPath();
		XPathExpression itemsExpr = null;
		try{
			itemsExpr = xpath.compile("//catalog_item/.");
		}catch(XPathExpressionException e){
			System.err.println("unable to compile xpath");
			e.printStackTrace();
			System.exit(1);
		}

		/* get catalog_item nodes list*/
		NodeList items = null;
		try{
			assert itemsExpr != null;
			items = (NodeList) itemsExpr.evaluate(doc, XPathConstants.NODESET);
		}catch(XPathExpressionException e){
			System.err.println("unable to evaluate xpath");
			e.printStackTrace();
			System.exit(1);
		}

		/* create xPath to get item_number and price from a catalog_item */
		XPathExpression priceExpr = null;
		XPathExpression numberExpr = null;
		try{
			numberExpr = xpath.compile("./item_number/text()");
		}catch(XPathExpressionException e){
			System.err.println("unable to compile item_number xpath");
			e.printStackTrace();
			System.exit(1);
		}
		assert numberExpr != null;

		try{
			priceExpr = xpath.compile("./price/text()");
		}catch(XPathExpressionException e){
			System.err.println("unable to compile item_number xpath");
			e.printStackTrace();
			System.exit(1);
		}
		assert priceExpr != null;

		/* iterate on each catalog_item and get his number and price, output on
		* file using csv format */
		writer.println("item_number,price");
		for(int i = 0; i < items.getLength(); i++){
			final Node item = items.item(i);
			Node price = null;
			Node number = null;
			try{
				number = (Node) numberExpr.evaluate(item, XPathConstants.NODE);
				price = (Node) priceExpr.evaluate(item, XPathConstants.NODE);
			}catch(XPathExpressionException e){
				System.err.println("unable to evaluate item_number or price xpath");
				e.printStackTrace();
				System.exit(1);
			}
			assert price != null;
			assert number != null;

			writer.println(number.getNodeValue() + "," + price.getNodeValue());
		}
		writer.close();
	}

	public static void main(String[] args){
		final Xml2Csv self = new Xml2Csv("source.xml");
		self.toCsvXslt("source.xsl", "out_xslt.csv");
		self.toCsvXPath("out_xpath.csv");
	}
}
