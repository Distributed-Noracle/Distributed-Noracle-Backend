package i5.las2peer.services.noracleService;

import javax.ws.rs.Path;

import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.services.noracleQuestionService.NoracleQuestionService;
import i5.las2peer.services.noracleSpaceService.NoracleSpaceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

@Api(
		tags = { NoracleService.RESOURCE_NAME })
@SwaggerDefinition(
		info = @Info(
				title = "Noracle Service",
				version = NoracleService.API_VERSION,
				description = "A bundle service for the distributed Noracle system",
				license = @License(
						name = "BSD-3",
						url = "https://github.com/rwth-acis/Noracle-Bundle-Service/blob/master/LICENSE")))
@ServicePath("/" + NoracleService.RESOURCE_NAME)
@Path("/" + NoracleService.RESOURCE_NAME)
public class NoracleService extends RESTService {

	public static final String RESOURCE_NAME = "distributed-noracle";
	public static final String API_VERSION = "0.1";

	@Override
	protected void initResources() {
		getResourceConfig().register(NoracleSpaceService.class);
		getResourceConfig().register(NoracleQuestionService.class);
	}

}
