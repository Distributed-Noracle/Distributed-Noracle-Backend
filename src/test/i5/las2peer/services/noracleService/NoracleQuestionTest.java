package i5.las2peer.services.noracleService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Test;

import i5.las2peer.services.noracleService.model.Question;
import i5.las2peer.services.noracleService.model.SpaceSubscription;
import i5.las2peer.services.noracleService.model.SpaceSubscriptionList;
import i5.las2peer.services.noracleService.model.VotedQuestionList;
import i5.las2peer.services.noracleService.pojo.ChangeQuestionPojo;
import i5.las2peer.services.noracleService.pojo.CreateQuestionPojo;
import i5.las2peer.services.noracleService.pojo.UpdateSelectedQuestionsPojo;
import i5.las2peer.services.noracleService.resources.AgentsResource;
import i5.las2peer.services.noracleService.resources.QuestionsResource;
import i5.las2peer.services.noracleService.resources.SpacesResource;

public class NoracleQuestionTest extends AbstractQuestionBasedTestBase {

	@Test
	public void testUpdateSelectedQuestions() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			// update selected questions
			UpdateSelectedQuestionsPojo body = new UpdateSelectedQuestionsPojo();
			body.setSelectedQuestions(new String[] { "1234" });
			WebTarget target = webClient
					.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/" + testAgent.getIdentifier() + "/"
							+ AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME + "/" + testSpaceId + "/selectedQuestions");
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.put(Entity.json(body));
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			SpaceSubscription result = response.readEntity(SpaceSubscription.class);
			Assert.assertEquals(testSpaceId, result.getSpaceId());
			Assert.assertEquals(1, result.getSelectedQuestionIds().length);
			Assert.assertArrayEquals(new String[] { "1234" }, result.getSelectedQuestionIds());
			// check if subscription is updated
			target = webClient.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/" + testAgent.getIdentifier()
					+ "/" + AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME);
			request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			response = request.get();
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			SpaceSubscriptionList result2 = response.readEntity(SpaceSubscriptionList.class);
			Assert.assertEquals(result2.size(), 1);
			Assert.assertEquals(testSpaceId, result2.get(0).getSpaceId());
			Assert.assertEquals(1, result2.get(0).getSelectedQuestionIds().length);
			Assert.assertArrayEquals(new String[] { "1234" }, result2.get(0).getSelectedQuestionIds());
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
			body.setText(TEST_QUESTION_TEXT);
			WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME);
			Response response = target.request().post(Entity.json(body));
			Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testQuestionReadPermission() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			// create question in space
			CreateQuestionPojo body = new CreateQuestionPojo();
			body.setText(TEST_QUESTION_TEXT);
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
			body.setText(TEST_QUESTION_TEXT);
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
			body.setText("How much is the fish?");
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
			VotedQuestionList questionList = response.readEntity(VotedQuestionList.class);
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
			questionList = response.readEntity(VotedQuestionList.class);
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

}
