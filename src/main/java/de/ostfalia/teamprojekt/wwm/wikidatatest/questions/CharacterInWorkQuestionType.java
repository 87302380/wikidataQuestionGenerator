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

public class CharacterInWorkQuestionType implements QuestionType {

//	private static final Logger LOGGER = LoggerFactory.getLogger(CharacterInWorkQuestionType.class);
//	private static final String PROPERTY_PRESENT_IN_WORK = "P1441";
	private static final String PROPERTY_GENRE = "P136";
	private static final String PROPERTY_CHARACTERS = "P674";
	private int counter = 0;

	private static final Map<String, Set<String>> workToCharacter = new HashMap<>();
	private static final Map<String, Set<String>> workType = new HashMap<>();
	private static final Map<String, String> workLabel = new HashMap<>();
	private static final Map<String, String> charactersLabel = new HashMap<>();

	public CharacterInWorkQuestionType() {

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

			for (StatementGroup sg : itemDocument.getStatementGroups()) {
				if (sg.getProperty().getId().equals(PROPERTY_GENRE)) {
                    workLabel.put(itemDocument.getEntityId().getId(),itemDocument.getLabels().get("de").getText());
                    for (Statement s : sg.getStatements()) {
                        ItemIdValue itemIdValue = (ItemIdValue) s.getValue();
                        if (itemIdValue!=null){
                            String  genre = itemIdValue.getId();
                            if (workType.get(genre)==null){
								Set<String> work = new HashSet<>();
								work.add(itemId);
								workType.put(genre,work);
							}else {
								Set<String> work = workType.get(genre);
								work.add(itemId);
							}
                        }
					}

				}
			}
			for (StatementGroup sg : itemDocument.getStatementGroups()){
				if (sg.getProperty().getId().equals(PROPERTY_CHARACTERS))	{

					Set<String> characters = new HashSet<>();

					for (Statement s : sg.getStatements()) {
                        ItemIdValue character = ((ItemIdValue)s.getValue());
                        if (character!=null) {
                            characters.add(character.getId());
                            charactersLabel.put(character.getId(), null);
                            workToCharacter.put(itemId,characters);
                        }
					}

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

			String correctAnswer = getCorrectAnswer();
            System.out.println(correctAnswer);
			List<String> answers = generateAnswers(correctAnswer);
			answers = idToLabel(answers, workLabel);

			String character = getRandomElement(workToCharacter.get(correctAnswer));
			String text ;
			if (charactersLabel.get(character)!=null){
				text = charactersLabel.get(character) + " kommt aus welchen folgenden Werken?";
			}else {
				text = character + " kommt aus welchen folgenden Werken?";
			}
			return new Question(text, ImmutableList.copyOf(answers));
		}

		private String randomAnswer(){
            String[] work = workToCharacter.keySet().toArray(new String[0]);
            return work[RANDOM.nextInt(work.length)];
        }

		private String getCorrectAnswer(){
			String correctAnswer = randomAnswer();
			while (true){
			    if (workType.containsKey(getWorkType(correctAnswer))){
                    if (workType.get(getWorkType(correctAnswer)).size()>=4){
                        break;
                    }else {
                        correctAnswer = randomAnswer();
                    }
                }else {
                    correctAnswer = randomAnswer();
                }

			}
            return correctAnswer;
		}

		private String getWorkType(String value){
			for (String key:workType.keySet()){
				if (workType.get(key).contains(value)){
					return key;
				}
			}
			return null;
		}

		private ImmutableList<String> generateAnswers(String correctAnswer) {
			String type = getWorkType(correctAnswer);
			Set<String> allAnswers = new HashSet<>(4);
			Set<String> allTheTypeWork = workType.get(type);
			allAnswers.add(correctAnswer);
			while (allAnswers.size() < 4 ) {
				String work = getRandomElement(allTheTypeWork);
				allAnswers.add(work);
			}

			ImmutableList.Builder<String> answers = new Builder<>();
			answers.addAll(allAnswers);

			return answers.build();
		}

		private static List<String> idToLabel(List<String> list, Map<String, String> map) {
			//return list.stream().map(map::get).collect(toList());
            List<String> finished = new ArrayList<>();
            for (String id : list) {
                if (map.containsKey(id)) {
                    String label = map.get(id);
                    finished.add(label);
                } else {
                    finished.add(id);
                }
            }
			return finished;
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
