package i5.las2peer.services.noracleService.resources;

import java.io.Serializable;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import i5.las2peer.api.Context;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.services.noracleService.NoracleQuestionService;
import i5.las2peer.services.noracleService.NoracleService;
import i5.las2peer.services.noracleService.api.ICrowdsourcingCompletionService;
import i5.las2peer.services.noracleService.model.Question;
import i5.las2peer.services.noracleService.model.QuestionList;
import i5.las2peer.services.noracleService.model.Space;
import io.swagger.annotations.Api;

@Api(tags = { CrowdsourcingCompletionResource.RESOURCE_NAME })
public class CrowdsourcingCompletionResource implements ICrowdsourcingCompletionService {

	public static final String RESOURCE_NAME = "crowdsourcing";
	public static final String NUMBER_OF_QUESTIONS_RESOURCE_NAME = "questionsOfMainAgent";

	@Override
	@GET
	@Path("/" + NUMBER_OF_QUESTIONS_RESOURCE_NAME + "/{spaceid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNumberOfQuestionsByMainAgentInSpace(@PathParam("spaceid") final String spaceId,
			@QueryParam("email") final String email) throws Exception {
		final Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), NoracleService.API_VERSION),
				"getQuestions", spaceId, null, 1000, null);
		if (!(rmiResult instanceof QuestionList))
			return Response.serverError().build();

		final QuestionList questions = (QuestionList) rmiResult;
		System.err.println("Questions: " + questions);

		final QuestionList matchingQuestions = getQuestionsForAgent(questions, email);
		System.err.println("Matching Questions: " + matchingQuestions);

		final QuestionList acceptableQuestions = new QuestionList();
		acceptableQuestions.addAll(matchingQuestions//
				.stream()//
				.filter(q -> isQuestionAcceptable(q))//
				.collect(Collectors.toList()));
		System.err.println("Acceptable Questions: " + acceptableQuestions);

		return Response.ok().entity(acceptableQuestions).build();
	}

	/**
	 * The filter that indicated whether a question is valid in some sense.
	 *
	 * @param question the {@link Question}
	 * @return <code>true</code> if the question is accepted
	 */
	private boolean isQuestionAcceptable(final Question question) {
		boolean bool = true;
		final String text = question.getText();
		bool = bool && text != null;
		final int textLength = text.length();
		bool = bool && textLength > 5;
		bool = bool && textLength <= 100;
		bool = bool && text.contains("?");

		return bool;
	}

	/**
	 * Filter the question from a {@link Space} which belong to the main agent.
	 *
	 * @param questions the list of all question in a {@link Space}
	 * @param email
	 * @return the {@link Question questions} from the main agent
	 */
	private QuestionList getQuestionsForAgent(final QuestionList questions, final String email) throws Exception {
		final String identifier = Context.get().getUserAgentIdentifierByEmail(email);
//		final String identifier = Context.getCurrent().getMainAgent().getIdentifier();
		final QuestionList matchingQuestions = new QuestionList();

		for (final Question question : questions)
			if (StringUtils.equals(question.getAuthorId(), identifier))
				matchingQuestions.add(question);

		return matchingQuestions;
	}

}
