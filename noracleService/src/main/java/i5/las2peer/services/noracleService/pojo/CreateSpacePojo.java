package i5.las2peer.services.noracleService.pojo;

public class CreateSpacePojo {

	private String name;
	private boolean isPrivate;

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
