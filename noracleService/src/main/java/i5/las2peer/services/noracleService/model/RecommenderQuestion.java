package i5.las2peer.services.noracleService.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class RecommenderQuestion implements Serializable {

    @Serial
    private static final long serialVersionUID = 3652217404875516479L;

    public RecommenderQuestion() {
        this.authorName = "unknown user";
    }

/*    public RecommenderQuestion(QuestionNeighbourIds questionNeighbourIds, VotedQuestion question, String authorName) {
        this.questionNeighbourIds = questionNeighbourIds;
        this.question = question;
        this.authorName = authorName;
    }*/

    //private QuestionNeighbourIds questionNeighbourIds;
    private VotedQuestion question;
    private String authorName;

/*    public List<String> getQuestionNeighbourIds() {
        return questionNeighbourIds;
    }*/

/*    public void setQuestionNeighbourIds(QuestionNeighbourIds questionNeighbourIds) {
        this.questionNeighbourIds = questionNeighbourIds;
    }*/

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(VotedQuestion question) {
        this.question = question;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
