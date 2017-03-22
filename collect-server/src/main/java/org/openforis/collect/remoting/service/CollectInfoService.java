package org.openforis.collect.remoting.service;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openforis.collect.Collect;
import org.openforis.collect.CollectInfo;
import org.openforis.collect.CollectInternalInfo;
import org.openforis.collect.CollectCompleteInfo;
import org.openforis.commons.versioning.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CollectInfoService {

	private static final String LATEST_RELEASE_MAVEN_METADATA_URL = 
			"http://www.openforis.org/nexus/service/local/repositories/releases/content/org/openforis/collect/collect-installer/maven-metadata.xml";
	private static final int RELEASE_FETCH_TIMEOUT = 10000;
	
	public CollectInfo getInfo() {
		return new CollectInfo();
	}
	
	public CollectCompleteInfo getCompleteInfo() {
		Version latestRelease = latestRelease();
		Version currentVersion = Collect.VERSION;
		return new CollectCompleteInfo(currentVersion, latestRelease);
	}
	
	public CollectInternalInfo getInternalInfo() {
		return new CollectInternalInfo();
	}

	public Version latestRelease() {
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

}
