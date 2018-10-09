package eu.jendrik.wikidatatest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.DatamodelConverter;
import org.wikidata.wdtk.datamodel.helpers.DatamodelFilter;
import org.wikidata.wdtk.datamodel.helpers.JsonSerializer;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.implementation.DataObjectFactoryImpl;
import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.implementation.StatementGroupImpl;
import org.wikidata.wdtk.datamodel.interfaces.DocumentDataFilter;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This example illustrates how to create a JSON serialization of some of the
 * data found in a dump. It uses a {@link DatamodelConverter} with filter
 * settings to eliminate some of the data.
 * <p>
 * As an example, the program only serializes data for people who were born in
 * Dresden, Germany. This can be changed by modifying the code in
 * {@link #includeDocument(ItemDocument)}.
 *
 * @author Markus Kroetzsch
 */
public class JsonSerializationProcessor implements EntityDocumentProcessor, AutoCloseable {
	
	private static final String OUTPUT_FILE_NAME = "output-birthdates.json.bz2";
	
	private static final String DUMP_FILE = "../wikidata-20181001-all.json.bz2";
	
	private static final BigInteger PRINT_INTERVAL = BigInteger.valueOf(10_000);
	
	private final JsonSerializer jsonSerializer;
	
	private final DatamodelFilter datamodelFilter;
	
	private BigInteger entitiesWithBirthDate = BigInteger.ZERO;
	
	/**
	 * Constructor. Initializes various helper objects we use for the JSON
	 * serialization, and opens the file that we want to write to.
	 *
	 * @throws IOException if there is a problem opening the output file
	 */
	private JsonSerializationProcessor() throws IOException {
		// The filter is used to copy selected parts of the data. We use this
		// to remove some parts from the documents we serialize.
		DocumentDataFilter filter = new DocumentDataFilter();
		
		
		// Only copy English labels, descriptions, and aliases:
		filter.setLanguageFilter(Collections.singleton("de"));
		// Only copy statements of some properties:
		Set<PropertyIdValue> propertyFilter = new HashSet<>();
		propertyFilter.add(Datamodel.makeWikidataPropertyIdValue("P569")); // birthdate
		filter.setPropertyFilter(propertyFilter);
		// Do not copy any sitelinks:
		filter.setSiteLinkFilter(Collections.emptySet());
		
		
		this.datamodelFilter = new DatamodelFilter(new DataObjectFactoryImpl(), filter);
		
		
		// The (compressed) file we write to.
		OutputStream outputStream = new BufferedOutputStream(IoHelpers.openExampleFileOuputStream(OUTPUT_FILE_NAME));
		this.jsonSerializer = new JsonSerializer(outputStream);
		
		this.jsonSerializer.open();
	}
	
	/**
	 * Runs the example program.
	 *
	 * @throws IOException if there was a problem in writing the output file
	 */
	public static void main(String[] args) throws IOException {
		IoHelpers.configureLogging();
		
		try (JsonSerializationProcessor jsonSerializationProcessor = new JsonSerializationProcessor()) {
			DumpProcessingController dumpProcessingController = new DumpProcessingController("wikidatawiki");
			dumpProcessingController.registerEntityDocumentProcessor(jsonSerializationProcessor, null, true);
			
			MwLocalDumpFile mwDumpFile = new Bzip2DumpFile(DUMP_FILE);
			dumpProcessingController.processDump(mwDumpFile);
		}
	}
	
	/**
	 * Closes the output. Should be called after the JSON serialization was
	 * finished.
	 */
	public void close() {
		System.out.println("Serialized "
		                   + this.jsonSerializer.getEntityDocumentCount()
		                   + " item documents to JSON file " + OUTPUT_FILE_NAME + ".");
		this.jsonSerializer.close();
	}
	
	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		if (includeDocument(itemDocument)) {
			entitiesWithBirthDate = entitiesWithBirthDate.add(BigInteger.ONE);
			
			// remove unneeded properties
			itemDocument = this.datamodelFilter.filter(itemDocument);
			
			// create new statement groups, because we have to change the statements (remove the references and qualifiers)
			List<StatementGroup> newStatementGroups = new ArrayList<>();
			for (final StatementGroup sg : itemDocument.getStatementGroups()) {
				List<Statement> newStatements = new ArrayList<>();
				for (final Statement s : sg.getStatements()) {
					// create a new statement with the same value, but ignore the rest
					Statement newS = StatementBuilder.forSubjectAndProperty(itemDocument.getEntityId(), s.getMainSnak().getPropertyId())
					                                 .withValue(s.getValue())
					                                 .build();
					newS = new CorrectSerializeStatement(newS);
					newStatements.add(newS);
				}
				newStatementGroups.add(new StatementGroupImpl(newStatements));
			}
			
			// create a new item document with the nwe statement groups
			itemDocument = new ItemDocumentImpl(
					itemDocument.getEntityId(),
					new ArrayList<>(itemDocument.getLabels().values()),
					new ArrayList<>(itemDocument.getDescriptions().values()),
					itemDocument.getAliases().values().stream().flatMap(Collection::stream).collect(Collectors.toList()),
					newStatementGroups,
					Collections.emptyList(),
					0
			);
			
			if (entitiesWithBirthDate.mod(PRINT_INTERVAL).equals(BigInteger.ZERO)) {
				Logger.getLogger(getClass()).log(Level.INFO, "found " + entitiesWithBirthDate + " entities");
			}
			
			this.jsonSerializer.processItemDocument(itemDocument);
		}
	}
	
	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {
		// we do not serialize any properties
	}
	
	/**
	 * Returns true if the given document should be included in the
	 * serialization.
	 *
	 * @param itemDocument the document to check
	 *
	 * @return true if the document should be serialized
	 */
	private boolean includeDocument(ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			// "P31" is "instance of" on Wikidata
			if (!sg.getProperty().getId().equals("P31")) {
				continue;
			}
			for (Statement s : sg.getStatements()) {
				if (s.getClaim().getMainSnak() instanceof ValueSnak) {
					Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
					// "Q5" is "human" on Wikidata
					if (v instanceof ItemIdValue && ((ItemIdValue) v).getId().equals("Q5")) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
