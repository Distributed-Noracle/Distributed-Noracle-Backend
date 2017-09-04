package i5.las2peer.services.noracleService.resources;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import i5.las2peer.api.Context;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.ServiceAccessDeniedException;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.execution.ServiceInvocationFailedException;
import i5.las2peer.api.execution.ServiceMethodNotFoundException;
import i5.las2peer.api.execution.ServiceNotAvailableException;
import i5.las2peer.api.execution.ServiceNotFoundException;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.api.persistency.EnvelopeNotFoundException;
import i5.las2peer.restMapper.ExceptionEntity;
import i5.las2peer.services.noracleService.INoracleQuestionService;
import i5.las2peer.services.noracleService.NoracleQuestionService;
import i5.las2peer.services.noracleService.NoracleService;
import i5.las2peer.services.noracleService.Question;
import i5.las2peer.services.noracleService.QuestionList;
import i5.las2peer.services.noracleService.pojo.ChangeQuestionPojo;
import i5.las2peer.services.noracleService.pojo.CreateQuestionPojo;
import i5.las2peer.services.noracleService.pojo.GetQuestionsPojo;
import i5.las2peer.services.noracleService.pojo.GetQuestionsResponsePojo;
import i5.las2peer.services.noracleService.pojo.LinkPojo;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public class QuestionsResource implements INoracleQuestionService {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "Question successfully created",
			response = Question.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_BAD_REQUEST,
					message = "No question text given",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_UNAUTHORIZED,
					message = "You have to be logged in to create a question",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public Question createQuestion(@PathParam("spaceId") String questionSpaceId, CreateQuestionPojo createQuestionPojo)
			throws ServiceInvocationException {
		return createQuestion(questionSpaceId, createQuestionPojo.getQuestionText());
	}

	@Override
	public Question createQuestion(String questionText, String questionSpaceId) throws ServiceInvocationException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), NoracleService.API_VERSION),
				"createQuestion", questionText, questionSpaceId);
		if (rmiResult instanceof Question) {
			return (Question) rmiResult;
		} else if (rmiResult instanceof InvocationTargetException) {
			Throwable cause = ((InvocationTargetException) rmiResult).getCause();
			if (cause instanceof IllegalArgumentException) {
				throw new BadRequestException(cause.getMessage(), cause);
			} else if (cause instanceof ServiceAccessDeniedException) {
				throw new ForbiddenException(cause.getMessage(), cause);
			} else if (cause instanceof EnvelopeNotFoundException) {
				throw new NotFoundException(cause.getMessage(), cause);
			} else {
				throw new InternalServerErrorException("Exception in RMI call", cause);
			}
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
	}

	@Override
	@GET
	@Path("/{questionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "A question object from the network",
			response = Question.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_BAD_REQUEST,
					message = "No question id given",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_UNAUTHORIZED,
					message = "Access Denied",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_NOT_FOUND,
					message = "Question Not Found",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public Question getQuestion(@PathParam("questionId") String questionId) throws ServiceNotFoundException,
			ServiceNotAvailableException, InternalServiceException, ServiceMethodNotFoundException,
			ServiceInvocationFailedException, ServiceAccessDeniedException, SecurityException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), NoracleService.API_VERSION),
				"getQuestion", questionId);
		if (rmiResult instanceof Question) {
			return (Question) rmiResult;
		} else if (rmiResult instanceof InvocationTargetException) {
			Throwable cause = ((InvocationTargetException) rmiResult).getCause();
			if (cause instanceof IllegalArgumentException) {
				throw new BadRequestException(cause.getMessage(), cause);
			} else if (cause instanceof ServiceAccessDeniedException) {
				throw new ForbiddenException(cause.getMessage(), cause);
			} else if (cause instanceof EnvelopeNotFoundException) {
				throw new NotFoundException(cause.getMessage(), cause);
			} else {
				throw new InternalServerErrorException("Exception in RMI call", cause);
			}
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
			message = "A list of questions from the network",
			response = GetQuestionsResponsePojo.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_BAD_REQUEST,
					message = "No space id given",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_UNAUTHORIZED,
					message = "Not logged in",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_FORBIDDEN,
					message = "Access Denied",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public GetQuestionsResponsePojo getQuestions(@PathParam("spaceId") String spaceId,
			@QueryParam("order") String paramOrder, @QueryParam("limit") Integer paramLimit,
			@QueryParam("startAt") Integer paramStartAt, GetQuestionsPojo getQuestionsPojo)
			throws ServiceInvocationException {
		String order = getQuestionsPojo.getOrder();
		if (order == null || order.isEmpty()) {
			order = paramOrder;
		}
		Integer limit = getQuestionsPojo.getLimit();
		if (limit == null) {
			limit = paramLimit;
		}
		Integer startAt = getQuestionsPojo.getStartAt();
		if (startAt == null) {
			startAt = paramStartAt;
		}
		QuestionList questionList = getQuestions(spaceId, order, limit, startAt);
		GetQuestionsResponsePojo response = new GetQuestionsResponsePojo();
		response.setContent(questionList);
		ArrayList<LinkPojo> links = new ArrayList<>();
		String queryOrder = order != null ? order : "";
		String queryLimit = limit != null ? Integer.toString(limit) : "";
		String queryStartAt = startAt != null ? Integer.toString(startAt) : "";
		LinkPojo nextLink = new LinkPojo("next", "?" + String.join("&", queryOrder, queryLimit, queryStartAt));
		links.add(nextLink);
		response.setLinks(links);
		return response;
	}

	@Override
	public QuestionList getQuestions(String spaceId, String order, Integer limit, Integer startAt)
			throws ServiceInvocationException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), NoracleService.API_VERSION),
				"getQuestions", spaceId, order, limit, startAt);
		if (rmiResult instanceof QuestionList) {
			return (QuestionList) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
	}

	@PUT
	@Path("/{questionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses({ @ApiResponse(
			code = HttpURLConnection.HTTP_OK,
			message = "A list of questions from the network",
			response = Question.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_BAD_REQUEST,
					message = "No question id given",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_UNAUTHORIZED,
					message = "Not logged in",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_FORBIDDEN,
					message = "Access Denied",
					response = ExceptionEntity.class),
			@ApiResponse(
					code = HttpURLConnection.HTTP_INTERNAL_ERROR,
					message = "Internal Server Error",
					response = ExceptionEntity.class) })
	public Question changeQuestionText(@PathParam("questionId") String questionId,
			ChangeQuestionPojo changeQuestionPojo) throws ServiceInvocationException {
		return changeQuestionText(questionId, changeQuestionPojo.getQuestionText());
	}

	@Override
	public Question changeQuestionText(String questionId, String questionText) throws ServiceInvocationException {
		Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), NoracleService.API_VERSION),
				"changeQuestion", questionId, questionText);
		if (rmiResult instanceof Question) {
			return (Question) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
	}

}