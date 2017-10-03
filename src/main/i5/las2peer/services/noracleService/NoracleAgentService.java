package i5.las2peer.services.noracleService;

import java.util.Iterator;

import i5.las2peer.api.Context;
import i5.las2peer.api.Service;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.InvocationBadArgumentException;
import i5.las2peer.api.execution.ResourceNotFoundException;
import i5.las2peer.api.execution.ServiceAccessDeniedException;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.api.persistency.Envelope;
import i5.las2peer.api.persistency.EnvelopeAccessDeniedException;
import i5.las2peer.api.persistency.EnvelopeNotFoundException;
import i5.las2peer.api.persistency.EnvelopeOperationFailedException;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.services.noracleService.api.INoracleAgentService;
import i5.las2peer.services.noracleService.model.SpaceSubscription;
import i5.las2peer.services.noracleService.model.SpaceSubscriptionList;

/**
 * Noracle Agents Service
 * 
 * This service is used to handle agents metadata in a distributed Noracle system.
 * 
 */
public class NoracleAgentService extends Service implements INoracleAgentService {

	@Override
	public SpaceSubscription subscribeToSpace(String spaceId, String spaceSecret) throws ServiceInvocationException {
		Agent mainAgent = Context.get().getMainAgent();
		if (spaceId == null || spaceId.isEmpty()) {
			throw new InvocationBadArgumentException("No space id given");
		} else if (mainAgent instanceof AnonymousAgent) {
			throw new ServiceAccessDeniedException("You have to be logged in to subscribe to a space");
		}
		Context.get().invoke(
				new ServiceNameVersion(NoracleSpaceService.class.getCanonicalName(), NoracleService.API_VERSION),
				"joinSpace", spaceId, spaceSecret);
		SpaceSubscription subscription = new SpaceSubscription(spaceId, spaceSecret);
		String envIdentifier = buildSubscriptionId(mainAgent.getIdentifier());
		Envelope env;
		SpaceSubscriptionList subscriptionList;
		try {
			try {
				env = Context.get().requestEnvelope(envIdentifier);
				subscriptionList = (SpaceSubscriptionList) env.getContent();
			} catch (EnvelopeNotFoundException e) {
				env = Context.get().createEnvelope(envIdentifier);
				subscriptionList = new SpaceSubscriptionList();
			}
		} catch (EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not create envelope for space subscription", e);
		}
		subscriptionList.add(subscription);
		env.setContent(subscriptionList);
		try {
			Context.get().storeEnvelope(env, mainAgent);
		} catch (EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not store space subscription envelope", e);
		}
		return subscription;
	}

	@Override
	public void unsubscribeFromSpace(String spaceId) throws ServiceInvocationException {
		Agent mainAgent = Context.get().getMainAgent();
		if (spaceId == null || spaceId.isEmpty()) {
			throw new InvocationBadArgumentException("No space id given");
		} else if (mainAgent instanceof AnonymousAgent) {
			throw new ServiceAccessDeniedException("You have to be logged in to unsubscribe to a space");
		}
		String envIdentifier = buildSubscriptionId(mainAgent.getIdentifier());
		Envelope env;
		SpaceSubscriptionList subscriptionList;
		try {
			try {
				env = Context.get().requestEnvelope(envIdentifier);
				subscriptionList = (SpaceSubscriptionList) env.getContent();
			} catch (EnvelopeNotFoundException e) {
				return;
			}
		} catch (EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not read envelope for space unsubscription", e);
		}
		Iterator<SpaceSubscription> itSubscription = subscriptionList.iterator();
		while (itSubscription.hasNext()) {
			SpaceSubscription subscription = itSubscription.next();
			if (subscription.getSpaceId().equals(spaceId)) {
				itSubscription.remove();
			}
		}
		env.setContent(subscriptionList);
		try {
			Context.get().storeEnvelope(env, mainAgent);
		} catch (EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not store space subscription envelope", e);
		}
	}

	@Override
	public SpaceSubscriptionList getSpaceSubscriptions(String agentId) throws ServiceInvocationException {
		String envIdentifier = buildSubscriptionId(agentId);
		try {
			Envelope env = Context.get().requestEnvelope(envIdentifier);
			return (SpaceSubscriptionList) env.getContent();
		} catch (EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not fetch space subscription envelope", e);
		} catch (EnvelopeNotFoundException e) {
			return new SpaceSubscriptionList();
		}
	}

	private String buildSubscriptionId(String agentId) {
		return "spacesubscriptions-" + agentId;
	}

	@Override
	public SpaceSubscription updateSpaceSubscription(String agentId, String spaceId, String[] selectedQuestions)
			throws ServiceInvocationException {
		Agent mainAgent = Context.get().getMainAgent();
		String envIdentifier = buildSubscriptionId(agentId);
		try {
			Envelope env = Context.get().requestEnvelope(envIdentifier);
			SpaceSubscriptionList spaceSubscriptionList = (SpaceSubscriptionList) env.getContent();
			for (SpaceSubscription spaceSubscription : spaceSubscriptionList) {
				if (spaceSubscription.getSpaceId().equals(spaceId)) {
					spaceSubscription.setSelectedQuestionIds(selectedQuestions);
					env.setContent(spaceSubscriptionList);
					try {
						Context.get().storeEnvelope(env, mainAgent);
					} catch (EnvelopeAccessDeniedException e) {
						throw new ServiceAccessDeniedException("Envelope Access Denied");
					} catch (EnvelopeOperationFailedException e) {
						throw new InternalServiceException("Could not store space subscription envelope", e);
					}
					return spaceSubscription;
				}
			}
			throw new ResourceNotFoundException("Space subscription not found");
		} catch (EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not fetch space subscription envelope", e);
		} catch (EnvelopeNotFoundException e) {
			throw new ResourceNotFoundException("No space subscriptions found");
		}
	}

}
