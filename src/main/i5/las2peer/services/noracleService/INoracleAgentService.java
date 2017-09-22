package i5.las2peer.services.noracleService;

import i5.las2peer.api.execution.ServiceInvocationException;

public interface INoracleAgentService {

	public SpaceSubscription subscribeToSpace(String spaceId, String name) throws ServiceInvocationException;

	public void unsubscribeFromSpace(String spaceId) throws ServiceInvocationException;

	public SpaceSubscriptionList getSpaceSubscriptions(String agentId) throws ServiceInvocationException;

}
