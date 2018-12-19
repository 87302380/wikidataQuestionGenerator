package de.ostfalia.teamprojekt.wwm.wikidatatest;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.CharacterInWorkQuestionType;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.QuestionType;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.SharedBordersQuestion;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.SubclassOfQuestionType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private final QuestionType questionType;
	private final Optional<Integer> difficulty;
	private final String inputFileName = "reduced.json.gz";

	/**
	 * Constructor. Initializes various helper objects we use for the JSON
	 * serialization, and opens the file that we want to write to.
	 */
	public Main(final QuestionType questionType, final Optional<Integer> difficulty) {
		this.questionType = questionType;
		this.difficulty = difficulty;
	}


	/**
	 * Runs the example program.
	 */
	public static void main(String[] args) throws IOException {
		IoHelpers.configureLogging();

		Options os = new Options();
		os.addOption("d", "difficulty", true, "Only output questions with this difficulty (1 to 15)");
		os.addRequiredOption("t", "type", true, "The question type (subclass, transitive or characters)");

		Optional<Integer> difficulty;
		QuestionType questionType;

		try {
			CommandLine arguments = new DefaultParser().parse(os, args);
			switch (arguments.getOptionValue("t")) {
				case "subclass":
					questionType = new SubclassOfQuestionType();
					break;
				case "transitive":
					questionType = new SharedBordersQuestion();
					break;
				case "characters":
					questionType = new CharacterInWorkQuestionType();
					break;
				default:
					throw new ParseException("unknown question type");
			}
			if (arguments.hasOption("d")) {
				String difficultyStr = arguments.getOptionValue("d");
				int d = Integer.parseInt(difficultyStr);
				if (d < 1 || d > 15) {
					throw new ParseException("invalid diffucilty");
				}
				difficulty = Optional.of(d);
			} else {
				difficulty = Optional.empty();
			}
		} catch (ParseException | NumberFormatException e) {
			new HelpFormatter().printHelp("java " + Main.class.getName(), os, true);
			System.exit(1);
			throw null;
		}

		Main main = new Main(questionType, difficulty);
		main.start();
	}

	public void start() throws IOException {
		do {
			questionType.onStartDumpReading();
			final DumpReader reader = new DumpReader("results/" + inputFileName, questionType);
			reader.start();
		} while (!questionType.canGenerateQuestions());
		Stream<Question> questions = questionType.generateQuestions(difficulty);
		if (difficulty.isPresent()) {
			int d = difficulty.get();
			questions = questions.filter(q -> q.difficulty == d);
		}

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
