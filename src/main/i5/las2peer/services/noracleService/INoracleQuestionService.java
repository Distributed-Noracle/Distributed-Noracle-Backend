package i5.las2peer.services.noracleService;

import i5.las2peer.api.execution.ServiceInvocationException;

public interface INoracleQuestionService {

	public Question createQuestion(String questionSpaceId, String questionText) throws ServiceInvocationException;

	public Question getQuestion(String questionId) throws ServiceInvocationException;

	public QuestionList getQuestions(String spaceId, String order, Integer limit, Integer startAt)
			throws ServiceInvocationException;

	public Question changeQuestionText(String questionId, String questionText) throws ServiceInvocationException;

}
