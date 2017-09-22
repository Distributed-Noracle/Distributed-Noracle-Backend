package i5.las2peer.services.noracleService.pojo;

import java.util.ArrayList;

import i5.las2peer.services.noracleService.model.QuestionList;

public class GetQuestionsResponsePojo {

	private QuestionList content;
	private ArrayList<LinkPojo> links;

	public QuestionList getContent() {
		return content;
	}

	public void setContent(QuestionList content) {
		this.content = content;
	}

	public ArrayList<LinkPojo> getLinks() {
		return links;
	}

	public void setLinks(ArrayList<LinkPojo> links) {
		this.links = links;
	}

}
