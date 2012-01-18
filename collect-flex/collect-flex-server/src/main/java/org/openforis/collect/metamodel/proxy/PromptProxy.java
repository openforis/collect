/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.Prompt;

/**
 * @author M. Togna
 * 
 */
public class PromptProxy implements Proxy {

	private transient Prompt prompt;

	public enum Type {
		INTERVIEW, PAPER, HANDHELD, PC;
	}

	static List<PromptProxy> fromList(List<Prompt> list) {
		List<PromptProxy> proxies = new ArrayList<PromptProxy>();
		if (list != null) {
			for (Prompt p : list) {
				proxies.add(new PromptProxy(p));
			}
		}
		return proxies;
	}

	public PromptProxy(Prompt prompt) {
		super();
		this.prompt = prompt;
	}

	@ExternalizedProperty
	public Type getType() {
		return prompt.getType() != null ? Type.valueOf(prompt.getType().toString()) : null;
	}

	@ExternalizedProperty
	public String getLanguage() {
		return prompt.getLanguage();
	}

	@ExternalizedProperty
	public String getText() {
		return prompt.getText();
	}

}
