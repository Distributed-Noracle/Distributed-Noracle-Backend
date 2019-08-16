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

import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.pojo.SubscribeSpacePojo;
import i5.las2peer.services.noracleService.resources.AgentsResource;

public class NoracleSpaceTest extends AbstractNoracleServiceTestBase {

	@Test
	public void testCreateSpaceNoLogin() {
		try {
			Response response = postDefaultTestSpaceCreation(false);
			Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testSpaceReadPermission() {
		try {
			Response response = postDefaultTestSpaceCreation(true);
			Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
			// check if non space member has read permission
			Response responseSpace = requestSpaceWithAuthorizationHeader(response, basicAuthHeader_eve);
			Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), responseSpace.getStatus());
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
					+ testAgent_eve.getIdentifier() + "/" + AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_eve);
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
					+ testAgent_eve.getIdentifier() + "/" + AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_eve);
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
					+ testAgent_eve.getIdentifier() + "/" + AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_eve);
			Response response = request.post(Entity.json(body));
			Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

}
