package de.ostfalia.teamprojekt.wwm.wikidatatest;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.FairyTaleCharacterQuestionType;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.SubclassOfQuestionType;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.QuestionType;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.SharedBordersQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private final QuestionType questionType;
	private final String inputFileName;

	/**
	 * Constructor. Initializes various helper objects we use for the JSON
	 * serialization, and opens the file that we want to write to.
	 */
	private Main(String argument) {
		switch (argument) {
			case "pokemon":
				questionType = new SubclassOfQuestionType();
				inputFileName = "reduced2.json.gz";
				break;
			case "borders":
				questionType = new SharedBordersQuestion();
				inputFileName = "borders.json";
				break;
			case "maerchen":
				questionType = new FairyTaleCharacterQuestionType();
				inputFileName = "maerchen.json";
				break;
			default:
				throw new IllegalArgumentException("Bitte Argument übergeben!");
		}
	}

	/**
	 * Runs the example program.
	 *
	 */
	public static void main(String[] args) throws IOException{
		IoHelpers.configureLogging();

		if (args.length < 1) {
			LOGGER.error("no argument given");
			System.exit(1);
		}
		Main main = new Main(args[0]);
		main.start();
	}

	public void start() throws IOException {
		do {
			questionType.onStartDumpReading();
			final DumpReader reader = new DumpReader("results/" + inputFileName, questionType);
			reader.start();
		} while (!questionType.canGenerateQuestions());
		Stream<Question> questions = questionType.generateQuestions();
		questions.limit(50).forEach(System.out::println);
	}

}
