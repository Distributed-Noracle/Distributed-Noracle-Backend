package i5.las2peer.services.noracleService.resources;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import i5.las2peer.api.Context;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.ExceptionEntity;
import i5.las2peer.services.noracleService.NoracleAgentService;
import i5.las2peer.services.noracleService.NoracleRecommenderService;
import i5.las2peer.services.noracleService.NoracleService;
import i5.las2peer.services.noracleService.api.INoracleRecommenderService;
import i5.las2peer.services.noracleService.model.*;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
                    "Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call -> getRecommendedQuestionsForSpace(...)");
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
        // logger.info("RecommenderResource -> getRecommendedQuestions() with agentid " + agentId);
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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_HTML)
    @ApiResponses({ @ApiResponse(
            code = HttpURLConnection.HTTP_OK,
            message = "Recommendations successfully created for bot",
            response = BotResponse.class),
            @ApiResponse(
                    code = HttpURLConnection.HTTP_INTERNAL_ERROR,
                    message = "Internal Server Error",
                    response = ExceptionEntity.class) })
    @Path("/bot")
    public BotResponse getRecommendedQuestionsForBot(@ApiParam(required = true) String request) {
        RecommenderQuestionList recommenderQuestionList = new RecommenderQuestionList();

        Gson gson = new Gson();
        BotRequest botRequest;
        try {
            // Read out data and save it into object
            botRequest = gson.fromJson(request, BotRequest.class);
            System.out.println(botRequest.getMsg());
            System.out.println(botRequest.getBotName());
            System.out.println(botRequest.getChannel());
            System.out.println(botRequest.getIntent());
            System.out.println(botRequest.getEntities());
            System.out.println(botRequest.getEmail());
            System.out.println(botRequest.getUser());
            System.out.println(botRequest.getTime());

            // invite the bot into the space
            String invitationLink = botRequest.getMsg();
            String spaceSecret = "";
            String spaceId = "";
            try {
                new URL(invitationLink);
                spaceSecret = invitationLink.substring(invitationLink.indexOf("?pw=") + 4);
                spaceId = invitationLink.substring(invitationLink.indexOf("/spaces/") + 8, invitationLink.indexOf("?pw="));
            } catch (MalformedURLException | IndexOutOfBoundsException ex) {
                return new BotResponse("Sorry, but I cannot use this link.", true);
            }

            // Read out users profile
            String loginName = botRequest.getUser();
            String agentId = Context.get().getUserAgentIdentifierByLoginName(loginName);

            System.out.println("mainAgentId: " + Context.get().getMainAgent().getIdentifier());
            System.out.println("agentId: " + agentId);
            System.out.println("SpaceSecret: " + spaceSecret);
            System.out.println("spaceId: " + spaceId);

            Serializable rmiResult = Context.get().invoke(
                    new ServiceNameVersion(NoracleAgentService.class.getCanonicalName(), NoracleService.API_VERSION),
                    "checkIfAlreadySubscribedToSpace", Context.get().getMainAgent().getIdentifier(), spaceId);

            if (rmiResult instanceof Boolean) {
                if (!((Boolean) rmiResult)) {
                    System.out.println("The Bot is not subscribed to this space");
                    Context.get().invoke(
                            new ServiceNameVersion(NoracleAgentService.class.getCanonicalName(), NoracleService.API_VERSION),
                            "subscribeToSpace", spaceId, spaceSecret);
                } else {
                    System.out.println("The Bot is already subscribed to this space");
                }
            } else {
                throw new InternalServiceException(
                        "Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call -> getRecommendedQuestions(...)");
            }

            rmiResult = Context.get().invoke(
                    new ServiceNameVersion(NoracleRecommenderService.class.getCanonicalName(), NoracleService.API_VERSION),
                    "getRecommendedQuestionsForSpace", agentId, spaceId);
            if (rmiResult instanceof RecommenderQuestionList) {
                recommenderQuestionList = (RecommenderQuestionList) rmiResult;
            } else {
                throw new InternalServiceException(
                        "Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call -> getRecommendedQuestions(...)");
            }

        } catch (JsonSyntaxException ex) {
            System.out.println("Input " + request + " is invalid json.");
            System.out.println(ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (recommenderQuestionList.isEmpty()) {
            return new BotResponse("Sorry, I could not find any recommendations for you :(", true);
        }

        String text = "Here are the top " + recommenderQuestionList.size() + " questions for you :)\n";
        int index = 1;
        DateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        DateFormat formatter2 = new SimpleDateFormat("E, dd MMM yyyy HH:mm");
        for (RecommenderQuestion rq : recommenderQuestionList) {
            if (!text.isEmpty()) {
                text += "\n";
            }
            String q = rq.getQuestion().getText();
            q = q.replace("\n", "").replace("\r", "");
            text += index + ". " + q;
            text += ", Asked by " + rq.getAuthorName();

            try {
                Date date = formatter1.parse(rq.getQuestion().getTimestampCreated());
                text += ", Created at " + formatter2.format(date) + "\n";
            } catch (ParseException ex) {
                logger.warning(ex.getMessage());
            }
            text += "https://noracle.tech4comp.dbis.rwth-aachen.de/spaces/";
            text += rq.getQuestion().getSpaceId();
            text += "?sq=%5B%22";
            text += rq.getQuestion().getQuestionId();
            text += "%22%5D";
            index++;
        }
        return new BotResponse(text, true);
    }

    private final L2pLogger logger = L2pLogger.getInstance(RecommenderResource.class.getName());
}
