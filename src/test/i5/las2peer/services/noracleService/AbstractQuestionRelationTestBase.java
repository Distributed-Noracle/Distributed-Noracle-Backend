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
import i5.las2peer.services.noracleService.model.QuestionRelation;
import i5.las2peer.services.noracleService.pojo.CreateRelationPojo;
import i5.las2peer.services.noracleService.resources.QuestionRelationsResource;
import i5.las2peer.services.noracleService.resources.SpacesResource;

public class AbstractQuestionRelationTestBase extends AbstractQuestionBasedTestBase {

	/**
	 * Create a {@link QuestionRelation} between two given {@link Question
	 * Questions} using adams credentials
	 *
	 * @param spaceId          the space of the two questions
	 * @param firstQuestionId  the first question's ID
	 * @param secondQuestionId the second question's ID
	 * @return the {@link QuestionRelation}
	 */
	protected QuestionRelation createTestQuestionRelation(final String spaceId, final String firstQuestionId,
			final String secondQuestionId) {
		// create test question relation
		final Response response = postQuestionRelationCreation(spaceId, firstQuestionId, secondQuestionId);
		Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
		// fetch test question relation
		final Response responseQuestionRelation = fetchQuestionRelation(response);
		Assert.assertEquals(Status.OK.getStatusCode(), responseQuestionRelation.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, responseQuestionRelation.getMediaType());
		return responseQuestionRelation.readEntity(QuestionRelation.class);
	}

	/**
	 * Gets the {@link QuestionRelation} from the location defined in the given
	 * response
	 *
	 * @param response the response with the {@link QuestionRelation
	 *                 QuestionRelations} location
	 * @return the response to the GET-request
	 */
	private Response fetchQuestionRelation(final Response response) {
		final String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
		final WebTarget targetQuestionRelation = webClient.target(locationHeader);
		final Builder requestQuestionRelation = targetQuestionRelation.request().header(HttpHeaders.AUTHORIZATION,
				basicAuthHeader_adam);
		final Response responseQuestionRelation = requestQuestionRelation.get();
		return responseQuestionRelation;
	}

	/**
	 * Posts a request to create a {@link QuestionRelation} for given questions in a
	 * given space
	 *
	 * @param spaceId          the space of the two questions
	 * @param firstQuestionId  the first question's ID
	 * @param secondQuestionId the second question's ID
	 * @return the response to the POST-request
	 */
	private Response postQuestionRelationCreation(final String spaceId, final String firstQuestionId,
			final String secondQuestionId) {
		final CreateRelationPojo body = new CreateRelationPojo();
		body.setName("duplicate");
		body.setFirstQuestionId(firstQuestionId);
		body.setSecondQuestionId(secondQuestionId);
		final WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME + "/" + spaceId + "/"
				+ QuestionRelationsResource.RESOURCE_NAME);
		final Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam);
		final Response response = request.post(Entity.json(body));
		return response;
	}
}
