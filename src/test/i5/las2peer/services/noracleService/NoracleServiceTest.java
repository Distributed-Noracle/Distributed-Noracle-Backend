package i5.las2peer.services.noracleService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.p2p.Node;
import i5.las2peer.p2p.PastryNodeImpl;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.services.noracleService.model.Question;
import i5.las2peer.services.noracleService.model.QuestionList;
import i5.las2peer.services.noracleService.model.QuestionRelation;
import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.model.SpaceSubscriptionList;
import i5.las2peer.services.noracleService.pojo.ChangeQuestionPojo;
import i5.las2peer.services.noracleService.pojo.CreateQuestionPojo;
import i5.las2peer.services.noracleService.pojo.CreateRelationPojo;
import i5.las2peer.services.noracleService.pojo.CreateSpacePojo;
import i5.las2peer.services.noracleService.pojo.GetQuestionsResponsePojo;
import i5.las2peer.services.noracleService.pojo.LinkPojo;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.testing.TestSuite;

public class NoracleServiceTest {

	private static final String TEST_SPACE_ID = "123456";
	private static final String TEST_SPACE_NAME = "so many questions";

	protected int networkSize = 1;
	protected ArrayList<PastryNodeImpl> nodes;
	protected WebConnector connector;
	protected Client webClient;
	protected UserAgentImpl testAgent;
	protected String basicAuthHeader;
	protected String baseUrl;

