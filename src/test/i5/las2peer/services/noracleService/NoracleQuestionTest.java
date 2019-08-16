package i5.las2peer.services.noracleService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.tuple.Pair;
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
					.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/" + testAgent_adam.getIdentifier() + "/"
							+ AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME + "/" + testSpaceId + "/selectedQuestions");
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam);
			Response response = request.put(Entity.json(body));
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			SpaceSubscription result = response.readEntity(SpaceSubscription.class);
			Assert.assertEquals(testSpaceId, result.getSpaceId());
			Assert.assertEquals(1, result.getSelectedQuestionIds().length);
			Assert.assertArrayEquals(new String[] { "1234" }, result.getSelectedQuestionIds());
			// check if subscription is updated
			target = webClient.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/" + testAgent_adam.getIdentifier()
					+ "/" + AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME);
			request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam);
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
			Response response = postQuestionCreation(testSpaceId, false);
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
			Response response = postQuestionCreation(testSpaceId);
			Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
			// check if non space member has read permission
			String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
			WebTarget targetQuestion = webClient.target(locationHeader);
			Builder requestQuestion = targetQuestion.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_eve);
			Response responseQuestion = requestQuestion.get();
			Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), responseQuestion.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testQuestionNonExistentSpace() {
		final String spaceId = "xxxxx";
		try {
			Response response = postQuestionCreation(spaceId);
			Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testChangeQuestion() {
		String newQuestionText = "How much is the fish?";
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			String questionId = createTestQuestion(testSpaceId);
			Response response = postQuestionUpdate(newQuestionText, testSpaceId, questionId);			
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			
			Question result = response.readEntity(Question.class);
			Assert.assertEquals(testSpaceId, result.getSpaceId());
			Assert.assertEquals(questionId, result.getQuestionId());
			Assert.assertEquals(newQuestionText, result.getText());
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
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam);
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
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}
	
	@Test
	public void testGetQuestionsInverseOrder() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			String questionId1 = createTestQuestion(testSpaceId);
			String questionId2 = createTestQuestion(testSpaceId);
			String questionId3 = createTestQuestion(testSpaceId);
			
//			WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
//					+ QuestionsResource.RESOURCE_NAME);
//			Builder request = target.queryParam("order", "desc").queryParam("startat", "3").request();
//			Response response = request.header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam).get();
			
			@SuppressWarnings("unchecked")
			Response response = getAllQuestions(testSpaceId, Pair.of("order", "desc"), Pair.of("startat", "3"));
			
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			
			VotedQuestionList questionList = response.readEntity(VotedQuestionList.class);
			Assert.assertEquals(3, questionList.size());
			Assert.assertEquals(questionList.get(0).getQuestionId(), questionId3);
			Assert.assertEquals(questionList.get(1).getQuestionId(), questionId2);
			Assert.assertEquals(questionList.get(2).getQuestionId(), questionId1);
			
			String linkHeader = response.getHeaderString(HttpHeaders.LINK);
			Assert.assertNotNull(linkHeader);
			Assert.assertEquals("<?order=desc>; rel=\"next\"", linkHeader);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Response getAllQuestions(String spaceId, Pair<String, String>...pairs) {
		WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + spaceId + "/"
				+ QuestionsResource.RESOURCE_NAME);
		
		for(Pair<String, String> pair : pairs) {
			target = target.queryParam(pair.getLeft(), pair.getRight());
		}
		
		Builder request = target.request();
		return request.header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam).get();		
	}

}
