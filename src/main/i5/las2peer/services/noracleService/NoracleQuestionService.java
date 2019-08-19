package i5.las2peer.services.noracleService;

import java.io.Serializable;
import java.time.Instant;
import java.util.Random;

import i5.las2peer.api.Context;
import i5.las2peer.api.Service;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.InvocationBadArgumentException;
import i5.las2peer.api.execution.ResourceNotFoundException;
import i5.las2peer.api.execution.ServiceAccessDeniedException;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.execution.ServiceNotAuthorizedException;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.api.persistency.Envelope;
import i5.las2peer.api.persistency.EnvelopeAccessDeniedException;
import i5.las2peer.api.persistency.EnvelopeNotFoundException;
import i5.las2peer.api.persistency.EnvelopeOperationFailedException;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AgentAccessDeniedException;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.api.security.AgentOperationFailedException;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.api.security.GroupAgent;
import i5.las2peer.services.noracleService.api.INoracleQuestionService;
import i5.las2peer.services.noracleService.model.Question;
import i5.las2peer.services.noracleService.model.QuestionList;
import i5.las2peer.services.noracleService.model.Space;

/**
 * Noracle Question Service
 *
 * This service is used to handle questions in a distributed Noracle system.
 *
 */
public class NoracleQuestionService extends Service implements INoracleQuestionService {

	private static final int MAX_QUESTIONS_PER_SPACE = 1000;

	private final Random myRandom;

	public NoracleQuestionService() {
		myRandom = new Random();
	}

