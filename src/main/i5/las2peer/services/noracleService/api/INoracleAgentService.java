package i5.las2peer.services.noracleService.api;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.services.noracleService.model.SpaceSubscription;
import i5.las2peer.services.noracleService.model.SpaceSubscriptionList;

public interface INoracleAgentService {

	public SpaceSubscription subscribeToSpace(String spaceId, String name) throws ServiceInvocationException;

	public void unsubscribeFromSpace(String spaceId) throws ServiceInvocationException;

	public SpaceSubscriptionList getSpaceSubscriptions(String agentId) throws ServiceInvocationException;

}
