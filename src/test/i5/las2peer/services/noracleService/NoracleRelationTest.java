package i5.las2peer.services.noracleService;

import org.junit.Assert;
import org.junit.Test;

import i5.las2peer.services.noracleService.model.QuestionRelation;

public class NoracleRelationTest extends AbstractQuestionRelationTestBase {

	@Test
	public void testQuestionRelations() {
		try {
			String testSpaceId = createAndFetchTestSpace().getSpaceId();
			String questionId1 = createTestQuestion(testSpaceId);
			String questionId2 = createTestQuestion(testSpaceId);
			QuestionRelation questionRelation = createTestQuestionRelation(testSpaceId, questionId1, questionId2);
			// TODO: fetch and test list
			// Assert.assertEquals(1, questionRelationList.size());
			Assert.assertEquals(testAgent.getIdentifier(), questionRelation.getAuthorId());
			Assert.assertEquals("duplicate", questionRelation.getName());
			Assert.assertEquals(questionId1, questionRelation.getFirstQuestionId());
			Assert.assertEquals(questionId2, questionRelation.getSecondQuestionId());
			Assert.assertEquals(false, questionRelation.isDirected());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

}
