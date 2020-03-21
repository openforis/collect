/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.viewmodel.JobStatusPopUpVM.JobEndHandler;
import org.openforis.collect.io.metadata.codelist.CodeListImportJob;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Files;
import org.openforis.concurrency.Job;
import org.openforis.idm.metamodel.CodeList;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListImportVM extends BaseSurveyFileImportVM {

	@WireVariable
	private CodeListManager codeListManager;
	
	// input
	private int codeListId;

	// temp
	private Window jobStatusPopUp;

	@Init(superclass = false)
	public void init(@ExecutionArgParam("codeListId") int codeListId) {
		super.init();
		this.codeListId = codeListId;
	}

	@Command
	public void close() {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("undoChanges", confirmed);
				BindUtils.postGlobalCommand(null, null, CodeListsVM.CLOSE_CODE_LIST_IMPORT_POP_UP_COMMAND, args);
			}
		});
	}

	@Override
	protected void checkCanImportFile(Media media) {
		String fileName = media.getName();
		String extension = FilenameUtils.getExtension(fileName);
		if (!(Files.CSV_FILE_EXTENSION.equalsIgnoreCase(extension)
				|| Files.EXCEL_FILE_EXTENSION.equalsIgnoreCase(extension))) {
			throw new RuntimeException(
					String.format("Only CSV or Excel file upload is supported, found: %s", extension));
		}
	}

	@Command
	public void importCodeList() {
		CollectSurvey survey = getSurvey();
		CodeList codeList = survey.getCodeListById(codeListId);

		CodeListImportJob job = new CodeListImportJob();
		job.setJobManager(jobManager);
		job.setCodeListManager(codeListManager);
		job.setCodeList(codeList);
		job.setFile(uploadedFile);
		job.setOverwriteData(true);
		jobManager.start(job);
		
		jobStatusPopUp = JobStatusPopUpVM.openPopUp("survey.code_list.import_data.title", job, true, new JobEndHandler() {
			@Override
			public void onJobEnd(Job job) {
				closePopUp(jobStatusPopUp);
				switch(job.getStatus()) {
				case COMPLETED:
					MessageUtil.showInfo("survey.code_list.import_data.completed");
					break;
				case FAILED:
					
					break;
				default:
				}
				//SurveyEditVM.dispatchSurveySaveCommand();
			}
		});
	}
}
