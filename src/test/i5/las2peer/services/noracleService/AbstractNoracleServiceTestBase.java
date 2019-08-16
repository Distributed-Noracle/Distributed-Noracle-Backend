package i5.las2peer.services.noracleService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
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
	
	protected int networkSize = 1;
	protected ArrayList<PastryNodeImpl> nodes;
	protected WebConnector connector;
	protected Client webClient;
	protected UserAgentImpl testAgent_adam;
	protected UserAgentImpl testAgent_eve;
	protected String basicAuthHeader_adam;
	protected String basicAuthHeader_eve;
	protected String baseUrl;

	@Before
	public void beforeTest() {
		try {
			nodes = TestSuite.launchNetwork(networkSize);
			connector = new WebConnector(null);
			PastryNodeImpl activeNode = nodes.get(0);
			connector.start(activeNode);

			// don't follow redirects, some tests want to test for correct redirect
			// responses
			webClient = ClientBuilder.newBuilder().register(MultiPartFeature.class)
					.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE).build();

			// start the services
			startServices();

			// define test agent: adam
			defineAdam(activeNode);

			// define test agent: eve
			defineEve(activeNode);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	/**
	 * Start the services used in the tests
	 * 
	 * @throws Exception multiple potential {@link Exception Exceptions} that may occur
	 */
	private void startServices() throws Exception {
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
	}

	
	/**
	 * Create the test agent 'eve' using the {@link MockAgentFactory}
	 * 
	 * @param activeNode the {@link Node} on which eve resides 
	 * @throws Exception multiple potential {@link Exception Exceptions} that may occur
	 */
	private void defineEve(PastryNodeImpl activeNode) throws Exception {
		testAgent_eve = MockAgentFactory.getEve();
		testAgent_eve.unlock("evespass");
		activeNode.storeAgent(testAgent_eve);
		basicAuthHeader_eve = "basic " + Base64.getEncoder()
				.encodeToString((testAgent_eve.getLoginName() + ":" + "evespass").getBytes(StandardCharsets.UTF_8));
		baseUrl = connector.getHttpEndpoint() + "/" + NoracleService.RESOURCE_NAME + "/v" + NoracleService.API_VERSION
				+ ".0";
	}

	/**
	 * Create the test agent 'adam' using the {@link MockAgentFactory}
	 * 
	 * @param activeNode the {@link Node} on which adam resides
	 * @throws Exception multiple potential {@link Exception Exceptions} that may occur
	 */
	private void defineAdam(PastryNodeImpl activeNode) throws Exception {
		testAgent_adam = MockAgentFactory.getAdam();
		testAgent_adam.unlock("adamspass");
		activeNode.storeAgent(testAgent_adam);
		basicAuthHeader_adam = "basic " + Base64.getEncoder()
				.encodeToString((testAgent_adam.getLoginName() + ":" + "adamspass").getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Start a Service on the given node
	 * 
	 * @param node the {@link Node} running the service
	 * @param clsName the Service's name
	 * @param version the Service's version 
	 * @throws Exception multiple potential {@link Exception Exceptions} that may occur 
	 */
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

	/**
	 * Creates a default {@link Space} using adam's credentials
	 * 
	 * @return the created {@link Space}
	 * @throws Exception multiple potential {@link Exception Exceptions} that may occur 
	 */
	protected Space createAndFetchTestSpace() throws Exception {
		Response response = postDefaultTestSpaceCreation(true);
		Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getMediaType());
		// fetch test space
		Response responseSpace = requestSpaceWithAuthorizationHeader(response, basicAuthHeader_adam);
		Assert.assertEquals(Status.OK.getStatusCode(), responseSpace.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, responseSpace.getMediaType());
		Space space = responseSpace.readEntity(Space.class);
		return space;
	}

	/**
	 * Posts a default {@link Space} creation call using the {@link #TEST_SPACE_NAME}, with optional credentials
	 * 
	 * @param withAuthorization indicated whether the credentials of adam should be used
	 * @return the {@link Response} to the creation message 
	 */
	protected Response postDefaultTestSpaceCreation(boolean withAuthorization) {
		// create test space with first test user
		CreateSpacePojo body = new CreateSpacePojo();
		body.setName(TEST_SPACE_NAME);
		WebTarget target = webClient.target(baseUrl + "/" + SpacesResource.RESOURCE_NAME);
		Builder request = target.request();
		if (withAuthorization)
			request = request.header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam);
		return request.post(Entity.json(body));
	}

	/**
	 * Requests a space using the given credentials
	 * 
	 * @param response the response to the HTTP request of the location
	 * @param authorizationHeader the credentials to be used
	 * @return the requests response
	 */
	protected Response requestSpaceWithAuthorizationHeader(Response response, String authorizationHeader) {
		String locationHeader = response.getHeaderString(HttpHeaders.LOCATION);
		WebTarget targetSpace = webClient.target(locationHeader);
		Builder requestSpace = targetSpace.request().header(HttpHeaders.AUTHORIZATION, authorizationHeader);
		return requestSpace.get();
	}
	
	// TODO: asserts

}
