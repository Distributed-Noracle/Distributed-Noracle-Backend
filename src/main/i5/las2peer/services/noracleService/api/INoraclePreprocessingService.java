package i5.las2peer.services.noracleService.api;

import i5.las2peer.api.execution.ServiceInvocationException;

public interface INoraclePreprocessingService {

	public void checkStatus() throws ServiceInvocationException;
}
