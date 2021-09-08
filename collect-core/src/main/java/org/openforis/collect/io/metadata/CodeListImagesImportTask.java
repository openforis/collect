package org.openforis.collect.io.metadata;

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
			if (CodeListImageEntry.isValidEntry(entryName)) {
				CodeListImageEntry codeListImageEntry = CodeListImageEntry.parseEntryName(entryName);
				CodeList codeList = survey.getCodeListById(codeListImageEntry.getListId());
				if (codeList != null) {
					CodeListItem item = codeListManager.loadItem(codeList, codeListImageEntry.getItemId());
					if (item != null && item instanceof PersistedCodeListItem) {
						byte[] content = IOUtils.toByteArray(zipFile.getInputStream(entry));
						codeListManager.saveImageContent((PersistedCodeListItem) item, new FileWrapper(content, codeListImageEntry.getFileName()));
					} else {
						logWarning("Error restoring code list image file : " + entry.getName());
					}
				}
			}
		}
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
	
	private static class CodeListImageEntry {
		private static final Pattern PATTERN = Pattern.compile("^" + Pattern.quote(SurveyBackupJob.CODE_LIST_IMAGES_FOLDER) + "[/|\\\\](\\d+)[/|\\\\](\\d+)[/|\\\\](.+)$");
		
		private int listId;
		private int itemId;
		private String fileName;
		
		public CodeListImageEntry(int listId, int itemId, String fileName) {
			super();
			this.listId = listId;
			this.itemId = itemId;
			this.fileName = fileName;
		}
		
		public static boolean isValidEntry(String entryName) {
			return PATTERN.matcher(entryName).matches();
		}
		
		public static CodeListImageEntry parseEntryName(String entryName) {
			Matcher matcher = PATTERN.matcher(entryName);
			if (matcher.find()) {
				String listId = matcher.group(1);
				String itemId = matcher.group(2);
				String fileName = matcher.group(3);
				return new CodeListImageEntry(Integer.parseInt(listId), Integer.parseInt(itemId), fileName);
			} else {
				return null;
			}
		}
		
		public int getListId() {
			return listId;
		}
		
		public int getItemId() {
			return itemId;
		}
		
		public String getFileName() {
			return fileName;
		}
		
	}
}
