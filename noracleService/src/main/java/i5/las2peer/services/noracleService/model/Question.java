package i5.las2peer.services.noracleService.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

public class Question implements Serializable {

	@Serial
	private static final long serialVersionUID = 220865019536362474L;

	private String questionId;
	private String text;
	private String spaceId;
	private String authorId;
	private String timestampCreated;
	private String timestampLastModified;
	private int depth;

	public Question() { // used in tests
		this.timestampCreated = Instant.now().toString();
		this.timestampLastModified = Instant.now().toString();
	}

	public Question(String questionId, String text, String spaceId, String authorId, String timestampCreated) {
		this.questionId = questionId;
		this.text = text;
		this.spaceId = spaceId;
		this.authorId = authorId;
		this.timestampCreated = timestampCreated;
		this.timestampLastModified = timestampCreated;
		this.depth = 0;
	}

	public Question(Question question) {
		this.questionId = question.questionId;
		this.text = question.text;
		this.spaceId = question.spaceId;
		this.authorId = question.authorId;
		this.timestampCreated = question.timestampCreated;
		this.timestampLastModified = question.timestampLastModified;
		this.depth = question.depth;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(String spaceId) {
		this.spaceId = spaceId;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}

	public String getTimestampCreated() {
		return timestampCreated;
	}

	public void setTimestampCreated(String timestampCreated) {
		this.timestampCreated = timestampCreated;
	}

	public String getTimestampLastModified() {
		return timestampLastModified;
	}

	public void setTimestampLastModified(String timestampLastModified) {
		this.timestampLastModified = timestampLastModified;
	}

	public int getDepth() {
		return this.depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

}
