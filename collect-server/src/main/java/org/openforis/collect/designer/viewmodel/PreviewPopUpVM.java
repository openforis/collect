/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SessionRecordFileManager;
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
	@WireVariable
	private SessionRecordFileManager sessionRecordFileManager;

	@Init(superclass = false)
	public void init(@ExecutionArgParam("surveyId") String surveyId, @ExecutionArgParam("work") String work,
			@ExecutionArgParam("rootEntityId") String rootEntityId, @ExecutionArgParam("versionId") String versionId,
			@ExecutionArgParam("locale") String locale, @ExecutionArgParam("recordStep") String recordStep) throws URISyntaxException {
		super.init();
				
		URIBuilder uriBuilderParams = new URIBuilder()
				.addParameter("rootEntityId", rootEntityId)
				.addParameter("locale", locale);
		if (StringUtils.isNotBlank(versionId)) {
			uriBuilderParams.addParameter("versionId", versionId);
		}
		uriBuilderParams.addParameter("recordStep", recordStep);
		this.uri = Resources.Page.PREVIEW_PATH.getLocation() + surveyId + uriBuilderParams.build().toString();
	}

	public String getContentUrl() {
		return uri;
	}

	@Command
	public void close() {
		sessionRecordFileManager.deleteAllTempFiles();
	}
}
