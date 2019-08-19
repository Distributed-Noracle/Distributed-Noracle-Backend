package i5.las2peer.services.noracleService;

import java.security.SecureRandom;
import java.util.Random;

import i5.las2peer.api.Context;
import i5.las2peer.api.Service;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.InvocationBadArgumentException;
import i5.las2peer.api.execution.ResourceNotFoundException;
import i5.las2peer.api.execution.ServiceAccessDeniedException;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.execution.ServiceNotAuthorizedException;
import i5.las2peer.api.persistency.Envelope;
import i5.las2peer.api.persistency.EnvelopeAccessDeniedException;
import i5.las2peer.api.persistency.EnvelopeNotFoundException;
import i5.las2peer.api.persistency.EnvelopeOperationFailedException;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AgentAccessDeniedException;
import i5.las2peer.api.security.AgentAlreadyExistsException;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.api.security.AgentOperationFailedException;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.api.security.GroupAgent;
import i5.las2peer.security.GroupAgentImpl;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.serialization.SerializationException;
import i5.las2peer.services.noracleService.api.INoracleSpaceService;
import i5.las2peer.services.noracleService.model.NoracleAgentProfile;
import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.model.SpaceInviteAgent;
import i5.las2peer.services.noracleService.model.SpaceSubscribersList;
import i5.las2peer.tools.CryptoException;

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
	public Space createSpace(final String name) throws ServiceInvocationException {
		final Agent mainAgent = Context.get().getMainAgent();
		if (mainAgent instanceof AnonymousAgent) {
			throw new ServiceNotAuthorizedException("You have to be logged in to create a space");
		}
		final String spaceId = buildSpaceId();
		final String spaceSecret = generateSpaceSecret();
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
			final Envelope envInviteMapping = Context.get().createEnvelope(getInviteMappingIdentifier(spaceId));
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
			final Envelope envGroupMapping = Context.get().createEnvelope(getMemberMappingIdentifier(spaceId));
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
		final Space space = new Space(spaceId, spaceSecret, name, mainAgent.getIdentifier(),
				spaceMemberGroupAgent.getIdentifier());
		env.setContent(space);
		try {
			Context.get().storeEnvelope(env, mainAgent);
		} catch (EnvelopeAccessDeniedException | EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not store space envelope", e);
		}
		return space;
	}

	@Override
	public Space getSpace(final String spaceId) throws ServiceInvocationException {
		if (spaceId == null || spaceId.isEmpty()) {
			throw new InvocationBadArgumentException("No space id given");
		}
		Envelope env;
		try {
			env = Context.get().requestEnvelope(getSpaceEnvelopeIdentifier(spaceId));
		} catch (final EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Access Denied", e);
		} catch (final EnvelopeNotFoundException e) {
			throw new ResourceNotFoundException("Space Not Found", e);
		} catch (final EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not deserialize space object", e);
		}
		final Space space = (Space) env.getContent();
		return space;
	}

	@Override
	public SpaceSubscribersList getSubscribers(final String spaceId) throws ServiceInvocationException {
		if (spaceId == null || spaceId.isEmpty()) {
			throw new InvocationBadArgumentException("No space id given");
		}
		try {
			final UserAgentImpl inviteAgent = getInviteAgent(spaceId);
			final GroupAgent memberAgent = getMemberAgent(spaceId);
			memberAgent.unlock(Context.get().getMainAgent());
			final String[] memberIds = memberAgent.getMemberList();
			final SpaceSubscribersList subscribers = new SpaceSubscribersList();
			for (int i = 0; i < memberIds.length; i++) {
				if (!memberIds[i].equals(inviteAgent.getIdentifier())) {
					final UserAgentImpl agent = (UserAgentImpl) Context.get().fetchAgent(memberIds[i]);
					final NoracleAgentProfile profile = new NoracleAgentProfile();
					profile.setName(agent.getLoginName());
					subscribers.add(profile);
				}
			}
			return subscribers;
		} catch (final Exception e) {
			throw new ServiceInvocationException("Could not fetch list", e);
		}
	}

	public void joinSpace(final String spaceId, final String spaceSecret) throws ServiceInvocationException {
		if (spaceId == null || spaceId.isEmpty()) {
			throw new InvocationBadArgumentException("No space id given");
		} else if (spaceSecret == null || spaceSecret.isEmpty()) {
			throw new ServiceAccessDeniedException("No space secret given");
		}
		try {
			final UserAgentImpl inviteAgent = getInviteAgent(spaceId);
			inviteAgent.unlock(spaceSecret);
			final GroupAgent memberAgent = getMemberAgent(spaceId);
			memberAgent.unlock(inviteAgent);
			memberAgent.addMember(Context.get().getMainAgent());
			Context.get().storeAgent(memberAgent);
		} catch (final AgentAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Could not unlock agent", e);
		} catch (final Exception e) {
			throw new ServiceInvocationException("Could not join space", e);
		}
	}

	private UserAgentImpl getInviteAgent(final String spaceId)
			throws AgentNotFoundException, AgentOperationFailedException, EnvelopeAccessDeniedException,
			EnvelopeNotFoundException, EnvelopeOperationFailedException {
		final String inviteAgentEnvelopeId = getInviteMappingIdentifier(spaceId);
		final Envelope inviteAgentMapping = Context.get().requestEnvelope(inviteAgentEnvelopeId);
		final String inviteAgentId = (String) inviteAgentMapping.getContent();
		final UserAgentImpl inviteAgent = (UserAgentImpl) Context.get().fetchAgent(inviteAgentId);
		return inviteAgent;
	}

	private GroupAgent getMemberAgent(final String spaceId)
			throws EnvelopeAccessDeniedException, EnvelopeNotFoundException, EnvelopeOperationFailedException,
			AgentNotFoundException, AgentOperationFailedException {
		final String memberAgentEnvelopeId = getMemberMappingIdentifier(spaceId);
		final Envelope memberAgentMapping = Context.get().requestEnvelope(memberAgentEnvelopeId);
		final String memberAgentId = (String) memberAgentMapping.getContent();
		final GroupAgent memberAgent = (GroupAgent) Context.get().fetchAgent(memberAgentId);
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

	private String getInviteMappingIdentifier(final String spaceId) {
		return "invite-" + spaceId;
	}

	private String getMemberMappingIdentifier(final String spaceId) {
		return "group-" + spaceId;
	}

	private String getSpaceEnvelopeIdentifier(final String questionId) {
		return "space-" + questionId;
	}

}
