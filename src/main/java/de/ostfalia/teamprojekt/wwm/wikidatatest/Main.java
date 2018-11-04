package de.ostfalia.teamprojekt.wwm.wikidatatest;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.MaerchenFigurQuestion;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.PokemonTypeQuestion;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.QuestionType;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.SharedBordersQuestionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
			case "maerchen":
				questionType = new MaerchenFigurQuestion();
				inputFileName = "maerchen.json";
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
		reader.start();
		Stream<Question> questions = questionType.generateQuestions();
		questions.limit(10).forEach(System.out::println);
	}

}
