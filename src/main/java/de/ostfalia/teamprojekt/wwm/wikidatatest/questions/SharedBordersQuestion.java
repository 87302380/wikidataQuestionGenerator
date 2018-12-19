package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import de.ostfalia.teamprojekt.wwm.wikidatatest.DifficultyCalculator;
import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;



public class SharedBordersQuestion implements QuestionType {

	private static final String PROPERTY_CONSTRAINT = "P2302";
	private static final Logger LOGGER = LoggerFactory.getLogger(SharedBordersQuestion.class);
	private static final String ITEM_TYPE_CONSTRAINT = "Q21503250";
	private static final String ITEM_VALUE_TYPE_CONSTRAINT = "Q21510865";
	private static final Random RANDOM = new Random();
	private static final String PROPERTY_CLASS_CONSTRAINT = "P2308";
	private final DifficultyCalculator difficultyCalculator = new DifficultyCalculator(15);
	private final Map<String, ItemDocument> itemMap = new HashMap<>();
	private final Map<String, String> propertyMap = new HashMap<>();
	private final Map<String, Integer> counterMap = new HashMap<>();
	private final List<String> toAdd = new ArrayList<>();

	private int counter = 0;

	@Override
	public boolean canGenerateQuestions() {
		return counter == 2;
	}

	@Override
	public void onStartDumpReading() {
		counter++;
	}

	@Override
	public Stream<Question> generateQuestions(Optional<Integer> difficulty) {
		LOGGER.info("{} {}", counterMap, propertyMap.values());
		ArrayList<ItemDocument> items = new ArrayList<>(itemMap.values());
		for (ItemDocument value : itemMap.values()) {
			difficultyCalculator.registerStatementCount(Iterators.size(value.getAllStatements()));
		}
		return Stream.generate(new QuestionSupplier(items));
	}


	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {
		if (counter == 3 || counter == 2) {
			return;
		}

		for (StatementGroup sg : propertyDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_CONSTRAINT)) {
				Set<ItemIdValue> typeConstraint = null;
				Set<ItemIdValue> valueConstraint = null;

				for (Statement s : sg.getStatements()) {
					Value value = s.getValue();
					if (value instanceof ItemIdValue) {
						ItemIdValue v = (ItemIdValue) value;
						if (v.getId().equals(ITEM_TYPE_CONSTRAINT)) {
							typeConstraint = getQualifierIds(s);
						}
						if (v.getId().equals(ITEM_VALUE_TYPE_CONSTRAINT)) {
							valueConstraint = getQualifierIds(s);
						}
					}
				}
				if (typeConstraint != null && typeConstraint.equals(valueConstraint) && propertyDocument.findLabel("de") != null) {
					propertyMap.put(propertyDocument.getEntityId().getId(), propertyDocument.findLabel("de"));
				}
			}
		}
	}

	private static Set<ItemIdValue> getQualifierIds(Statement s) {
		Set<ItemIdValue> result = new HashSet<>();
		for (SnakGroup snakGroup : s.getQualifiers()) {
			if (snakGroup.getProperty().getId().equals(PROPERTY_CLASS_CONSTRAINT)) {

				for (Snak snak : snakGroup.getSnaks()) {
					if (snak instanceof ValueSnak) {
						Value snakValue = ((ValueSnak) snak).getValue();
						if (snakValue instanceof ItemIdValue) {
							result.add(((ItemIdValue) snakValue));
						}
					}
				}
			}
		}
		return result;
	}


	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		if (counter == 1) {
			return;
		}
		if (counter == 3){
			return;
		}
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (!propertyMap.containsKey(sg.getProperty().getId())) {
				continue;
			}

			itemMap.put(itemDocument.getEntityId().getId(), itemDocument);
			if (counterMap.containsKey(sg.getProperty().getId())){
				counterMap.put(sg.getProperty().getId(),counterMap.get(sg.getProperty().getId())+1);
			} else {
				counterMap.put(sg.getProperty().getId(),1);
			}
			return;

		}
	}

	private List<ItemDocument> getAnswerList(String id, String prop) {
		ItemDocument obj = itemMap.get(id);
		ArrayList<ItemDocument> answers = new ArrayList<>();
		if (obj == null) {
			return Collections.emptyList();
		}
		StatementGroup sg = obj.findStatementGroup(prop);
		if (sg == null) {
			return Collections.emptyList();
		}
		for (Statement statement : sg) {
			if (!(statement.getValue() instanceof ItemIdValue)) {
				continue;
			}
			String answer = ((ItemIdValue) statement.getValue()).getId();
			ItemDocument getAnswer = itemMap.get(answer);
			if (getAnswer == null) {
				continue;
			}
			answers.add(getAnswer);
		}
		return answers;
	}

	private class QuestionSupplier implements Supplier<Question> {
		private final List<ItemDocument> items;

		QuestionSupplier(ArrayList<ItemDocument> items) {
			this.items = new ArrayList<>(items);
		}

		@Override
		public Question get() {
			do {
				final ItemDocument item = items.get(RANDOM.nextInt(items.size()));
				final String property = getRandomProperty(item);

				List<ItemDocument> correctAnswers = getAnswerList(item.getEntityId().getId(), property);
				if (correctAnswers.isEmpty()){
//					LOGGER.warn("{} hat keine Werte für {}",item.findLabel("de"),property );
					continue;
				}
				String text = item.findLabel("de") + " " + propertyMap.get(property) + "... ?";

				Set<ItemDocument> transitiveAnswers = new HashSet<>();
				for (ItemDocument answer : correctAnswers) {
					transitiveAnswers.addAll(getAnswerList(answer.getEntityId().getId(), property));
				}

				transitiveAnswers.removeAll(correctAnswers);
				if (transitiveAnswers.size() < 3) {
					continue;
				}
				List<String> wrongAnswers;
				if (transitiveAnswers.size() == 3) {
					wrongAnswers = transitiveAnswers.stream().map(itemDocument -> itemDocument.findLabel("de")).collect(toList());
				} else {
					List<ItemDocument> trans = new ArrayList<>(transitiveAnswers);
					wrongAnswers = RANDOM.ints(0, trans.size())
							.mapToObj(trans::get)
							.distinct()
							.limit(3)
							.map(itemDocument -> itemDocument.findLabel("de"))
							.collect(toList());
				}
				ItemDocument correctAnswer = correctAnswers.size() == 1 ? correctAnswers.get(0) : correctAnswers.get(RANDOM.nextInt(correctAnswers.size() - 1));
				ImmutableList<String> answers = ImmutableList.<String>builder().add(correctAnswer.findLabel("de")).addAll(wrongAnswers).build();
				int difficulty = difficultyCalculator.getDifficulty(Iterators.size(item.getAllStatements()));
				return new Question(text, answers, difficulty);
			} while (true);
		}

		private String getRandomProperty(ItemDocument item) {
			Set<String> itemProperties = item.getStatementGroups().stream().map(sg -> sg.getProperty().getId()).collect(toSet());
			itemProperties.retainAll(propertyMap.keySet());
			List<String> list = new ArrayList<>(itemProperties);
			return list.get(RANDOM.nextInt(list.size()));
		}

	}
}
