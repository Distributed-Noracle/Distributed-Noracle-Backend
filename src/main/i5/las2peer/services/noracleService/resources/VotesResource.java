package i5.las2peer.services.noracleService.resources;

import java.io.Serializable;
import java.net.HttpURLConnection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import i5.las2peer.api.Context;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.ResourceNotFoundException;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.restMapper.ExceptionEntity;
import i5.las2peer.services.noracleService.NoracleService;
import i5.las2peer.services.noracleService.NoracleVoteService;
import i5.las2peer.services.noracleService.api.INoracleVoteService;
import i5.las2peer.services.noracleService.model.Vote;
import i5.las2peer.services.noracleService.model.VoteList;
import i5.las2peer.services.noracleService.pojo.SetVotePojo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(
		tags = { VotesResource.RESOURCE_NAME })
public class VotesResource implements INoracleVoteService {

	public static final String RESOURCE_NAME = "votes";

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Vote successfully set"),
			@ApiResponse(
					code = HttpURLConnection.HTTP_UNAUTHORIZED,
					message = "You have to be logged in to vote",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public Response postSetVote(SetVotePojo setVotePojo) throws ServiceInvocationException {
		setVote(setVotePojo.getObjectId(), setVotePojo.getVote());
		return Response.ok().build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Vote successfully set"),
			@ApiResponse(
					code = HttpURLConnection.HTTP_UNAUTHORIZED,
					message = "You have to be logged in to vote",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public Response putSetVote(SetVotePojo setVotePojo) throws ServiceInvocationException {
		setVote(setVotePojo.getObjectId(), setVotePojo.getVote());
		return Response.ok().build();
	}

	@Override
	public void setVote(String objectId, int vote) throws ServiceInvocationException {
		Context.get().invoke(
				new ServiceNameVersion(NoracleVoteService.class.getCanonicalName(), NoracleService.API_VERSION),
				"setVote", objectId, vote);
	}

	@Override
	public Vote getMyVote(String objectId) throws ServiceInvocationException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleVoteService.class.getCanonicalName(), NoracleService.API_VERSION),
				"getMyVote", objectId);
		Vote vote;
		if (rmiResult instanceof Vote) {
			vote = (Vote) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
		return vote;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public VoteList getAllVotes(@PathParam("spaceId") String spaceId, @PathParam("questionId") String questionId,
			@PathParam("relationId") String relationId) throws ServiceInvocationException {
		if (questionId != null && !questionId.isEmpty()) {
			return getAllVotes("question-" + questionId);
		} else if (relationId != null && !relationId.isEmpty()) {
			return getAllVotes("relation-" + relationId);
		} else if (spaceId != null && !spaceId.isEmpty()) {
			return getAllVotes("space-" + spaceId);
		} else {
			throw new ResourceNotFoundException("No vote target identifier given");
		}
	}

	@Override
	public VoteList getAllVotes(String objectId) throws ServiceInvocationException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleVoteService.class.getCanonicalName(), NoracleService.API_VERSION),
				"getAllVotes", objectId);
		VoteList vote;
		if (rmiResult instanceof VoteList) {
			vote = (VoteList) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
		return vote;
	}

}
