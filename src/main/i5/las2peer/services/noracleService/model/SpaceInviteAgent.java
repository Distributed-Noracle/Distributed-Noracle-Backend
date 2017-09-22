package i5.las2peer.services.noracleService.model;

import java.util.Base64;

import i5.las2peer.api.security.AgentOperationFailedException;
import i5.las2peer.communication.Message;
import i5.las2peer.communication.MessageException;
import i5.las2peer.security.AgentContext;
import i5.las2peer.security.PassphraseAgentImpl;
import i5.las2peer.serialization.SerializationException;
import i5.las2peer.serialization.SerializeTools;
import i5.las2peer.tools.CryptoException;
import i5.las2peer.tools.CryptoTools;

public class SpaceInviteAgent extends PassphraseAgentImpl {

	public SpaceInviteAgent(String spaceSecret) throws AgentOperationFailedException, CryptoException {
		super(CryptoTools.generateKeyPair(), spaceSecret, CryptoTools.generateSalt());
	}

	@Override
	public String toXmlString() throws SerializationException {
		StringBuffer result = new StringBuffer("<las2peer:agent type=\"spaceowner\">\n" + "\t<id>" + getIdentifier()
				+ "</id>\n" + "\t<publickey encoding=\"base64\">" + SerializeTools.serializeToBase64(getPublicKey())
				+ "</publickey>\n" + "\t<privatekey encrypted=\"" + CryptoTools.getSymmetricAlgorithm() + "\" keygen=\""
				+ CryptoTools.getSymmetricKeygenMethod() + "\">\n" + "\t\t<salt encoding=\"base64\">"
				+ Base64.getEncoder().encodeToString(getSalt()) + "</salt>\n" + "\t\t<data encoding=\"base64\">"
				+ getEncodedPrivate() + "</data>\n" + "\t</privatekey>\n");
		result.append("</las2peer:agent>\n");
		return result.toString();
	}

	@Override
	public void receiveMessage(Message message, AgentContext c) throws MessageException {
		// do nothing
	}

}
