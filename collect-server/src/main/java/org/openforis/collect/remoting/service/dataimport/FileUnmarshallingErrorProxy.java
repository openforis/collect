package org.openforis.collect.remoting.service.dataimport;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.io.data.DataImportSummary.FileErrorItem;

/**
 * 
 * @author S. Ricci
 *
 */
public class FileUnmarshallingErrorProxy implements Proxy {

	private transient FileErrorItem fileErrorItem;

	public FileUnmarshallingErrorProxy(FileErrorItem fileErrorItem) {
		super();
		this.fileErrorItem = fileErrorItem;
	}

	public static List<FileUnmarshallingErrorProxy> fromList(List<FileErrorItem> list) {
		List<FileUnmarshallingErrorProxy> result = new ArrayList<FileUnmarshallingErrorProxy>();
		if (list != null) {
			for (FileErrorItem item : list) {
				FileUnmarshallingErrorProxy proxy = new FileUnmarshallingErrorProxy(item);
				result.add(proxy);
			}
		}
		return result;
	}

	public String getFileName() {
		return fileErrorItem.getFileName();
	}

	public List<NodeUnmarshallingErrorProxy> getErrors() {
		List<NodeUnmarshallingErrorProxy> list = NodeUnmarshallingErrorProxy.fromList(fileErrorItem.getErrors());
		return list;
	}

}
