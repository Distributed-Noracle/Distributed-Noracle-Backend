package i5.las2peer.services.noracleService.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SpaceSubscription implements Serializable {

	@Serial
	private static final long serialVersionUID = -3983233729934822604L;

	private String spaceId;
	//private List<String> selectedQuestionIds;

	public SpaceSubscription() { // used in test
	}

	public SpaceSubscription(String spaceId, String name) {
		this.setSpaceId(spaceId);
		//this.selectedQuestionIds = new ArrayList<>();
	}

	public String getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(String spaceId) {
		this.spaceId = spaceId;
	}

/*	public List<String> getSelectedQuestionIds() {
		return selectedQuestionIds;
	}

	public void setSelectedQuestionIds(List<String>selectedQuestionIds) {
		this.selectedQuestionIds = selectedQuestionIds;
	}*/

}
