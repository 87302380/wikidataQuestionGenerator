package de.ostfalia.teamprojekt.wwm.wikidatatest;

import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.LanguageFilter;
import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.PredicateItemFilter;
import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.StatementCleaner;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;

import java.io.IOException;

public class Main implements AutoCloseable {
	
	private static final String OUTPUT_FILE_NAME = "output.json";
	private static final String INPUT_FILE_NAME  = "./src/main/resources/firstlines.json.gz";
	
	private final DumpReader reader;
	private final DumpWriter writer;
	
	/**
	 * Constructor. Initializes various helper objects we use for the JSON
	 * serialization, and opens the file that we want to write to.
	 *
	 * @throws IOException if there is a problem opening the output file
	 */
	private Main() throws IOException {
		EntityDocumentProcessor processor =
				new PredicateItemFilter(
						new LanguageFilter(
								new StatementCleaner(/*TODO: insert real processor here*/null)
						), item -> true
				);
		
		this.reader = new DumpReader(INPUT_FILE_NAME, processor);
		this.writer = new DumpWriter(OUTPUT_FILE_NAME);
	}
	
	/**
	 * Runs the example program.
	 *
	 * @throws IOException if there was a problem in writing the output file
	 */
	public static void main(String[] args) throws IOException {
		IoHelpers.configureLogging();
		
		try (Main main = new Main()) {
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
