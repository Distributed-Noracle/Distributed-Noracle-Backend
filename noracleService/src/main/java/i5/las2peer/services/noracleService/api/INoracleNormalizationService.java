package i5.las2peer.services.noracleService.api;

import i5.las2peer.services.noracleService.model.VotedQuestion;
import i5.las2peer.services.noracleService.model.VotedQuestionList;

public interface INoracleNormalizationService {
    VotedQuestion normalizeQuestion(VotedQuestion question);

    VotedQuestionList normalizeQuestions(VotedQuestionList questionList);
}
