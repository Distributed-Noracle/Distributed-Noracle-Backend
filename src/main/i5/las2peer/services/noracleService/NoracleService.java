package i5.las2peer.services.noracleService;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
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
import i5.las2peer.api.execution.ServiceInvocationFailedException;
import i5.las2peer.api.execution.ServiceMethodNotFoundException;
import i5.las2peer.api.execution.ServiceNotAvailableException;
import i5.las2peer.api.execution.ServiceNotFoundException;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.api.persistency.EnvelopeNotFoundException;
import i5.las2peer.restMapper.ExceptionEntity;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.services.noracleService.pojo.CreateQuestionPojo;
import i5.las2peer.services.noracleService.pojo.RelateQuestionsPojo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

@Api
@SwaggerDefinition(
		info = @Info(
				title = "Noracle Service",
				version = NoracleService.API_VERSION,
				description = "A bundle service for the distributed Noracle system",
				license = @License(
						name = "BSD-3",
						url = "https://github.com/rwth-acis/Noracle-Bundle-Service/blob/master/LICENSE")))
@ServicePath("/" + NoracleService.RESOURCE_NAME)
public class NoracleService extends RESTService {

	public static final String RESOURCE_NAME = "distributed-noracle";
	public static final String API_VERSION = "0.3";

	@Override
	protected void initResources() { // XXX possibly obsolete in near future
		getResourceConfig().register(this.getClass());
	}

	@Path("/spaces")
	public SpacesResource spaces() {
		return new SpacesResource();
	}

	@Api(
			tags = { "spaces" })
	public class SpacesResource implements INoracleSpaceService {

		@Override
		@POST
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses({ @ApiResponse(
				code = HttpURLConnection.HTTP_OK,
				message = "Space successfully created",
				response = Space.class),
				@ApiResponse(
						code = HttpURLConnection.HTTP_FORBIDDEN,
						message = "You have to be logged in to create a space",
						response = ExceptionEntity.class),
				@ApiResponse(
						code = HttpURLConnection.HTTP_INTERNAL_ERROR,
						message = "Internal Server Error",
						response = ExceptionEntity.class) })
		public Space createSpace() throws ServiceInvocationException {
			Serializable rmiResult = Context.get().invoke(
					new ServiceNameVersion(NoracleSpaceService.class.getCanonicalName(), API_VERSION), "createSpace");
			if (rmiResult instanceof Space) {
				return (Space) rmiResult;
			} else {
				throw new InternalServiceException(
						"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
			}
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
						code = HttpURLConnection.HTTP_UNAUTHORIZED,
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
		public Space getSpace(@PathParam("spaceId") String spaceId) throws Exception {
			try {
				Serializable rmiResult = Context.get().invoke(
						new ServiceNameVersion(NoracleSpaceService.class.getCanonicalName(), API_VERSION), "getSpace",
						spaceId);
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
			public Question createQuestion(@PathParam("spaceId") String questionSpaceId,
					CreateQuestionPojo createQuestionPojo) throws ServiceInvocationException {
				// TODO add more API responses for exceptions
				return createQuestion(questionSpaceId, createQuestionPojo.getQuestionText());
			}

			@Override
			public Question createQuestion(String questionText, String questionSpaceId)
					throws ServiceInvocationException {
				Serializable rmiResult = Context.get().invoke(
						new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), API_VERSION),
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
				// TODO add more API responses for exceptions
				Serializable rmiResult = Context.get().invoke(
						new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), API_VERSION),
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
		}

		@Path("/{spaceId}/relations")
		public QuestionRelationsResource relations() {
			return new QuestionRelationsResource();
		}

		public class QuestionRelationsResource implements INoracleQuestionRelationService {

			@POST
			@Consumes(MediaType.APPLICATION_JSON)
			@Produces(MediaType.APPLICATION_JSON)
			@ApiResponses({ @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Questions related",
					response = QuestionRelation.class),
					@ApiResponse(
							code = HttpURLConnection.HTTP_UNAUTHORIZED,
							message = "You have to be logged in to relate a question",
							response = ExceptionEntity.class),
					@ApiResponse(
							code = HttpURLConnection.HTTP_INTERNAL_ERROR,
							message = "Internal Server Error",
							response = ExceptionEntity.class) })
			public QuestionRelation postRelateQuestions(@PathParam("spaceId") String spaceId,
					RelateQuestionsPojo relateQuestionsPojo) throws ServiceNotFoundException,
					ServiceNotAvailableException, InternalServiceException, ServiceMethodNotFoundException,
					ServiceInvocationFailedException, ServiceAccessDeniedException, SecurityException {
				// TODO add more API responses for exceptions
				return relateQuestions(spaceId, relateQuestionsPojo.getFirstQuestionId(),
						relateQuestionsPojo.getSecondQuestionId(), relateQuestionsPojo.getRelationType());
			}

			@Override
			public QuestionRelation relateQuestions(String spaceId, String firstQuestionId, String secondQuestionId,
					String relationType) throws ServiceNotFoundException, ServiceNotAvailableException,
					InternalServiceException, ServiceMethodNotFoundException, ServiceInvocationFailedException,
					ServiceAccessDeniedException, SecurityException {
				Serializable rmiResult = Context.get().invoke(
						new ServiceNameVersion(NoracleQuestionRelationService.class.getCanonicalName(), API_VERSION),
						"relateQuestions", spaceId, firstQuestionId, secondQuestionId, relationType);
				if (rmiResult instanceof QuestionRelation) {
					return (QuestionRelation) rmiResult;
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
					message = "All relations for the given questionid",
					response = QuestionRelationList.class),
					@ApiResponse(
							code = HttpURLConnection.HTTP_BAD_REQUEST,
							message = "No question id given",
							response = ExceptionEntity.class),
					@ApiResponse(
							code = HttpURLConnection.HTTP_INTERNAL_ERROR,
							message = "Internal Server Error",
							response = ExceptionEntity.class) })
			public QuestionRelationList getQuestionRelations(@PathParam("spaceId") String spaceId,
					@PathParam("questionId") String questionId) throws ServiceNotFoundException,
					ServiceNotAvailableException, InternalServiceException, ServiceMethodNotFoundException,
					ServiceInvocationFailedException, ServiceAccessDeniedException, SecurityException {
				// TODO add more API responses for exceptions
				Serializable rmiResult = Context.get().invoke(
						new ServiceNameVersion(NoracleQuestionRelationService.class.getCanonicalName(), API_VERSION),
						"getQuestionRelations", questionId);
				if (rmiResult instanceof QuestionRelationList) {
					return (QuestionRelationList) rmiResult;
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
		}

	}

}
