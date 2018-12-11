package de.ostfalia.teamprojekt.wwm.wikidatatest.model;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestionTest {

	@Test
	void testConstructorListLength() {
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of(), 1));
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of(""), 1));
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of("", ""), 1));
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of("", "", ""), 1));
		new Question("", ImmutableList.of("", "", "", ""), 1);
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of("", "", "", "", ""), 1));
	}

	@Test
	void testDifficulty() {
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of("", "", "", ""), 0));
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of("", "", "", ""), -1));
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of("", "", "", ""), Integer.MIN_VALUE));
		new Question("", ImmutableList.of("", "", "", ""), 1);
		new Question("", ImmutableList.of("", "", "", ""), 2);
		new Question("", ImmutableList.of("", "", "", ""), 1000);
		new Question("", ImmutableList.of("", "", "", ""), Integer.MAX_VALUE);
	}

	@Test
	void testToString() {
		assertEquals("<1> a 1) b, 2) c, 3) d, 4) e", new Question("a", ImmutableList.of("b", "c", "d", "e"), 1).toString());
	}
}
