package i5.las2peer.services.noracleService;

import i5.las2peer.api.Context;
import i5.las2peer.api.Service;
import i5.las2peer.api.execution.*;
import i5.las2peer.api.persistency.Envelope;
import i5.las2peer.api.persistency.EnvelopeAccessDeniedException;
import i5.las2peer.api.persistency.EnvelopeNotFoundException;
import i5.las2peer.api.persistency.EnvelopeOperationFailedException;
import i5.las2peer.api.security.*;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.security.GroupAgentImpl;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.serialization.SerializationException;
import i5.las2peer.services.noracleService.api.INoracleSpaceService;
import i5.las2peer.services.noracleService.model.*;
import i5.las2peer.tools.CryptoException;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;

/**
 * Noracle Space Service
 * 
 * This service is used to handle spaces in a distributed Noracle system.
 * 
 */
public class NoracleSpaceService extends Service implements INoracleSpaceService {

	private final Random myRandom;
	private final SecureRandom secureRandom;

	public NoracleSpaceService() {
		myRandom = new Random();
		secureRandom = new SecureRandom();
	}

	@Override
	public Space createSpace(String name, boolean isPrivate) throws ServiceInvocationException {
		Agent mainAgent = Context.get().getMainAgent();
		if (mainAgent instanceof AnonymousAgent) {
			throw new ServiceNotAuthorizedException("You have to be logged in to create a space");
		}
		String spaceId = buildSpaceId();
		String spaceSecret = generateSpaceSecret();
		SpaceInviteAgent spaceInviteAgent;
		try {
			spaceInviteAgent = new SpaceInviteAgent(spaceSecret);
			spaceInviteAgent.unlock(spaceSecret);
			Context.get().storeAgent(spaceInviteAgent);
		} catch (AgentOperationFailedException | CryptoException e) {
			throw new InternalServiceException("Could not create space invite agent", e);
		} catch (AgentAccessDeniedException | AgentAlreadyExistsException | AgentLockedException e) {
			throw new InternalServiceException("Could not store space invite agent", e);
		}
		try {
			Envelope envInviteMapping = Context.get().createEnvelope(getInviteMappingIdentifier(spaceId));
			envInviteMapping.setPublic();
			envInviteMapping.setContent(spaceInviteAgent.getIdentifier());
			Context.get().storeEnvelope(envInviteMapping);
		} catch (EnvelopeOperationFailedException | EnvelopeAccessDeniedException e) {
			throw new InternalServiceException("Could not store space invite mapping", e);
		}
		GroupAgentImpl spaceMemberGroupAgent;
		try {
			spaceMemberGroupAgent = GroupAgentImpl.createGroupAgent(new Agent[] { spaceInviteAgent, mainAgent });
			spaceMemberGroupAgent.unlock(mainAgent);
			Context.get().storeAgent(spaceMemberGroupAgent);
		} catch (AgentOperationFailedException | CryptoException | SerializationException e) {
			throw new InternalServiceException("Could not create space member group agent", e);
		} catch (AgentAccessDeniedException | AgentAlreadyExistsException | AgentLockedException e) {
			throw new InternalServiceException("Could not store space member group agent", e);
		}
		try {
			Envelope envGroupMapping = Context.get().createEnvelope(getMemberMappingIdentifier(spaceId));
			envGroupMapping.setPublic();
			envGroupMapping.setContent(spaceMemberGroupAgent.getIdentifier());
			Context.get().storeEnvelope(envGroupMapping);
		} catch (EnvelopeOperationFailedException | EnvelopeAccessDeniedException e) {
			throw new InternalServiceException("Could not store space group member mapping", e);
		}
		Envelope env;
		try {
			env = Context.get().createEnvelope(getSpaceEnvelopeIdentifier(spaceId), mainAgent);
		} catch (EnvelopeOperationFailedException | EnvelopeAccessDeniedException e) {
			throw new InternalServiceException("Could not create envelope for space", e);
		}
		env.addReader(spaceMemberGroupAgent);
		Space space = new Space(spaceId, spaceSecret, name, mainAgent.getIdentifier(),
				spaceMemberGroupAgent.getIdentifier(), isPrivate);
		env.setContent(space);
		try {
			Context.get().storeEnvelope(env, mainAgent);
		} catch (EnvelopeAccessDeniedException | EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not store space envelope", e);
		}

		// Handle public spaces
		if(!isPrivate) {
			Envelope envelope;
			try {
				envelope = Context.get().requestEnvelope(getPublicSpacesIdentifier());
				SpaceList list = (SpaceList) envelope.getContent();
				list.add(space);
				envelope.setContent(list);
				Context.get().storeEnvelope(envelope, mainAgent);
			} catch (EnvelopeOperationFailedException e) {
				e.printStackTrace();
			} catch (EnvelopeNotFoundException e) {
				try {
					envelope = Context.get().createEnvelope(getPublicSpacesIdentifier());
					envelope.setPublic();
					SpaceList list = new SpaceList();
					list.add(space);
					envelope.setContent(list);
					Context.get().storeEnvelope(envelope, mainAgent);
				} catch (EnvelopeOperationFailedException ex) {
					ex.printStackTrace();
				} catch (EnvelopeAccessDeniedException ex) {
					ex.printStackTrace();
				}
			} catch (EnvelopeAccessDeniedException e) {
				e.printStackTrace();
			}
		}

		return space;
	}

