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

import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.services.noracleService.model.NoracleAgentProfile;
import i5.las2peer.services.noracleService.model.SpaceSubscriptionList;
import i5.las2peer.services.noracleService.pojo.ChangeProfilePojo;
import i5.las2peer.services.noracleService.resources.AgentsResource;

public class NoracleAgentTest extends AbstractNoracleServiceTestBase {

	@Test
	public void testAutoSubscribe() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			// check if agent is auto-subscribed
			WebTarget target = webClient.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/"
					+ testAgent_adam.getIdentifier() + "/" + AgentsResource.SUBSCRIPTIONS_RESOURCE_NAME);
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam);
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
			Response response = changeNameOfAdam(testName);
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
			Response response = retrieveNameOfAgent(testAgent_adam, basicAuthHeader_adam);
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
			Response response = retrieveNameOfAgent(testAgent_adam, basicAuthHeader_eve);
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
					.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/" + testAgent_eve.getIdentifier());
			Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam);
			Response response = request.get();
			Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
			Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	/**
	 * Get the current name of the given agent
	 *
	 * @param agent               the agent from which the name should be retrieved
	 * @param authorizationHeader the authorization header to be used
	 * @return the response to the GET-request
	 */
	private Response retrieveNameOfAgent(final UserAgentImpl agent, final String authorizationHeader) {
		WebTarget target = webClient.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/" + agent.getIdentifier());
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, authorizationHeader);
		Response response = request.get();
		return response;
	}

	/**
	 * Changes the name of Adam with his credentials
	 * 
	 * @param newName Adam's new name
	 * @return the response to the PUT-request
	 */
	private Response changeNameOfAdam(final String newName) {
		ChangeProfilePojo body = new ChangeProfilePojo();
		body.setAgentName(newName);
		WebTarget target = webClient
				.target(baseUrl + "/" + AgentsResource.RESOURCE_NAME + "/" + testAgent_adam.getIdentifier());
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam);
		Response response = request.put(Entity.json(body));
		return response;
	}
}
