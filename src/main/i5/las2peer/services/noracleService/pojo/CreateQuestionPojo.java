package i5.las2peer.services.noracleService.pojo;

import java.io.Serializable;

public class CreateQuestionPojo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String questionText;

	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

}
