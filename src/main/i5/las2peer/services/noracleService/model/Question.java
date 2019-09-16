package i5.las2peer.services.noracleService.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class Question implements Serializable {

	private static final long serialVersionUID = 1L;

	private String questionId;
	private String text;
	private String spaceId;
	private String authorId;
	private String timestampCreated;
	private String timestampLastModified;
	private int depth;

	public Question() { // used in tests
	}

	public Question(final String questionId, final String text, final String spaceId, final String authorId,
			final String timestampCreated) {
		this.questionId = questionId;
		this.text = text;
		this.spaceId = spaceId;
		this.authorId = authorId;
		this.timestampCreated = timestampCreated;
		timestampLastModified = timestampCreated;
		depth = 0;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(final String questionId) {
		this.questionId = questionId;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public String getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(final String spaceId) {
		this.spaceId = spaceId;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(final String authorId) {
		this.authorId = authorId;
	}

	public String getTimestampCreated() {
		return timestampCreated;
	}

	public void setTimestampCreated(final String timestampCreated) {
		this.timestampCreated = timestampCreated;
	}

	public String getTimestampLastModified() {
		return timestampLastModified;
	}

	public void setTimestampLastModified(final String timestampLastModified) {
		this.timestampLastModified = timestampLastModified;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(final int depth) {
		this.depth = depth;
	}

	@Override
	public String toString() {
		return "Question(ID=" + questionId + ", text=" + text + ", depth=" + depth + ")";
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Question))
			return false;

		final Question other = (Question) obj;
		boolean bool = true;

		bool = bool && StringUtils.equals(questionId, other.questionId);
		bool = bool && StringUtils.equals(text, other.text);
		bool = bool && StringUtils.equals(spaceId, other.spaceId);
		bool = bool && StringUtils.equals(authorId, other.authorId);
		bool = bool && StringUtils.equals(timestampCreated, other.timestampCreated);
		bool = bool && StringUtils.equals(timestampLastModified, other.timestampLastModified);
		bool = bool && depth == other.depth;

		return bool;

	}

}
