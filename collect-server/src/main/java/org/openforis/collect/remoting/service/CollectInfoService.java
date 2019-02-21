package org.openforis.collect.remoting.service;

import java.io.InputStream;
import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openforis.collect.Collect;
import org.openforis.collect.CollectCompleteInfo;
import org.openforis.collect.CollectInfo;
import org.openforis.collect.CollectInternalInfo;
import org.openforis.collect.reporting.SaikuConfiguration;
import org.openforis.commons.versioning.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
public class CollectInfoService {

	private static final String LATEST_RELEASE_MAVEN_METADATA_URL = 
			"http://www.openforis.org/nexus/service/local/repositories/releases/content/org/openforis/collect/collect-installer/maven-metadata.xml";
	private static final int RELEASE_FETCH_TIMEOUT = 10000;
	
	private static final String DEV_LOCAL_ADDRESS = "127.0.0.1";
	private static final String DEV_REQUEST_LOCAL_ADDRESS = "0:0:0:0:0:0:0:1";
	
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
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpGet request = new HttpGet(LATEST_RELEASE_MAVEN_METADATA_URL);
			request.setConfig(RequestConfig.custom().setConnectTimeout(RELEASE_FETCH_TIMEOUT).build());
			
			CloseableHttpResponse response = client.execute(request);
			try {
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
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
		} catch(Exception e) {
		}
		return null;
	}

	private String determineSaikuUrl(HttpServletRequest request) {
		String protocol = request.isSecure() ? "https" : "http";
		String host = determineHost(request);
		return String.format("%s://%s/%s", protocol, host, saikuConfiguration.getContextPath());
	}
	
	private String determineHost(HttpServletRequest request) {
		String host = request.getHeader(HttpHeaders.HOST);
		if (host == null) {
			int port = request.getLocalPort();
			String localHostName = request.getLocalAddr();
			if (localHostName == null) {
				try {
					InetAddress localHost = InetAddress.getLocalHost();
					localHostName = localHost.getHostName();
				} catch (Exception e) {
				}
			}
			if (DEV_REQUEST_LOCAL_ADDRESS.equals(localHostName)) {
				localHostName = DEV_LOCAL_ADDRESS;
			}
			return String.format("%s:%d", localHostName, port);
		} else {
			return host;
		}
	}
}