	@Override
	public void deleteSpace(String spaceId) throws ServiceInvocationException {
		// Delete space and reclaim corresponding envelopes
		try {
			Context.get().reclaimEnvelope(getInviteMappingIdentifier(spaceId));
		} catch (EnvelopeAccessDeniedException e) {
			e.printStackTrace();
		} catch (EnvelopeNotFoundException e) {
			e.printStackTrace();
		} catch (EnvelopeOperationFailedException e) {
			e.printStackTrace();
		}

		try {
			Context.get().reclaimEnvelope(getMemberMappingIdentifier(spaceId));
		} catch (EnvelopeAccessDeniedException e) {
			e.printStackTrace();
		} catch (EnvelopeNotFoundException e) {
			e.printStackTrace();
		} catch (EnvelopeOperationFailedException e) {
			e.printStackTrace();
		}

		//Space space = null;
		try {
			//space = (Space) Context.get().requestEnvelope(getSpaceEnvelopeIdentifier(spaceId));
			Context.get().reclaimEnvelope(getSpaceEnvelopeIdentifier(spaceId));
		} catch (EnvelopeAccessDeniedException e) {
			e.printStackTrace();
		} catch (EnvelopeNotFoundException e) {
			e.printStackTrace();
		} catch (EnvelopeOperationFailedException e) {
			e.printStackTrace();
		}

		// Handle public spaces
		Envelope envelope;
		try {
			envelope = Context.get().requestEnvelope(getPublicSpacesIdentifier());
			SpaceList list = (SpaceList) envelope.getContent();
			if (list != null) {
				list.removeIf(s -> s.getSpaceId().equals(spaceId));
			}
			envelope.setContent(list);
			Context.get().storeEnvelope(envelope);
		} catch (EnvelopeOperationFailedException e) {
			e.printStackTrace();
		} catch (EnvelopeNotFoundException e) {
			e.printStackTrace();
		} catch (EnvelopeAccessDeniedException e) {
			e.printStackTrace();
		}

		//return space;
	}

	@Override
	public Space getSpace(String spaceId) throws ServiceInvocationException {
		if (spaceId == null || spaceId.isEmpty()) {
			throw new InvocationBadArgumentException("No space id given");
		}
		Envelope env;
		try {
			env = Context.get().requestEnvelope(getSpaceEnvelopeIdentifier(spaceId));
		} catch (EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Access Denied", e);
		} catch (EnvelopeNotFoundException e) {
			throw new ResourceNotFoundException("Space Not Found", e);
		} catch (EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not deserialize space object", e);
		}
		Space space = (Space) env.getContent();
		return space;
	}
	
