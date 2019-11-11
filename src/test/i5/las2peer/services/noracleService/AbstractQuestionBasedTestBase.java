package i5.las2peer.services.noracleService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;

import i5.las2peer.services.noracleService.model.Question;
import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.pojo.ChangeQuestionPojo;
import i5.las2peer.services.noracleService.pojo.CreateQuestionPojo;
import i5.las2peer.services.noracleService.resources.QuestionsResource;
import i5.las2peer.services.noracleService.resources.SpacesResource;

public abstract class AbstractQuestionBasedTestBase extends AbstractNoracleServiceTestBase {

	protected static final String TEST_QUESTION_TEXT = "How are you?";

	/**
	 * Creates a new {@link Question} by posting a creation request, fetching it
	 * from the network and asserting its integrity using Adams credentials.
	 *
	 * @param spaceId the {@link Space} in which test question should be asked
	 * @return the {@link Question Questions}-id
	 * @throws Exception multiple Exceptions that might occur
	 */
	protected String createTestQuestion(final String spaceId) throws Exception {
		return createTestQuestion(spaceId, basicAuthHeader_adam);
	}

	protected String createTestQuestion(final String spaceId, final String authenficiationHeader) throws Exception {
		return createTestQuestion(spaceId, authenficiationHeader, TEST_QUESTION_TEXT);
	}

	protected String createTestQuestion(final String spaceId, final String authenficiationHeader, final String text)
			throws Exception {
		// create question in space
		final Response response = postQuestionCreation(spaceId, authenficiationHeader, text);
		// assert no HTTP error
		Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());

		// fetch test question
		final Response responseQuestion = fetchQuestion(response);
		// assert no HTTP error
		Assert.assertEquals(Status.OK.getStatusCode(), responseQuestion.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, responseQuestion.getMediaType());

		// read question from response
		final Question question = responseQuestion.readEntity(Question.class);
		// assert that the question has the desired properties
		Assert.assertEquals(spaceId, question.getSpaceId());
		Assert.assertEquals(text, question.getText());
		Assert.assertEquals(question.getTimestampCreated(), question.getTimestampLastModified());

		return question.getQuestionId();
	}

	/**
	 * Gets the {@link Question} from the location defined in the given response
	 *
	 * @param response the response with the question's location
	 * @return the response of the GET-request
	 */
	private Response fetchQuestion(final Response response) {
		final String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
		final WebTarget targetQuestion = webClient.target(locationHeader);
		final Builder requestQuestion = targetQuestion.request().header(HttpHeaders.AUTHORIZATION,
				basicAuthHeader_adam);
		final Response responseQuestion = requestQuestion.get();
		return responseQuestion;
	}

	/**
	 * Posts a request to create a {@link Question} in a given space using adam's
	 * credentials
	 *
	 * @param spaceId the ID of the space
	 * @return the response to the POST-request
	 * @see #postQuestionCreation(String, boolean)
	 */
	protected Response postQuestionCreation(final String spaceId) {
		return postQuestionCreation(spaceId, true);
	}

	/**
	 * Posts a request to create a {@link Question} in a given space
	 *
	 * @param spaceId             the ID of the space
	 * @param useAuthentification indicate whether adam's credentials should be used
	 * @return the response to the POST-request
	 */
	protected Response postQuestionCreation(final String spaceId, final boolean useAuthentification) {
		return postQuestionCreation(spaceId, basicAuthHeader_adam);
	}

	protected Response postQuestionCreation(final String spaceId, final String authentification_header) {
		return postQuestionCreation(spaceId, authentification_header, TEST_QUESTION_TEXT);
	}

	protected Response postQuestionCreation(final String spaceId, final String authentification_header,
			final String text) {
		final CreateQuestionPojo body = new CreateQuestionPojo();
		body.setText(text);
		final WebTarget target = webClient.target(
				baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + spaceId + "/" + QuestionsResource.RESOURCE_NAME);
		final Builder request = target.request();
		if (authentification_header != null)
			request.header(HttpHeaders.AUTHORIZATION, authentification_header);
		final Response response = request.post(Entity.json(body));
		return response;
	}

	/**
	 * Changes the text of an already existing question
	 *
	 * @param newQuestionText    the new text of the question
	 * @param spaceId            the ID of the space
	 * @param existingQuestionId the Id of the existing question
	 * @return the response to the PUT-request
	 */
	protected Response postQuestionUpdate(final String newQuestionText, final String spaceId,
			final String existingQuestionId) {
		final ChangeQuestionPojo body = new ChangeQuestionPojo();
		body.setText(newQuestionText);
		final WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + spaceId + "/"
				+ QuestionsResource.RESOURCE_NAME + "/" + existingQuestionId);
		final Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam);
		return request.put(Entity.json(body));
	}

}
