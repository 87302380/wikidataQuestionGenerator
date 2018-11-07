package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.*;
import java.util.stream.Stream;


/**
 *
 */
public class SharedBordersQuestion implements QuestionType {
	private static final Logger LOGGER = LoggerFactory.getLogger(SharedBordersQuestion.class);
	private static final String PROPERTY_CONTINENT = "P30";
	private static final String PROPERTY_INSTANCE_OF = "P31";
	private static final String PROPERTY_DISSOLVED_OR_ABOLISHED = "P576";
	private static final String PROPERTY_COUNTRY = "Q6256";
	private Map<String, List<ItemDocument>> countriesByContinent = new HashMap<>();
	private Map<String, String> typeLabels = new HashMap<>();

	public SharedBordersQuestion() {
		try (Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream("continents.csv"), "UTF-8")) {
			s.useDelimiter("[,\n]");
			s.nextLine(); // skip first line
			while (s.hasNext()) {
				String typeId = s.next();
				String label = s.next();
				countriesByContinent.put(typeId, new ArrayList<>());
				typeLabels.put(typeId, label);
			}
		}
	}

	/**
	 * @param itemDocument
	 * @return
	 */
	@Override
	public boolean itemRelevant(ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {

			if (sg.getProperty().getId().equals(PROPERTY_DISSOLVED_OR_ABOLISHED)){ return false; }

			if (!sg.getProperty().getId().equals(PROPERTY_INSTANCE_OF)) { continue; }

			for (Statement s : sg.getStatements()) {
				if (s.getClaim().getMainSnak() instanceof ValueSnak) {
					Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
					if (v instanceof ItemIdValue && ((ItemIdValue) v).getId().equals(PROPERTY_COUNTRY)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override public Stream<Question> generateQuestions() {

		return Stream.empty();
	}

	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		for (StatementGroup sg:itemDocument.getStatementGroups()){
			if (sg.getProperty().getId().equals(PROPERTY_CONTINENT)){
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							String borderTypeId = ((ItemIdValue) v).getId();
							List<ItemDocument> borderingCountries = countriesByContinent.get(borderTypeId);
							if (borderingCountries != null){
								LOGGER.info("{}: {}", itemDocument.getLabels().get("de").getText(), borderTypeId);
								countriesByContinent.get(borderTypeId).add(itemDocument);
							}
						}
					}
				}
			}
		}
	}

}
