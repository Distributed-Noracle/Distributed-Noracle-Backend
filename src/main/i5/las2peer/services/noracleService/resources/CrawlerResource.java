package i5.las2peer.services.noracleService.resources;

import java.net.HttpURLConnection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import i5.las2peer.api.Context;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.services.noracleService.NoracleObserverCrawlerService;
import i5.las2peer.services.noracleService.NoracleService;
import i5.las2peer.services.noracleService.api.ICrawlerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CrawlerResource.RESOURCE_NAME })
public class CrawlerResource implements ICrawlerService {

	public final static String RESOURCE_NAME = "crawler";
	public static final String CHECK_SERVICE = "checkservice";

	@Override
	@GET
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Connection established successfully") })
	@Path("/" + CrawlerResource.CHECK_SERVICE)
	public boolean checkService() {
		try {
			Context.get().invoke(new ServiceNameVersion(NoracleObserverCrawlerService.class.getCanonicalName(),
					NoracleService.API_VERSION), "checkService");
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}
