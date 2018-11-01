package de.ostfalia.teamprojekt.wwm.wikidatatest.model;

import com.google.common.collect.ImmutableList;

public class Question {

	public final String questionText;
	// the right answer should be first
	public final ImmutableList<String> answers;

	public Question(final String questionText, final ImmutableList<String> answers) {
		if (answers.size() != 4) {
			throw new IllegalArgumentException("A question requires 4 answers, " + answers.size() + " given");
		}
		this.questionText = questionText;
		this.answers = ImmutableList.copyOf(answers);
	}

	@Override public String toString() {
		return String.format("%s 1) %s, 2) %s, 3) %s, 4) %s", questionText, answers.get(0), answers.get(1), answers.get(2), answers.get(3));
	}
}
