package i5.las2peer.services.noracleService.model;

import java.io.Serializable;

public class Vote implements Serializable {

	private static final long serialVersionUID = 1L;

	private int value;
	private String voterAgentId;

	public Vote() { // used in tests
	}

	public Vote(final int value, final String voterAgentId) {
		this.value = value;
		this.voterAgentId = voterAgentId;
	}

	public int getValue() {
		return value;
	}

	public void setValue(final int value) {
		this.value = value;
	}

	public String getVoterAgentId() {
		return voterAgentId;
	}

	public void setVoterAgentId(final String voterAgentId) {
		this.voterAgentId = voterAgentId;
	}

}
