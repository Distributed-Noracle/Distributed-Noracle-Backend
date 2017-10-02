package i5.las2peer.services.noracleService.resources;

import java.io.Serializable;
import java.net.HttpURLConnection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
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

@Api
public class RelationVotesResource implements INoracleVoteService {

	public static final String RESOURCE_NAME = "votes";

	@PUT
	@Path("/{agentId}")
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
	public Response putSetVote(@PathParam("spaceId") String spaceId, @PathParam("relationId") String relationId,
			@PathParam("agentId") String agentId, SetVotePojo setVotePojo) throws ServiceInvocationException {
		String objectId = buildObjectId(spaceId, relationId);
		setVote(agentId, objectId, setVotePojo.getValue());
		return Response.ok().build();
	}

	@Override
	public void setVote(String agentId, String objectId, int value) throws ServiceInvocationException {
		Context.get().invoke(
				new ServiceNameVersion(NoracleVoteService.class.getCanonicalName(), NoracleService.API_VERSION),
				"setVote", agentId, objectId, value);
	}

	@GET
	@Path("/{agentId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Vote successfully retrieved",
			response = Vote.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public Vote getAgentVote(@PathParam("spaceId") String spaceId, @PathParam("relationId") String relationId,
			@PathParam("agentId") String agentId) throws ServiceInvocationException {
		String objectId = buildObjectId(spaceId, relationId);
		return getAgentVote(objectId, agentId);
	}

	@Override
	public Vote getAgentVote(String objectId, String agentId) throws ServiceInvocationException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleVoteService.class.getCanonicalName(), NoracleService.API_VERSION),
				"getAgentVote", objectId, agentId);
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
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Votes successfully retrieved",
			response = VoteList.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public VoteList getAllVotes(@PathParam("spaceId") String spaceId, @PathParam("relationId") String relationId)
			throws ServiceInvocationException {
		String objectId = buildObjectId(spaceId, relationId);
		return getAllVotes(objectId);
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

	private String buildObjectId(String spaceId, String relationId) throws ResourceNotFoundException {
		if (relationId != null && !relationId.isEmpty()) {
			return "relation-" + relationId;
		} else {
			throw new ResourceNotFoundException("No vote target identifier given");
		}
	}

}
