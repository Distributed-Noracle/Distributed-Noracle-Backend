package i5.las2peer.services.noracleService.model;

import java.io.Serial;
import java.io.Serializable;

public class Space implements Serializable {

	@Serial
	private static final long serialVersionUID = 8800170853451989942L;

	private String spaceId;
	private String spaceSecret;
	private String name;
	private String spaceOwnerId;
	private String spaceReaderGroupId;
	private boolean isPrivate;

	public Space() { // used in tests
	}

	public Space(String spaceId, String spaceSecret, String name, String spaceOwnerId, String spaceReaderGroupId, boolean isPrivate) {
		this.setSpaceId(spaceId);
		this.setSpaceSecret(spaceSecret);
		this.setName(name);
		this.setSpaceOwnerId(spaceOwnerId);
		this.setSpaceReaderGroupId(spaceReaderGroupId);
		this.setPrivate(isPrivate);
	}

	public String getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(String spaceId) {
		this.spaceId = spaceId;
	}

	public String getSpaceSecret() {
		return spaceSecret;
	}

	public void setSpaceSecret(String spaceSecret) {
		this.spaceSecret = spaceSecret;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSpaceOwnerId() {
		return spaceOwnerId;
	}

	public void setSpaceOwnerId(String spaceOwnerId) {
		this.spaceOwnerId = spaceOwnerId;
	}

	public String getSpaceReaderGroupId() {
		return spaceReaderGroupId;
	}

	public void setSpaceReaderGroupId(String spaceReaderGroupId) {
		this.spaceReaderGroupId = spaceReaderGroupId;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean aPrivate) {
		isPrivate = aPrivate;
	}
}
