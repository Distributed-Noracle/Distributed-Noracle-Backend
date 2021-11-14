package i5.las2peer.services.noracleService;

import i5.las2peer.api.Context;
import i5.las2peer.api.Service;
import i5.las2peer.api.execution.*;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.services.noracleService.api.INoracleQuestionUtilityService;
import i5.las2peer.services.noracleService.model.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import info.debatty.java.stringsimilarity.Cosine;

public class NoracleQuestionUtilityService extends Service implements INoracleQuestionUtilityService {

    // weights
    private final double cosineSimilarityWeight = 0.25;
    private final double voteSimilarityWeight = 0.25;
    private final double relativeVoteCountWeight = 0.5;

    private final Cosine cosine = new Cosine();

    @Override
    public HashMap<String, Double> computeUtilityForQuestions(String agentId, VotedQuestionList questions) {
        logger.info("NoracleQuestionUtilityService -> computeUtilityForQuestions(...)");
        HashMap<String, Double> utilityMap = new HashMap<>();
        double utility;
        for (VotedQuestion q : questions) {
            utility = 0.0;
            if (!q.getAuthorId().equals(agentId)) {
                // begin simple
                double cosineSimilarity = computeMaxCosineSimilarity(agentId, q);
                double voteSimilarity = computeMaxVoteSimilarity(agentId, q);
                double relativeVoteCount = computeRelativeVoteCount(questions, q);

                utility = cosineSimilarityWeight * cosineSimilarity
                        + voteSimilarityWeight * voteSimilarity
                        + relativeVoteCountWeight * relativeVoteCount;
            }

            utilityMap.put(q.getQuestionId(), utility);
        }
        return utilityMap;
    }

    private double computeMaxCosineSimilarity(String agentId, VotedQuestion question) {
        logger.info("NoracleQuestionUtilityService -> computeMaxCosineSimilarity(...)");
        double maxCosineSimilarity = 0.0;
        String spaceId = question.getSpaceId();
        try {
            Serializable rmiResult = Context.get().invoke(
                    new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), NoracleService.API_VERSION),
                    "getAllVotedQuestions", spaceId, agentId);

            VotedQuestionList userQuestions = (VotedQuestionList) rmiResult;
            for (VotedQuestion userQuestion : userQuestions) {
                double cosineSimilarity = cosine.similarity(userQuestion.getText(), question.getText());
                maxCosineSimilarity = Math.max(maxCosineSimilarity, cosineSimilarity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        logger.info("maxCosineSimilarity: " + maxCosineSimilarity);
        return maxCosineSimilarity;
    }

    private double computeMaxVoteSimilarity(String agentId, VotedQuestion question) {
        logger.info("NoracleQuestionUtilityService -> computeMaxVoteSimilarity(...)");
        double maxVoteSimilarity = 0.0;
        String spaceId = question.getSpaceId();
        try {
            Serializable rmiResult = Context.get().invoke(
                    new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), NoracleService.API_VERSION),
                    "getAllVotedQuestions", spaceId, agentId);

            VotedQuestionList userQuestions = (VotedQuestionList) rmiResult;
            for (VotedQuestion userQuestion : userQuestions) {
                double voteSimilarity = computeVoteSimilarity(userQuestion, question);
                maxVoteSimilarity = Math.max(maxVoteSimilarity, voteSimilarity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        logger.info("maxVoteSimilarity: " + maxVoteSimilarity);
        return maxVoteSimilarity;
    }

    private double computeVoteSimilarity(VotedQuestion q1, VotedQuestion q2) {
        VoteList v1 = q1.getVotes();
        VoteList v2 = q2.getVotes();
        Map<String, Integer> noOfUsers = new HashMap<>();

        for (Vote v : v1) {
            noOfUsers.put(v.getVoterAgentId(), 1);
        }

        for (Vote v : v2) {
            if (noOfUsers.containsKey(v.getVoterAgentId())) {
                noOfUsers.put(v.getVoterAgentId(), 2);
            } else {
                noOfUsers.put(v.getVoterAgentId(), 1);
            }
        }

        int noOfDistinctUsers = 0;
        for (var entry : noOfUsers.entrySet()) {
            if (entry.getValue() == 2) {
                noOfDistinctUsers++;
            }
        }

        if (noOfUsers.size() == 0) {
            return 0.0;
        } else {
            return  (double) noOfDistinctUsers / noOfUsers.size();
        }
    }

    private double computeRelativeVoteCount(VotedQuestionList questions, VotedQuestion q) {
        logger.info("NoracleQuestionUtilityService -> computeRelativeVoteCount(...)");
        int voteCount = q.getVotes().size();
        int voteCountAll = 0;
        for (VotedQuestion question : questions) {
            voteCountAll += question.getVotes().size();
        }
        double relativeVoteCount = 0.0;
        if (voteCountAll != 0) {
            relativeVoteCount = (1.0 / voteCountAll) * voteCount;
        }
        logger.info("relativeVoteCount: " + relativeVoteCount);
        return relativeVoteCount;
    }

    private final L2pLogger logger = L2pLogger.getInstance(NoracleQuestionUtilityService.class.getName());
}
