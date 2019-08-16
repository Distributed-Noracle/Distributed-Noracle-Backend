package i5.las2peer.services.noracleService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Test;

import i5.las2peer.services.noracleService.model.Vote;
import i5.las2peer.services.noracleService.model.VoteList;
import i5.las2peer.services.noracleService.pojo.SetVotePojo;
import i5.las2peer.services.noracleService.resources.QuestionRelationsResource;
import i5.las2peer.services.noracleService.resources.QuestionVotesResource;
import i5.las2peer.services.noracleService.resources.QuestionsResource;
import i5.las2peer.services.noracleService.resources.SpacesResource;

public class NoracleVoteTest extends AbstractQuestionRelationTestBase {

	@Test
	public void testVotes() {
		try {
			// create space, question, relation
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			String questionId1 = createTestQuestion(testSpaceId);
			String questionId2 = createTestQuestion(testSpaceId);
			String relationId = createTestQuestionRelation(testSpaceId, questionId1, questionId2).getRelationId();
			// test get votes for all resources
			VoteList question1Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId1 + "/" + QuestionVotesResource.RESOURCE_NAME);
			Assert.assertEquals(0, question1Votes.size());
			VoteList question2Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId2 + "/" + QuestionVotesResource.RESOURCE_NAME);
			Assert.assertEquals(0, question2Votes.size());
			VoteList relationVotes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/"
					+ QuestionVotesResource.RESOURCE_NAME);
			Assert.assertEquals(0, relationVotes.size());
			// vote for each with one agent
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + QuestionsResource.RESOURCE_NAME + "/"
					+ questionId1 + "/" + QuestionVotesResource.RESOURCE_NAME + "/" + testAgent.getIdentifier(),
					basicAuthHeader, 3);
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + QuestionsResource.RESOURCE_NAME + "/"
					+ questionId2 + "/" + QuestionVotesResource.RESOURCE_NAME + "/" + testAgent.getIdentifier(),
					basicAuthHeader, 3);
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/"
					+ QuestionVotesResource.RESOURCE_NAME + "/" + testAgent.getIdentifier(), basicAuthHeader, 3);
			// check my votes for each resource
			Vote question1AgentVote = getAgentVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId1 + "/" + QuestionVotesResource.RESOURCE_NAME
					+ "/" + testAgent.getIdentifier());
			Assert.assertEquals(3, question1AgentVote.getValue());
			Vote question2AgentVote = getAgentVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId2 + "/" + QuestionVotesResource.RESOURCE_NAME
					+ "/" + testAgent.getIdentifier());
			Assert.assertEquals(3, question2AgentVote.getValue());
			Vote relationAgentVote = getAgentVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/"
					+ QuestionVotesResource.RESOURCE_NAME + "/" + testAgent.getIdentifier());
			Assert.assertEquals(3, relationAgentVote.getValue());
			// test get votes for all resources
			question1Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId1 + "/" + QuestionVotesResource.RESOURCE_NAME);
			Assert.assertEquals(1, question1Votes.size());
			Assert.assertEquals(1, question1Votes.get(0).getValue());
			question2Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId2 + "/" + QuestionVotesResource.RESOURCE_NAME);
			Assert.assertEquals(1, question2Votes.size());
			Assert.assertEquals(1, question2Votes.get(0).getValue());
			relationVotes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/"
					+ QuestionVotesResource.RESOURCE_NAME);
			Assert.assertEquals(1, relationVotes.size());
			Assert.assertEquals(1, relationVotes.get(0).getValue());
			// (down-)vote for each with another agent and check all votes
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + QuestionsResource.RESOURCE_NAME + "/"
					+ questionId1 + "/" + QuestionVotesResource.RESOURCE_NAME + "/" + testAgent2.getIdentifier(),
					basicAuthHeader2, -5);
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/" + QuestionsResource.RESOURCE_NAME + "/"
					+ questionId2 + "/" + QuestionVotesResource.RESOURCE_NAME + "/" + testAgent2.getIdentifier(),
					basicAuthHeader2, -5);
			setVote("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/"
					+ QuestionVotesResource.RESOURCE_NAME + "/" + testAgent2.getIdentifier(), basicAuthHeader2, -5);
			// test get votes for all resources
			question1Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId1 + "/" + QuestionVotesResource.RESOURCE_NAME);
			Assert.assertEquals(2, question1Votes.size());
			Assert.assertEquals(1, question1Votes.get(0).getValue());
			Assert.assertEquals(-1, question1Votes.get(1).getValue());
			question2Votes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionsResource.RESOURCE_NAME + "/" + questionId2 + "/" + QuestionVotesResource.RESOURCE_NAME);
			Assert.assertEquals(2, question2Votes.size());
			Assert.assertEquals(1, question2Votes.get(0).getValue());
			Assert.assertEquals(-1, question2Votes.get(1).getValue());
			relationVotes = getVotes("/" + SpacesResource.RESOURCE_NAME + "/" + testSpaceId + "/"
					+ QuestionRelationsResource.RESOURCE_NAME + "/" + relationId + "/"
					+ QuestionVotesResource.RESOURCE_NAME);
			Assert.assertEquals(2, relationVotes.size());
			Assert.assertEquals(1, relationVotes.get(0).getValue());
			Assert.assertEquals(-1, relationVotes.get(1).getValue());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	private VoteList getVotes(String path) {
		WebTarget target = webClient.target(baseUrl + path);
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response response = request.get();
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		return response.readEntity(VoteList.class);
	}

	private void setVote(String path, String authHeader, int value) {
		SetVotePojo body = new SetVotePojo();
		body.setValue(value);
		WebTarget target = webClient.target(baseUrl + path);
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, authHeader);
		Response response = request.put(Entity.json(body));
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
	}

	private Vote getAgentVote(String path) {
		WebTarget target = webClient.target(baseUrl + path);
		Builder request = target.request().header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		Response response = request.get();
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		return response.readEntity(Vote.class);
	}

}