	@Override
	public SpaceSubscribersList getSubscribers(String spaceId) throws ServiceInvocationException {
		if (spaceId == null || spaceId.isEmpty()) {
			throw new InvocationBadArgumentException("No space id given");
		}
		try {
			UserAgentImpl inviteAgent = this.getInviteAgent(spaceId);
			GroupAgent memberAgent = this.getMemberAgent(spaceId);
			memberAgent.unlock(Context.get().getMainAgent());
			String[] memberIds = memberAgent.getMemberList();
			SpaceSubscribersList subscribers = new SpaceSubscribersList();
			for (int i = 0; i < memberIds.length; i++) {
				if (!memberIds[i].equals(inviteAgent.getIdentifier())) {
					UserAgentImpl agent = (UserAgentImpl) Context.get().fetchAgent(memberIds[i]);
					NoracleAgentProfile profile = new NoracleAgentProfile();
					profile.setName(agent.getLoginName());
					subscribers.add(profile);
				}
			}
			return subscribers;
		} catch (Exception e) {
			throw new ServiceInvocationException("Could not fetch list", e);
		}
	}

	public SpaceList getPublicSpaces() {
		Envelope envelope = null;
		SpaceList list = new SpaceList();
		try {
			envelope = Context.get().requestEnvelope(getPublicSpacesIdentifier());
			list = (SpaceList) envelope.getContent();
		} catch (EnvelopeAccessDeniedException e) {
			e.printStackTrace();
		} catch (EnvelopeNotFoundException e) {
			// No public spaces
		} catch (EnvelopeOperationFailedException e) {
			e.printStackTrace();
		}
		return list;
	}

	public void joinSpace(String spaceId, String spaceSecret) throws ServiceInvocationException {
		// logger.info("NoracleSpaceService -> joinSpace() with spaceId " + spaceId + " and spaceSecret " + spaceSecret + " called");
		if (spaceId == null || spaceId.isEmpty()) {
			throw new InvocationBadArgumentException("No space id given");
		} else if (spaceSecret == null || spaceSecret.isEmpty()) {
			throw new ServiceAccessDeniedException("No space secret given");
		}
		try {
			UserAgentImpl inviteAgent = this.getInviteAgent(spaceId);
			inviteAgent.unlock(spaceSecret);
			GroupAgent memberAgent = this.getMemberAgent(spaceId);
			memberAgent.unlock(inviteAgent);
			memberAgent.addMember(Context.get().getMainAgent());
			Context.get().storeAgent(memberAgent);
		} catch (AgentAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Could not unlock agent", e);
		} catch (Exception e) {
			throw new ServiceInvocationException("Could not join space", e);
		}
	}
	
	private UserAgentImpl getInviteAgent(String spaceId) throws AgentNotFoundException, AgentOperationFailedException, EnvelopeAccessDeniedException, EnvelopeNotFoundException, EnvelopeOperationFailedException {
		String inviteAgentEnvelopeId = getInviteMappingIdentifier(spaceId);
		Envelope inviteAgentMapping = Context.get().requestEnvelope(inviteAgentEnvelopeId);
		String inviteAgentId = (String) inviteAgentMapping.getContent();
		UserAgentImpl inviteAgent = (UserAgentImpl) Context.get().fetchAgent(inviteAgentId);
		return inviteAgent;
	}
	
	private GroupAgent getMemberAgent(String spaceId) throws EnvelopeAccessDeniedException, EnvelopeNotFoundException, EnvelopeOperationFailedException, AgentNotFoundException, AgentOperationFailedException {
		String memberAgentEnvelopeId = getMemberMappingIdentifier(spaceId);
		Envelope memberAgentMapping = Context.get().requestEnvelope(memberAgentEnvelopeId);
		String memberAgentId = (String) memberAgentMapping.getContent();
		GroupAgent memberAgent = (GroupAgent) Context.get().fetchAgent(memberAgentId);
		return memberAgent;
	}

	private String buildSpaceId() {
		String result = "";
		for (int c = 0; c < 10; c++) {
			result += myRandom.nextInt(10);
		}
		return result;
	}

	private String generateSpaceSecret() {
		String result = "";
		for (int c = 0; c < 20; c++) {
			result += secureRandom.nextInt(10);
		}
		return result;
	}

	private String getInviteMappingIdentifier(String spaceId) {
		return "invite-" + spaceId;
	}

	private String getMemberMappingIdentifier(String spaceId) {
		return "group-" + spaceId;
	}

	private String getSpaceEnvelopeIdentifier(String spaceId) {
		return "space-" + spaceId;
	}

	private String getPublicSpacesIdentifier() {
		return "spaces-public";
	}

	private final L2pLogger logger = L2pLogger.getInstance(NoracleSpaceService.class.getName());

}
