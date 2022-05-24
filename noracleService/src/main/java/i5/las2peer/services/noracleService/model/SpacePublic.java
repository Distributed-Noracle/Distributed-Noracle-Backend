package i5.las2peer.services.noracleService.model;

import java.io.Serializable;

public class SpacePublic implements Serializable {
    private Space space;
    private int numberOfQuestions;

    public SpacePublic() {}

    public SpacePublic(Space space, int numberOfQuestions) {
        this.space = space;
        this.numberOfQuestions = numberOfQuestions;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(int numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }
}
