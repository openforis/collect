package org.openforis.idm.metamodel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class Languages {

	private static final String LANG_CODES_ISO_639_1_TXT_FILE = "lang_codes_iso_639_1.txt";
	private static final String LANG_CODES_ISO_639_3_TXT_FILE = "lang_codes_iso_639_3.txt";
	
	private static Map<Standard, List<String>> languageCodes;

	public enum Standard {
		ISO_639_1, ISO_639_3
	}
	
	static {
		init();
	}
	
	public static List<String> getCodes(Standard standard) {
		return languageCodes.get(standard);
	}
	
	private static void init() {
		languageCodes = new HashMap<Standard, List<String>>();
		List<String> temp = extractLanguageCodes(LANG_CODES_ISO_639_1_TXT_FILE);
		languageCodes.put(Standard.ISO_639_1, Collections.unmodifiableList(temp));
		temp = extractLanguageCodes(LANG_CODES_ISO_639_3_TXT_FILE);
		languageCodes.put(Standard.ISO_639_3, Collections.unmodifiableList(temp));
	}

	protected static List<String> extractLanguageCodes(String languagesFileName) {
		List<String> temp = new ArrayList<String>();
		InputStream is = null;
		BufferedReader br = null;
		try {
			String fileName = languagesFileName;
			is = Languages.class.getResourceAsStream(fileName);
			br = new BufferedReader(new InputStreamReader(is));
			String langCode;
			while ((langCode = br.readLine()) != null) {
				temp.add(langCode);
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(br);
		}
		return temp;
	}

	public static boolean exists(Standard standard, String code) {
		List<String> list = languageCodes.get(standard);
		return list.contains(code);
	}


}
