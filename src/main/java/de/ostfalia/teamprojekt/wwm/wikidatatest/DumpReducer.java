package de.ostfalia.teamprojekt.wwm.wikidatatest;

import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.LanguageFilter;
import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.PredicateItemFilter;
import de.ostfalia.teamprojekt.wwm.wikidatatest.processors.StatementCleaner;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

import java.io.IOException;

public class DumpReducer implements AutoCloseable {

	private static final String INPUT_FILE_NAME = "results/reduced.json.gz";
	private static final String PROPERTY_INSTANCE_OF = "P31";
	private static final String ITEM_GENE = "Q7187";
	private static final String ITEM_PROTEIN = "Q8054";
	private static final String ITEM_WIKIMEDIA_DISAMBIGUATION_PAGE = "Q4167410";
	private static final String ITEM_SCHOLARLY_ARTICLE = "Q13442814";


	private final DumpReader reader;
	private final DumpWriter writer;

	/**
	 * Constructor. Initializes various helper objects we use for the JSON
	 * serialization, and opens the file that we want to write to.
	 *
	 * @param arg argument that was passed over from the caller and specifies the type of filtering
	 *
	 * @throws IOException if there is a problem opening the output file
	 */
	private DumpReducer(String arg) throws IOException {
		if (!arg.equals("general")) {
			throw new IllegalArgumentException("Bitte Argument übergeben!");
		}

		this.writer = new DumpWriter("reduced2.json.gz");
		EntityDocumentProcessor processor =
				new PredicateItemFilter(
						new LanguageFilter(
								new StatementCleaner(this.writer)
						),
						DumpReducer::generalFilter
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

	private static boolean generalFilter(final ItemDocument itemDocument) {
		// ignore some stuff
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_INSTANCE_OF)) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (!(v instanceof ItemIdValue)) {
							return false;
						}
						ItemIdValue value = (ItemIdValue) v;
						if (value.getId().equals(ITEM_GENE)
						    || value.getId().equals(ITEM_PROTEIN)
						    || value.getId().equals(ITEM_WIKIMEDIA_DISAMBIGUATION_PAGE)
						    || value.getId().equals(ITEM_SCHOLARLY_ARTICLE)) {
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
