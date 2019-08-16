package i5.las2peer.services.noracleService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.p2p.Node;
import i5.las2peer.p2p.PastryNodeImpl;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.pojo.CreateSpacePojo;
import i5.las2peer.services.noracleService.resources.SpacesResource;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.testing.TestSuite;

public abstract class AbstractNoracleServiceTestBase {

	protected static final String TEST_SPACE_NAME = "so many questions";
	protected static final String TEST_QUESTION_TEXT = "How are you?";

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
			// don't follow redirects, some tests want to test for correct redirect
			// responses
			webClient = ClientBuilder.newBuilder().register(MultiPartFeature.class)
					.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE).build();
			// start the services
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
			startService(nodes.get(0), "i5.las2peer.services.noracleService." + TestService.class.getSimpleName(),
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
		node.storeAgent(serviceAgent);
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

	protected Space createAndFetchTestSpace() throws Exception {
		Response response = tryCreatingDefaultTestSpace(true);
		Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
		// fetch test space
		Builder requestSpace = requestSpaceWithAuthorizationHeader(response, basicAuthHeader);
		Response responseSpace = requestSpace.get();
		Assert.assertEquals(Status.OK.getStatusCode(), responseSpace.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, responseSpace.getMediaType());
		Space space = responseSpace.readEntity(Space.class);
		return space;
	}

	protected Response tryCreatingDefaultTestSpace(boolean withAuthorization) {
		// create test space with first test user
		CreateSpacePojo body = new CreateSpacePojo();
		body.setName(TEST_SPACE_NAME);
		WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME);
		Builder request = target.request();
		if (withAuthorization)
			request = request.header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		return request.post(Entity.json(body));
	}

	protected Builder requestSpaceWithAuthorizationHeader(Response response, String authorizationHeader) {
		String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
		WebTarget targetSpace = webClient.target(locationHeader);
		return targetSpace.request().header(HttpHeaders.AUTHORIZATION, authorizationHeader);
	}

}
