package i5.las2peer.services.noracleService;

import i5.las2peer.api.execution.ServiceInvocationException;

public interface INoracleSpaceService {

	public Space createSpace(String name) throws ServiceInvocationException;

	public Space getSpace(String spaceId) throws ServiceInvocationException;

}
