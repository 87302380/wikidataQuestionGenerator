package de.ostfalia.teamprojekt.wwm.wikidatatest;

import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.LanguageFilter;
import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.PredicateItemFilter;
import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.StatementCleaner;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.PokemonTypeQuestion;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.QuestionType;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.SharedBordersQuestionType;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;

import java.io.IOException;

public class DumpReducer implements AutoCloseable {

	private static final String INPUT_FILE_NAME  = "wikidata-20181001-all.json.bz2";
	
	private final DumpReader reader;
	private final DumpWriter writer;

	//TODO write a method to prefilter all irrelevant Data that are never needed e.g.referrences or links

	/**
	 * Constructor. Initializes various helper objects we use for the JSON
	 * serialization, and opens the file that we want to write to.
	 *
	 * @throws IOException if there is a problem opening the output file
	 * @param arg argument that was passed over from the caller and specifies the type of filtering
	 */
	private DumpReducer(String arg) throws IOException {
		QuestionType q;
		String outputFileName;
		switch (arg) {
			case "pokemon":
				q = new PokemonTypeQuestion();
				outputFileName = "pokemon.json";
				break;
			case "borders":
				q = new SharedBordersQuestionType();
				outputFileName = "borders.json";
				break;
			default:
				throw new IllegalArgumentException("Bitte Argument übergeben!");
		}

		this.writer = new DumpWriter(outputFileName);
		EntityDocumentProcessor processor =
				new PredicateItemFilter(
						new LanguageFilter(
								new StatementCleaner(this.writer)
						),
						q::itemRelevant
				);
		
		this.reader = new DumpReader(INPUT_FILE_NAME, processor);
	}
	
	/**
	 * Runs the example program.
	 *
	 * @throws IOException if there was a problem in writing the output file
	 */
	public static void main(String[] args) throws IOException {
		IoHelpers.configureLogging();
		
		try (DumpReducer main = new DumpReducer(args[0])) {
			main.start();
		}
	}
	
	public void start() {
		reader.start();
	}
	
	/**
	 * Closes the output. Should be called after the JSON serialization was
	 * finished.
	 */
	@Override public void close() {
		writer.close();
	}
	
}
