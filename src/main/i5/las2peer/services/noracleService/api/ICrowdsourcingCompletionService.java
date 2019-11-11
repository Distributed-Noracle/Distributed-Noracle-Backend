package i5.las2peer.services.noracleService.api;

import javax.ws.rs.core.Response;

public interface ICrowdsourcingCompletionService {

	public Response getNumberOfQuestionsByMainAgentInSpace(String spaceId, final String email) throws Exception;
}
