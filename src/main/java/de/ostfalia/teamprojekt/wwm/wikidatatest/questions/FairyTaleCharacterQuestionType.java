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
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;


import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class FairyTaleCharacterQuestionType implements QuestionType {

	private static final Logger LOGGER = LoggerFactory.getLogger(FairyTaleCharacterQuestionType.class);
	private static final String PROPERTY_PRESENT_IN_WORK = "P1441";
	private static final String PROPERTY_INSTANCE_OF = "P136";
//	private static final String PROPERTY_CHARACTERS = "P674";
//	private static final String FAIRY_TALE_PATH = "fairyTaleResources/fairyTales.csv";

	private static final Map<String, Set<String>> fairyTaleToCharacters = new HashMap<>();
	private static final Map<String, String> fairyTaleLabel = new HashMap<>();
	private static final Set<String> genreProperty = new HashSet<>();

	public FairyTaleCharacterQuestionType() {
		addGenreProperty("Q699");
		addGenreProperty("Q7832362");
		addGenreProperty("Q20730955");

//		if (fairyTaleToCharacters.isEmpty()) {
//			try (Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream(FAIRY_TALE_PATH), "UTF-8")) {
//				s.useDelimiter("\n");
//				while (s.hasNext()) {
//					String[] fairyTale = s.next().split(",");
//					fairyTaleToCharacters.put(fairyTale[0], new HashSet<>(1000));
//					fairyTaleLabel.put(fairyTale[0],fairyTale[1]);
//					s.nextLine();
//				}
//			}
//		}
	}

	private void addGenreProperty(String fairyEntity){
		genreProperty.add(fairyEntity);
	}

	@Override public boolean canGenerateQuestions() {
		return true;
	}

	@Override public Stream<Question> generateQuestions() {

		QuestionGenerator questionGenerator = new QuestionGenerator();

		return Stream.generate(questionGenerator::getQuestion);
	}

	@Override public void processItemDocument(final ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_PRESENT_IN_WORK)) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							String fairyTaleId = ((ItemIdValue) v).getId();
							LOGGER.info("{} is a character in {}", itemDocument.getLabels().get("de").getText(), fairyTaleId);
							Set<String> characters = fairyTaleToCharacters.get(fairyTaleId);
							if (characters != null) {
								characters.add(itemDocument.getLabels().get("de").getText());
							}
						}
					}
				}
			}
			if (sg.getProperty().getId().equals(PROPERTY_INSTANCE_OF)) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							if (genreProperty.contains(((ItemIdValue) v).getId())){
								fairyTaleToCharacters.put(s.getClaim().getSubject().getId()
										, new HashSet<>(1000));
								fairyTaleLabel.put(s.getClaim().getSubject().getId()
										,itemDocument.getLabels().get("de").getText());
							}
						}
					}
				}
			}
		}
	}

	private static class QuestionGenerator {

		private static final Random RANDOM = new Random();

		QuestionGenerator(){
			ArrayList<String> emptyKeyValue = new ArrayList<>();
			for (String key : fairyTaleToCharacters.keySet()){
				if (fairyTaleToCharacters.get(key).isEmpty()){
					emptyKeyValue.add(key);
				}
			}
			for (String key : emptyKeyValue){
				fairyTaleToCharacters.remove(key);
			}
		}

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

			answers = idToLabel(answers, fairyTaleLabel);

			Iterator character = fairyTaleToCharacters.get(fairyTaleInQuestion).iterator();
			String text = character.next() + " kommt aus welchen folgenden Märchen?";

			return new Question(text, ImmutableList.copyOf(answers));
		}

		private String randomFairyTale() {

			String[] FairyTale = fairyTaleToCharacters.keySet().toArray(new String[0]);
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
	}

}
