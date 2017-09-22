package i5.las2peer.services.noracleService.resources;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import i5.las2peer.api.Context;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.restMapper.ExceptionEntity;
import i5.las2peer.services.noracleService.NoracleQuestionRelationService;
import i5.las2peer.services.noracleService.NoracleService;
import i5.las2peer.services.noracleService.api.INoracleQuestionRelationService;
import i5.las2peer.services.noracleService.model.Question;
import i5.las2peer.services.noracleService.model.QuestionRelation;
import i5.las2peer.services.noracleService.model.QuestionRelationList;
import i5.las2peer.services.noracleService.pojo.ChangeQuestionRelationPojo;
import i5.las2peer.services.noracleService.pojo.CreateRelationPojo;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public class QuestionRelationsResource implements INoracleQuestionRelationService {

	public static final String RESOURCE_NAME = "relations";

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_CREATED,
			message = "Relation successfully created"),
			@ApiResponse(
					code = HttpURLConnection.HTTP_UNAUTHORIZED,
					message = "You have to be logged in to create a relation",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public Response createQuestionRelation(@PathParam("spaceId") String spaceId, CreateRelationPojo createRelationPojo)
			throws ServiceInvocationException {
		createQuestionRelation(spaceId, createRelationPojo.getName(), createRelationPojo.getQuestionId1(),
				createRelationPojo.getQuestionId2(), createRelationPojo.isDirected());
		try {
			return Response.created(
					new URI(null, null, SpacesResource.RESOURCE_NAME + "/" + spaceId + "/" + RESOURCE_NAME, null))
					.build();
		} catch (URISyntaxException e) {
			throw new InternalServerErrorException(e);
		}
	}

	@Override
	public QuestionRelation createQuestionRelation(String spaceId, String name, String questionId1, String questionId2,
			Boolean directed) throws ServiceInvocationException {
		Serializable rmiResult = Context.get()
				.invoke(new ServiceNameVersion(NoracleQuestionRelationService.class.getCanonicalName(),
						NoracleService.API_VERSION), "createQuestionRelation", spaceId, name, questionId1, questionId2,
						directed);
		if (rmiResult instanceof QuestionRelation) {
			return (QuestionRelation) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
	}

	@Override
	@GET
	@Path("/{relationId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "A question relation object from the network",
			response = QuestionRelation.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_BAD_REQUEST,
					message = "No relation id given",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_FORBIDDEN,
					message = "Access Denied",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_NOT_FOUND,
					message = "Relation Not Found",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public QuestionRelation getQuestionRelation(@PathParam("relationId") String relationId)
			throws ServiceInvocationException {
		Serializable rmiResult = Context.get()
				.invoke(new ServiceNameVersion(NoracleQuestionRelationService.class.getCanonicalName(),
						NoracleService.API_VERSION), "getQuestionRelation", relationId);
		if (rmiResult instanceof Question) {
			return (QuestionRelation) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "A list of relations from the network",
			response = QuestionRelationList.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_BAD_REQUEST,
					message = "No space id given",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_FORBIDDEN,
					message = "Access Denied",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public Response getQuestions(@PathParam("spaceId") String spaceId, @QueryParam("order") String order,
			@QueryParam("limit") Integer limit, @QueryParam("startAt") Integer startAt)
			throws ServiceInvocationException {
		QuestionRelationList questionRelationList = getQuestionRelations(spaceId, order, limit, startAt);
		ResponseBuilder responseBuilder = Response.ok(questionRelationList);
		String queryOrder = order != null ? "order=" + order : "";
		String queryLimit = limit != null ? "limit=" + Integer.toString(limit) : "";
		String queryStartAt = "";
		if (startAt != null && limit != null) {
			if (order.equalsIgnoreCase("desc")) {
				queryStartAt = "startat=" + Integer.toString(startAt - limit);
			} else {
				queryStartAt = "startat=" + Integer.toString(startAt + limit);
			}
		}
		String nextLinkStr = "";
		for (String param : new String[] { queryOrder, queryLimit, queryStartAt }) {
			if (param != null && !param.isEmpty()) {
				if (nextLinkStr.isEmpty()) {
					nextLinkStr += "?";
				} else {
					nextLinkStr += "&";
				}
				nextLinkStr += param;
			}
		}
		if (!nextLinkStr.isEmpty()) {
			responseBuilder.header(HttpHeaders.LINK, "<" + nextLinkStr + ">; rel=\"next\"");
		}
		return responseBuilder.build();
	}

	@Override
	public QuestionRelationList getQuestionRelations(String spaceId, String order, Integer limit, Integer startAt)
			throws ServiceInvocationException {
		Serializable rmiResult = Context.get()
				.invoke(new ServiceNameVersion(NoracleQuestionRelationService.class.getCanonicalName(),
						NoracleService.API_VERSION), "getQuestionRelations", spaceId, order, limit, startAt);
		if (rmiResult instanceof QuestionRelationList) {
			return (QuestionRelationList) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
	}

	@PUT
	@Path("/{relationId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Changes a question relation",
			response = QuestionRelation.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_BAD_REQUEST,
					message = "No relation id given",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_FORBIDDEN,
					message = "Access Denied",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_NOT_FOUND,
					message = "Relation Not Found",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public QuestionRelation changeQuestionRelation(@PathParam("relationId") String relationId,
			ChangeQuestionRelationPojo changeQuestionRelationPojo) throws ServiceInvocationException {
		return changeQuestionRelation(relationId, changeQuestionRelationPojo.getName(),
				changeQuestionRelationPojo.getQuestionId1(), changeQuestionRelationPojo.getQuestionId2(),
				changeQuestionRelationPojo.getDirected());
	}

	@Override
	public QuestionRelation changeQuestionRelation(String relationId, String name, String questionId1,
			String questionId2, Boolean directed) throws ServiceInvocationException {
		Serializable rmiResult = Context.get()
				.invoke(new ServiceNameVersion(NoracleQuestionRelationService.class.getCanonicalName(),
						NoracleService.API_VERSION), "changeQuestionRelation", relationId, name, questionId1,
						questionId2, directed);
		if (rmiResult instanceof QuestionRelation) {
			return (QuestionRelation) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
	}

}