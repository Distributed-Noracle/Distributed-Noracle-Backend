package i5.las2peer.services.noracleService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;

import i5.las2peer.services.noracleService.model.Question;
import i5.las2peer.services.noracleService.pojo.CreateQuestionPojo;
import i5.las2peer.services.noracleService.resources.QuestionsResource;
import i5.las2peer.services.noracleService.resources.SpacesResource;

public abstract class AbstractQuestionBasedTestBase extends AbstractNoracleServiceTestBase {

	protected String createTestQuestion(String testSpaceId) throws Exception {
		// create question in space
		CreateQuestionPojo body = new CreateQuestionPojo();
		body.setText(TEST_QUESTION_TEXT);
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

}
