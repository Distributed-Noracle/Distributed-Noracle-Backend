package i5.las2peer.services.noracleService.api;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.services.noracleService.model.RecommenderQuestion;
import i5.las2peer.services.noracleService.model.RecommenderQuestionList;

import java.util.List;

public interface INoracleRecommenderService {
    RecommenderQuestionList getRecommendedQuestionsForSpace(String agentId, String spaceId) throws ServiceInvocationException;

    RecommenderQuestionList getRecommendedQuestions(String agentId) throws ServiceInvocationException;
}
