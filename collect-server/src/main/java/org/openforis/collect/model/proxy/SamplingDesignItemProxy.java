package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.SamplingDesignItem;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignItemProxy implements Proxy {

	private transient SamplingDesignItem item;
	private String srsId;
	private double x;
	private double y;

	public SamplingDesignItemProxy(SamplingDesignItem item) {
		super();
		this.item = item;
		srsId = item.getSrsId();
		x = item.getX();
		y = item.getY();
	}

	public static List<SamplingDesignItemProxy> fromList(List<SamplingDesignItem> list) {
		List<SamplingDesignItemProxy> result = new ArrayList<SamplingDesignItemProxy>();
		if ( list != null ) {
			for (SamplingDesignItem item : list) {
				SamplingDesignItemProxy proxy = new SamplingDesignItemProxy(item);
				result.add(proxy);
			}
		}
		return result;
	}
	
	@ExternalizedProperty
	public Long getId() {
		return item.getId();
	}

	@ExternalizedProperty
	public int getLevel() {
		return item.getLevel();
	}
	
	@ExternalizedProperty
	public List<String> getLevelCodes() {
		return item.getLevelCodes();
	}
	
	@ExternalizedProperty
	public List<String> getInfos() {
		return item.getInfoAttributes();
	}

	public String getSrsId() {
		return srsId;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
}
