package org.openforis.idm.metamodel;

import org.openforis.commons.lang.Identifiable;

/**
 * An object that will be persisted in a database, so it will be identifiable
 * using a unique integer identifier
 * 
 * @author A. Modragon
 *
 */
public class AbstractPersistedObject<I extends Number> implements Identifiable<I>, PersistedObject<I> {

	private I id;

	@Override
	public I getId() {
		return id;
	}
	
	@Override
	public void setId(I id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		AbstractPersistedObject other = (AbstractPersistedObject) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
