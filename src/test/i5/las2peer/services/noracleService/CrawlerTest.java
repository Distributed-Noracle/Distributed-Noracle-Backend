package i5.las2peer.services.noracleService;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Test;

import i5.las2peer.services.noracleService.resources.CrawlerResource;

public class CrawlerTest extends AbstractNoracleServiceTestBase {

	@Test
	public void checkService() {
//		System.out.println("BEFORE");
//		NoracleObserverCrawlerService.go();
//		System.out.println("AFTER");
		final WebTarget target = webClient
				.target(baseUrl + "/" + CrawlerResource.RESOURCE_NAME + "/" + CrawlerResource.CHECK_SERVICE);
		final Builder request = target.request();
		@SuppressWarnings("unused")
		final Response response = request.get();
	}

	@Override
	protected void startServices() throws Exception {
		super.startServices();
		startService(nodes.get(0), "i5.las2peer.services.noracleService.NoracleObserverCrawlerService",
				NoracleService.API_VERSION + ".0");
	}

}
