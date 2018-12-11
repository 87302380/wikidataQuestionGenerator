package de.ostfalia.teamprojekt.wwm.wikidatatest.model;

import com.google.common.collect.ImmutableList;

public class Question {

	public final String questionText;
	// the right answer should be first
	public final ImmutableList<String> answers;
	public final int difficulty;

	public Question(final String questionText, final ImmutableList<String> answers, final int difficulty) {
		if (answers.size() != 4) {
			throw new IllegalArgumentException("A question requires 4 answers, " + answers.size() + " given");
		}
//		if (difficulty < 1) {
//			throw new IllegalArgumentException("difficulty must be >= 1, was " + difficulty);
//		}
		this.questionText = questionText;
		this.answers = answers;
		this.difficulty = difficulty;
	}

	@Override public String toString() {
		return String.format("<%d> %s 1) %s, 2) %s, 3) %s, 4) %s", difficulty, questionText, answers.get(0), answers.get(1), answers.get(2), answers.get(3));
	}
}
