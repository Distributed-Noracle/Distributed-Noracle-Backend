package i5.las2peer.services.noracleService.model;

import java.io.Serializable;

public class Vote implements Serializable {

	private static final long serialVersionUID = 1L;

	private int vote;

	public Vote(int vote) {
		this.vote = vote;
	}

	public int getVote() {
		return vote;
	}

	public void setVote(int vote) {
		this.vote = vote;
	}

}
