package i5.las2peer.services.noracleService.resources;

import java.net.HttpURLConnection;

import javax.ws.rs.GET;

import i5.las2peer.api.Context;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.services.noracleService.NoraclePreprocessingService;
import i5.las2peer.services.noracleService.NoracleService;
import i5.las2peer.services.noracleService.api.INoraclePreprocessingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { PreprocessingResource.RESOURCE_NAME })
public class PreprocessingResource implements INoraclePreprocessingService {

	public static final String RESOURCE_NAME = "preprocessing";

	public static final String CHECK_SERVICE = "check-service";

	@Override
	@GET
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Connection established successfully") })
	public void checkStatus() throws ServiceInvocationException {
		System.err.println("TEST");
		Context.get().invoke(new ServiceNameVersion(NoraclePreprocessingService.class.getCanonicalName(),
				NoracleService.API_VERSION), "checkService", "");
	}
}
