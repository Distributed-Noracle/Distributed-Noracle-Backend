package i5.las2peer.services.noracleService.model;

import java.io.Serial;
import java.io.Serializable;

public class Vote implements Serializable {

	@Serial
	private static final long serialVersionUID = 7487058393243794605L;

	private int value;
	private String voterAgentId;

	public Vote() { // used in tests
	}

	public Vote(int value, String voterAgentId) {
		this.value = value;
		this.voterAgentId = voterAgentId;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getVoterAgentId() {
		return voterAgentId;
	}

	public void setVoterAgentId(String voterAgentId) {
		this.voterAgentId = voterAgentId;
	}

}
