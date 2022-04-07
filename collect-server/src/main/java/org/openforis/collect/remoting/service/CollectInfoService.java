package org.openforis.collect.remoting.service;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.Collect;
import org.openforis.collect.CollectCompleteInfo;
import org.openforis.collect.CollectInfo;
import org.openforis.collect.CollectInternalInfo;
import org.openforis.collect.reporting.SaikuConfiguration;
import org.openforis.commons.versioning.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
public class CollectInfoService {

	protected static final Logger LOG = LogManager.getLogger(CollectInfoService.class);

	private static final String LATEST_RELEASE_METADATA_URL = Collect.NEXUS_URL
			+ "/org/openforis/collect/collect/maven-metadata.xml";

	private static final int RELEASE_FETCH_TIMEOUT = 10000;

	@Autowired
	private SaikuConfiguration saikuConfiguration;

	public CollectInfo getInfo() {
		return new CollectInfo();
	}

	public CollectCompleteInfo getCompleteInfo() {
		Version latestRelease = latestRelease();
		Version currentVersion = Collect.VERSION;
		CollectCompleteInfo info = new CollectCompleteInfo(currentVersion, latestRelease);
		return info;
	}

	public CollectCompleteInfo getCompleteInfo(HttpServletRequest request) {
		Version latestRelease = latestRelease();
		Version currentVersion = Collect.VERSION;
		CollectCompleteInfo info = new CollectCompleteInfo(currentVersion, latestRelease);
		info.setSaikuUrl(determineSaikuUrl(request));
		return info;
	}

	public CollectInternalInfo getInternalInfo() {
		return new CollectInternalInfo();
	}

	private Version latestRelease() {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpGet request = new HttpGet(LATEST_RELEASE_METADATA_URL);
			request.setConfig(RequestConfig.custom().setConnectTimeout(RELEASE_FETCH_TIMEOUT).build());

			CloseableHttpResponse response = client.execute(request);
			try {
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				// completely disable DOCTYPE declaration to avoid access to external entities in XML parsing
				dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(is);
				doc.getDocumentElement().normalize();
				Element versioningElement = (Element) doc.getElementsByTagName("versioning").item(0);
				NodeList releaseNodeList = versioningElement.getElementsByTagName("release");
				if (releaseNodeList.getLength() == 1) {
					Node releaseNode = releaseNodeList.item(0).getFirstChild();
					String release = releaseNode.getNodeValue();
					return new Version(release);
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
		}
		return null;
	}

	private String determineSaikuUrl(HttpServletRequest request) {
		String protocol = determineProtocol(request);
		String host = determineHost(request);
		return String.format("%s://%s/%s", protocol, host, saikuConfiguration.getContextPath());
	}

	private String determineHost(HttpServletRequest request) {
		try {
			URL url = new URL(request.getRequestURL().toString());
			String host = url.getHost();
			int port = url.getPort();
			return port > 0 ? String.format("%s:%d", host, port) : host;
		} catch (MalformedURLException e1) {
			// it should never be thrown, url is the request url and is always correct
			return null;
		}
	}
	
	private String determineProtocol(HttpServletRequest request) {
		try {
			String requestUrl = request.getRequestURL().toString();
			LOG.info("Getting info from url " + requestUrl);
			URL url = new URL(requestUrl);
			return url.getProtocol();
		} catch (MalformedURLException e1) {
			// it should never be thrown, url is the request url and is always correct
			return null;
		}
	}
}
