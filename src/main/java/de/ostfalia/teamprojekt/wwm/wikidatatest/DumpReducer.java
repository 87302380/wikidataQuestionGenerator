package de.ostfalia.teamprojekt.wwm.wikidatatest;

import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.LanguageFilter;
import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.PredicateItemFilter;
import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.StatementCleaner;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.PokemonTypeQuestion;
import de.ostfalia.teamprojekt.wwm.wikidatatest.questions.SharedBordersQuestionType;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

import java.io.IOException;
import java.util.function.Predicate;

public class DumpReducer implements AutoCloseable {

	private static final String INPUT_FILE_NAME = "wikidata-20181001-all.json.bz2";

	private final DumpReader reader;
	private final DumpWriter writer;

	//TODO write a method to prefilter all irrelevant Data that are never needed e.g.referrences or links

	/**
	 * Constructor. Initializes various helper objects we use for the JSON
	 * serialization, and opens the file that we want to write to.
	 *
	 * @param arg argument that was passed over from the caller and specifies the type of filtering
	 *
	 * @throws IOException if there is a problem opening the output file
	 */
	private DumpReducer(String arg) throws IOException {
		Predicate<ItemDocument> predicate;
		String outputFileName;
		switch (arg) {
			case "pokemon":
				predicate = new PokemonTypeQuestion()::itemRelevant;
				outputFileName = "pokemon.json";
				break;
			case "borders":
				predicate = new SharedBordersQuestionType()::itemRelevant;
				outputFileName = "borders.json";
				break;
			case "general":
				predicate = this::generalFilter;
				outputFileName = "reduced.json.gz";
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
						predicate
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

	private boolean generalFilter(final ItemDocument itemDocument) {
		// ignore scholarly articles
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals("P31")) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						// scholarly article
						if (v instanceof ItemIdValue && ((ItemIdValue) v).getId().equals("Q13442814")) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Closes the output. Should be called after the JSON serialization was
	 * finished.
	 */
	@Override public void close() {
		writer.close();
	}

}
