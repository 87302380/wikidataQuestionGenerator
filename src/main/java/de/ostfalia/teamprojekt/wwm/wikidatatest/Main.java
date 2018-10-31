package de.ostfalia.teamprojekt.wwm.wikidatatest;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.PokemonTypeQuestion;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.QuestionType;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.SharedBordersQuestionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	private final DumpReader reader;
	private final QuestionType questionType;

	/**
	 * Constructor. Initializes various helper objects we use for the JSON
	 * serialization, and opens the file that we want to write to.
	 */
	private Main(String argument) {
		String inputFileName;

		switch (argument) {
			case "pokemon":
				questionType = new PokemonTypeQuestion();
				inputFileName = "pokemon.json";
				break;
			case "borders":
				questionType = new SharedBordersQuestionType();
				inputFileName = "borders.json";
				break;
			default:
				throw new IllegalArgumentException("Bitte Argument übergeben!");
		}

		this.reader = new DumpReader("results/" + inputFileName, questionType);
	}

	/**
	 * Runs the example program.
	 *
	 */
	public static void main(String[] args) {
		IoHelpers.configureLogging();

		if (args.length < 1) {
			LOGGER.error("no argument given");
			System.exit(1);
		}
		Main main = new Main(args[0]);
		main.start();
	}

	public void start() {
		reader.start();
		Stream<Question> questions = questionType.generateQuestions();
		questions.limit(10).forEach(System.out::println);
	}

}
