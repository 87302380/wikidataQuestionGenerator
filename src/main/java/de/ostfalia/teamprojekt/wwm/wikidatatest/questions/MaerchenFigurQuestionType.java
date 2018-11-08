package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import com.google.common.collect.ImmutableList;
import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MaerchenFigurQuestionType implements QuestionType {

	private static final Logger LOGGER = LoggerFactory.getLogger(MaerchenFigurQuestionType.class);
	private static Map<String, Set<String>> maerchenFigur = new HashMap<>();

	public MaerchenFigurQuestionType() {
		if (maerchenFigur.isEmpty()) {
			try (Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream("maerchenResources/maerchenList.csv"), "UTF-8")) {
				s.useDelimiter(",");
				while (s.hasNext()) {
					maerchenFigur.put(s.next(), new HashSet<>(1000));
					s.nextLine();
				}
			}
		}
	}

	@Override public boolean itemRelevant(final ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals("P1441")) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue && maerchenFigur.containsKey(((ItemIdValue) v).getId())) {
							// german label might not exist
							//LOGGER.log(Level.INFO, itemDocument.getLabels().get("de").getText() + ": " + ((ItemIdValue) v).getId());
							LOGGER.info(itemDocument.getEntityId().getId() + " is of type " + ((ItemIdValue) v).getId());
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

		Supplier<Question> questions = questionGenerator::getQuestion;

		return Stream.generate(questions);
	}

	@Override public void processItemDocument(final ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals("P1441")) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							String maerchen = ((ItemIdValue) v).getId();
							maerchenFigur.get(maerchen).add(itemDocument.getLabels().get("de").getText());
							LOGGER.info(itemDocument.getLabels().get("de").getText() + ": " + maerchen);
						}
					}
				}
			}
		}
	}

	public static class QuestionGenerator {

		Map<String, List<String>> maerchenList = new HashMap<>();
		Map<String, List<String>> map = new HashMap<>();
		private String maerchenPath = "./src/main/resources/maerchenResources/maerchenList.csv";
		private String maerchenFigurPath = "./src/main/resources/maerchenResources/maerchenFigurList.csv";
		private String answerPath = "./src/main/resources/maerchenResources/antwort.csv";

		private QuestionGenerator() throws IOException {

			String[] maerchen = this.dataRead(maerchenPath);
			String[] maerchenFigur = dataRead(maerchenFigurPath);
			String[] answer = dataRead(answerPath);

			mapGenerator(this.maerchenList, mergeArray(maerchen, maerchenFigur));
			mapGenerator(this.map, answer);

		}

		String[] dataRead(String input) throws IOException {
			File file = new File(input);
			String content = FileUtils.readFileToString(file, "UTF-8");

			return content.split("\n");
		}

		private void mapGenerator(Map<String, List<String>> map, String[] answer) {

			for (String a : answer) {
				String[] entity = a.split(",");
				if (map.get(entity[0]) == null) {
					List<String> answerList = new ArrayList<>();
					answerList.add(entity[1]);
					map.put(entity[0], answerList);
				} else {
					map.get(entity[0]).add(entity[1]);
				}
			}
		}

		private String[] mergeArray(String[] a, String[] b) {
			String[] c = new String[a.length + b.length];
			System.arraycopy(a, 0, c, 0, a.length);
			System.arraycopy(b, 0, c, a.length, b.length);
			return c;
		}

		private Question getQuestion() {

			List<String> enty = entityGenerator(this.map);
			List<String> option = optionGenerator(enty, this.map);

			idToLabel(enty, this.maerchenList);
			idToLabel(option, this.maerchenList);

			return questionLoad(enty, option);

		}

		private List<String> entityGenerator(Map<String, List<String>> map) {
			String[] key = map.keySet().toArray(new String[0]);

			Random random = new Random();

			List<String> entityList = new ArrayList<>();

			while (entityList.size() < 4) {

				boolean isNewOption = true;
				String option = key[random.nextInt(key.length)];
				for (String string : entityList) {
					if (option.equals(string)) {
						isNewOption = false;
					}
				}
				if (isNewOption) {
					entityList.add(option);
				}
			}

			return entityList;
		}

		private List<String> optionGenerator(List<String> entityList, Map<String, List<String>> map) {

			String[] key = map.keySet().toArray(new String[0]);

			Random random = new Random();

			List<String> optionlist = new ArrayList<>();

			optionlist.add(map.get(entityList.get(0)).get(random.nextInt(map.get(entityList.get(0)).size())));

			while (optionlist.size() < 4) {

				boolean isNewOption = true;

				String keyValue = key[random.nextInt(key.length)];
				String option = map.get(keyValue).get(random.nextInt(map.get(keyValue).size()));

				for (String string : optionlist) {
					if (option.equals(string)) {
						isNewOption = false;
						break;
					}
				}
				if (isNewOption) {
					optionlist.add(option);
				}

			}

			return optionlist;
		}

		private void idToLabel(List<String> list, Map<String, List<String>> maerchenList) {
			for (int i = 0; i < list.size(); i++) {
				if (maerchenList.containsKey(list.get(i))) {
					list.set(i, maerchenList.get(list.get(i)).get(0));
				}
			}
		}

		private Question questionLoad(List<String> entylist, List<String> optionlist) {

			String text = entylist.get(0) + " kommt aus welchen folfenden Märchen?";

			return new Question(text, ImmutableList.copyOf(optionlist));
		}

	}

}
