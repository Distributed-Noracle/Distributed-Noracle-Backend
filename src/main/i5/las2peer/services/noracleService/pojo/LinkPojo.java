package i5.las2peer.services.noracleService.pojo;

public class LinkPojo {

	private String rel;
	private String href;

	public LinkPojo() { // used in tests
	}

	public LinkPojo(String rel, String href) {
		this.rel = rel;
		this.href = href;
	}

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

}
