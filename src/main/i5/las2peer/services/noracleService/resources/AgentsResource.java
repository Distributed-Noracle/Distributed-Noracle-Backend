package i5.las2peer.services.noracleService.resources;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import i5.las2peer.api.Context;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.restMapper.ExceptionEntity;
import i5.las2peer.services.noracleService.NoracleAgentService;
import i5.las2peer.services.noracleService.NoracleService;
import i5.las2peer.services.noracleService.api.INoracleAgentService;
import i5.las2peer.services.noracleService.model.SpaceSubscription;
import i5.las2peer.services.noracleService.model.SpaceSubscriptionList;
import i5.las2peer.services.noracleService.pojo.SubscribeSpacePojo;
import i5.las2peer.services.noracleService.pojo.UpdateSelectedQuestionsPojo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(
		tags = { AgentsResource.RESOURCE_NAME })
public class AgentsResource implements INoracleAgentService {

	public static final String RESOURCE_NAME = "agents";
	public static final String SUBSCRIPTIONS_RESOURCE_NAME = "spacesubscriptions";

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_CREATED,
			message = "Subscription successfully created"),
			@ApiResponse(
					code = HttpURLConnection.HTTP_BAD_REQUEST,
					message = "No space id given",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_UNAUTHORIZED,
					message = "You have to be logged in to subscribe to a space",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_FORBIDDEN,
					message = "You can only subscribe yourself to a space",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	@Path("/" + SUBSCRIPTIONS_RESOURCE_NAME)
	public Response subscribeToSpace(@PathParam("agentid") String agentId, SubscribeSpacePojo subscribeSpacePojo)
			throws ServiceInvocationException {
		if (!Context.get().getMainAgent().getIdentifier().equals(agentId)) {
			throw new ForbiddenException("You can only subscribe yourself to a space");
		}
		subscribeToSpace(subscribeSpacePojo.getSpaceId(), subscribeSpacePojo.getSpaceSecret());
		try {
			return Response.created(
					new URI(null, null, RESOURCE_NAME + "/" + agentId + "/" + SUBSCRIPTIONS_RESOURCE_NAME, null))
					.build();
		} catch (URISyntaxException e) {
			throw new InternalServerErrorException(e);
		}
	}

	@Override
	public SpaceSubscription subscribeToSpace(String spaceId, String spaceSecret)
			throws ServiceInvocationException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleAgentService.class.getCanonicalName(), NoracleService.API_VERSION),
				"subscribeToSpace", spaceId, spaceSecret);
		if (rmiResult instanceof SpaceSubscription) {
			return (SpaceSubscription) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
	}

	@DELETE
	@Produces(MediaType.TEXT_HTML)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Unsubscribed from space"),
			@ApiResponse(
					code = HttpURLConnection.HTTP_BAD_REQUEST,
					message = "No space id given",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_UNAUTHORIZED,
					message = "You have to be logged in to unsubscribe from a space",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_FORBIDDEN,
					message = "You can only unsubscribe yourself from a space",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	@Path("/" + SUBSCRIPTIONS_RESOURCE_NAME + "/{spaceId}")
	public Response unsubscribeFromSpace(@PathParam("agentid") String agentId, @PathParam("spaceId") String spaceId)
			throws ServiceInvocationException {
		if (!Context.get().getMainAgent().getIdentifier().equals(agentId)) {
			throw new ForbiddenException("You can only unsubscribe yourself from a space");
		}
		unsubscribeFromSpace(spaceId);
		return Response.ok().build();
	}

	@Override
	public void unsubscribeFromSpace(String spaceId) throws ServiceInvocationException {
		Context.get().invoke(
				new ServiceNameVersion(NoracleAgentService.class.getCanonicalName(), NoracleService.API_VERSION),
				"unsubscribeFromSpace", spaceId);
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Subscriptions successfully fetched",
			response = SpaceSubscriptionList.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	@Path("/" + SUBSCRIPTIONS_RESOURCE_NAME)
	public SpaceSubscriptionList getSpaceSubscriptions(@PathParam("agentid") String agentId)
			throws ServiceInvocationException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleAgentService.class.getCanonicalName(), NoracleService.API_VERSION),
				"getSpaceSubscriptions", agentId);
		if (rmiResult instanceof SpaceSubscriptionList) {
			return (SpaceSubscriptionList) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Selected questions successfully updated",
			response = SpaceSubscription.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	@Path("/" + SUBSCRIPTIONS_RESOURCE_NAME + "/{spaceId}/selectedQuestions")
	public SpaceSubscription updateSelectedQuestions(@PathParam("agentid") String agentId,
			@PathParam("spaceId") String spaceId, UpdateSelectedQuestionsPojo updateSelectedQuestionsPojo)
			throws ServiceInvocationException {
		return updateSpaceSubscription(agentId, spaceId, updateSelectedQuestionsPojo.getSelectedQuestions());
	}

	@Override
	public SpaceSubscription updateSpaceSubscription(String agentId, String spaceId, String[] selectedQuestions)
			throws ServiceInvocationException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleAgentService.class.getCanonicalName(), NoracleService.API_VERSION),
				"updateSpaceSubscription", agentId, spaceId, selectedQuestions);
		if (rmiResult instanceof SpaceSubscription) {
			return (SpaceSubscription) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
	}

}