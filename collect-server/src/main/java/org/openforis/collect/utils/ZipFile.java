package org.openforis.collect.utils;

import java.io.File;
import java.io.InputStream;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

public class ZipFile {

	private File outputFile;
	private ZipParameters zipParameters;
	private net.lingala.zip4j.ZipFile delegate;

	public ZipFile(File file) throws ZipException {
		this.delegate = new net.lingala.zip4j.ZipFile(file);
		this.zipParameters = new ZipParameters();
		this.zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
		this.zipParameters.setCompressionLevel(CompressionLevel.ULTRA);
	}

	public ZipFile add(File file, String name) throws ZipException {
		try {
			ZipParameters zipParams = createParams(name);
			delegate.addFile(file, zipParams);
		} catch (net.lingala.zip4j.exception.ZipException e) {
			throw new ZipException(e);
		}
		return this;
	}

	public ZipFile add(InputStream inputStream, String name) throws ZipException {
		try {
			ZipParameters zipParams = createParams(name);
			delegate.addStream(inputStream, zipParams);
		} catch (net.lingala.zip4j.exception.ZipException e) {
			throw new ZipException(e);
		}
		return this;
	}
	
	public void extractAll(String destinationPath) throws ZipException {
		try {
			this.delegate.extractAll(destinationPath);
		} catch (net.lingala.zip4j.exception.ZipException e) {
			throw new ZipException(e);
		}
	}

	public File getOutputFile() {
		return outputFile;
	}

	private ZipParameters createParams(String name) {
		ZipParameters zipParams = new ZipParameters(zipParameters);
		zipParams.setFileNameInZip(name);
//		zipParams.setSourceExternalStream(true);
		return zipParams;
	}

	public static class ZipException extends Exception {

		private static final long serialVersionUID = 1L;

		public ZipException(Exception e) {
			super("Error generating zip file", e);
		}
	}

}
