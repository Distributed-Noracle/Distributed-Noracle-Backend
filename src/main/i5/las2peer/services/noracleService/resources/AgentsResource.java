package i5.las2peer.services.noracleService.resources;

import java.io.Serializable;
import java.net.HttpURLConnection;

import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(
		tags = { "agents" })
public class AgentsResource implements INoracleAgentService {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Subscription successfully created",
			response = SpaceSubscription.class),
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
	@Path("/spacesubscriptions")
	public SpaceSubscription subscribeToSpace(@PathParam("agentid") String agentId,
			SubscribeSpacePojo subscribeSpacePojo) throws ServiceInvocationException {
		if (!Context.get().getMainAgent().getIdentifier().equals(agentId)) {
			throw new ForbiddenException("You can only subscribe yourself to a space");
		}
		return subscribeToSpace(subscribeSpacePojo.getSpaceId(), subscribeSpacePojo.getName());
	}

	@Override
	public SpaceSubscription subscribeToSpace(String spaceId, String name) throws ServiceInvocationException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleAgentService.class.getCanonicalName(), NoracleService.API_VERSION),
				"subscribeToSpace", spaceId, name);
		if (rmiResult instanceof SpaceSubscription) {
			return (SpaceSubscription) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
	}

	@Override
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Subscriptions successfully fetched",
			response = SpaceSubscriptionList.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	@Path("/spacesubscriptions")
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

}