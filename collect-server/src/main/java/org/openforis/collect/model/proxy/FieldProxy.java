/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.idm.model.Field;

/**
 * @author S. Ricci
 *
 */
public class FieldProxy implements Proxy {

	private Object value;
	private FieldSymbol symbol;
	private String remarks;

	public FieldProxy(Field<?> field) {
		super();
		value = field.getValue();
		if(field.getSymbol() != null) {
			symbol = FieldSymbol.valueOf(field.getSymbol());
		}
		remarks = field.getRemarks();
	}


	public static List<FieldProxy> fromList(List<Field<?>> list) {
		List<FieldProxy> proxies = new ArrayList<FieldProxy>();
		if (list != null) {
			for (Field<?> item : list) {
				FieldProxy proxy = new FieldProxy(item);
				proxies.add(proxy);
			}
		}
		return proxies;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}

	public String getRemarks() {
		return remarks;
	}

	public FieldSymbol getSymbol() {
		return symbol;
	}
	
	public void setSymbol(FieldSymbol symbol) {
		this.symbol = symbol;
	}
	
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
}
