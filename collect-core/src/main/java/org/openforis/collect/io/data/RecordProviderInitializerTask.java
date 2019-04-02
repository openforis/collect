package org.openforis.collect.io.data;

import java.io.File;

import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.concurrency.Progress;
import org.openforis.concurrency.ProgressListener;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RecordProviderInitializerTask extends Task {

	private Input input;
	private RecordProvider output;
	
	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		output = new XMLParsingRecordProvider(input.file, input.packagedSurvey, input.existingSurvey, input.activeUser, 
				input.userManager, input.initializeRecords, input.validateRecords, true);
	}
	
	@Override
	protected void execute() throws Throwable {
		output.init(new ProgressListener() {
			public void progressMade(Progress progress) {
				setTotalItems(progress.getTotalItems());
				setProcessedItems(progress.getProcessedItems());
			}
		});
	}
	
	public Input getInput() {
		return input;
	}
	
	public void setInput(Input input) {
		this.input = input;
	}
	
	public RecordProvider getOutput() {
		return output;
	}
	
	public static class Input {
		private File file;
		private CollectSurvey packagedSurvey;
		private CollectSurvey existingSurvey;
		private User activeUser;
		private UserManager userManager;
		private boolean validateRecords;
		private boolean initializeRecords;
		
		public File getFile() {
			return file;
		}
		
		public void setFile(File file) {
			this.file = file;
		}
		
		public CollectSurvey getPackagedSurvey() {
			return packagedSurvey;
		}
		
		public void setPackagedSurvey(CollectSurvey packagedSurvey) {
			this.packagedSurvey = packagedSurvey;
		}
		
		public CollectSurvey getExistingSurvey() {
			return existingSurvey;
		}
		
		public void setExistingSurvey(CollectSurvey existingSurvey) {
			this.existingSurvey = existingSurvey;
		}
		
		public User getActiveUser() {
			return activeUser;
		}
		
		public void setActiveUser(User activeUser) {
			this.activeUser = activeUser;
		}
		
		public UserManager getUserManager() {
			return userManager;
		}

		public void setUserManager(UserManager userManager) {
			this.userManager = userManager;
		}
		
		public boolean isValidateRecords() {
			return validateRecords;
		}
		
		public void setValidateRecords(boolean validateRecords) {
			this.validateRecords = validateRecords;
		}
		
		public boolean isInitializeRecords() {
			return initializeRecords;
		}
		
		public void setInitializeRecords(boolean initializeRecords) {
			this.initializeRecords = initializeRecords;
		}
	}
	
}
