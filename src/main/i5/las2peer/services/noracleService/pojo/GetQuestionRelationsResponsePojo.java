package i5.las2peer.services.noracleService.pojo;

import java.util.ArrayList;

import i5.las2peer.services.noracleService.model.QuestionRelationList;

public class GetQuestionRelationsResponsePojo {

	private QuestionRelationList content;
	private ArrayList<LinkPojo> links;

	public QuestionRelationList getContent() {
		return content;
	}

	public void setContent(QuestionRelationList content) {
		this.content = content;
	}

	public ArrayList<LinkPojo> getLinks() {
		return links;
	}

	public void setLinks(ArrayList<LinkPojo> links) {
		this.links = links;
	}

}
