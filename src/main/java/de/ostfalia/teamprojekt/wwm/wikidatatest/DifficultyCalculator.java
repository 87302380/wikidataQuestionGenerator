package de.ostfalia.teamprojekt.wwm.wikidatatest;

import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class DifficultyCalculator {

	private static final Logger LOGGER = LoggerFactory.getLogger(DifficultyCalculator.class);
	private final SortedMap<Integer, Integer> itemCountByNumberOfStatements = new TreeMap<>();
	private final int maxDifficultyValue;
	private SortedMap<Integer, Long> cumulativeCount;
	private long totalNumberOfItems = 0;

	public DifficultyCalculator(int maxDifficultyValue) {
		this.maxDifficultyValue = maxDifficultyValue;
	}

	public void registerStatementCount(final int statementCount) {
		if (cumulativeCount != null) {
			LOGGER.error("processItemDocument called after getDifficulty, ignoring the item.");
		}
		totalNumberOfItems++;
		Integer count = itemCountByNumberOfStatements.getOrDefault(statementCount, 0);
		itemCountByNumberOfStatements.put(statementCount, count + 1);
	}

	public int getDifficulty(int numberOfStatements) {
		if (cumulativeCount == null) {
			calculateDifficulties();
		}
		Long numberOfItemsWithFewerStatements = cumulativeCount.get(numberOfStatements);
		if (numberOfItemsWithFewerStatements == null) {
			throw new IllegalArgumentException("Cannot calculate difficulty for statement count " + numberOfStatements + ", because it was never registered");
		}
		numberOfItemsWithFewerStatements -= itemCountByNumberOfStatements.get(numberOfStatements);
		double ratio = ((double) numberOfItemsWithFewerStatements) / totalNumberOfItems; // should be in range (0, 1]
		// use reverse ratio (large number of statements -> small difficulty)
		int result = (int) Math.ceil((1 - ratio) * maxDifficultyValue);
		LOGGER.info("calculated difficulty for {} statements: ratio={}, 1-ratio={}, (1-ratio)*maxDifficultyValue={}, difficulty={}", numberOfStatements, ratio, 1-ratio, (1-ratio)*maxDifficultyValue, result);
		return result;
	}

	private void calculateDifficulties() {
		cumulativeCount = new TreeMap<>();
		long count = 0;
		for (final Entry<Integer, Integer> entry : itemCountByNumberOfStatements.entrySet()) {
			count += entry.getValue();
			cumulativeCount.put(entry.getKey(), count);
		}
		LOGGER.info("The cumulative statement distribution is {}", cumulativeCount);
	}
}
