package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class ReferenceDataSchema {

	private SamplingPointDefinition samplingPointDefinition = new SamplingPointDefinition();
	private Map<String, TaxonomyDefinition> taxonomyDefinitionByName = new HashMap<String, TaxonomyDefinition>();

	public SamplingPointDefinition getSamplingPointDefinition() {
		return samplingPointDefinition;
	}

	public void setSamplingPointDefinition(SamplingPointDefinition samplingPoint) {
		this.samplingPointDefinition = samplingPoint;
	}

	public List<TaxonomyDefinition> getTaxonomyDefinitions() {
		return Collections.unmodifiableList(new ArrayList<TaxonomyDefinition>(taxonomyDefinitionByName.values()));
	}

	public TaxonomyDefinition getTaxonomyDefinition(String name) {
		TaxonomyDefinition taxonomyDefinition = taxonomyDefinitionByName.get(name);
		if (taxonomyDefinition == null) {
			// for backwards compatibility
			taxonomyDefinition = new TaxonomyDefinition(name);
			taxonomyDefinitionByName.put(name, taxonomyDefinition);
		}
		return taxonomyDefinition;
	}

	public void addTaxonomyDefinition(TaxonomyDefinition taxonomyDefinition) {
		this.taxonomyDefinitionByName.put(taxonomyDefinition.getTaxonomyName(), taxonomyDefinition);
	}
	
	public void updateTaxonomyDefinitionName(String oldName, String newName) {
		TaxonomyDefinition taxonomyDefinition = taxonomyDefinitionByName.get(oldName);
		taxonomyDefinitionByName.remove(oldName);
		taxonomyDefinition.setTaxonomyName(newName);
		taxonomyDefinitionByName.put(newName, taxonomyDefinition);
	}
	
	public void removeTaxonomyDefinition(String taxonomyName) {
		this.taxonomyDefinitionByName.remove(taxonomyName);
	}

	public static abstract class ReferenceDataDefinition {

		private List<ReferenceDataDefinition.Attribute> attributes;

		public ReferenceDataDefinition() {
			this.attributes = new ArrayList<ReferenceDataDefinition.Attribute>();
		}

		public List<ReferenceDataDefinition.Attribute> getAttributes() {
			return CollectionUtils.unmodifiableList(attributes);
		}

		public List<String> getAttributeNames() {
			List<String> result = new ArrayList<String>();
			for (Attribute attribute : attributes) {
				result.add(attribute.name);
			}
			return result;
		}

		public List<ReferenceDataDefinition.Attribute> getAttributes(boolean key) {
			List<ReferenceDataDefinition.Attribute> result = new ArrayList<ReferenceDataDefinition.Attribute>();
			for (ReferenceDataDefinition.Attribute attribute : attributes) {
				if (key == attribute.isKey()) {
					result.add(attribute);
				}
			}
			return CollectionUtils.unmodifiableList(result);
		}

		public ReferenceDataDefinition.Attribute getAttribute(String name) {
			for (ReferenceDataDefinition.Attribute attribute : attributes) {
				if (attribute.getName().equals(name)) {
					return attribute;
				}
			}
			return null;
		}

		public void setAttributes(List<ReferenceDataDefinition.Attribute> attributes) {
			if (attributes == null) {
				this.attributes.clear();
			} else {
				this.attributes = new ArrayList<ReferenceDataDefinition.Attribute>(attributes);
			}
		}

		public void addAttribute(String name) {
			addAttribute(name, false);
		}

		public void addAttribute(String name, boolean key) {
			if (this.attributes == null) {
				this.attributes = new ArrayList<ReferenceDataDefinition.Attribute>();
			}
			ReferenceDataDefinition.Attribute attribute = new ReferenceDataDefinition.Attribute(name);
			attribute.setKey(key);
			this.attributes.add(attribute);
		}

		public void setAttribute(int index, Attribute attribute) {
			this.attributes.set(index, attribute);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ReferenceDataDefinition other = (ReferenceDataDefinition) obj;
			if (attributes == null) {
				if (other.attributes != null)
					return false;
			} else if (!attributes.equals(other.attributes))
				return false;
			return true;
		}

		public static class Attribute implements Cloneable {

			private String name;
			private boolean key;

			public Attribute() {
			}

			public Attribute(String name) {
				this.name = name;
			}

			public static List<Attribute> fromNames(List<String> names) {
				List<ReferenceDataDefinition.Attribute> attributes = new ArrayList<ReferenceDataDefinition.Attribute>();
				for (String name : names) {
					ReferenceDataDefinition.Attribute attribute = new ReferenceDataDefinition.Attribute(name);
					attributes.add(attribute);
				}
				return attributes;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public boolean isKey() {
				return key;
			}

			public void setKey(boolean key) {
				this.key = key;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + (key ? 1231 : 1237);
				result = prime * result + ((name == null) ? 0 : name.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Attribute other = (Attribute) obj;
				if (key != other.key)
					return false;
				if (name == null) {
					if (other.name != null)
						return false;
				} else if (!name.equals(other.name))
					return false;
				return true;
			}

			@Override
			public Object clone() throws CloneNotSupportedException {
				return (Attribute) super.clone();
			}
		}
	}

	public static class SamplingPointDefinition extends ReferenceDataDefinition {

	}

	public static class TaxonomyDefinition extends ReferenceDataDefinition {

		private String taxonomyName;

		public TaxonomyDefinition(String taxonomyName) {
			super();
			this.taxonomyName = taxonomyName;
		}

		public String getTaxonomyName() {
			return taxonomyName;
		}

		public void setTaxonomyName(String taxonomyName) {
			this.taxonomyName = taxonomyName;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((samplingPointDefinition == null) ? 0 : samplingPointDefinition.hashCode());
		result = prime * result + ((taxonomyDefinitionByName == null) ? 0 : taxonomyDefinitionByName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReferenceDataSchema other = (ReferenceDataSchema) obj;
		if (samplingPointDefinition == null) {
			if (other.samplingPointDefinition != null)
				return false;
		} else if (!samplingPointDefinition.equals(other.samplingPointDefinition))
			return false;
		if (taxonomyDefinitionByName == null) {
			if (other.taxonomyDefinitionByName != null)
				return false;
		} else if (!taxonomyDefinitionByName.equals(other.taxonomyDefinitionByName))
			return false;
		return true;
	}

}
