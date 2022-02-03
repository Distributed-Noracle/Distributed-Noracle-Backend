package i5.las2peer.services.noracleService;

import i5.las2peer.api.Context;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.services.noracleService.model.BotResponse;
import i5.las2peer.services.noracleService.model.NoracleAgent;
import i5.las2peer.services.noracleService.resources.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Api
@SwaggerDefinition(
		info = @Info(
				title = "Noracle Service",
				version = NoracleService.API_VERSION,
				description = "A bundle service for the distributed Noracle system",
				license = @License(
						name = "BSD-3",
						url = "https://github.com/Distributed-Noracle/Distributed-Noracle-Backend/blob/master/LICENSE.txt")))
@ServicePath("/" + NoracleService.RESOURCE_NAME)
public class NoracleService extends RESTService {

	public static final String RESOURCE_NAME = "distributed-noracle";
	public static final String API_VERSION = "1.0.0";

	@Override
	protected void initResources() {
		getResourceConfig().register(this.getClass());
		getResourceConfig().register(SpacesResource.class);
		getResourceConfig().register(QuestionsResource.class);
		getResourceConfig().register(QuestionRelationsResource.class);
		getResourceConfig().register(AgentsResource.class);
	}

	@Path("/" + SpacesResource.RESOURCE_NAME)
	public SpacesResource spaces() {
		return new SpacesResource();
	}

	@Path("/" + AgentsResource.RESOURCE_NAME + "/{agentid}")
	public AgentsResource agents() {
		return new AgentsResource();
	}

	@Path("/" + RecommenderResource.RESOURCE_NAME)
	public RecommenderResource recommendations() {
		return new RecommenderResource();
	}

	@GET
	@Path("/version")
	public String version() {

		return this.API_VERSION;
	}

	@GET
	@Path("/versionForBot")
	@Produces(MediaType.APPLICATION_JSON)
	public BotResponse versionForBot() {
		return new BotResponse("The version of the Distributed Noracle application is " + this.API_VERSION, true);
	}

	@GET
	@Path("/mainAgent")
	@Produces(MediaType.APPLICATION_JSON)
	public NoracleAgent getMainAgentId() {
		String agentid = Context.get().getMainAgent().getIdentifier();
		return new NoracleAgent(agentid);
	}

}
