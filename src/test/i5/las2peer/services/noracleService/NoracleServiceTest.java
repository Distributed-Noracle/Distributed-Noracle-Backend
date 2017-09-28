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
import i5.las2peer.services.noracleService.model.QuestionRelationList;
import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.model.SpaceSubscriptionList;
import i5.las2peer.services.noracleService.model.Vote;
import i5.las2peer.services.noracleService.model.VoteList;
import i5.las2peer.services.noracleService.pojo.ChangeQuestionPojo;
import i5.las2peer.services.noracleService.pojo.CreateQuestionPojo;
import i5.las2peer.services.noracleService.pojo.CreateRelationPojo;
import i5.las2peer.services.noracleService.pojo.CreateSpacePojo;
import i5.las2peer.services.noracleService.pojo.SetVotePojo;
import i5.las2peer.services.noracleService.pojo.SubscribeSpacePojo;
import i5.las2peer.services.noracleService.resources.AgentsResource;
import i5.las2peer.services.noracleService.resources.QuestionRelationsResource;
import i5.las2peer.services.noracleService.resources.QuestionsResource;
import i5.las2peer.services.noracleService.resources.SpacesResource;
import i5.las2peer.services.noracleService.resources.VotesResource;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.testing.TestSuite;

public class NoracleServiceTest {

	private static final String TEST_SPACE_NAME = "so many questions";
	private static final String TEST_QUESTION_TEXT = "How are you?";

