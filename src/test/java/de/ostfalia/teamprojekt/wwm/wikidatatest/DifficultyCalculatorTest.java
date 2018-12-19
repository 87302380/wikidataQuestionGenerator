package de.ostfalia.teamprojekt.wwm.wikidatatest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.SiteLink;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DifficultyCalculatorTest {

	@BeforeAll
	static void setUp() {
		IoHelpers.configureLogging();
	}

	@Test
	void test1Item() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		assertEquals(4, d.getDifficulty(1));
	}

	@Test
	void test2ItemsLinear() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(2, d.getDifficulty(2));
	}

	@Test
	void test3ItemsLinear() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(3, d.getDifficulty(2));
		assertEquals(2, d.getDifficulty(3));
	}

	@Test
	void test4ItemsLinear() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(4);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(3, d.getDifficulty(2));
		assertEquals(2, d.getDifficulty(3));
		assertEquals(1, d.getDifficulty(4));
	}

	@Test
	void test5ItemsLinear() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(4);
		d.registerStatementCount(5);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(4, d.getDifficulty(2));
		assertEquals(3, d.getDifficulty(3));
		assertEquals(2, d.getDifficulty(4));
		assertEquals(1, d.getDifficulty(5));
	}

	@Test
	void test6ItemsLinear() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(4);
		d.registerStatementCount(5);
		d.registerStatementCount(6);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(4, d.getDifficulty(2));
		assertEquals(3, d.getDifficulty(3));
		assertEquals(2, d.getDifficulty(4));
		assertEquals(2, d.getDifficulty(5));
		assertEquals(1, d.getDifficulty(6));
	}

	@Test
	void test7ItemsLinear() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(4);
		d.registerStatementCount(5);
		d.registerStatementCount(6);
		d.registerStatementCount(7);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(4, d.getDifficulty(2));
		assertEquals(3, d.getDifficulty(3));
		assertEquals(3, d.getDifficulty(4));
		assertEquals(2, d.getDifficulty(5));
		assertEquals(2, d.getDifficulty(6));
		assertEquals(1, d.getDifficulty(7));
	}

	@Test
	void test8ItemsLinear() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(4);
		d.registerStatementCount(5);
		d.registerStatementCount(6);
		d.registerStatementCount(7);
		d.registerStatementCount(8);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(4, d.getDifficulty(2));
		assertEquals(3, d.getDifficulty(3));
		assertEquals(3, d.getDifficulty(4));
		assertEquals(2, d.getDifficulty(5));
		assertEquals(2, d.getDifficulty(6));
		assertEquals(1, d.getDifficulty(7));
		assertEquals(1, d.getDifficulty(8));
	}

	@Test
	void test9ItemsLinear() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(4);
		d.registerStatementCount(5);
		d.registerStatementCount(6);
		d.registerStatementCount(7);
		d.registerStatementCount(8);
		d.registerStatementCount(9);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(4, d.getDifficulty(2));
		assertEquals(4, d.getDifficulty(3));
		assertEquals(3, d.getDifficulty(4));
		assertEquals(3, d.getDifficulty(5));
		assertEquals(2, d.getDifficulty(6));
		assertEquals(2, d.getDifficulty(7));
		assertEquals(1, d.getDifficulty(8));
		assertEquals(1, d.getDifficulty(9));
	}

	@Test
	void test10ItemsLinear() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(4);
		d.registerStatementCount(5);
		d.registerStatementCount(6);
		d.registerStatementCount(7);
		d.registerStatementCount(8);
		d.registerStatementCount(9);
		d.registerStatementCount(10);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(4, d.getDifficulty(2));
		assertEquals(4, d.getDifficulty(3));
		assertEquals(3, d.getDifficulty(4));
		assertEquals(3, d.getDifficulty(5));
		assertEquals(2, d.getDifficulty(6));
		assertEquals(2, d.getDifficulty(7));
		assertEquals(2, d.getDifficulty(8));
		assertEquals(1, d.getDifficulty(9));
		assertEquals(1, d.getDifficulty(10));
	}

	@Test
	void test20ItemsLinear() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		for (int i = 1; i <= 20; i++) {
			d.registerStatementCount(i);
		}
		// 5 times 4, 5 times 4, 5 times 3, 5 times 2, 5 times 1
		int statementCount = 0;
		for (int i = 4; i >= 1; i--) {
			for (int j = 0; j < 5; j++) {
				statementCount++;
				System.out.println(d.getDifficulty(statementCount));
//				assertEquals(i, d.getDifficulty(statementCount));
			}
		}
	}

	@Test
	void test3ItemsReverseProportional() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(3);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(2, d.getDifficulty(3));
	}

	@Test
	void test4ItemsReverseProportional() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(4);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(2, d.getDifficulty(2));
		assertEquals(1, d.getDifficulty(4));
	}

	@Test
	void test5ItemsReverseProportional() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(5);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(3, d.getDifficulty(2));
		assertEquals(2, d.getDifficulty(3));
		assertEquals(1, d.getDifficulty(5));
	}

	@Test
	void test6ItemsReverseProportional() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(6);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(3, d.getDifficulty(2));
		assertEquals(2, d.getDifficulty(3));
		assertEquals(1, d.getDifficulty(6));
	}

	@Test
	void test7ItemsReverseProportional() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(7);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(3, d.getDifficulty(2));
		assertEquals(2, d.getDifficulty(3));
		assertEquals(1, d.getDifficulty(7));
	}

	@Test
	void test8ItemsReverseProportional() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(4);
		d.registerStatementCount(8);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(3, d.getDifficulty(2));
		assertEquals(2, d.getDifficulty(3));
		assertEquals(1, d.getDifficulty(4));
		assertEquals(1, d.getDifficulty(8));
	}

	@Test
	void test9ItemsReverseProportional() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(2);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(5);
		d.registerStatementCount(9);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(3, d.getDifficulty(2));
		assertEquals(2, d.getDifficulty(3));
		assertEquals(1, d.getDifficulty(5));
		assertEquals(1, d.getDifficulty(9));
	}

	@Test
	void test10ItemsReverseProportional() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(2);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(5);
		d.registerStatementCount(10);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(3, d.getDifficulty(2));
		assertEquals(2, d.getDifficulty(3));
		assertEquals(1, d.getDifficulty(5));
		assertEquals(1, d.getDifficulty(10));
	}

	@Test
	void test20ItemsReverseProportional() {
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(2);
		d.registerStatementCount(2);
		d.registerStatementCount(2);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(3);
		d.registerStatementCount(3);
		d.registerStatementCount(4);
		d.registerStatementCount(5);
		d.registerStatementCount(7);
		d.registerStatementCount(10);
		d.registerStatementCount(20);
		assertEquals(4, d.getDifficulty(1));
		assertEquals(3, d.getDifficulty(2));
		assertEquals(2, d.getDifficulty(3));
		assertEquals(1, d.getDifficulty(4));
		assertEquals(1, d.getDifficulty(5));
		assertEquals(1, d.getDifficulty(7));
		assertEquals(1, d.getDifficulty(10));
		assertEquals(1, d.getDifficulty(20));
	}

	@Test
	void test20ItemsNormalDistributionSigma3() {
		// normal distribution with mu=10 and sigma=3, scaled by 100
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(0);
		d.registerStatementCount(0);
		d.registerStatementCount(1);
		d.registerStatementCount(2);
		d.registerStatementCount(3);
		d.registerStatementCount(5);
		d.registerStatementCount(8);
		d.registerStatementCount(11);
		d.registerStatementCount(12);
		d.registerStatementCount(13);
		d.registerStatementCount(12);
		d.registerStatementCount(11);
		d.registerStatementCount(8);
		d.registerStatementCount(5);
		d.registerStatementCount(3);
		d.registerStatementCount(2);
		d.registerStatementCount(1);
		d.registerStatementCount(0);
		d.registerStatementCount(0);
		assertEquals(4, d.getDifficulty(0));
		assertEquals(4, d.getDifficulty(1));
		assertEquals(3, d.getDifficulty(2));
		assertEquals(3, d.getDifficulty(3));
		assertEquals(2, d.getDifficulty(5));
		assertEquals(2, d.getDifficulty(8));
		assertEquals(2, d.getDifficulty(11));
		assertEquals(1, d.getDifficulty(12));
		assertEquals(1, d.getDifficulty(13));
	}

	@Test
	void test20ItemsNormalDistributionSigma2() {
		// normal distribution with mu=10 and sigma=2, scaled by 100
		DifficultyCalculator d = new DifficultyCalculator(4);
		d.registerStatementCount(0);
		d.registerStatementCount(0);
		d.registerStatementCount(0);
		d.registerStatementCount(0);
		d.registerStatementCount(1);
		d.registerStatementCount(3);
		d.registerStatementCount(6);
		d.registerStatementCount(12);
		d.registerStatementCount(18);
		d.registerStatementCount(20);
		d.registerStatementCount(18);
		d.registerStatementCount(12);
		d.registerStatementCount(6);
		d.registerStatementCount(3);
		d.registerStatementCount(1);
		d.registerStatementCount(0);
		d.registerStatementCount(0);
		d.registerStatementCount(0);
		d.registerStatementCount(0);
		assertEquals(4, d.getDifficulty(0));
		assertEquals(3, d.getDifficulty(1));
		assertEquals(2, d.getDifficulty(3));
		assertEquals(2, d.getDifficulty(6));
		assertEquals(2, d.getDifficulty(12));
		assertEquals(1, d.getDifficulty(18));
		assertEquals(1, d.getDifficulty(20));
	}

}