	@Before
	public void beforeTest() {
		try {
			nodes = TestSuite.launchNetwork(networkSize);
			connector = new WebConnector(null);
			connector.start(nodes.get(0));
			// don't follow redirects, some tests want to test for correct redirect responses
			webClient = ClientBuilder.newBuilder().register(MultiPartFeature.class)
					.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE).build();
			startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleService",
					NoracleService.API_VERSION + ".0");
			startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleAgentService",
					NoracleService.API_VERSION + ".0");
			startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleSpaceService",
					NoracleService.API_VERSION + ".0");
			startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleQuestionService",
					NoracleService.API_VERSION + ".0");
			startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleQuestionRelationService",
					NoracleService.API_VERSION + ".0");
			testAgent = MockAgentFactory.getAdam();
			testAgent.unlock("adamspass");
			PastryNodeImpl activeNode = nodes.get(0);
			activeNode.storeAgent(testAgent);
			basicAuthHeader = "basic " + Base64.getEncoder()
					.encodeToString((testAgent.getLoginName() + ":" + "adamspass").getBytes(StandardCharsets.UTF_8));
			baseUrl = connector.getHttpEndpoint() + "/" + NoracleService.RESOURCE_NAME + "/v"
					+ NoracleService.API_VERSION + ".0";
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	private void startService(Node node, String clsName, String version) throws Exception {
		ServiceAgentImpl serviceAgent = ServiceAgentImpl.createServiceAgent(new ServiceNameVersion(clsName, version),
				"testtest");
		serviceAgent.unlock("testtest");
		node.registerReceiver(serviceAgent);
	}

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
	public void testCreateSpaceNoLogin() {
		try {
			CreateSpacePojo body = new CreateSpacePojo();
			body.setSpaceId(TEST_SPACE_ID);
			body.setName(TEST_SPACE_NAME);
			WebTarget target = webClient.target(baseUrl + "/spaces");
			Response response = target.request().post(Entity.json(body));
			Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	protected void createTestSpace() throws Exception {
		// create test space
		CreateSpacePojo body = new CreateSpacePojo();
		body.setSpaceId(TEST_SPACE_ID);
		body.setName(TEST_SPACE_NAME);
		WebTarget target = webClient.target(baseUrl + "/spaces");
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response response = request.post(Entity.json(body));
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Space result = response.readEntity(Space.class);
		Assert.assertEquals(TEST_SPACE_ID, result.getSpaceId());
		Assert.assertEquals(TEST_SPACE_NAME, result.getName());
	}

	@Test
	public void testAutoSubscribe() {
		try {
			createTestSpace();
			// check if agent is auto-subscribed
			WebTarget target = webClient
					.target(baseUrl + "/agents/" + testAgent.getIdentifier() + "/spacesubscriptions");
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.get();
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			SpaceSubscriptionList result2 = response.readEntity(SpaceSubscriptionList.class);
			Assert.assertEquals(result2.size(), 1);
			Assert.assertEquals(TEST_SPACE_ID, result2.get(0).getSpaceId());
			Assert.assertEquals(TEST_SPACE_NAME, result2.get(0).getName());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testCreateQuestionNoLogin() {
		try {
			createTestSpace();
			// create question in space
			CreateQuestionPojo body = new CreateQuestionPojo();
			body.setQuestionText("How are you?");
			WebTarget target = webClient.target(baseUrl + "/spaces/" + TEST_SPACE_ID + "/questions");
			Response response = target.request().post(Entity.json(body));
			Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	protected String createTestQuestion() throws Exception {
		// create question in space
		CreateQuestionPojo body = new CreateQuestionPojo();
		body.setQuestionText("How are you?");
		WebTarget target = webClient.target(baseUrl + "/spaces/" + TEST_SPACE_ID + "/questions");
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response response = request.post(Entity.json(body));
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Question result = response.readEntity(Question.class);
		Assert.assertEquals(TEST_SPACE_ID, result.getSpaceId());
		Assert.assertEquals("How are you?", result.getText());
		Assert.assertEquals(result.getTimestampCreated(), result.getTimestampLastModified());
		return result.getQuestionId();
	}

	@Test
	public void testChangeQuestion() {
		try {
			createTestSpace();
			String questionId = createTestQuestion();
			ChangeQuestionPojo body = new ChangeQuestionPojo();
			body.setQuestionText("How much is the fish?");
			WebTarget target = webClient.target(baseUrl + "/spaces/" + TEST_SPACE_ID + "/questions/" + questionId);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.put(Entity.json(body));
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			Question result = response.readEntity(Question.class);
			Assert.assertEquals(TEST_SPACE_ID, result.getSpaceId());
			Assert.assertEquals(questionId, result.getQuestionId());
			Assert.assertEquals("How much is the fish?", result.getText());
			Assert.assertNotEquals(result.getTimestampCreated(), result.getTimestampLastModified());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testGetQuestions() {
		try {
			createTestSpace();
			String questionId1 = createTestQuestion();
			String questionId2 = createTestQuestion();
			String questionId3 = createTestQuestion();
			// no params at all
			WebTarget target = webClient.target(baseUrl + "/spaces/" + TEST_SPACE_ID + "/questions");
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.get();
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			GetQuestionsResponsePojo result = response.readEntity(GetQuestionsResponsePojo.class);
			QuestionList questionList = result.getContent();
			Assert.assertEquals(3, questionList.size());
			Assert.assertEquals(questionList.get(0).getQuestionId(), questionId1);
			Assert.assertEquals(questionList.get(1).getQuestionId(), questionId2);
			Assert.assertEquals(questionList.get(2).getQuestionId(), questionId3);
			ArrayList<LinkPojo> links = result.getLinks();
			Assert.assertEquals(1, links.size());
			Assert.assertEquals("next", links.get(0).getRel());
			Assert.assertEquals("", links.get(0).getHref());
			// inverse order
			target = webClient.target(baseUrl + "/spaces/" + TEST_SPACE_ID + "/questions");
			request = target.queryParam("order", "desc").queryParam("startat", "3").request();
			response = request.header(HttpHeaders.AUTHORIZATION, basicAuthHeader).get();
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			result = response.readEntity(GetQuestionsResponsePojo.class);
			questionList = result.getContent();
			Assert.assertEquals(3, questionList.size());
			Assert.assertEquals(questionList.get(0).getQuestionId(), questionId3);
			Assert.assertEquals(questionList.get(1).getQuestionId(), questionId2);
			Assert.assertEquals(questionList.get(2).getQuestionId(), questionId1);
			links = result.getLinks();
			Assert.assertEquals(1, links.size());
			Assert.assertEquals("next", links.get(0).getRel());
			Assert.assertEquals("?order=desc", links.get(0).getHref());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testQuestionRelations() {
		try {
			createTestSpace();
			String questionId1 = createTestQuestion();
			String questionId2 = createTestQuestion();
			CreateRelationPojo body = new CreateRelationPojo();
			body.setName("duplicate");
			body.setQuestionId1(questionId1);
			body.setQuestionId2(questionId2);
			WebTarget target = webClient.target(baseUrl + "/spaces/" + TEST_SPACE_ID + "/relations");
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.post(Entity.json(body));
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			QuestionRelation result = response.readEntity(QuestionRelation.class);
			Assert.assertEquals("duplicate", result.getName());
			Assert.assertEquals(questionId1, result.getFirstQuestionId());
			Assert.assertEquals(questionId2, result.getSecondQuestionId());
			Assert.assertEquals(false, result.isDirected());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

}
