package org.openforis.collect.designer.util;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.openforis.commons.io.OpenForisIOUtils;
import org.zkoss.util.media.Media;

public abstract class MediaUtil {

	public static File copyToTempFile(Media media) {
		String extension = FilenameUtils.getExtension(media.getName());
		return media.isBinary() 
				? OpenForisIOUtils.copyToTempFile(media.getStreamData(), extension)
				: OpenForisIOUtils.copyToTempFile(media.getReaderData(), extension);
	}
}
