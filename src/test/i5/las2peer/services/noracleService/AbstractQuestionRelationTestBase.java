package i5.las2peer.services.noracleService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;

import i5.las2peer.services.noracleService.model.QuestionRelation;
import i5.las2peer.services.noracleService.pojo.CreateRelationPojo;
import i5.las2peer.services.noracleService.resources.QuestionRelationsResource;
import i5.las2peer.services.noracleService.resources.SpacesResource;

public class AbstractQuestionRelationTestBase extends AbstractQuestionBasedTestBase {

	protected QuestionRelation createTestQuestionRelation(String spaceId, String firstQuestionId,
			String secondQuestionId) {
		// create test question relation
		CreateRelationPojo body = new CreateRelationPojo();
		body.setName("duplicate");
		body.setFirstQuestionId(firstQuestionId);
		body.setSecondQuestionId(secondQuestionId);
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
		return responseQuestionRelation.readEntity(QuestionRelation.class);
	}
}
