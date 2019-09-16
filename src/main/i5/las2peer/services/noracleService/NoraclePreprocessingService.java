package i5.las2peer.services.noracleService;

import i5.las2peer.api.Service;
import i5.las2peer.services.noracleService.api.INoraclePreprocessingService;

public class NoraclePreprocessingService extends Service implements INoraclePreprocessingService {

	@Override
	public void checkStatus() {
		System.err.println("I'm alive!");
	}

}
