package i5.las2peer.services.noracleService.api;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.services.noracleService.model.Vote;
import i5.las2peer.services.noracleService.model.VoteList;

public interface INoracleVoteService {

	Vote setVote(String agentId, String objectId, int value) throws ServiceInvocationException;

	Vote getAgentVote(String objectId, String agentId) throws ServiceInvocationException;

	VoteList getAllVotes(String objectId) throws ServiceInvocationException;

}
