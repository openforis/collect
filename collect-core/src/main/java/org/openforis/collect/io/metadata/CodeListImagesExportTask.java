package org.openforis.collect.io.metadata;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FileWrapper;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.PersistedCodeListItem;
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
public class CodeListImagesExportTask extends Task {
	
	private CodeListManager codeListManager;

	//parameters
	private CollectSurvey survey;
	private ZipOutputStream zipOutputStream;

	@Override
	protected void execute() throws Throwable {
		List<CodeList> codeLists = survey.getCodeLists();
		for (CodeList list : codeLists) {
			if (! list.isExternal()) {
				Deque<CodeListItem> stack = new LinkedList<CodeListItem>();
				List<CodeListItem> rootItems = codeListManager.loadRootItems(list);
				stack.addAll(rootItems);
				while (! stack.isEmpty()) {
					if ( ! isRunning() ) {
						break;
					}
					CodeListItem item = stack.pop();
					if (item instanceof PersistedCodeListItem && item.hasUploadedImage()) {
						FileWrapper imageFileWrapper = codeListManager.loadImageContent((PersistedCodeListItem) item);
						ZipEntry entry = new ZipEntry(getEntryName(item));
						zipOutputStream.putNextEntry(entry);
						IOUtils.write(imageFileWrapper.getContent(), zipOutputStream);
						zipOutputStream.closeEntry();
					}
					List<CodeListItem> childItems = codeListManager.loadChildItems(item);
					for (CodeListItem childItem : childItems) {
						stack.push(childItem);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static String getEntryName(CodeListItem item) {
		CodeList list = item.getCodeList();
		return StringUtils.join(Arrays.asList(
				SurveyBackupJob.CODE_LIST_IMAGES_FOLDER, 
				list.getId(), 
				item.getId(), 
				item.getImageFileName()), 
				SurveyBackupJob.ZIP_FOLDER_SEPARATOR);
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public void setZipOutputStream(ZipOutputStream zipOutputStream) {
		this.zipOutputStream = zipOutputStream;
	}
	
	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
}
