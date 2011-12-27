/**
 * 
 */
package org.openforis.collect.blazeds.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller.Listener;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.session.SessionState;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyUnmarshallerListener;
import org.openforis.idm.metamodel.impl.SurveyImpl;
import org.openforis.idm.util.XmlBindingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.remoting.RemotingInclude;

/**
 * @author M. Togna
 * 
 */
public class SessionService {

	// private static Log LOG = LogUtils.getLog(SessionService.class);

	@Autowired
	protected SessionManager sessionManager;

	/**
	 * Method used to keep the session alive
	 */
	@RemotingInclude
	public void keepAlive() {
		this.sessionManager.keepSessionAlive();
	}

	/**
	 * Return the session state of the active httpsession
	 * 
	 */
	@RemotingInclude
	public SessionState getSessionState() {
		return this.sessionManager.getSessionState();
	}

	//TEST
	@RemotingInclude
	public List<Survey> testGetValue() {
		List<Survey> result = new ArrayList<Survey>();
		try {
			String filename = "naforma.idm.xml";
			Listener listener = new SurveyUnmarshallerListener();
			SurveyImpl survey = XmlBindingUtil.unmarshall(SurveyImpl.class, filename, listener);
			result.add(survey);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	
	
}
