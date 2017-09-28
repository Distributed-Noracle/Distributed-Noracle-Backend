package i5.las2peer.services.noracleService.api;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.services.noracleService.model.Vote;
import i5.las2peer.services.noracleService.model.VoteList;

public interface INoracleVoteService {

	public void setVote(String objectId, int vote) throws ServiceInvocationException;

	public Vote getMyVote(String objectId) throws ServiceInvocationException;

	public VoteList getAllVotes(String objectId) throws ServiceInvocationException;

}
