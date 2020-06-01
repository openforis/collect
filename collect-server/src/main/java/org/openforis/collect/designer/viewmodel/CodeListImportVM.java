/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.viewmodel.JobStatusPopUpVM.JobEndHandler;
import org.openforis.collect.designer.viewmodel.referencedata.ReferenceDataImportErrorsPopUpVM;
import org.openforis.collect.io.metadata.codelist.CodeListImportJob;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Files;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.idm.metamodel.CodeList;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
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

	public CodeListImportVM() {
		super(new String[] {Files.CSV_FILE_EXTENSION, Files.EXCEL_FILE_EXTENSION});
	}
	
	@Init(superclass = false)
	public void init(@ExecutionArgParam("codeListId") int codeListId) {
		super.init();
		this.codeListId = codeListId;
	}

	@Command
	public void close(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		event.stopPropagation();
		BindUtils.postGlobalCommand(null, null, CodeListsVM.CLOSE_CODE_LIST_IMPORT_POP_UP_COMMAND, null);
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

		jobStatusPopUp = JobStatusPopUpVM.openPopUp("survey.code_list.import_data.title", job, true,
				new JobEndHandler<CodeListImportJob>() {
					@Override
					public void onJobEnd(CodeListImportJob job) {
						closePopUp(jobStatusPopUp);
						switch (job.getStatus()) {
						case COMPLETED:
							MessageUtil.showInfo("survey.code_list.import_data.completed");
							// Survey has been updated (last id changed for new code list items): save it!
							SurveyEditVM.dispatchSurveySaveCommand();
							break;
						case FAILED:
							String title = Labels.getLabel("survey.code_list.import_data.error_popup.title",
									new String[] { getUploadedFileName() });
							ReferenceDataImportErrorsPopUpVM.showPopUp(job.getErrors(), title);
							break;
						default:
						}
					}
				});
	}

	@Command
	public void downloadExample() {
		ServletContext context = getSession().getWebApp().getServletContext();
		String fileName = "code-list-import-example.xlsx";
		InputStream is = context.getResourceAsStream("/WEB-INF/resources/io/" + fileName);
		Filedownload.save(is, MediaTypes.XLSX_CONTENT_TYPE, fileName);
	}
}
