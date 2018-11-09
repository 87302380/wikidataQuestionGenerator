package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class FairyTaleCharacterQuestionType implements QuestionType {

	private static final Logger LOGGER = LoggerFactory.getLogger(FairyTaleCharacterQuestionType.class);
	private static final String PROPERTY_PRESENT_IN_WORK = "P1441";
	private static final String FAIRY_TALE_PATH = "fairyTaleResources/fairyTales.csv";
	private static final Map<String, Set<String>> fairyTaleToCharacters = new HashMap<>();

	public FairyTaleCharacterQuestionType() {
		if (fairyTaleToCharacters.isEmpty()) {
			try (Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream(FAIRY_TALE_PATH), "UTF-8")) {
				s.useDelimiter(",");
				while (s.hasNext()) {
					fairyTaleToCharacters.put(s.next(), new HashSet<>(1000));
					s.nextLine();
				}
			}
		}
	}

	@Override public boolean itemRelevant(final ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_PRESENT_IN_WORK)) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue && fairyTaleToCharacters.containsKey(((ItemIdValue) v).getId())) {
							// german label might not exist
							//LOGGER.log(Level.INFO, itemDocument.getLabels().get("de").getText() + ": " + ((ItemIdValue) v).getId());
							LOGGER.info("{} is a character in {}", itemDocument.getEntityId().getId(), ((ItemIdValue) v).getId());
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override public Stream<Question> generateQuestions() {

		QuestionGenerator questionGenerator;
		try {
			questionGenerator = new QuestionGenerator();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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
							fairyTaleToCharacters.get(fairyTaleId).add(itemDocument.getLabels().get("de").getText());
						}
					}
				}
			}
		}
	}

	private static class QuestionGenerator {

		private static final Random RANDOM = new Random();
		private final Map<String, String> idToLabel;
		private final Map<String, List<String>> characterToFairyTales;

		private static final String CHARACTERS_PATH = "fairyTaleResources/characters.csv";
		private static final String ANSWER_PATH = "fairyTaleResources/answers.csv";

		private QuestionGenerator() throws IOException {

			String[] fairyTales = readLines(FAIRY_TALE_PATH);
			String[] characters = readLines(CHARACTERS_PATH);
			String[] answer = readLines(ANSWER_PATH);

			// assumes no ids occur more than once
			this.idToLabel = Arrays.stream(mergeArray(fairyTales, characters))
					.map(line -> line.split(","))
					.collect(toMap(parts -> parts[0], parts -> parts[1]));
			this.characterToFairyTales = mapGenerator(answer);

		}

		private static String[] readLines(String filename) throws IOException {
			InputStream is = FairyTaleCharacterQuestionType.class.getClassLoader().getResourceAsStream(filename);
			String content = IOUtils.toString(is, StandardCharsets.UTF_8);
			return content.split("\n");
		}

		private static String[] mergeArray(String[] a, String[] b) {
			String[] c = new String[a.length + b.length];
			System.arraycopy(a, 0, c, 0, a.length);
			System.arraycopy(b, 0, c, a.length, b.length);
			return c;
		}

		/**
		 * Convert lines of a csv file to a map of all key-value-pairs.
		 * <p>
		 * Example:
		 * <pre>
		 *     String[] lines = {"a,1", "b,3", "a,2"};
		 *     Map&lt;String, List&lt;String&gt;&gt; m = mapGenerator(lines);
		 * </pre>
		 * m will contain the mappings "a" -> ["1", "2"] and "b" -> ["3"].
		 *
		 * @param lines the lines of a csv file with 2 columns
		 *
		 * @return a mapping of every value in the first column to all values of the right column that occur on the same line.
		 */
		private static Map<String, List<String>> mapGenerator(String[] lines) {
			return Arrays.stream(lines).map(line -> line.split(",")).collect(groupingBy(parts -> parts[0], mapping(parts -> parts[1], toList())));
		}

		private Question getQuestion() {

			String characterInQuestion = randomCharacter();
			List<String> answers = generateAnswers(characterInQuestion);

			characterInQuestion = idToLabel.get(characterInQuestion);
			answers = idToLabel(answers, idToLabel);

			String text = characterInQuestion + " kommt aus welchen folgenden Märchen?";

			return new Question(text, ImmutableList.copyOf(answers));
		}

		private String randomCharacter() {
			String[] characters = characterToFairyTales.keySet().toArray(new String[0]);
			return characters[RANDOM.nextInt(characters.length)];
		}

		private ImmutableList<String> generateAnswers(String correctAnswer) {

			ArrayList<String> allCharacters = new ArrayList<>(characterToFairyTales.keySet());

			Set<String> wrongAnswers = new HashSet<>(4);

			List<String> fairyTalesWithCorrectCharacter = characterToFairyTales.get(correctAnswer);

			while (wrongAnswers.size() < 3) {

				String randomCharacter = allCharacters.get(RANDOM.nextInt(allCharacters.size()));
				List<String> fairyTalesWithRandomCharacter = characterToFairyTales.get(randomCharacter);
				String randomFairyTale = fairyTalesWithRandomCharacter.get(RANDOM.nextInt(fairyTalesWithRandomCharacter.size()));
				if (fairyTalesWithCorrectCharacter.contains(randomFairyTale)) {
					// dont put correct things in yet
					continue;
				}
				wrongAnswers.add(randomFairyTale);
			}


			ImmutableList.Builder<String> answers = new Builder<>();
			// add the correct answer
			answers.add(fairyTalesWithCorrectCharacter.get(RANDOM.nextInt(fairyTalesWithCorrectCharacter.size())));
			answers.addAll(wrongAnswers);

			return answers.build();
		}

		private static List<String> idToLabel(List<String> list, Map<String, String> map) {
			return list.stream().map(map::get).peek(e -> Objects.requireNonNull(e, "no label for " + e)).collect(toList());
		}
	}

}
