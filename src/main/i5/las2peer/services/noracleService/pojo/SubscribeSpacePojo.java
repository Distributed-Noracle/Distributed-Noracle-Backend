package i5.las2peer.services.noracleService.pojo;

import java.io.Serializable;

public class SubscribeSpacePojo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String spaceId;
	private String name;

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
