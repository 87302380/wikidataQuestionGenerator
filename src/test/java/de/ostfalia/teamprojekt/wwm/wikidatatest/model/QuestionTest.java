package de.ostfalia.teamprojekt.wwm.wikidatatest.model;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestionTest {

	@Test
	void testConstructorListLength() {
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of()));
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of("")));
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of("", "")));
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of("", "", "")));
		new Question("", ImmutableList.of("", "", "", ""));
		assertThrows(IllegalArgumentException.class, () -> new Question("", ImmutableList.of("", "", "", "", "")));
	}

	@Test
	void testToString() {
		assertEquals("a 1) b, 2) c, 3) d, 4) e", new Question("a", ImmutableList.of("b", "c", "d", "e")).toString());
	}
}
