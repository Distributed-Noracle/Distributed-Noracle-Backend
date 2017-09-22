package i5.las2peer.services.noracleService;

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
import i5.las2peer.api.security.AgentOperationFailedException;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.security.GroupAgentImpl;
import i5.las2peer.serialization.SerializationException;
import i5.las2peer.services.noracleService.api.INoracleSpaceService;
import i5.las2peer.services.noracleService.model.Space;
import i5.las2peer.services.noracleService.model.SpaceInviteAgent;
import i5.las2peer.tools.CryptoException;

/**
 * Noracle Space Service
 * 
 * This service is used to handle spaces in a distributed Noracle system.
 * 
 */
public class NoracleSpaceService extends Service implements INoracleSpaceService {

	private final Random myRandom;

	public NoracleSpaceService() {
		myRandom = new Random();
	}

	@Override
	public Space createSpace(String name) throws ServiceInvocationException {
		Agent mainAgent = Context.get().getMainAgent();
		if (mainAgent instanceof AnonymousAgent) {
			throw new ServiceNotAuthorizedException("You have to be logged in to create a space");
		}
		SpaceInviteAgent spaceInviteAgent;
		try {
			// FIXME use secure secret
			spaceInviteAgent = new SpaceInviteAgent("topsecret");
			spaceInviteAgent.unlock("topsecret");
			Context.get().storeAgent(spaceInviteAgent);
		} catch (AgentOperationFailedException | CryptoException e) {
			throw new InternalServiceException("Could not create space invite agent", e);
		} catch (AgentAccessDeniedException | AgentAlreadyExistsException | AgentLockedException e) {
			throw new InternalServiceException("Could not store space invite agent", e);
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
		String spaceId = buildSpaceId();
		Envelope env;
		try {
			env = Context.get().createEnvelope(getSpaceEnvelopeIdentifier(spaceId), mainAgent);
		} catch (EnvelopeOperationFailedException | EnvelopeAccessDeniedException e) {
			throw new InternalServiceException("Could not create envelope for space", e);
		}
		env.addReader(spaceMemberGroupAgent);
		Space space = new Space(spaceId, name, mainAgent.getIdentifier());
		env.setContent(space);
		try {
			Context.get().storeEnvelope(env, mainAgent);
		} catch (EnvelopeAccessDeniedException | EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not store space envelope", e);
		}
		return space;
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

	private String buildSpaceId() {
		String result = "";
		for (int c = 0; c < 10; c++) {
			result += myRandom.nextInt(10);
		}
		return result;
	}

	private String getSpaceEnvelopeIdentifier(String questionId) {
		return "space-" + questionId;
	}

}
