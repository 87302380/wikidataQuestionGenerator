package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import de.ostfalia.teamprojekt.wwm.wikidatatest.DifficultyCalculator;
import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class SubclassOfQuestionType implements QuestionType {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubclassOfQuestionType.class);
	private static final String PROPERTY_INSTANCE_OF = "P31";
	private static final String PROPERTY_SUBCLASS_OF = "P279";
	private static final Random RANDOM = new Random();
	private static final String LANGUAGE = "de";
	private final DifficultyCalculator difficultyCalculator = new DifficultyCalculator(15);
	private Map<String, Category> categories = new HashMap<>();
	private Map<ItemIdValue, SubCategory> subCategoriesById;
	private int numberOfDumpReading = 0;

	public SubclassOfQuestionType() { }

	@Override public boolean canGenerateQuestions() {
		return numberOfDumpReading == 3;
	}

	@Override public void onStartDumpReading() {
		numberOfDumpReading++;
		LOGGER.info("Dump reading nr. {}", numberOfDumpReading);
		if (numberOfDumpReading == 2) {
			subCategoriesById = categories.values().parallelStream()
					.flatMap(c -> c.subCategories.parallelStream())
					.collect(toMap(sc -> sc.itemDocument.getEntityId(), Function.identity(), (sc1, sc2) -> sc1));
		} else if (numberOfDumpReading == 3) {
			LOGGER.info("found {} categories", categories.size());
			categories.values().forEach(c -> c.subCategories.removeIf(s -> s.itemDocument.findLabel(LANGUAGE) == null));
			categories = categories.entrySet().stream().filter(e -> e.getValue().subCategories.size() >= 2).collect(toMap(Entry::getKey, Entry::getValue));
			LOGGER.info("found {} categories with more than 2 subcategories", categories.size());
			categories = categories.entrySet().stream().filter(e -> e.getValue().itemDocument != null).collect(toMap(Entry::getKey, Entry::getValue));
			LOGGER.info("{} of those categories have an itemdocument", categories.size());
			categories.values()
					.stream()
					.sorted(Comparator.comparingInt((Category c) -> c.subCategories.size()).reversed())
					.limit(20)
					.forEachOrdered(c -> System.out.println(c.itemDocument.findLabel(LANGUAGE) + " has " + c.subCategories.size() + " subcategories"));
		}
	}

	@Override public Stream<Question> generateQuestions(Optional<Integer> difficulty) {
		categories.values()
				.stream()
				.flatMap(c -> c.subCategories.stream())
				.flatMap(s -> s.instances.stream())
				.forEach(i -> difficultyCalculator.registerStatementCount(i.numberOfStatements));

		for (Iterator<Category> categoriesIterator = categories.values().iterator(); categoriesIterator.hasNext(); ) {
			final Category c = categoriesIterator.next();
			c.subCategories.removeIf(sc -> sc.instances.isEmpty());

			if (c.subCategories.isEmpty()) {
				categoriesIterator.remove();
			}
		}


		return Stream.generate(new SubclassOfQuestionSupplier());
	}

	@Override public void processItemDocument(final ItemDocument itemDocument) {

		if (numberOfDumpReading == 1) {
			// find things that have the property "subclass of"
			for (StatementGroup sg : itemDocument.getStatementGroups()) {
				if (sg.getProperty().getId().equals(PROPERTY_SUBCLASS_OF)) {
					Set<Category> parents = new HashSet<>();
					for (Statement s : sg.getStatements()) {
						if (s.getClaim().getMainSnak() instanceof ValueSnak) {
							Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
							if (v instanceof ItemIdValue) {
								ItemIdValue value = ((ItemIdValue) v);
								Category parent = findOrCreateCategoryWithId(value);
								parents.add(parent);
							}
						}
					}
					SubCategory subCategoryForItemDocument = new SubCategory();
					subCategoryForItemDocument.itemDocument = itemDocument;
					parents.forEach(p -> p.subCategories.add(subCategoryForItemDocument));
				}
			}
		} else if (numberOfDumpReading == 2) {
			// store the itemDocument for the categories (needed for labels later)
			Category category = categories.get(itemDocument.getEntityId().getId());
			if (category != null) {
				category.itemDocument = itemDocument;
			}
		} else if (numberOfDumpReading == 3) {
			// store items that are instance of one of our subcategories
			for (StatementGroup sg : itemDocument.getStatementGroups()) {
				if (sg.getProperty().getId().equals(PROPERTY_INSTANCE_OF)) {
					for (Statement s : sg.getStatements()) {
						Value v = s.getValue();
						if (v instanceof ItemIdValue) {
							ItemIdValue value = ((ItemIdValue) v);
							SubCategory subCategory = subCategoriesById.get(value);
							if (subCategory != null) {
								String germanLabel = itemDocument.findLabel(LANGUAGE);
								if (germanLabel != null) {
									Item i = new Item();
									i.label = germanLabel;
									i.numberOfStatements = Iterators.size(itemDocument.getAllStatements());
									subCategory.instances.add(i);
								}
							}
						}
					}
				}
			}
		}
	}

	private Category findOrCreateCategoryWithId(final ItemIdValue idValue) {
		Category category = categories.get(idValue.getId());
		if (category == null) {
			category = new Category();
			categories.put(idValue.getId(), category);
		}
		return category;
	}


	public static class Category {
		private final Set<SubCategory> subCategories = new HashSet<>();
		private ItemDocument itemDocument;

		private Stream<Item> allItems() {
			return subCategories.stream().flatMap(s -> s.instances.stream());
		}
	}




	public static class SubCategory {
		private final Set<Item> instances = new HashSet<>();
		private ItemDocument itemDocument;
	}




	public static class Item {
		String label;
		int numberOfStatements;
	}




	public class SubclassOfQuestionSupplier implements Supplier<Question> {
		private final List<Category> categoryList;

		private SubclassOfQuestionSupplier() {
			categoryList = new ArrayList<>(categories.values());
			if (categoryList.isEmpty()) {
				throw new IllegalStateException();
			}
		}

		@Override public Question get() {
			do {
				Category randomCategory = categoryList.get(RANDOM.nextInt(categoryList.size()));
				List<SubCategory> subCategories = new ArrayList<>(randomCategory.subCategories);
				if (subCategories.isEmpty()) {
					continue;
				}

				SubCategory randomSubCategory = subCategories.get(RANDOM.nextInt(subCategories.size()));
				List<Item> correctAnswers = new ArrayList<>(randomSubCategory.instances);
				if (correctAnswers.isEmpty()) {
					continue;
				}

				Item correctAnswer = correctAnswers.get(RANDOM.nextInt(correctAnswers.size()));

				String subCategoryLabel = randomSubCategory.itemDocument.findLabel("de");
				if (correctAnswer.label.toLowerCase().contains(subCategoryLabel.toLowerCase())) {
					continue;
				}

				String text = "Welches ist ein " + subCategoryLabel + "?";

				final String finalCorrectAnswerLabel = correctAnswer.label;
				List<String> allWrongAnswers = subCategories.stream()
						.filter(c -> c != randomSubCategory)
						.flatMap(c -> c.instances.stream())
						.map(i -> i.label)
						.filter(l -> !l.equals(finalCorrectAnswerLabel))
						.distinct()
						.collect(toList());

				List<String> wrongAnswers;
				if (allWrongAnswers.size() < 3) {
					continue;
				} else if (allWrongAnswers.size() == 3) {
					wrongAnswers = allWrongAnswers;
				} else {
					wrongAnswers = RANDOM.ints(0, allWrongAnswers.size())
							.distinct()
							.limit(3)
							.mapToObj(allWrongAnswers::get)
							.collect(toList());
				}

				ImmutableList<String> answers = ImmutableList.<String>builder().add(correctAnswer.label).addAll(wrongAnswers).build();
				int difficulty = difficultyCalculator.getDifficulty(correctAnswer.numberOfStatements);
				return new Question(text, answers, difficulty);

			} while (true);
		}
	}
}
