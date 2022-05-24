package i5.las2peer.services.noracleService.api;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.services.noracleService.model.NoracleAgentProfile;
import i5.las2peer.services.noracleService.model.SpaceSubscription;
import i5.las2peer.services.noracleService.model.SpaceSubscriptionList;

import java.util.List;

public interface INoracleAgentService {

	NoracleAgentProfile updateAgentProfile(String agentName) throws ServiceInvocationException;

	NoracleAgentProfile getAgentProfile(String agentId) throws ServiceInvocationException;

	SpaceSubscription subscribeToSpace(String spaceId, String spaceSecret) throws ServiceInvocationException;

	void unsubscribeFromSpace(String spaceId) throws ServiceInvocationException;

	SpaceSubscriptionList getSpaceSubscriptions(String agentId) throws ServiceInvocationException;

	SpaceSubscription updateSpaceSubscription(String agentId, String spaceId, List<String> selectedQuestions)
			throws ServiceInvocationException;

	Boolean checkIfAlreadySubscribedToSpace(String agentId, String spaceId) throws ServiceInvocationException;

}
