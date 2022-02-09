package i5.las2peer.services.noracleService;

import i5.las2peer.api.Context;
import i5.las2peer.api.Service;
import i5.las2peer.api.execution.*;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.services.noracleService.api.INoracleRecommenderService;
import i5.las2peer.services.noracleService.model.*;
import info.debatty.java.stringsimilarity.Cosine;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class NoracleRecommenderService extends Service implements INoracleRecommenderService {

    private RecommenderQuestionList getRecommendations(String agentId, VotedQuestionList votedQuestionList) throws ServiceInvocationException {

        // Normalize questions
        /*rmiResult = Context.get().invoke(
        new ServiceNameVersion(NoracleNormalizationService.class.getCanonicalName(), NoracleService.API_VERSION),
        "normalizeQuestions", votedQuestionList);*/

        // Compute Utility of questions
        /*rmiResult = Context.get().invoke(
                new ServiceNameVersion(NoracleQuestionUtilityService.class.getCanonicalName(), NoracleService.API_VERSION),
                "computeUtilityForQuestions", agentId, votedQuestionList);*/
        HashMap<VotedQuestion, Double> recommendationUtility = computeUtilityForQuestions(agentId, votedQuestionList);

        List<VotedQuestion> topRecommendations = recommendationUtility
                .entrySet()
                .stream()
                .sorted(Map.Entry.<VotedQuestion, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        RecommenderQuestionList recommendations = new RecommenderQuestionList();
        RecommenderQuestion rq;
        for (VotedQuestion q : topRecommendations) {

            rq = new RecommenderQuestion();
            rq.setQuestion(q);

            // set author name
            Serializable rmiResult = Context.get().invoke(
                    new ServiceNameVersion(NoracleAgentService.class.getCanonicalName(), NoracleService.API_VERSION),
                    "getAgentProfile", q.getAuthorId());
            NoracleAgentProfile ap = (NoracleAgentProfile) rmiResult;
            if (ap != null || ap.getName() != null) {
                rq.setAuthorName(ap.getName());
            }

/*            rmiResult = Context.get().invoke(
                    new ServiceNameVersion(NoracleQuestionRelationService.class.getCanonicalName(), NoracleService.API_VERSION),
                    "getQuestionRelationIds", q.getSpaceId(), q.getQuestionId());
            rq.setQuestionNeighbourIds((QuestionNeighbourIds) rmiResult);*/

            recommendations.add(rq);
        }

        return recommendations;
    }

    @Override
    public RecommenderQuestionList getRecommendedQuestions(String agentId) throws ServiceInvocationException {
        logger.info("NoracleRecommenderService -> getRecommendedQuestions() with agentid " + agentId);

        // Retrieving questions
        logger.info("Get all (voted) questions for agent with agentId = " + agentId);
        Serializable rmiResult = Context.get().invoke(
                new ServiceNameVersion(NoracleAgentService.class.getCanonicalName(), NoracleService.API_VERSION),
                "getSpaceSubscriptions", agentId);

        SpaceSubscriptionList spaces = new SpaceSubscriptionList();
        if (rmiResult instanceof SpaceSubscriptionList) {
            spaces = (SpaceSubscriptionList) rmiResult;
        } else {
            logger.warning("RmiResult not an instance of SpaceSubscriptionList!");
        }

        VotedQuestionList votedQuestionList = new VotedQuestionList();
        for (SpaceSubscription s : spaces) {
            rmiResult = Context.get().invoke(
                    new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), NoracleService.API_VERSION),
                    "getAllVotedQuestions", s.getSpaceId());
            if (rmiResult instanceof VotedQuestionList) {
                votedQuestionList.addAll((VotedQuestionList) rmiResult);
            } else {
                logger.warning("RmiResult not an instance of VotedQuestionList!");
            }
        }
        RecommenderQuestionList recommenderQuestionList = getRecommendations(agentId, votedQuestionList);

        return recommenderQuestionList;
    }

    @Override
    public RecommenderQuestionList getRecommendedQuestionsForSpace(String agentId, String spaceId) throws ServiceInvocationException {
        logger.info("NoracleRecommenderService -> getRecommendedQuestionsForSpace() with agentid " + agentId + " and spaceId " + spaceId + " called");

        // Retrieving questions
        logger.info("Get all (voted) questions of space with spaceId " + spaceId);
        Serializable rmiResult = Context.get().invoke(
                new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), NoracleService.API_VERSION),
                "getAllVotedQuestions", spaceId);

        VotedQuestionList votedQuestionList = new VotedQuestionList();
        if (rmiResult instanceof VotedQuestionList) {
            votedQuestionList = (VotedQuestionList) rmiResult;
        } else {
            logger.warning("RmiResult not an instance of VotedQuestionList!");
        }

        return getRecommendations(agentId, votedQuestionList);
    }

    // TEMP -> Utility service!
    // weights
    private final double cosineSimilarityWeight = 0.0;
    private final double voteSimilarityWeight = 0.0;
    private final double relativeVoteCountWeight = 1;
    private final double absoluteVoteCountWeight = 1;
    private final double positiveVoteCountWeight = 1;
    private final double negativeVoteCountWeight = -1;
    // private final double timeWeight = 1;

    private final Cosine cosine = new Cosine();

    private HashMap<VotedQuestion, Double> computeUtilityForQuestions(String agentId, VotedQuestionList questions) {
        logger.info("NoracleQuestionUtilityService -> computeUtilityForQuestions(...)");
        HashMap<VotedQuestion, Double> utilityMap = new HashMap<>();
        double utility;
        for (VotedQuestion q : questions) {
            utility = 0.0;
            if (!q.getAuthorId().equals(agentId)) {
                // begin simple
                //double cosineSimilarity = computeMaxCosineSimilarity(agentId, q);
                //double voteSimilarity = computeMaxVoteSimilarity(agentId, q);
                double relativeVoteCount = computeRelativeVoteCount(questions, q);
                double absoluteVoteCount = computeAbsoluteVoteCount(q);
                double positiveVoteCount = computePositiveVoteCount(q);
                double negativeVoteCount = computeNegativeVoteCount(q);

                utility = //cosineSimilarityWeight * cosineSimilarity +
                          //voteSimilarityWeight * voteSimilarity +
                          relativeVoteCountWeight * relativeVoteCount +
                                  absoluteVoteCountWeight * absoluteVoteCount +
                                  positiveVoteCountWeight * positiveVoteCount +
                                  negativeVoteCountWeight * negativeVoteCount;
            }

            utilityMap.put(q, utility);
        }
        return utilityMap;
    }

    private double computeNegativeVoteCount(VotedQuestion q) {
        if (q.getVotes() == null) {
            return 0.0;
        }
        return q.getVotes().stream().filter(v -> v.getValue() == -1).collect(Collectors.toList()).size();
    }

    private double computeAbsoluteVoteCount(VotedQuestion q) {
        if (q.getVotes() == null) {
            return 0.0;
        }
        return q.getVotes().size();
    }

    private double computePositiveVoteCount(VotedQuestion q) {
        if (q.getVotes() == null) {
            return 0.0;
        }
        return q.getVotes().stream().filter(v -> v.getValue() == 1).collect(Collectors.toList()).size();
    }

    private double computeMaxCosineSimilarity(String agentId, VotedQuestion question) {
        logger.info("NoracleQuestionUtilityService -> computeMaxCosineSimilarity(...)");
        if (question == null) {
            return 0;
        }
        double maxCosineSimilarity = 0.0;
        String spaceId = question.getSpaceId();
        try {
            Serializable rmiResult = Context.get().invoke(
                    new ServiceNameVersion(NoracleQuestionService.class.getCanonicalName(), NoracleService.API_VERSION),
                    "getAllVotedQuestions", spaceId, agentId);

            VotedQuestionList userQuestions = new VotedQuestionList();
            if (rmiResult instanceof VotedQuestionList) {
                userQuestions = (VotedQuestionList) rmiResult;
            }
            for (VotedQuestion userQuestion : userQuestions) {
                double cosineSimilarity = cosine.similarity(userQuestion.getText(), question.getText());
                maxCosineSimilarity = Math.max(maxCosineSimilarity, cosineSimilarity);
            }
        } catch (Exception ex) {
            logger.warning("Exception inside computeMaxCosineSimilarity:");
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
        if (v1 == null || v2 == null) {
            return 0.0;
        }

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
        if (q == null || q.getVotes() == null) {
            return 0.0;
        }

        int voteCount = q.getVotes().size();
        int voteCountAll = 0;
        for (VotedQuestion question : questions) {
            if (q.getSpaceId().equals(question.getSpaceId())) {
                voteCountAll += question.getVotes().size();
            }
        }
        double relativeVoteCount = 0.0;
        if (voteCountAll != 0) {
            relativeVoteCount = (1.0 / voteCountAll) * voteCount;
        }
        return relativeVoteCount;
    }

    private final L2pLogger logger = L2pLogger.getInstance(NoracleRecommenderService.class.getName());
}
