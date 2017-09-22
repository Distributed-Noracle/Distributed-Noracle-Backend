package i5.las2peer.services.noracleService;

import java.io.Serializable;

public class Space implements Serializable {

	private static final long serialVersionUID = 1L;

	private String spaceId;
	private String name;

	public Space() { // used in tests
	}

	public Space(String spaceId, String name) {
		this.setSpaceId(spaceId);
		this.setName(name);
	}

	public String getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(String spaceId) {
		this.spaceId = spaceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
