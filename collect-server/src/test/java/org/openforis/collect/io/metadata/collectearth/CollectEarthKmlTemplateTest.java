package org.openforis.collect.io.metadata.collectearth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * The KML template of a Collect Earth project file is rendered by Collect Earth, not by Collect,
 * so a broken template is only noticed once a project is opened in Google Earth. This test renders it
 * with the same data model that the KML generators of Collect Earth fill in.
 */
public class CollectEarthKmlTemplateTest {

	private static final String TEMPLATE_PATH = "org/openforis/collect/designer/templates/collectearth/kml_template.txt";
	private static final String PLACEMARK_ID = "1";

	@Test
	public void plotAndReferenceAreasAreRenderedAsSeparatePlacemarks() throws Exception {
		Document kml = renderAndParse(2);

		// the plot keeps the placemark carrying the balloon; the reference areas are drawn on their own
		assertEquals("#placemark-balloon-style", styleOfPlacemarkContaining(kml, "poly_" + PLACEMARK_ID));
		assertEquals("#reference-area-style", styleOfPlacemarkContaining(kml, "poly_" + PLACEMARK_ID + "_reference_area_0"));
		assertEquals("#reference-area-style", styleOfPlacemarkContaining(kml, "poly_" + PLACEMARK_ID + "_reference_area_1"));

		// a polygon has a single outer boundary: the plot has the sample points as holes, a reference area has none
		assertEquals(1, boundariesOf(kml, "poly_" + PLACEMARK_ID, "outerBoundaryIs"));
		assertEquals(1, boundariesOf(kml, "poly_" + PLACEMARK_ID, "innerBoundaryIs"));
		assertEquals(1, boundariesOf(kml, "poly_" + PLACEMARK_ID + "_reference_area_0", "outerBoundaryIs"));
		assertEquals(0, boundariesOf(kml, "poly_" + PLACEMARK_ID + "_reference_area_0", "innerBoundaryIs"));
	}

	@Test
	public void plotWithoutReferenceAreaHasNoExtraPlacemark() throws Exception {
		Document kml = renderAndParse(0);

		assertEquals(0, kml.getElementsByTagName("MultiGeometry").getLength());
		NodeList placemarks = kml.getElementsByTagName("Placemark");
		for (int i = 0; i < placemarks.getLength(); i++) {
			Element placemark = (Element) placemarks.item(i);
			String style = placemark.getElementsByTagName("styleUrl").item(0).getTextContent().trim();
			assertTrue("unexpected reference area placemark", !"#reference-area-style".equals(style));
		}
	}

	private String styleOfPlacemarkContaining(Document kml, String polygonId) {
		NodeList polygons = kml.getElementsByTagName("Polygon");
		for (int i = 0; i < polygons.getLength(); i++) {
			Element polygon = (Element) polygons.item(i);
			if (polygonId.equals(polygon.getAttribute("id"))) {
				Element placemark = (Element) polygon.getParentNode();
				while (!"Placemark".equals(placemark.getTagName())) {
					placemark = (Element) placemark.getParentNode();
				}
				return placemark.getElementsByTagName("styleUrl").item(0).getTextContent().trim();
			}
		}
		throw new AssertionError("Polygon not found: " + polygonId);
	}

	private int boundariesOf(Document kml, String polygonId, String boundaryTag) {
		NodeList polygons = kml.getElementsByTagName("Polygon");
		for (int i = 0; i < polygons.getLength(); i++) {
			Element polygon = (Element) polygons.item(i);
			if (polygonId.equals(polygon.getAttribute("id"))) {
				return polygon.getElementsByTagName(boundaryTag).getLength();
			}
		}
		throw new AssertionError("Polygon not found: " + polygonId);
	}

	private Document renderAndParse(int referenceAreas) throws Exception {
		String kml = render(referenceAreas);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		return factory.newDocumentBuilder().parse(new InputSource(new java.io.StringReader(kml)));
	}

	private String render(int referenceAreas) throws Exception {
		Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		Template template = new Template("kml", loadTemplate(), configuration);
		StringWriter writer = new StringWriter();
		template.process(dataModel(referenceAreas), writer);
		return writer.toString();
	}

	private String loadTemplate() throws IOException {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH)) {
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		}
	}

	/**
	 * The same values that the KML generators of Collect Earth put in the model for a square plot
	 */
	private Map<String, Object> dataModel(int referenceAreas) {
		Map<String, Object> placemark = new HashMap<String, Object>();
		placemark.put("placemarkId", PLACEMARK_ID);
		placemark.put("nextPlacemarkId", "2");
		placemark.put("coord", coordinate(0d, 0d));
		placemark.put("shape", ring(50));
		placemark.put("points", Arrays.asList(samplePoint()));
		placemark.put("samplePointOutlined", 0);
		List<Object> buffers = new ArrayList<Object>();
		for (int i = 0; i < referenceAreas; i++) {
			Map<String, Object> buffer = new HashMap<String, Object>();
			buffer.put("shape", ring(150 + i * 50));
			buffers.add(buffer);
		}
		placemark.put("buffers", buffers);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("placemarks", Arrays.asList(placemark));
		model.put("expiration", "2026-01-01T00:00:00Z");
		model.put("region_center_X", "0");
		model.put("region_center_Y", "0");
		model.put("html_for_balloon", "<html></html>");
		model.put("host", "http://127.0.0.1:8028/earth/");
		model.put("local_port", "8028");
		model.put("plotFileName", "test_plots.ced");
		model.put("randomNumber", "1");
		return model;
	}

	private Map<String, Object> samplePoint() {
		Map<String, Object> point = new HashMap<String, Object>();
		point.put("shape", ring(1));
		return point;
	}

	private List<Object> ring(double halfSide) {
		return Arrays.<Object>asList(
				coordinate(-halfSide, halfSide), coordinate(halfSide, halfSide),
				coordinate(halfSide, -halfSide), coordinate(-halfSide, -halfSide),
				coordinate(-halfSide, halfSide));
	}

	private Map<String, Object> coordinate(double longitude, double latitude) {
		Map<String, Object> coordinate = new HashMap<String, Object>();
		coordinate.put("longitude", String.valueOf(longitude / 10000));
		coordinate.put("latitude", String.valueOf(latitude / 10000));
		return coordinate;
	}
}
