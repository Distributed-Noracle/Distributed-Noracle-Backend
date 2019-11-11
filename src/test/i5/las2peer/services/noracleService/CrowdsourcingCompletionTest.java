package i5.las2peer.services.noracleService;

import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import i5.las2peer.services.noracleService.model.Question;
import i5.las2peer.services.noracleService.model.QuestionList;
import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.resources.CrowdsourcingCompletionResource;

public class CrowdsourcingCompletionTest extends AbstractQuestionBasedTestBase {

	/**
	 * We create a space and retrieve all the questions from a specific user.
	 *
	 * @throws Exception
	 */
	@Test
	public void retrieveQuestionsOfAgentFromSpace() throws Exception {
		// create Space and subscribe adam & eve
		final Space space = createAndFetchTestSpace();
		subscribeAgentToSpace(space, testAgent_eve, basicAuthHeader_eve);

		// create questions within the space
		final ArrayList<Pair<String, Boolean>> questions = new ArrayList<Pair<String, Boolean>>();
		questions.add(Pair.of("Is this a valid test question?", Boolean.TRUE));
		questions.add(Pair.of("This does count, right?", Boolean.TRUE));
		questions.add(Pair.of("no, it doesn't!", Boolean.FALSE));
		questions.add(Pair.of("min", Boolean.FALSE));
		questions.add(Pair.of(
				"This is a test just to show that questions that are too long are not considered as acceptable, and to the best of my knowledge it works correct, doesn't it?",
				Boolean.FALSE));

		final HashMap<String, String> adamsQuestionPairs = createTestSpaceWithQuestions(space.getSpaceId(), questions);

		// request applicable question of adam
		final WebTarget target = webClient.target(baseUrl + "/" + CrowdsourcingCompletionResource.RESOURCE_NAME + "/"
				+ CrowdsourcingCompletionResource.NUMBER_OF_QUESTIONS_RESOURCE_NAME + "/" + space.getSpaceId());
		Builder request = target.request();
		request = request.header(HttpHeaders.AUTHORIZATION, basicAuthHeader_adam);
		final Response response = request.get();
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		final QuestionList applicableQuestions = response.readEntity(QuestionList.class);
		Assert.assertEquals(adamsQuestionPairs.size(), applicableQuestions.size());

		assertTexts(adamsQuestionPairs, applicableQuestions);
	}

	/**
	 * Check whether all the texts and ids of the retrieved questions are as
	 * expected.
	 *
	 * @param expectedOutput      a {@link HashMap} of id and text entries
	 * @param applicableQuestions the actual questions
	 */
	private void assertTexts(final HashMap<String, String> expectedOutput, final QuestionList applicableQuestions) {
		for (final Question question : applicableQuestions) {
			final String questionId = question.getQuestionId();
			final String expectedText = expectedOutput.get(questionId);
			Assert.assertNotNull("There is no question expected with ID " + questionId + " in " + expectedOutput,
					expectedText);
			Assert.assertEquals(expectedText, question.getText());
		}
	}

	/**
	 * Adds the texts given as new questions by Adam and additionally three from Eve
	 * to ensure, that they have to get filtered
	 *
	 * @param spaceId      The {@link Space} to enter the questions in
	 * @param texts        the texts of the new {@link Question Questions}
	 * @param isAcceptable defines whether the i-th question should be considered
	 *                     acceptable
	 * @return a HashMap of Id-Text entries
	 * @throws Exception an exception during question creation
	 */
	private HashMap<String, String> createTestSpaceWithQuestions(final String spaceId,
			final ArrayList<Pair<String, Boolean>> questions) throws Exception {

		final HashMap<String, String> acceptableQuestions = new HashMap<String, String>();

		for (final Pair<String, Boolean> entry : questions) {
			final String id = createTestQuestion(spaceId, basicAuthHeader_adam, entry.getLeft());
			if (entry.getRight())
				acceptableQuestions.put(id, entry.getLeft());
		}

		createTestQuestion(spaceId, basicAuthHeader_eve, "NOT APPLICABLE");
		createTestQuestion(spaceId, basicAuthHeader_eve, "NOT APPLICABLE");
		createTestQuestion(spaceId, basicAuthHeader_eve, "NOT APPLICABLE");

		return acceptableQuestions;
	}
}
