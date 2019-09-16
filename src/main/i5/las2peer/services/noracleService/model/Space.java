package i5.las2peer.services.noracleService.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class Space implements Serializable {

	private static final long serialVersionUID = 1L;

	private String spaceId;
	private String spaceSecret;
	private String name;
	private String spaceOwnerId;
	private String spaceReaderGroupId;

	public Space() { // used in tests
	}

	public Space(final String spaceId, final String spaceSecret, final String name, final String spaceOwnerId,
			final String spaceReaderGroupId) {
		setSpaceId(spaceId);
		setSpaceSecret(spaceSecret);
		setName(name);
		setSpaceOwnerId(spaceOwnerId);
		setSpaceReaderGroupId(spaceReaderGroupId);
	}

	public String getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(final String spaceId) {
		this.spaceId = spaceId;
	}

	public String getSpaceSecret() {
		return spaceSecret;
	}

	public void setSpaceSecret(final String spaceSecret) {
		this.spaceSecret = spaceSecret;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getSpaceOwnerId() {
		return spaceOwnerId;
	}

	public void setSpaceOwnerId(final String spaceOwnerId) {
		this.spaceOwnerId = spaceOwnerId;
	}

	public String getSpaceReaderGroupId() {
		return spaceReaderGroupId;
	}

	public void setSpaceReaderGroupId(final String spaceReaderGroupId) {
		this.spaceReaderGroupId = spaceReaderGroupId;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Space))
			return false;

		final Space other = (Space) obj;
		boolean bool = true;

		bool = bool && StringUtils.equals(spaceId, other.spaceId);
		bool = bool && StringUtils.equals(spaceSecret, other.spaceSecret);
		bool = bool && StringUtils.equals(name, other.name);
		bool = bool && StringUtils.equals(spaceOwnerId, other.spaceOwnerId);
		bool = bool && StringUtils.equals(spaceReaderGroupId, other.spaceReaderGroupId);

		return bool;
	}

}
