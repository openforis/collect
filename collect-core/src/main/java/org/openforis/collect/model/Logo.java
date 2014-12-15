package org.openforis.collect.model;

/**
 * @author S. Ricci
 */
public class Logo {

	public static String TOP_RIGHT_POSITION = "top_right";
	
	private Integer id;
	private LogoPosition position;
	private byte[] image;
	private String contentType;
	
	public Logo() {
	}

	public Logo(LogoPosition position, byte[] image, String contentType) {
		this(null, position, image, contentType);
	}
	
	public Logo(Integer id, LogoPosition position, byte[] image, String contentType) {
		this.id = id;
		this.position = position;
		this.image = image;
		this.contentType = contentType;
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public LogoPosition getPosition() {
		return position;
	}

	public void setPosition(LogoPosition position) {
		this.position = position;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
}
