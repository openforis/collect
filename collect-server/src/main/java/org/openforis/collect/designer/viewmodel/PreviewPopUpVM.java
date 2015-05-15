/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;


/**
 * @author S. Ricci
 *
 */
public class PreviewPopUpVM extends SurveyBaseVM {

	private String uri;

	@Init(superclass=false)
	public void init(
			@ExecutionArgParam("surveyId") String surveyId, 
			@ExecutionArgParam("work") String work,
			@ExecutionArgParam("rootEntityId") String rootEntityId,
			@ExecutionArgParam("versionId") String versionId,
			@ExecutionArgParam("locale") String locale
			) throws URISyntaxException {
		super.init();
		URIBuilder uriBuilder = new URIBuilder("/index.htm");
		uriBuilder.addParameter("preview", "true");
		uriBuilder.addParameter("surveyId", surveyId);
		uriBuilder.addParameter("work", work);
		uriBuilder.addParameter("rootEntityId", rootEntityId);
		uriBuilder.addParameter("locale", locale);
		if (StringUtils.isNotBlank(versionId)) {
			uriBuilder.addParameter("versionId", versionId);
		}
		this.uri = uriBuilder.build().toString();
	}
	
	public String getContentUrl() throws URISyntaxException {
		return uri;
	}
	
}
