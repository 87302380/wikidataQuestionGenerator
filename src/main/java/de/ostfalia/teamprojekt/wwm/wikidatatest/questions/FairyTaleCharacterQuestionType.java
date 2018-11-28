package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;


import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class FairyTaleCharacterQuestionType implements QuestionType {

	private static final Logger LOGGER = LoggerFactory.getLogger(FairyTaleCharacterQuestionType.class);
//	private static final String PROPERTY_PRESENT_IN_WORK = "P1441";
	private static final String PROPERTY_GENRE = "P136";
	private static final String PROPERTY_CHARACTERS = "P674";
	private int counter = 0;

	private static final Map<String, Set<String>> workToCharacter = new HashMap<>();
	private static final Map<String, String> workLabel = new HashMap<>();
	private static final Map<String, String> charactersLabel = new HashMap<>();
	private static final Set<String> work = new HashSet<>();
	private static final Set<String> genreProperty = new HashSet<>();

	public FairyTaleCharacterQuestionType() {

		addGenreProperty("Q699");
		addGenreProperty("Q7832362");
		addGenreProperty("Q20730955");

	}
	private void addGenreProperty(String fairyEntity){
		genreProperty.add(fairyEntity);             //add need Work
	}


	@Override
	public void onStartDumpReading() {
		counter++;
	}

	@Override
	public boolean canGenerateQuestions() {
		return counter == 2;
	}

	@Override public Stream<Question> generateQuestions() {

		QuestionGenerator questionGenerator = new QuestionGenerator();

		return Stream.generate(questionGenerator::getQuestion);
	}

	@Override public void processItemDocument(final ItemDocument itemDocument) {
		String itemId = itemDocument.getEntityId().getId();
		if (counter == 2){
			if (charactersLabel.containsKey(itemId)){
				String characterName = itemDocument.getLabels().get("de").getText();
				charactersLabel.put(itemId,characterName);
			}

		}else if (counter == 1){

			boolean isNeedWork = false;

			for (StatementGroup sg : itemDocument.getStatementGroups()) {
				if (sg.getProperty().getId().equals(PROPERTY_GENRE)) {
					for (Statement s : sg.getStatements()) {
                        ItemIdValue itemIdValue = (ItemIdValue) s.getValue();
                        if (itemIdValue!=null){
                            String  genre = itemIdValue.getId();
                            if (genreProperty.contains(genre)) {
                                isNeedWork = true;
                            }
                        }
					}

				}
			}
			for (StatementGroup sg : itemDocument.getStatementGroups()){
				if (sg.getProperty().getId().equals(PROPERTY_CHARACTERS)&&isNeedWork)	{

					workLabel.put(itemDocument.getEntityId().getId(),itemDocument.getLabels().get("de").getText());

					Set<String> characters = new HashSet<>();

					for (Statement s : sg.getStatements()) {
						String character = ((ItemIdValue)s.getValue()).getId();
						characters.add(character);
						charactersLabel.put(character,null);
					}
					workToCharacter.put(itemId,characters);

				}
			}
		}
	}

	private static class QuestionGenerator {

		private static final Random RANDOM = new Random();


//		/**
//		 * Convert lines of a csv file to a map of all key-value-pairs.
//		 * <p>
//		 * Example:
//		 * <pre>
//		 *     String[] lines = {"a,1", "b,3", "a,2"};
//		 *     Map&lt;String, List&lt;String&gt;&gt; m = mapGenerator(lines);
//		 * </pre>
//		 * m will contain the mappings "a" -> ["1", "2"] and "b" -> ["3"].
//		 *
//		 * @param lines the lines of a csv file with 2 columns
//		 *
//		 * @return a mapping of every value in the first column to all values of the right column that occur on the same line.
//		 */
//		private static Map<String, List<String>> mapGenerator(String[] lines) {
//			return Arrays.stream(lines).map(line -> line.split(",")).collect(groupingBy(parts -> parts[0], mapping(parts -> parts[1], toList())));
//		}

		private Question getQuestion() {

			String fairyTaleInQuestion = randomFairyTale();
			List<String> answers = generateAnswers(fairyTaleInQuestion);

			answers = idToLabel(answers, workLabel);

			String character = getRandomElement(workToCharacter.get(fairyTaleInQuestion));
			String text ;
			if (charactersLabel.get(character)!=null){
				text = charactersLabel.get(character) + " kommt aus welchen folgenden Märchen?";
			}else {
				text = character + " kommt aus welchen folgenden Märchen?";
			}
			return new Question(text, ImmutableList.copyOf(answers));
		}

		private String randomFairyTale() {
			String[] FairyTale = workToCharacter.keySet().toArray(new String[0]);
			return FairyTale[RANDOM.nextInt(FairyTale.length)];
		}

		private ImmutableList<String> generateAnswers(String correctAnswer) {

			Set<String> allAnswers = new HashSet<>(4);

			allAnswers.add(correctAnswer); 			// add the correct answer
			while (allAnswers.size() < 4) {

				String randomFairyTales = randomFairyTale();
				if (!randomFairyTales.equals(correctAnswer)){
					allAnswers.add(randomFairyTales);
				}
			}

			ImmutableList.Builder<String> answers = new Builder<>();
			answers.addAll(allAnswers);

			return answers.build();
		}

		private static List<String> idToLabel(List<String> list, Map<String, String> map) {
			return list.stream().map(map::get).peek(e -> Objects.requireNonNull(e, "no label for " + e)).collect(toList());
		}
		private static <E> E getRandomElement(Set<E> set){
			int rn = (int) (Math.random() * (set.size()));
			int i = 0;
			for (E e : set) {
				if(i==rn){
					return e;
				}
				i++;
			}
			return null;
		}
	}

}