	protected int networkSize = 1;
	protected ArrayList<PastryNodeImpl> nodes;
	protected WebConnector connector;
	protected Client webClient;
	protected UserAgentImpl testAgent;
	protected UserAgentImpl testAgent2;
	protected String basicAuthHeader;
	protected String basicAuthHeader2;
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
			startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleVoteService",
					NoracleService.API_VERSION + ".0");
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
			body.setName(TEST_SPACE_NAME);
			WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME);
			Response response = target.request().post(Entity.json(body));
			Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	protected Space createAndFetchTestSpace() throws Exception {
		// create test space
		CreateSpacePojo body = new CreateSpacePojo();
		body.setName(TEST_SPACE_NAME);
		WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME);
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response response = request.post(Entity.json(body));
		Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
		// fetch test space
		String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
		WebTarget targetSpace = webClient.target(locationHeader);
		Builder requestSpace = targetSpace.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response responseSpace = requestSpace.get();
		Assert.assertEquals(Status.OK.getStatusCode(), responseSpace.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, responseSpace.getMediaType());
		Space space = responseSpace.readEntity(Space.class);
		return space;
	}

	@Test
	public void testSpaceReadPermission() {
		try {
			// create test space with first test user
			CreateSpacePojo body = new CreateSpacePojo();
			body.setName(TEST_SPACE_NAME);
			WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.post(Entity.json(body));
			Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
			// check if non space member has read permission
			String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
			WebTarget targetSpace = webClient.target(locationHeader);
			Builder requestSpace = targetSpace.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader2);
			Response responseSpace = requestSpace.get();
			Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), responseSpace.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testAutoSubscribe() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			// check if agent is auto-subscribed
			WebTarget target = webClient.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/"
					+ testAgent.getIdentifier() + "/" + AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.get();
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			SpaceSubscriptionList result2 = response.readEntity(SpaceSubscriptionList.class);
			Assert.assertEquals(result2.size(), 1);
			Assert.assertEquals(testSpaceId, result2.get(0).getSpaceId());
			Assert.assertEquals(TEST_SPACE_NAME, result2.get(0).getName());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testSpaceForeignSubscribeNoSecret() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			SubscribeSpacePojo body = new SubscribeSpacePojo();
			body.setSpaceId(testSpaceId);
			WebTarget target = webClient.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/"
					+ testAgent2.getIdentifier() + "/" + AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader2);
			Response response = request.post(Entity.json(body));
			Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testSpaceForeignSubscribeIncorrectSecret() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			SubscribeSpacePojo body = new SubscribeSpacePojo();
			body.setSpaceId(testSpaceId);
			body.setSpaceSecret("xxx");
			WebTarget target = webClient.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/"
					+ testAgent2.getIdentifier() + "/" + AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader2);
			Response response = request.post(Entity.json(body));
			Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testSpaceForeignSubscribe() {
		try {
			Space testSpace = createAndFetchTestSpace();
			SubscribeSpacePojo body = new SubscribeSpacePojo();
			body.setSpaceId(testSpace.getSpaceId());
			body.setSpaceSecret(testSpace.getSpaceSecret());
			WebTarget target = webClient.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/"
					+ testAgent2.getIdentifier() + "/" + AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader2);
			Response response = request.post(Entity.json(body));
			Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testCreateQuestionNoLogin() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			// create question in space
			CreateQuestionPojo body = new CreateQuestionPojo();
			body.setQuestionText(TEST_QUESTION_TEXT);
			WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME);
			Response response = target.request().post(Entity.json(body));
			Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	protected String createTestQuestion(String testSpaceId) throws Exception {
		// create question in space
		CreateQuestionPojo body = new CreateQuestionPojo();
		body.setQuestionText(TEST_QUESTION_TEXT);
		WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
				+ QuestionsResource.RESOURCE_NAME);
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response response = request.post(Entity.json(body));
		Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
		// fetch test question
		String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
		WebTarget targetQuestion = webClient.target(locationHeader);
		Builder requestQuestion = targetQuestion.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response responseQuestion = requestQuestion.get();
		Assert.assertEquals(Status.OK.getStatusCode(), responseQuestion.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, responseQuestion.getMediaType());
		Question question = responseQuestion.readEntity(Question.class);
		Assert.assertEquals(testSpaceId, question.getSpaceId());
		Assert.assertEquals(TEST_QUESTION_TEXT, question.getText());
		Assert.assertEquals(question.getTimestampCreated(), question.getTimestampLastModified());
		return question.getQuestionId();
	}

	@Test
	public void testQuestionReadPermission() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			// create question in space
			CreateQuestionPojo body = new CreateQuestionPojo();
			body.setQuestionText(TEST_QUESTION_TEXT);
			WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.post(Entity.json(body));
			Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
			// check if non space member has read permission
			String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
			WebTarget targetQuestion = webClient.target(locationHeader);
			Builder requestQuestion = targetQuestion.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader2);
			Response responseQuestion = requestQuestion.get();
			Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), responseQuestion.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testQuestionNonExistentSpace() {
		try {
			CreateQuestionPojo body = new CreateQuestionPojo();
			body.setQuestionText(TEST_QUESTION_TEXT);
			WebTarget target = webClient
					.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/xxxxx/" + QuestionsResource.RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.post(Entity.json(body));
			Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testChangeQuestion() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			String questionId = createTestQuestion(testSpaceId);
			ChangeQuestionPojo body = new ChangeQuestionPojo();
			body.setQuestionText("How much is the fish?");
			WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.put(Entity.json(body));
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			Question result = response.readEntity(Question.class);
			Assert.assertEquals(testSpaceId, result.getSpaceId());
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
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			String questionId1 = createTestQuestion(testSpaceId);
			String questionId2 = createTestQuestion(testSpaceId);
			String questionId3 = createTestQuestion(testSpaceId);
			// no params at all
			WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.get();
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			QuestionList questionList = response.readEntity(QuestionList.class);
			Assert.assertEquals(3, questionList.size());
			Assert.assertEquals(questionList.get(0).getQuestionId(), questionId1);
			Assert.assertEquals(questionList.get(1).getQuestionId(), questionId2);
			Assert.assertEquals(questionList.get(2).getQuestionId(), questionId3);
			String linkHeader = response.getHeaderString(HttpHeaders.LINK);
			Assert.assertNull(linkHeader);
			// inverse order
			target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME);
			request = target.queryParam("order", "desc").queryParam("startat", "3").request();
			response = request.header(HttpHeaders.AUTHORIZATION, basicAuthHeader).get();
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			questionList = response.readEntity(QuestionList.class);
			Assert.assertEquals(3, questionList.size());
			Assert.assertEquals(questionList.get(0).getQuestionId(), questionId3);
			Assert.assertEquals(questionList.get(1).getQuestionId(), questionId2);
			Assert.assertEquals(questionList.get(2).getQuestionId(), questionId1);
			linkHeader = response.getHeaderString(HttpHeaders.LINK);
			Assert.assertNotNull(linkHeader);
			Assert.assertEquals("<?order=desc>; rel=\"next\"", linkHeader);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	protected QuestionRelationList createTestQuestionRelation(String spaceId, String questionId1, String questionId2) {
		// create test question relation
		CreateRelationPojo body = new CreateRelationPojo();
		body.setName("duplicate");
		body.setQuestionId1(questionId1);
		body.setQuestionId2(questionId2);
		WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + spaceId + "/"
				+ QuestionRelationsResource.RESOURCE_NAME);
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response response = request.post(Entity.json(body));
		Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
		// fetch test question relation
		String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
		WebTarget targetQuestionRelation = webClient.target(locationHeader);
		Builder requestQuestionRelation = targetQuestionRelation.request().header(HttpHeaders.AUTHORIZATION,
				basicAuthHeader);
		Response responseQuestionRelation = requestQuestionRelation.get();
		Assert.assertEquals(Status.OK.getStatusCode(), responseQuestionRelation.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, responseQuestionRelation.getMediaType());
		return responseQuestionRelation.readEntity(QuestionRelationList.class);
	}

	@Test
	public void testQuestionRelations() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			String questionId1 = createTestQuestion(testSpaceId);
			String questionId2 = createTestQuestion(testSpaceId);
			QuestionRelationList questionRelationList = createTestQuestionRelation(testSpaceId, questionId1,
					questionId2);
			Assert.assertEquals(1, questionRelationList.size());
			QuestionRelation questionRelation = questionRelationList.get(0);
			Assert.assertEquals("duplicate", questionRelation.getName());
			Assert.assertEquals(questionId1, questionRelation.getFirstQuestionId());
			Assert.assertEquals(questionId2, questionRelation.getSecondQuestionId());
			Assert.assertEquals(false, questionRelation.isDirected());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testVotes() {
		try {
			// create space, question, relation
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			String questionId1 = createTestQuestion(testSpaceId);
			String questionId2 = createTestQuestion(testSpaceId);
			String relationId = createTestQuestionRelation(testSpaceId, questionId1, questionId2).get(0)
					.getRelationId();
			// test get votes for all resources
			VoteList spaceVotes = getVotes(
					"/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(0, spaceVotes.size());
			VoteList question1Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId1 + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(0, question1Votes.size());
			VoteList question2Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId2 + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(0, question2Votes.size());
			VoteList relationVotes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(0, relationVotes.size());
			// vote for each with one agent
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + VotesResource.RESOURCE_NAME,
					basicAuthHeader, 3);
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + QuestionsResource.RESOURCE_NAME + "/"
					+ questionId1 + "/" + VotesResource.RESOURCE_NAME, basicAuthHeader, 3);
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + QuestionsResource.RESOURCE_NAME + "/"
					+ questionId2 + "/" + VotesResource.RESOURCE_NAME, basicAuthHeader, 3);
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/" + VotesResource.RESOURCE_NAME,
					basicAuthHeader, 3);
			// check my votes for each resource
			Vote spaceMyVote = getMyVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ VotesResource.RESOURCE_NAME + "/myvote");
			Assert.assertEquals(3, spaceMyVote.getValue());
			Vote question1MyVote = getMyVote(
					"/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + QuestionsResource.RESOURCE_NAME + "/"
							+ questionId1 + "/" + VotesResource.RESOURCE_NAME + "/myvote");
			Assert.assertEquals(3, question1MyVote.getValue());
			Vote question2MyVote = getMyVote(
					"/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + QuestionsResource.RESOURCE_NAME + "/"
							+ questionId2 + "/" + VotesResource.RESOURCE_NAME + "/myvote");
			Assert.assertEquals(3, question2MyVote.getValue());
			Vote relationMyVote = getMyVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/" + VotesResource.RESOURCE_NAME
					+ "/myvote");
			Assert.assertEquals(3, relationMyVote.getValue());
			// test get votes for all resources
			spaceVotes = getVotes(
					"/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(1, spaceVotes.size());
			Assert.assertEquals(1, spaceVotes.get(0).getValue());
			question1Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId1 + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(1, question1Votes.size());
			Assert.assertEquals(1, question1Votes.get(0).getValue());
			question2Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId2 + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(1, question2Votes.size());
			Assert.assertEquals(1, question2Votes.get(0).getValue());
			relationVotes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(1, relationVotes.size());
			Assert.assertEquals(1, relationVotes.get(0).getValue());
			// (down-)vote for each with another agent and check all votes
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + VotesResource.RESOURCE_NAME,
					basicAuthHeader2, -5);
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + QuestionsResource.RESOURCE_NAME + "/"
					+ questionId1 + "/" + VotesResource.RESOURCE_NAME, basicAuthHeader2, -5);
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + QuestionsResource.RESOURCE_NAME + "/"
					+ questionId2 + "/" + VotesResource.RESOURCE_NAME, basicAuthHeader2, -5);
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/" + VotesResource.RESOURCE_NAME,
					basicAuthHeader2, -5);
			// test get votes for all resources
			spaceVotes = getVotes(
					"/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(2, spaceVotes.size());
			Assert.assertEquals(1, spaceVotes.get(0).getValue());
			Assert.assertEquals(-1, spaceVotes.get(1).getValue());
			question1Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId1 + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(2, question1Votes.size());
			Assert.assertEquals(1, question1Votes.get(0).getValue());
			Assert.assertEquals(-1, question1Votes.get(1).getValue());
			question2Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId2 + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(2, question2Votes.size());
			Assert.assertEquals(1, question2Votes.get(0).getValue());
			Assert.assertEquals(-1, question2Votes.get(1).getValue());
			relationVotes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/" + VotesResource.RESOURCE_NAME);
			Assert.assertEquals(2, relationVotes.size());
			Assert.assertEquals(1, relationVotes.get(0).getValue());
			Assert.assertEquals(-1, relationVotes.get(1).getValue());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	private VoteList getVotes(String path) {
		WebTarget target = webClient.target(baseUrl + path);
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response response = request.get();
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		return response.readEntity(VoteList.class);
	}

	private void setVote(String path, String authHeader, int value) {
		SetVotePojo body = new SetVotePojo();
		body.setValue(value);
		WebTarget target = webClient.target(baseUrl + path);
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, authHeader);
		Response response = request.put(Entity.json(body));
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
	}

	private Vote getMyVote(String path) {
		WebTarget target = webClient.target(baseUrl + path);
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response response = request.get();
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		return response.readEntity(Vote.class);
	}

}
