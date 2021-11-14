package i5.las2peer.services.noracleService;

import i5.las2peer.api.Context;
import i5.las2peer.api.Service;
import i5.las2peer.api.execution.*;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.services.noracleService.api.INoracleRecommenderService;
import i5.las2peer.services.noracleService.model.*;
import info.debatty.java.stringsimilarity.Cosine;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class NoracleRecommenderService extends Service implements INoracleRecommenderService {

    private RecommenderQuestionList getRecommendations(String agentId, VotedQuestionList votedQuestionList) throws ServiceInvocationException {
        //VotedQuestionList normQuestionList = normalizeQuestions(votedQuestionList);

        // Compute Utility of questions
        /*rmiResult = Context.get().invoke(
                new ServiceNameVersion(NoracleQuestionUtilityService.class.getCanonicalName(), NoracleService.API_VERSION),
                "computeUtilityForQuestions", agentId, votedQuestionList);*/
        HashMap<VotedQuestion, Double> recommendationUtility = computeUtilityForQuestions(agentId, votedQuestionList);

        List<VotedQuestion> topRecommendations = recommendationUtility
                .entrySet()
                .stream()
                .sorted(Map.Entry.<VotedQuestion, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        RecommenderQuestionList recommendations = new RecommenderQuestionList();
        RecommenderQuestion rq;
        for (VotedQuestion q : topRecommendations) {
            // VotedQuestion q = votedQuestionList.stream().filter(vq -> vq.getQuestionId().equals(normQ.getQuestionId())).findFirst().get();

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

            rmiResult = Context.get().invoke(
                    new ServiceNameVersion(NoracleQuestionRelationService.class.getCanonicalName(), NoracleService.API_VERSION),
                    "getQuestionRelationIds", q.getSpaceId(), q.getQuestionId());
            rq.setQuestionNeighbourIds((List<String>) rmiResult);

            recommendations.add(rq);
        }

        return recommendations;
    }

    @Override
    public RecommenderQuestionList getRecommendedQuestions(String agentId) throws ServiceInvocationException {
        // get all questions for user with agentId
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

        return getRecommendations(agentId, votedQuestionList);
    }

    @Override
    public RecommenderQuestionList getRecommendedQuestionsForSpace(String agentId, String spaceId) throws ServiceInvocationException {
        logger.info("NoracleRecommenderService -> getRecommendedQuestions() with agentid " + agentId + " and spaceId " + spaceId + " called");

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

/*        logger.info("Test Version");
        String testParam = "Siiiiimon";
        rmiResult = Context.get().invoke(
                new ServiceNameVersion(NoracleNormalizationService.class.getCanonicalName(), NoracleService.API_VERSION),
                "getVersion", testParam);
        if (rmiResult instanceof String) {
            logger.info(rmiResult.toString());
        }*/

/*        try {
            rmiResult = Context.get().invoke(
                    //"i5.las2peer.services.noracleService.NoracleNormalizationService@1.0.0",
                    new ServiceNameVersion(NoracleNormalizationService.class.getCanonicalName(), NoracleService.API_VERSION),
                    "getSimpleQuestion", new Question());
            if (rmiResult instanceof Question) {
                logger.info("Called worked!");
            }
        } catch (ServiceMethodNotFoundException ex) {
            logger.info(ex.getMessage());
        }*/

        // Normalizing questions
/*        logger.info("Normalize Questions");
        rmiResult = Context.get().invoke(
                new ServiceNameVersion(NoracleNormalizationService.class.getCanonicalName(), NoracleService.API_VERSION),
                "normalizeQuestions", votedQuestionList);
        VotedQuestionList normQuestionList = new VotedQuestionList();
        if (rmiResult instanceof VotedQuestionList) {
            normQuestionList = (VotedQuestionList) rmiResult;
        }*/

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TEMP -> Irgendwann müssen wir das komplett in die Services verlagern!
    // Leider können noch keine Service Funktionen aufgerufen werden, bei denen wir Objekte als Argumente geben
    // Las2peer kann diese Service Funktionen nicht finden -> ServiceNotFoundException

    // TEMP -> NormalizationService
    public VotedQuestion normalizeQuestion(VotedQuestion question) {
        logger.info("NoracleNormalizationService -> normalizeQuestion(...) called");
        VotedQuestion normQuestion = new VotedQuestion(question);
        String text = normQuestion.getText();
        // 1. to lower case
        text = text.toLowerCase();

        // 2. Expanding contractions
        text = expandContractions(text);

        // 3. Removing dashes
        text = text.replaceAll("[\\s\\-()]", " ");

        // 4. Remove stop words
        text = removeStopWords(text);

        // 5. Stemming
        text = stemming(text);

        // 6. Remove all non-letter characters
        //text = text.replaceAll("[^a-zA-Z ]", "");

        // 7. Replacing words with synonyms
        // TODO: Do actual replacing with words or index which leads to a pool of synonyms
        //text = replaceWithSynonyms(text);

        normQuestion.setText(text);

        return normQuestion;
    }

    /*private String replaceWithSynonyms(String text) {
        // TODO: Do actual replacing with words or index which leads to a pool of synonyms
        File f = new File("WordNet/2.1/dict");
        System.setProperty("wordnet.database.dir", f.toString());
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets;
        try {
            synsets = database.getSynsets(text);
        } catch (Exception ex) {
            throw ex;
        }
        if (synsets.length > 0) {
            ArrayList<String> al = new ArrayList<String>();
            // add elements to al, including duplicates
            HashSet hs = new HashSet();
            for (int i = 0; i < synsets.length; i++) {
                String[] wordForms = synsets[i].getWordForms();
                for (int j = 0; j < wordForms.length; j++)
                {
                    al.add(wordForms[j]);
                }


                //removing duplicates
                hs.addAll(al);
                al.clear();
                al.addAll(hs);
            }
            //showing all synsets
            for (int k = 0; k < al.size(); k++) {
                //System.out.println(al.get(k));
            }
        } else {
            //System.err.println("No synsets exist that contain the word form '" + wordForm + "'");
        }
        return text;
    }*/

    private String removeStopWords(String inputString) {
        inputString = inputString.replace(" a ", " ");
        inputString = inputString.replace(" the ", " ");
        // inputString = inputString.replace(" is ", " ");
        inputString = inputString.replace(" are ", " ");
        return inputString;
    }

    public VotedQuestionList normalizeQuestions(VotedQuestionList questionList) {
        logger.info("NoracleNormalizationService -> normalizeQuestions(...) called");
        questionList.stream().forEach(q -> logger.info(q.getText()));
        VotedQuestionList normQuestionList = new VotedQuestionList();
        for (VotedQuestion q : questionList) {
            VotedQuestion normQ = normalizeQuestion(q);
            normQuestionList.add(normQ);
        }
        return normQuestionList;
    }

    private String expandContractions(String inputString) {
        inputString = inputString.replaceAll("n't", " not");
        inputString = inputString.replaceAll("'re", " are");
        inputString = inputString.replaceAll("'m", " am");
        inputString = inputString.replaceAll("'ll", " will");
        inputString = inputString.replaceAll("'ve", " have");
        inputString = inputString.replaceAll("'s", " is");
        return inputString;
    }

    private String stemming(String string) {
        PorterStemmer stem = new PorterStemmer();
        stem.setCurrent(string);
        stem.stem();
        return stem.getCurrent();
    }

    // TEMP -> Utility service!
    // weights
    private final double cosineSimilarityWeight = 0.0;
    private final double voteSimilarityWeight = 0.0;
    private final double relativeVoteCountWeight = 1;
    private final double absoluteVoteCountWeight = 1;
    private final double positiveVoteCountWeight = 1;
    private final double negativeVoteCountWeight = -1;

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
