package org.openforis.collect.model;

/**
 * @author S. Ricci
 */
public class Logo {

	private int position;
	private byte[] image;
	
	
	public Logo() {
		super();
	}

	public Logo(int position, byte[] image) {
		super();
		this.position = position;
		this.image = image;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}
	
	
	
}
