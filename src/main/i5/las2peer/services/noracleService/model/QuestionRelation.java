package i5.las2peer.services.noracleService.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class QuestionRelation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String relationId;
	private String spaceId;
	private String authorId;
	private String name;
	private String firstQuestionId;
	private String secondQuestionId;
	private boolean directed;
	private String timestampCreated;
	private String timestampLastModified;

	public QuestionRelation() { // used in tests
	}

	public QuestionRelation(final String relationId, final String spaceId, final String authorId, final String name,
			final String firstQuestionId, final String secondQuestionId, final Boolean directed,
			final String timestampCreated) {
		this.relationId = relationId;
		this.spaceId = spaceId;
		this.authorId = authorId;
		this.name = name;
		this.firstQuestionId = firstQuestionId;
		this.secondQuestionId = secondQuestionId;
		this.directed = directed == null ? false : directed;
		this.timestampCreated = timestampCreated;
		timestampLastModified = timestampCreated;
	}

	public void setSpaceId(final String spaceId) {
		this.spaceId = spaceId;
	}

	public String getRelationId() {
		return relationId;
	}

	public void setRelationId(final String relationId) {
		this.relationId = relationId;
	}

	public String getSpaceId() {
		return spaceId;
	}

	public String getAuthorId() {
		return authorId;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getFirstQuestionId() {
		return firstQuestionId;
	}

	public void setFirstQuestionId(final String firstQuestionId) {
		this.firstQuestionId = firstQuestionId;
	}

	public String getSecondQuestionId() {
		return secondQuestionId;
	}

	public void setSecondQuestionId(final String secondQuestionId) {
		this.secondQuestionId = secondQuestionId;
	}

	public boolean isDirected() {
		return directed;
	}

	public void setDirected(final boolean directed) {
		this.directed = directed;
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

	public void setAuthorId(final String authorId) {
		this.authorId = authorId;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof QuestionRelation))
			return false;

		final QuestionRelation other = (QuestionRelation) obj;
		boolean bool = true;

		bool = bool && StringUtils.equals(relationId, other.relationId);
		bool = bool && StringUtils.equals(spaceId, other.spaceId);
		bool = bool && StringUtils.equals(authorId, other.authorId);
		bool = bool && StringUtils.equals(name, other.name);
		bool = bool && StringUtils.equals(firstQuestionId, other.firstQuestionId);
		bool = bool && StringUtils.equals(secondQuestionId, other.secondQuestionId);
		bool = bool && directed == other.directed;
		bool = bool && StringUtils.equals(timestampCreated, other.timestampCreated);
		bool = bool && StringUtils.equals(timestampLastModified, other.timestampLastModified);

		return bool;
	}
}
