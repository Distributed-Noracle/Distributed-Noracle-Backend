package i5.las2peer.services.noracleService.resources;

import java.io.Serializable;
import java.net.HttpURLConnection;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import i5.las2peer.api.Context;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.InvocationBadArgumentException;
import i5.las2peer.api.execution.ResourceNotFoundException;
import i5.las2peer.api.execution.ServiceAccessDeniedException;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.restMapper.ExceptionEntity;
import i5.las2peer.services.noracleService.NoracleService;
import i5.las2peer.services.noracleService.NoracleSpaceService;
import i5.las2peer.services.noracleService.api.INoracleSpaceService;
import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.pojo.CreateSpacePojo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(
		tags = { "spaces" })
public class SpacesResource implements INoracleSpaceService {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Space successfully created",
			response = Space.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_UNAUTHORIZED,
					message = "You have to be logged in to create a space",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public Space createSpace(CreateSpacePojo createSpacePojo) throws ServiceInvocationException {
		return createSpace(createSpacePojo.getName());
	}

	@Override
	public Space createSpace(String name) throws ServiceInvocationException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleSpaceService.class.getCanonicalName(), NoracleService.API_VERSION),
				"createSpace", name);
		Space space;
		if (rmiResult instanceof Space) {
			space = (Space) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
		new AgentsResource().subscribeToSpace(space.getSpaceId(), space.getName());
		return space;
	}

	@Override
	@GET
	@Path("/{spaceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "A space object from the network",
			response = Space.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_BAD_REQUEST,
					message = "No space id given",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_FORBIDDEN,
					message = "Access Denied",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_NOT_FOUND,
					message = "Space Not Found",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public Space getSpace(@PathParam("spaceId") String spaceId) throws ServiceInvocationException {
		try {
			Serializable rmiResult = Context.get().invoke(
					new ServiceNameVersion(NoracleSpaceService.class.getCanonicalName(), NoracleService.API_VERSION),
					"getSpace", spaceId);
			if (rmiResult instanceof Space) {
				return (Space) rmiResult;
			} else {
				throw new InternalServiceException(
						"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
			}
		} catch (InvocationBadArgumentException e) {
			throw new BadRequestException(e.getMessage(), e.getCause());
		} catch (ResourceNotFoundException e) {
			throw new NotFoundException(e.getMessage(), e.getCause());
		} catch (ServiceAccessDeniedException e) {
			throw new ForbiddenException(e.getMessage(), e.getCause());
		} catch (Exception e) {
			throw new InternalServerErrorException("Exception during RMI call", e);
		}
	}

	@Path("/{spaceId}/questions")
	public QuestionsResource questions() {
		return new QuestionsResource();
	}

	@Path("/{spaceId}/relations")
	public QuestionRelationsResource relations() {
		return new QuestionRelationsResource();
	}

}
