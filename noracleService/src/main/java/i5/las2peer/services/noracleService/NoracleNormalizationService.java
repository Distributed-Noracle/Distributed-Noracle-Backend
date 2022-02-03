package i5.las2peer.services.noracleService;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import i5.las2peer.api.Service;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.services.noracleService.api.INoracleNormalizationService;
import i5.las2peer.services.noracleService.model.Question;
import i5.las2peer.services.noracleService.model.VotedQuestion;

import i5.las2peer.services.noracleService.model.VotedQuestionList;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class NoracleNormalizationService extends Service implements INoracleNormalizationService {

    @Override
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
private String replaceWithSynonyms(String text) {
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
    }


    private String removeStopWords(String inputString) {
        inputString = inputString.replace(" a ", " ");
        inputString = inputString.replace(" the ", " ");
        // inputString = inputString.replace(" is ", " ");
        inputString = inputString.replace(" are ", " ");
        return inputString;
    }

    @Override
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

    public Question getSimpleQuestion(Question q) {
        logger.info("NoracleNormalizationService -> getSimpleQuestion(...) called");
        return q;
    }

    public String getVersion(String testParam) {
        return NoracleService.API_VERSION + " / " + testParam;
    }

    private final L2pLogger logger = L2pLogger.getInstance(NoracleNormalizationService.class.getName());
}
