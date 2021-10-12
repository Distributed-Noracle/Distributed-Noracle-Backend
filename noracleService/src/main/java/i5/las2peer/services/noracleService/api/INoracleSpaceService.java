package i5.las2peer.services.noracleService.api;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.model.SpaceSubscribersList;

public interface INoracleSpaceService {

	Space createSpace(String name) throws ServiceInvocationException;

	Space getSpace(String spaceId) throws ServiceInvocationException;
	
	SpaceSubscribersList getSubscribers(String spaceId) throws ServiceInvocationException;

}
