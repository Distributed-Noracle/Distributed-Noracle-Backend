package i5.las2peer.services.noracleService;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Test;

import i5.las2peer.services.noracleService.resources.PreprocessingResource;

public class NoraclePreprocessingTest extends AbstractNoracleServiceTestBase {

	@Test
	public void checkService() {
		final String URL = baseUrl + "/" + PreprocessingResource.RESOURCE_NAME + "/"
				+ PreprocessingResource.CHECK_SERVICE;
		System.err.println(URL);
		final WebTarget target = webClient.target(URL);
		final Response response = target.request().get();

		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}
}
