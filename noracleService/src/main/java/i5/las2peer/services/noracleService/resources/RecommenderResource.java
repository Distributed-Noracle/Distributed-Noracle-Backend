package i5.las2peer.services.noracleService.resources;

import i5.las2peer.api.Context;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.ExceptionEntity;
import i5.las2peer.services.noracleService.NoracleRecommenderService;
import i5.las2peer.services.noracleService.NoracleService;
import i5.las2peer.services.noracleService.api.INoracleRecommenderService;
import i5.las2peer.services.noracleService.model.RecommenderQuestionList;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.net.HttpURLConnection;

public class RecommenderResource implements INoracleRecommenderService {
    public static final String RESOURCE_NAME = "recommend";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({ @ApiResponse(
            code = HttpURLConnection.HTTP_OK,
            message = "Recommendations successfully created",
            response = RecommenderQuestionList.class),
            @ApiResponse(
                    code = HttpURLConnection.HTTP_INTERNAL_ERROR,
                    message = "Internal Server Error",
                    response = ExceptionEntity.class) })
    @Path("/{agentId}/{spaceId}")
    public RecommenderQuestionList getRecommendedQuestionsForSpace(@PathParam("agentId") String agentId,
                                                           @PathParam("spaceId") String spaceId) throws ServiceInvocationException {
        logger.info("RecommenderResource -> getRecommendedQuestions() with agentid " + agentId + " and spaceId " + spaceId + " called");
        Serializable rmiResult = Context.get().invoke(
                new ServiceNameVersion(NoracleRecommenderService.class.getCanonicalName(), NoracleService.API_VERSION),
                "getRecommendedQuestionsForSpace", agentId, spaceId);
        if (rmiResult instanceof RecommenderQuestionList) {
            return (RecommenderQuestionList) rmiResult;
        } else {
            throw new InternalServiceException(
                    "Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call -> getRecommendedQuestions(...)");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({ @ApiResponse(
            code = HttpURLConnection.HTTP_OK,
            message = "Recommendations successfully created",
            response = RecommenderQuestionList.class),
            @ApiResponse(
                    code = HttpURLConnection.HTTP_INTERNAL_ERROR,
                    message = "Internal Server Error",
                    response = ExceptionEntity.class) })
    @Path("/{agentId}")
    public RecommenderQuestionList getRecommendedQuestions(@PathParam("agentId") String agentId) throws ServiceInvocationException {
        logger.info("RecommenderResource -> getRecommendedQuestions() with agentid " + agentId);
        Serializable rmiResult = Context.get().invoke(
                new ServiceNameVersion(NoracleRecommenderService.class.getCanonicalName(), NoracleService.API_VERSION),
                "getRecommendedQuestions", agentId);
        if (rmiResult instanceof RecommenderQuestionList) {
            return (RecommenderQuestionList) rmiResult;
        } else {
            throw new InternalServiceException(
                    "Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call -> getRecommendedQuestions(...)");
        }
    }

    @GET
    @Path("/version")
    public String version() { return NoracleService.API_VERSION; }

    private final L2pLogger logger = L2pLogger.getInstance(RecommenderResource.class.getName());
}
