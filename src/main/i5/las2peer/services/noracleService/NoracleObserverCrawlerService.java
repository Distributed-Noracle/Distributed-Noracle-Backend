package i5.las2peer.services.noracleService;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import i5.las2peer.api.Service;
import i5.las2peer.services.noracleService.api.ICrawlerService;

public class NoracleObserverCrawlerService extends Service implements ICrawlerService {

	@Override
	public boolean checkService() {
		try {
			go();
		} catch (final Exception e) {
			System.err.println("Exception while establishing SSH connection: " + e);
		}
		return true;

	}

	public static void go() {
		final String user = "las2peer";
		final String password = "2PR4Lapqa";
		final String host = "las2peer.dbis.rwth-aachen.de";
		final int port = 22;
		try {
			final JSch jsch = new JSch();
			final Session session = jsch.getSession(user, host, port);
			final int lport = 4321;
			final String rhost = "localhost";
			final int rport = 3306;
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			System.out.println("Establishing Connection...");
			session.connect();
			final int assinged_port = session.setPortForwardingL(lport, rhost, rport);
			System.out.println("localhost:" + assinged_port + " -> " + rhost + ":" + rport);
		} catch (final Exception e) {
			System.err.print(e);
		}
	}
}
