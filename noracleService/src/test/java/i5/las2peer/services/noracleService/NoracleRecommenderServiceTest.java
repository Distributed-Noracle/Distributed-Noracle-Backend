package i5.las2peer.services.noracleService;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.p2p.Node;
import i5.las2peer.p2p.PastryNodeImpl;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.services.noracleService.model.Question;
import i5.las2peer.services.noracleService.model.RecommenderQuestionList;
import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.pojo.CreateQuestionPojo;
import i5.las2peer.services.noracleService.pojo.CreateSpacePojo;
import i5.las2peer.services.noracleService.pojo.SetVotePojo;
import i5.las2peer.services.noracleService.resources.*;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.testing.TestSuite;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class NoracleRecommenderServiceTest {
    private static final String TEST_SPACE_NAME = "so many questions";
    private static final String TEST_QUESTION_TEXT_1 = "How are you?";
    private static final String TEST_QUESTION_TEXT_2 = "What's up in-your-life?";
    private static final String TEST_QUESTION_TEXT_3 = "What is going on?";

    protected int networkSize = 1;
    protected ArrayList<PastryNodeImpl> nodes;
    protected WebConnector connector;
    protected Client webClient;
    protected UserAgentImpl testAgent;
    protected UserAgentImpl testAgent2;
    protected UserAgentImpl testAgent3;
    protected String basicAuthHeader;
    protected String basicAuthHeader2;
    protected String basicAuthHeader3;
    protected String baseUrl;

    /**
     * Called before a test starts.
     *
     * Sets up the node, initializes connector and adds user agent that can be used throughout the test.
     *
     * @throws Exception
     */
    @Before
    public void beforeTest() {

        try {
            nodes = TestSuite.launchNetwork(networkSize);
            connector = new WebConnector(null);
            connector.start(nodes.get(0));
            // don't follow redirects, some tests want to test for correct redirect
            // responses
            webClient = ClientBuilder.newBuilder().register(MultiPartFeature.class)
                    .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE).build();
            startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleService",
                    NoracleService.API_VERSION);
            startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleAgentService",
                    NoracleService.API_VERSION);
            startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleSpaceService",
                    NoracleService.API_VERSION);
            startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleQuestionService",
                    NoracleService.API_VERSION);
            startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleQuestionRelationService",
                    NoracleService.API_VERSION);
            startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleVoteService",
                    NoracleService.API_VERSION);
            startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleRecommenderService",
                    NoracleService.API_VERSION);
            startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleNormalizationService",
                    NoracleService.API_VERSION);
            testAgent = MockAgentFactory.getAdam();
            testAgent.unlock("adamspass");
            PastryNodeImpl activeNode = nodes.get(0);
            activeNode.storeAgent(testAgent);
            basicAuthHeader = "basic " + Base64.getEncoder()
                    .encodeToString((testAgent.getLoginName() + ":" + "adamspass").getBytes(StandardCharsets.UTF_8));

            testAgent2 = MockAgentFactory.getEve();
            testAgent2.unlock("evespass");
            activeNode.storeAgent(testAgent2);
            basicAuthHeader2 = "basic " + Base64.getEncoder()
                    .encodeToString((testAgent2.getLoginName() + ":" + "evespass").getBytes(StandardCharsets.UTF_8));

            testAgent3 = MockAgentFactory.getAbel();
            testAgent3.unlock("abelspass");
            activeNode.storeAgent(testAgent3);
            basicAuthHeader3 = "basic " + Base64.getEncoder()
                    .encodeToString((testAgent3.getLoginName() + ":" + "abelspass").getBytes(StandardCharsets.UTF_8));

            baseUrl = connector.getHttpEndpoint() + "/" + NoracleService.RESOURCE_NAME + "/v"
                    + NoracleService.API_VERSION;
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

    private void startService(Node node, String clsName, String version) throws Exception {
        ServiceAgentImpl serviceAgent = ServiceAgentImpl.createServiceAgent(new ServiceNameVersion(clsName, version),
                "testtest");
        serviceAgent.unlock("testtest");
        node.storeAgent(serviceAgent);
        node.registerReceiver(serviceAgent);
    }

    /**
     * Called after the test has finished. Shuts down the server and prints out the connector log file for reference.
     *
     * @throws Exception
     */
    @After
    public void afterTest() {
        for (PastryNodeImpl node : nodes) {
            try {
                node.shutDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testGetSimpleRecommendationsForSpace() {
        try {
            // create test space and questions with testAgent
            Space space = createAndFetchTestSpace();
            String questionId1 = createTestQuestions(space.getSpaceId(), TEST_QUESTION_TEXT_1);
            String questionId2 = createTestQuestions(space.getSpaceId(), TEST_QUESTION_TEXT_2);
            String questionId3 = createTestQuestions(space.getSpaceId(), TEST_QUESTION_TEXT_3);

            // set votes with testAgent2
            setVote("/" + SpacesResource.RESOURCE_NAME + "/" + space.getSpaceId() + "/" + QuestionsResource.RESOURCE_NAME + "/"
                            + questionId1 + "/" + QuestionVotesResource.RESOURCE_NAME + "/" + testAgent2.getIdentifier(),
                    basicAuthHeader, 0);
            setVote("/" + SpacesResource.RESOURCE_NAME + "/" + space.getSpaceId() + "/" + QuestionsResource.RESOURCE_NAME + "/"
                            + questionId2 + "/" + QuestionVotesResource.RESOURCE_NAME + "/" + testAgent2.getIdentifier(),
                    basicAuthHeader, 1);
            setVote("/" + SpacesResource.RESOURCE_NAME + "/" + space.getSpaceId() + "/" + QuestionsResource.RESOURCE_NAME + "/"
                            + questionId3 + "/" + QuestionVotesResource.RESOURCE_NAME + "/" + testAgent2.getIdentifier(),
                    basicAuthHeader, -1);

            // call recommender service with testAgent3
            WebTarget target = webClient.target(baseUrl + "/" + RecommenderResource.RESOURCE_NAME + "/" + testAgent3.getIdentifier() + "/" + space.getSpaceId());
            Invocation.Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
            Response response = request.get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

            RecommenderQuestionList recommendations = response.readEntity(RecommenderQuestionList.class);
            Assert.assertEquals(recommendations.size(), 3);
            Assert.assertEquals(recommendations.get(0).getQuestion().getQuestionId(), questionId2);
            Assert.assertEquals(recommendations.get(1).getQuestion().getQuestionId(), questionId1);
            Assert.assertEquals(recommendations.get(2).getQuestion().getQuestionId(), questionId3);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testGetSimpleRecommendations() {
        try {
            // create test space with questions
            Space space = createAndFetchTestSpace();
            createTestQuestions(space.getSpaceId(), TEST_QUESTION_TEXT_1);
            createTestQuestions(space.getSpaceId(), TEST_QUESTION_TEXT_2);
            createTestQuestions(space.getSpaceId(), TEST_QUESTION_TEXT_3);
            WebTarget target = webClient.target(baseUrl + "/" + RecommenderResource.RESOURCE_NAME + "/" + testAgent.getIdentifier());
            Invocation.Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
            Response response = request.get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

            RecommenderQuestionList recommendations = response.readEntity(RecommenderQuestionList.class);
            recommendations.stream().forEach(r -> System.out.println(r.getQuestion().getText()));
            Assert.assertEquals(recommendations.size(), 3);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

    protected Space createAndFetchTestSpace() {
        // create test space
        CreateSpacePojo body = new CreateSpacePojo();
        body.setName(TEST_SPACE_NAME);
        WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME);
        Invocation.Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
        Response response = request.post(Entity.json(body));
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
        // fetch test space
        String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
        WebTarget targetSpace = webClient.target(locationHeader);
        Invocation.Builder requestSpace = targetSpace.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
        Response responseSpace = requestSpace.get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), responseSpace.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, responseSpace.getMediaType());
        Space space = responseSpace.readEntity(Space.class);
        return space;
    }

    protected String createTestQuestions(String testSpaceId, String testQuestion) throws Exception {
        // create question in space
        CreateQuestionPojo body = new CreateQuestionPojo();
        body.setText(testQuestion);
        WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
                + QuestionsResource.RESOURCE_NAME);
        Invocation.Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
        Response response = request.post(Entity.json(body));
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
        // fetch test question
        String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
        WebTarget targetQuestion = webClient.target(locationHeader);
        Invocation.Builder requestQuestion = targetQuestion.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
        Response responseQuestion = requestQuestion.get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), responseQuestion.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, responseQuestion.getMediaType());
        Question question = responseQuestion.readEntity(Question.class);
        Assert.assertEquals(testSpaceId, question.getSpaceId());
        Assert.assertEquals(testQuestion, question.getText());
        Assert.assertEquals(question.getTimestampCreated(), question.getTimestampLastModified());
        return question.getQuestionId();
    }

    private void setVote(String path, String authHeader, int value) {
        SetVotePojo body = new SetVotePojo();
        body.setValue(value);
        WebTarget target = webClient.target(baseUrl + path);
        Invocation.Builder request = target.request().header(HttpHeaders.AUTHORIZATION, authHeader);
        Response response = request.put(Entity.json(body));
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

}
