package i5.las2peer.services.noracleService.resources;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import i5.las2peer.api.Context;
import i5.las2peer.api.execution.*;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.api.persistency.*;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.api.security.AgentOperationFailedException;
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
import java.util.*;

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
        //long start = System.currentTimeMillis();
        Serializable rmiResult = Context.get().invoke(
                new ServiceNameVersion(NoracleRecommenderService.class.getCanonicalName(), NoracleService.API_VERSION),
                "getRecommendedQuestionsForSpace", agentId, spaceId);
        if (rmiResult instanceof RecommenderQuestionList) {
            //logger.info("Computed " + ((RecommenderQuestionList) rmiResult).size() + " recommended questions!");
            //long end = System.currentTimeMillis();
            //System.out.println("getRecommendedQuestionsForSpace(...) took in seconds: "+ ((end-start) / 1000.0));
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
        logger.info("RecommenderResource -> getRecommendedQuestions() with agentid " + agentId);
        //long start = System.currentTimeMillis();
        Serializable rmiResult = Context.get().invoke(
                new ServiceNameVersion(NoracleRecommenderService.class.getCanonicalName(), NoracleService.API_VERSION),
                "getRecommendedQuestions", agentId);
        //logger.info("recommendations: check rmi Result");
        if (rmiResult instanceof RecommenderQuestionList) {
            //logger.info("return recommendations!");
            //long end = System.currentTimeMillis();
            //System.out.println("getRecommendedQuestions(...) took in seconds: "+ ((end-start) / 1000.0));
            return (RecommenderQuestionList) rmiResult;
        } else {
            throw new InternalServiceException(
                    "Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call -> getRecommendedQuestions(...)");
        }
    }

    // Everything related to the Noracle Bot
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
    public BotResponse getRecommendedQuestionsForBot(@ApiParam(required = true) String request) throws AgentOperationFailedException, AgentNotFoundException, ServiceNotAvailableException, ServiceInvocationFailedException, ServiceNotFoundException, ServiceAccessDeniedException, ServiceNotAuthorizedException, ServiceMethodNotFoundException, InternalServiceException {
        Gson gson = new Gson();
        BotRequest botRequest;
        botRequest = gson.fromJson(request, BotRequest.class);

        System.out.println(botRequest.getMsg());
        System.out.println(botRequest.getBotName());
        System.out.println(botRequest.getChannel());
        System.out.println(botRequest.getIntent());
        System.out.println(botRequest.getEntities());
        System.out.println(botRequest.getEmail());
        System.out.println(botRequest.getUser());
        System.out.println(botRequest.getTime());

        if (botRequest.getMsg().startsWith("http")) {
            return getRecommendationsFromSpaceLink(botRequest);
        } else if (botRequest.getIntent().equals("number_selection")) {
            // Recommendations for your saved spaces
            if (botRequest.getMsg().equals("1")) {
                try {
                    Envelope env = Context.get().requestEnvelope(buildBotSpacesId(botRequest.getUser()));
                    List<String> spaceIds = (List<String>) env.getContent();
                    RecommenderQuestionList rqList = new RecommenderQuestionList();
                    // Read out users profile
                    String loginName = botRequest.getUser();
                    String agentId = Context.get().getUserAgentIdentifierByLoginName(loginName);

                    for (String spaceId : spaceIds) {
                        RecommenderQuestionList list = (RecommenderQuestionList) Context.get().invoke(
                                new ServiceNameVersion(NoracleRecommenderService.class.getCanonicalName(), NoracleService.API_VERSION),
                                "getRecommendedQuestionsForSpace", agentId, spaceId);
                        rqList.addAll(list);
                    }
                    return createBotResponseFromRecommendations(rqList);
                } catch (EnvelopeNotFoundException | EnvelopeAccessDeniedException | EnvelopeOperationFailedException e) {
                    String msg = "You do not have any saved spaces.";
                    msg += "\n" + optionMsg;
                    return new BotResponse(msg, false);
                }
            // Recommendations for a specific space
            } else if (botRequest.getMsg().equals("2")) {
                return new BotResponse("Can you send me the invitation (share) link of the space where you want to get recommendations from?", false);
            } else if (botRequest.getMsg().equals("3")) {
                return new BotResponse("Okay :/", true);
            }
        }

        String msg = "Sorry, but I cannot answer.\n\n" + optionMsg;
        return new BotResponse(msg, false);

    }

    private BotResponse getRecommendationsFromSpaceLink(BotRequest botRequest) {
        RecommenderQuestionList recommenderQuestionList = new RecommenderQuestionList();
        try {
            // invite the bot into the space
            String invitationLink = botRequest.getMsg();
            String spaceSecret = "";
            String spaceId = "";
            try {
                new URL(invitationLink);
                spaceSecret = invitationLink.substring(invitationLink.indexOf("?pw=") + 4);
                spaceId = invitationLink.substring(invitationLink.indexOf("/spaces/") + 8, invitationLink.indexOf("?pw="));
            } catch (MalformedURLException | IndexOutOfBoundsException ex) {
                String msg = "Sorry, but I cannot use this link.\n\n" + optionMsg;
                return new BotResponse(msg, false);
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

            // Create or extend envelope
            Envelope env;
            List<String> spaceIdList;
            try {
                env = Context.get().requestEnvelope(buildBotSpacesId(botRequest.getUser()));
                spaceIdList = (List<String>) env.getContent();
                if (!spaceIdList.contains(spaceId)) {
                    spaceIdList.add(spaceId);
                }
            } catch (EnvelopeNotFoundException ex) {
                env = Context.get().createEnvelope(buildBotSpacesId(botRequest.getUser()));
                spaceIdList = new ArrayList<>();
                spaceIdList.add(spaceId);
            }
            env.setContent((Serializable) spaceIdList);
            Context.get().storeEnvelope(env, (toStore, inNetwork) -> {
                logger.info("onCollision was called....");
                return toStore;
            });

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
            System.out.println("Input is invalid json.");
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            // error
            ex.printStackTrace();
        }

        if (recommenderQuestionList.isEmpty()) {
            return new BotResponse("Sorry, I could not find any recommendations for you :(", true);
        }

        return createBotResponseFromRecommendations(recommenderQuestionList);
    }

    private BotResponse createBotResponseFromRecommendations(RecommenderQuestionList recommenderQuestionList) {
        // TODO: Order by Utility!!!
        if (recommenderQuestionList.size() > 6) {
            Collections.shuffle(recommenderQuestionList);
        }
        int nrRec = Math.min(6, recommenderQuestionList.size());

        String text = "Here are the top " + nrRec + " questions for you :)\n";
        int index = 1;
        DateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        DateFormat formatter2 = new SimpleDateFormat("E, dd MMM yyyy HH:mm");
        for (RecommenderQuestion rq : recommenderQuestionList) {
            if (index == nrRec + 1) {
                break;
            }
            if (!text.isEmpty()) {
                text += "\n";
            }
            // Markdown style [text](https://url.com)
            String q = rq.getQuestion().getText();
            q = q.replace("\n", "").replace("\r", "");
            //text += index + ". [" + q + "]";
            text += ":speech_balloon: [" + q + "]";

            text += "(https://noracle.tech4comp.dbis.rwth-aachen.de/spaces/";
            text += rq.getQuestion().getSpaceId();
            text += "?sq=%5B%22";
            text += rq.getQuestion().getQuestionId();
            text += "%22%5D)";

            try {
                Date date = formatter1.parse(rq.getQuestion().getTimestampCreated());
                text += ", Asked by " + rq.getAuthorName();
                text += ", Created at " + formatter2.format(date) + "\n";
            } catch (ParseException ex) {
                logger.warning(ex.getMessage());
            }
            index++;
        }
        return new BotResponse(text, true);
    }

    private String buildBotSpacesId(String user) {
        return "bot-recommender-spaces-user-" + user;
    }

    private final L2pLogger logger = L2pLogger.getInstance(RecommenderResource.class.getName());

    private final String optionMsg = "1. Recommended questions for your saved spaces\\n" +
            "2. Recommended questions for a specific space (+ save the space)\\n" +
            "3. Cancel\\n" +
            "Please specify one option (1 - 3).";
}
