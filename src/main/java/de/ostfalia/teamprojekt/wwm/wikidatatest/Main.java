package de.ostfalia.teamprojekt.wwm.wikidatatest;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.CharacterInWorkQuestionType;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.QuestionType;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.SharedBordersQuestion;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.SubclassOfQuestionType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
				inputFileName = "reduced.json.gz";
				break;
			case "borders":
				questionType = new SharedBordersQuestion();
				inputFileName = "borders.json";
				break;
			case "maerchen":
				questionType = new CharacterInWorkQuestionType();
				inputFileName = "reducedwithproperties.json.gz";
				break;
			default:
				throw new IllegalArgumentException("Bitte Argument übergeben!");
		}
	}

	/**
	 * Runs the example program.
	 */
	public static void main(String[] args) throws IOException {
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

		String input;
		Scanner in = new Scanner(System.in);

		do {
			Pair<List<Question>, Stream<Question>> pair = takeN(questions, 200);
			questions = pair.getRight();
			pair.getLeft().forEach(System.out::println);
			System.out.println("PRINT MORE QUESTIONS? [Y/n]");
			input = in.nextLine();
		} while (!input.equalsIgnoreCase("n"));
	}

	private static <T> Pair<List<T>, Stream<T>> takeN(Stream<T> stream, int n) {
		if (n < 0) {
			throw new IllegalArgumentException();
		}
		Iterator<T> it = stream.iterator();
		List<T> list = new ArrayList<>(n);
		for (int i = 0; i < n && it.hasNext(); i++) {
			list.add(it.next());
		}
		return new ImmutablePair<>(list, StreamSupport.stream(Spliterators.spliterator(it, 0, 0), false));
	}

}
