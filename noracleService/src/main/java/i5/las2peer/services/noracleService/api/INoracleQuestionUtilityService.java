package i5.las2peer.services.noracleService.api;

import i5.las2peer.services.noracleService.model.VotedQuestionList;

import java.util.HashMap;
import java.util.Map;

public interface INoracleQuestionUtilityService {
    // Key: QuestionId, Value: Utility
    HashMap<String, Double> computeUtilityForQuestions(String agentId, VotedQuestionList questions);

    // Map.Entry<String, Double> computeUtilityForQuestion(String agentId, Question question);
}
