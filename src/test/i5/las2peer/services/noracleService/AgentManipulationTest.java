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

import i5.las2peer.services.noracleService.model.NoracleAgentProfile;
import i5.las2peer.services.noracleService.model.SpaceSubscriptionList;
import i5.las2peer.services.noracleService.pojo.ChangeProfilePojo;
import i5.las2peer.services.noracleService.resources.AgentsResource;

public class AgentManipulationTest extends AbstractNoracleServiceTestBase {

	@Test
	public void testAutoSubscribe() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			// check if agent is auto-subscribed
			WebTarget target = webClient.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/"
					+ testAgent.getIdentifier() + "/" + AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.get();
			// assert no HTTP error
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

			// read subscribers of the space
			SpaceSubscriptionList result = response.readEntity(SpaceSubscriptionList.class);
			// ensure that the newly created space has exactly the agents as subscriber
			Assert.assertEquals(result.size(), 1);
			Assert.assertEquals(testSpaceId, result.get(0).getSpaceId());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testAgentProfile() {
		final String testName = "Test Name";
		try {
			// first update the sample agent with a new name
			Response response = changeAgentName(testName);
			// assert no HTTP error
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

			// get the new value of the agent
			NoracleAgentProfile result = response.readEntity(NoracleAgentProfile.class);
			// assert successful change
			Assert.assertEquals(testName, result.getName());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
		try {
			// now retrieve it
			Response response = retrieveAgentName(basicAuthHeader);
			// assert no HTTP error
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			// assert successful change
			NoracleAgentProfile result = response.readEntity(NoracleAgentProfile.class);
			Assert.assertEquals(testName, result.getName());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
		try {
			// test to read it with another agent
			Response response = retrieveAgentName(basicAuthHeader2);
			// assert no HTTP error
			Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
			// assert successful change
			NoracleAgentProfile result = response.readEntity(NoracleAgentProfile.class);
			Assert.assertEquals(testName, result.getName());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void testAgentChangeNameOfAnonymousUser() {
		try {
			// now test a not yet created profile:
			WebTarget target = webClient
					.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/" + testAgent2.getIdentifier());
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
			Response response = request.get();
			Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	private Response retrieveAgentName(String authorizationHeader) {
		WebTarget target = webClient
				.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/" + testAgent.getIdentifier());
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, authorizationHeader);
		Response response = request.get();
		return response;
	}

	private Response changeAgentName(final String testName) {
		ChangeProfilePojo body = new ChangeProfilePojo();
		body.setAgentName(testName);
		WebTarget target = webClient
				.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/" + testAgent.getIdentifier());
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response response = request.put(Entity.json(body));
		return response;
	}
}
