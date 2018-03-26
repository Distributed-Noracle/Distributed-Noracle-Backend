package i5.las2peer.services.noracleService.api;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.services.noracleService.model.Space;

public interface INoracleSpaceService {

	public Space createSpace(String name) throws ServiceInvocationException;

	public Space getSpace(String spaceId) throws ServiceInvocationException;

}
