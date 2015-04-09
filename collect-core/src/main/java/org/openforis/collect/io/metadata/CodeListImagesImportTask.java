package org.openforis.collect.io.metadata;

import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
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
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CodeListImagesImportTask extends Task {
	
	private CodeListManager codeListManager;

	//input
	private ZipFile zipFile;
	private CollectSurvey survey;

	@Override
	protected void execute() throws Throwable {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			String entryName = entry.getName();
			if (isCodeListImageEntry(entryName)) {
				int codeListId = extractCodeListId(entryName);
				CodeList codeList = survey.getCodeListById(codeListId);
				if (codeList != null) {
					int itemId = extractCodeListItemId(entryName);
					String imageFileName = extractImageFileName(entryName);
					CodeListItem item = codeListManager.loadItem(codeList, itemId);
					if (item != null && item instanceof PersistedCodeListItem) {
						byte[] content = IOUtils.toByteArray(zipFile.getInputStream(entry));
						codeListManager.saveImageContent((PersistedCodeListItem) item, new FileWrapper(content, imageFileName));
					} else {
						log().warn("Error restoring code list image file : " + entry.getName());
					}
				}
			}
		}
	}

	private boolean isCodeListImageEntry(String entryName) {
		return entryName.startsWith(SurveyBackupJob.CODE_LIST_IMAGES_FOLDER);
	}
	
	private int extractCodeListId(String entryName) {
		String[] parts = entryName.split(SurveyBackupJob.ZIP_FOLDER_SEPARATOR);
		return Integer.parseInt(parts[1]);
	}

	private int extractCodeListItemId(String entryName) {
		String[] parts = entryName.split(SurveyBackupJob.ZIP_FOLDER_SEPARATOR);
		return Integer.parseInt(parts[2]);
	}
	
	private String extractImageFileName(String entryName) {
		String[] parts = entryName.split(SurveyBackupJob.ZIP_FOLDER_SEPARATOR);
		return parts[3];
	}
	
	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	public void setZipFile(ZipFile zipFile) {
		this.zipFile = zipFile;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
}
