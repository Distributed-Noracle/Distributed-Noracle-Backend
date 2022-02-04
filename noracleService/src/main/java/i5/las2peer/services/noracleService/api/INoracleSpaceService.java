package i5.las2peer.services.noracleService.api;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.model.SpaceList;
import i5.las2peer.services.noracleService.model.SpacePublic;
import i5.las2peer.services.noracleService.model.SpaceSubscribersList;

import java.util.ArrayList;
import java.util.List;

public interface INoracleSpaceService {

	Space createSpace(String name, boolean isPrivate) throws ServiceInvocationException;

	Space getSpace(String spaceId) throws ServiceInvocationException;
	
	SpaceSubscribersList getSubscribers(String spaceId) throws ServiceInvocationException;

	SpaceList getPublicSpaces() throws ServiceInvocationException;

}
