package de.ostfalia.teamprojekt.wwm.wikidatatest;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.LanguageFilter;
import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.PredicateItemFilter;
import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.StatementCleaner;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.PokemonTypeQuestion;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.QuestionType;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.SharedBordersQuestionType;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;

import java.io.IOException;

public class DumpReducer implements AutoCloseable {
	private static final String INPUT_FILE_NAME  = "src/main/resources/wikidata-20181001-all.json.bz2";
	
	private final DumpReader reader;
	private final DumpWriter writer;
	
	/**
	 * Constructor. Initializes various helper objects we use for the JSON
	 * serialization, and opens the file that we want to write to.
	 *
	 * @throws IOException if there is a problem opening the output file
	 * @param arg argument that was passed over from the caller and specifies the type of filtering
	 */
	private DumpReducer(String arg) throws IOException {
		QuestionType q;
		String output_file_name;
		switch (arg) {
			case "pokemon":
				q = new PokemonTypeQuestion();
				output_file_name = "pokemon.json";
				break;
			case "borders":
				q = new SharedBordersQuestionType();
				output_file_name = "borders.json";
				break;
			default:
				throw new IllegalArgumentException("Bitte Argument übergeben!");
		}

		this.writer = new DumpWriter(output_file_name);
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
