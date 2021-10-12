package i5.las2peer.services.noracleService.api;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.services.noracleService.model.Question;
import i5.las2peer.services.noracleService.model.QuestionList;

public interface INoracleQuestionService {

	Question createQuestion(String questionSpaceId, String text) throws ServiceInvocationException;

	Question getQuestion(String questionId) throws ServiceInvocationException;

	QuestionList getQuestions(String spaceId, String order, Integer limit, Integer startAt)
			throws ServiceInvocationException;

	Question changeQuestionText(String questionId, String text) throws ServiceInvocationException;

	Question changeQuestionDepth(String questionId, int depth) throws ServiceInvocationException;

}
