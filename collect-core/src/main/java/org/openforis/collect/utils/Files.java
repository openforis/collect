package org.openforis.collect.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class Files {
	
	public static final String CSV_FILE_EXTENSION = "csv";
	public static final String EXCEL_FILE_EXTENSION = "xlsx";
	public static final String ZIP_FILE_EXTENSION = "zip";
	public static final String XML_FILE_EXTENSION = "xml";
	
	private static final String PATH_SEPARATOR_PATTERN = "[\\\\|/]";
	public static final String JAVA_IO_TMPDIR_SYS_PROP = "java.io.tmpdir";
	public static final File TEMP_FOLDER = getReadableSysPropLocation(JAVA_IO_TMPDIR_SYS_PROP, null);

	public static File writeToTempFile(InputStream is, String originalFileName, String tempFilePrefix) throws IOException {
		String orginalExtension = FilenameUtils.getExtension(originalFileName);
		File file = File.createTempFile(tempFilePrefix, "." + orginalExtension);
		FileUtils.copyInputStreamToFile(is, file);
		return file;
	}
	
	public static File writeToTempFile(String text, String tempFilePrefix, String tempFileSuffix) throws IOException {
		File file = File.createTempFile(tempFilePrefix, tempFileSuffix);
		Writer writer = null;
		try {
			writer = new FileWriter(file);
			IOUtils.write(text.getBytes(), writer, "UTF-8");			
		} finally {
			IOUtils.closeQuietly(writer);
		}
		return file;
	}
	
	public static File createTempDirectory() throws IOException {
		File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
		if (! temp.delete()) {
			throw new IOException("Could not delete temp file: "
					+ temp.getAbsolutePath());
		}
		if (! temp.mkdir()) {
			throw new IOException("Could not create temp directory: "
					+ temp.getAbsolutePath());
		}

		return (temp);
	}

	public static File getOrCreateFolder(File parentDestinationFolder, String path) {
		String[] entryParts = path.split(PATH_SEPARATOR_PATTERN);
		File folder = parentDestinationFolder;
		for (int i = 0; i < entryParts.length; i++) {
			String part = entryParts[i];
			folder = new File(folder, part);
		}
		if (! folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}

	public static String extractFileName(String entryName) {
		String name = FilenameUtils.getName(entryName);
		return name;
	}

	public static List<String> listFileNamesInFolder(File parentFolder) {
		return listFileNamesInFolder(parentFolder, null);
	}
	
	public static List<String> listFileNamesInFolder(File parentFolder, String folderPath) {
		List<File> files = listFilesInFolder(parentFolder, folderPath);
		List<String> result = extractNames(files);
		return result;
	}
	
	private static List<String> extractNames(List<File> files) {
		List<String> result = new ArrayList<String>(files.size());
		for (File file : files) {
			result.add(file.getName());
		}
		Collections.sort(result);
		return result;
	}

	public static List<File> listFilesInFolder(File parentFolder) {
		return listFilesInFolder(parentFolder, null);
	}
	
	public static List<File> listFilesInFolder(File parentFolder, String folderPath) {
		List<File> result = new ArrayList<File>();
		File folder = folderPath == null ? parentFolder : 
			Files.getOrCreateFolder(parentFolder, folderPath);
		File[] files = folder.listFiles();
		for (File file : files) {
			result.add(file);
		}
		return result;
	}
	
	public static void eraseFileContent(File file) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write("");
			writer.flush();
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}
	
	public static File getReadableSysPropLocation(String sysProp, String subDir) {
		String path = getSysPropPath(sysProp, subDir);
		if (path == null) {
			return null;
		} else {
			return getLocationIfAccessible(path);
		}
	}

	public static String getSysPropPath(String sysProp, String subdirectories) {
		String base = System.getProperty(sysProp);
		if ( base == null ) {
			return null;
		}
		String path = base;
		if ( subdirectories != null ) {
			String[] pathParts = subdirectories.split("[\\|/]");
			path += File.separator + StringUtils.join(pathParts, File.separator);
		}
		return path;
	}
	
	public static File getLocationIfAccessible(String path) {
		File result = new File(path);
		if ( result.exists() && result.canWrite() ) {
			return result;
		} else {
			return null;
		}
	}

	public static void deleteFolder(File folder) {
		//for non empty big folders it's better to use command line...
		if (SystemUtils.isWindows()) {
			SystemUtils.runCommandQuietly("cmd /c rmdir /S/Q \"" + folder.getAbsolutePath() + "\"");
		} else if (SystemUtils.isLinux()) {
			SystemUtils.runCommandQuietly("rm -r " + folder.getAbsolutePath());
		}
		//when error occurs, try to delete folder with Apache Commons FileUtils (it deletes it's content first)
		if (folder.exists()) {
			FileUtils.deleteQuietly(folder);
		}
	}
	
	public static String getContentType(String fileName) {
		return URLConnection.guessContentTypeFromName(fileName);
	}
}
