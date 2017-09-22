package i5.las2peer.services.noracleService.api;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.services.noracleService.model.QuestionRelation;
import i5.las2peer.services.noracleService.model.QuestionRelationList;

public interface INoracleQuestionRelationService {

	public QuestionRelation createQuestionRelation(String spaceId, String name, String questionId1, String questionId2,
			Boolean directed) throws ServiceInvocationException;

	public QuestionRelation getQuestionRelation(String relationId) throws ServiceInvocationException;

	public QuestionRelationList getQuestionRelations(String spaceId, String order, Integer limit, Integer startAt)
			throws ServiceInvocationException;

	public QuestionRelation changeQuestionRelation(String relationId, String name, String questionId1,
			String questionId2, Boolean directed) throws ServiceInvocationException;

}
