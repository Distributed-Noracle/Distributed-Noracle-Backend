package i5.las2peer.services.noracleService.model;

import java.io.Serial;
import java.io.Serializable;

public class NoracleAgentProfile implements Serializable {

	@Serial
	private static final long serialVersionUID = -843933540208902830L;

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