	@Override
	public Question createQuestion(final String questionSpaceId, final String text) throws ServiceInvocationException {
		final Agent mainAgent = Context.get().getMainAgent();
		if (questionSpaceId == null || questionSpaceId.isEmpty()) {
			throw new InvocationBadArgumentException("No question space id given");
		} else if (text == null || text.isEmpty()) {
			throw new InvocationBadArgumentException("No question text given");
		} else if (mainAgent instanceof AnonymousAgent) {
			throw new ServiceNotAuthorizedException("You have to be logged in to create a question");
		}
		Space targetSpace;
		final Serializable rmiResult = Context.get().invoke(
				new ServiceNameVersion(NoracleSpaceService.class.getCanonicalName(), NoracleService.API_VERSION),
				"getSpace", questionSpaceId);
		if (rmiResult instanceof Space) {
			targetSpace = (Space) rmiResult;
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}
		final String targetReaderGroupId = targetSpace.getSpaceReaderGroupId();
		GroupAgent targetReaderGroup;
		try {
			targetReaderGroup = (GroupAgent) Context.get().requestAgent(targetReaderGroupId, mainAgent);
		} catch (AgentNotFoundException | AgentOperationFailedException e) {
			throw new InternalServiceException("Could not fetch reader group agent for space", e);
		} catch (final ClassCastException e) {
			throw new InternalServiceException("Agent for space reader group is not a GroupAgent", e);
		} catch (final AgentAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Agent not in space reader group", e);
		}
		final String questionId = buildQuestionId();
		Envelope env;
		try {
			env = Context.get().createEnvelope(getQuestionEnvelopeIdentifier(questionId), mainAgent);
		} catch (final EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (final EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not create envelope for question", e);
		}
		env.addReader(targetReaderGroup);
		final Question question = new Question(questionId, text, questionSpaceId, mainAgent.getIdentifier(),
				Instant.now().toString());
		env.setContent(question);
		try {
			Context.get().storeEnvelope(env, mainAgent);
		} catch (final EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (final EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not store question envelope", e);
		}
		if (questionSpaceId != null && !questionSpaceId.isEmpty()) {
			linkQuestionToSpace(questionSpaceId, questionId);
		}
		return question;
	}

	public boolean linkQuestionToSpace(final String spaceId, final String questionId) {
		int questionNumber;
		for (questionNumber = 1; questionNumber < MAX_QUESTIONS_PER_SPACE; questionNumber++) {
			try {
				Context.get().requestEnvelope(buildSpaceQuestionNumberId(spaceId, questionNumber));
			} catch (final EnvelopeNotFoundException e) { // found free question number
				break;
			} catch (final Exception e) {
				// XXX logging
			}
		}
		try {
			final Envelope spaceQuestionEnv = Context.get()
					.createEnvelope(buildSpaceQuestionNumberId(spaceId, questionNumber));
			spaceQuestionEnv.setPublic();
			spaceQuestionEnv.setContent(questionId);
			Context.get().storeEnvelope(spaceQuestionEnv);
			return true;
		} catch (EnvelopeOperationFailedException | EnvelopeAccessDeniedException e) {
			// TODO exception handling
			e.printStackTrace();
		}
		return false;
	}

	private String buildSpaceQuestionNumberId(final String spaceId, final int questionNumber) {
		return "spacequestion-" + spaceId + "-" + questionNumber;
	}

	@Override
	public Question getQuestion(final String questionId) throws ServiceInvocationException {
		if (questionId == null || questionId.isEmpty()) {
			throw new InvocationBadArgumentException("No question id given");
		}
		Envelope env;
		try {
			env = Context.get().requestEnvelope(getQuestionEnvelopeIdentifier(questionId));
		} catch (final EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (final EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not fetch question envelope", e);
		} catch (final EnvelopeNotFoundException e) {
			throw new ResourceNotFoundException("Question Not Found");
		}
		final Question question = (Question) env.getContent();
		return question;
	}

	private String buildQuestionId() {
		String result = "";
		for (int c = 0; c < 10; c++) {
			result += myRandom.nextInt(10);
		}
		return result;
	}

	private String getQuestionEnvelopeIdentifier(final String questionId) {
		return "question-" + questionId;
	}

	@Override
	public QuestionList getQuestions(final String spaceId, String order, Integer limit, Integer startAt)
			throws ServiceInvocationException {
		if (spaceId == null || spaceId.isEmpty()) {
			throw new InvocationBadArgumentException("No space id given");
		} else if (limit != null && limit <= 0) {
			throw new InvocationBadArgumentException("Invalid limit given");
		} else if (startAt != null && startAt < 1) {
			throw new InvocationBadArgumentException("Invalid startAt given");
		}
		if (order == null || order.isEmpty()) {
			order = "asc";
		}
		if (limit == null) {
			limit = 10;
		}
		if (startAt == null) {
			startAt = 1;
		}
		final QuestionList result = new QuestionList();
		try {
			if (order.equalsIgnoreCase("desc")) {
				for (int questionNumber = startAt; questionNumber > startAt - limit; questionNumber--) {
					if (!retrieveQuestion(result, spaceId, questionNumber)) {
						limit++;
					}
				}
			} else {
				for (int questionNumber = startAt; questionNumber < startAt + limit; questionNumber++) {
					if (!retrieveQuestion(result, spaceId, questionNumber)) {
						limit++;
					}
				}
			}
		} catch (final EnvelopeNotFoundException e) {
			// done
		}
		return result;
	}

	private boolean retrieveQuestion(final QuestionList result, final String spaceId, final int questionNumber)
			throws EnvelopeNotFoundException {
		try {
			final Envelope spaceQuestionEnv = Context.get()
					.requestEnvelope(buildSpaceQuestionNumberId(spaceId, questionNumber));
			final String questionId = (String) spaceQuestionEnv.getContent();
			final Envelope questionEnv = Context.get().requestEnvelope(getQuestionEnvelopeIdentifier(questionId));
			final Question question = (Question) questionEnv.getContent();
			// TODO check if author is a member of this space?
//			String authorId = question.getAuthorId();
//			if (authorId == null || authorId.isEmpty()) {
//				return false;
//			}
			result.add(question);
			return true;
		} catch (final EnvelopeNotFoundException e) {
			throw e;
		} catch (final Exception e) {
			// XXX logging
		}
		return false;
	}

	@Override
	public Question changeQuestionText(final String questionId, final String text) throws ServiceInvocationException {
		if (questionId == null) {
			throw new InvocationBadArgumentException("No question id given");
		}
		try {
			final Envelope questionEnvelope = Context.get().requestEnvelope(getQuestionEnvelopeIdentifier(questionId));
			final Question question = (Question) questionEnvelope.getContent();
			question.setText(text);
			question.setTimestampLastModified(Instant.now().toString());
			questionEnvelope.setContent(question);
			Context.get().storeEnvelope(questionEnvelope);
			return question;
		} catch (final EnvelopeNotFoundException e) {
			throw new ResourceNotFoundException("Question not found");
		} catch (final EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (final EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not fetch question envelope", e);
		}
	}

	@Override
	public Question changeQuestionDepth(final String questionId, final int depth) throws ServiceInvocationException {
		if (questionId == null) {
			throw new InvocationBadArgumentException("No question id given");
		}
		try {
			final Envelope questionEnvelope = Context.get().requestEnvelope(getQuestionEnvelopeIdentifier(questionId));
			final Question question = (Question) questionEnvelope.getContent();
			question.setDepth(depth);
			question.setTimestampLastModified(Instant.now().toString());
			questionEnvelope.setContent(question);
			Context.get().storeEnvelope(questionEnvelope);
			return question;
		} catch (final EnvelopeNotFoundException e) {
			throw new ResourceNotFoundException("Question not found");
		} catch (final EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (final EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not fetch question envelope", e);
		}
	}

}
