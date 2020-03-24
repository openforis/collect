/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.WireVariable;


/**
 * @author S. Ricci
 *
 */
public class PreviewPopUpVM extends SurveyBaseVM {

	private String uri;
	
	@WireVariable
	private SessionManager sessionManager;

	@Init(superclass=false)
	public void init(
			@ExecutionArgParam("surveyId") String surveyId, 
			@ExecutionArgParam("work") String work,
			@ExecutionArgParam("rootEntityId") String rootEntityId,
			@ExecutionArgParam("versionId") String versionId,
			@ExecutionArgParam("locale") String locale,
			@ExecutionArgParam("recordStep") String recordStep
			) throws URISyntaxException {
		super.init();
		URIBuilder uriBuilder = new URIBuilder(Resources.Page.PREVIEW_PATH.getLocation());
		uriBuilder.addParameter("preview", "true");
		uriBuilder.addParameter("surveyId", surveyId);
		uriBuilder.addParameter("work", work);
		uriBuilder.addParameter("rootEntityId", rootEntityId);
		uriBuilder.addParameter("locale", locale);
		if (StringUtils.isNotBlank(versionId)) {
			uriBuilder.addParameter("versionId", versionId);
		}
		uriBuilder.addParameter("recordStep", recordStep);
		this.uri = uriBuilder.build().toString();
	}
	
	public String getContentUrl() throws URISyntaxException {
		return uri;
	}
	
	@Command
	public void close() {
		try {
			sessionManager.releaseRecord();
		} catch(RecordUnlockedException e) {
			// Ignore it
		}
	}
}
