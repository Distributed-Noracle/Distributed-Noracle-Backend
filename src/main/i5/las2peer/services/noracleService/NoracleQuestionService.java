package i5.las2peer.services.noracleService;

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
import i5.las2peer.api.persistency.Envelope;
import i5.las2peer.api.persistency.EnvelopeAccessDeniedException;
import i5.las2peer.api.persistency.EnvelopeNotFoundException;
import i5.las2peer.api.persistency.EnvelopeOperationFailedException;
import i5.las2peer.api.security.AnonymousAgent;

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
	public Question createQuestion(String questionSpaceId, String questionText) throws ServiceInvocationException {
		if (questionSpaceId == null || questionSpaceId.isEmpty()) {
			throw new InvocationBadArgumentException("No question space id given");
		} else if (questionText == null || questionText.isEmpty()) {
			throw new InvocationBadArgumentException("No question text given");
		} else if (Context.get().getMainAgent() instanceof AnonymousAgent) {
			throw new ServiceNotAuthorizedException("You have to be logged in to create a question");
		}
		String questionId = buildQuestionId();
		Envelope env;
		try {
			env = Context.get().createEnvelope(getQuestionEnvelopeIdentifier(questionId));
		} catch (EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not create envelope for question", e);
		}
		env.setPublic();
		Question question = new Question(questionId, questionText, questionSpaceId, Instant.now().toString());
		env.setContent(question);
		try {
			Context.get().storeEnvelope(env, Context.get().getMainAgent());
		} catch (EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not store question envelope", e);
		}
		if (questionSpaceId != null && !questionSpaceId.isEmpty()) {
			linkQuestionToSpace(questionSpaceId, questionId);
		}
		return question;
	}

	public boolean linkQuestionToSpace(String spaceId, String questionId) {
		int questionNumber;
		for (questionNumber = 1; questionNumber < MAX_QUESTIONS_PER_SPACE; questionNumber++) {
			try {
				Context.get().requestEnvelope(buildSpaceQuestionNumberId(spaceId, questionNumber));
			} catch (EnvelopeNotFoundException e) { // found free question number
				break;
			} catch (Exception e) {
				// XXX logging
			}
		}
		try {
			Envelope spaceQuestionEnv = Context.get()
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

	private String buildSpaceQuestionNumberId(String spaceId, int questionNumber) {
		return "spacequestion-" + spaceId + "-" + questionNumber;
	}

	@Override
	public Question getQuestion(String questionId) throws ServiceInvocationException {
		if (questionId == null || questionId.isEmpty()) {
			throw new InvocationBadArgumentException("No question id given");
		}
		Envelope env;
		try {
			env = Context.get().requestEnvelope(getQuestionEnvelopeIdentifier(questionId));
		} catch (EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not fetch question envelope", e);
		} catch (EnvelopeNotFoundException e) {
			throw new ResourceNotFoundException("Question Not Found");
		}
		Question question = (Question) env.getContent();
		return question;
	}

	private String buildQuestionId() {
		String result = "";
		for (int c = 0; c < 10; c++) {
			result += myRandom.nextInt(10);
		}
		return result;
	}

	private String getQuestionEnvelopeIdentifier(String questionId) {
		return "question-" + questionId;
	}

	@Override
	public QuestionList getQuestions(String spaceId, String order, Integer limit, Integer startAt)
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
		QuestionList result = new QuestionList();
		if (order.equalsIgnoreCase("desc")) {
			for (int questionNumber = startAt; questionNumber > startAt - limit; questionNumber--) {
				try {
					Envelope spaceQuestionEnv;
					try {
						spaceQuestionEnv = Context.get()
								.requestEnvelope(buildSpaceQuestionNumberId(spaceId, questionNumber));
					} catch (EnvelopeNotFoundException e) {
						break;
					}
					String questionId = (String) spaceQuestionEnv.getContent();
					Envelope questionEnv = Context.get().requestEnvelope(getQuestionEnvelopeIdentifier(questionId));
					result.add((Question) questionEnv.getContent());
				} catch (Exception e) {
					// XXX logging
				}
			}
		} else {
			for (int questionNumber = startAt; questionNumber < startAt + limit; questionNumber++) {
				try {
					Envelope spaceQuestionEnv;
					try {
						spaceQuestionEnv = Context.get()
								.requestEnvelope(buildSpaceQuestionNumberId(spaceId, questionNumber));
					} catch (EnvelopeNotFoundException e) {
						break;
					}
					String questionId = (String) spaceQuestionEnv.getContent();
					Envelope questionEnv = Context.get().requestEnvelope(getQuestionEnvelopeIdentifier(questionId));
					result.add((Question) questionEnv.getContent());
				} catch (Exception e) {
					// XXX logging
				}
			}
		}
		return result;
	}

	@Override
	public Question changeQuestionText(String questionId, String questionText) throws ServiceInvocationException {
		if (questionId == null) {
			throw new InvocationBadArgumentException("No question id given");
		}
		try {
			Envelope questionEnvelope = Context.get().requestEnvelope(getQuestionEnvelopeIdentifier(questionId));
			Question question = (Question) questionEnvelope.getContent();
			question.setText(questionText);
			question.setTimestampLastModified(Instant.now().toString());
			questionEnvelope.setContent(question);
			Context.get().storeEnvelope(questionEnvelope);
			return question;
		} catch (EnvelopeNotFoundException e) {
			throw new ResourceNotFoundException("Question not found");
		} catch (EnvelopeAccessDeniedException e) {
			throw new ServiceAccessDeniedException("Envelope Access Denied");
		} catch (EnvelopeOperationFailedException e) {
			throw new InternalServiceException("Could not fetch question envelope", e);
		}
	}

}
