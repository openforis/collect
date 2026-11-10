package org.openforis.collect.web.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.io.data.backup.BackupStorageManager;
import org.openforis.collect.utils.Controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
public class DataBackupController extends BasicController {

	@Autowired
	private BackupStorageManager backupStorageManager;
	private final AccessControl accessControl;
	
	public DataBackupController(AccessControl accessControl) {
		this.accessControl = accessControl;
	}

	@RequestMapping(value = "/surveys/{surveyName}/data/backup/last", method = RequestMethod.GET)
	public void downloadLastBackup(HttpServletResponse response, @PathVariable String surveyName) throws IOException {
		accessControl.checkAdmin();
		File file = backupStorageManager.getLastBackupFile(surveyName);
		Controllers.writeFileToResponse(response, file);
	}
	
}
