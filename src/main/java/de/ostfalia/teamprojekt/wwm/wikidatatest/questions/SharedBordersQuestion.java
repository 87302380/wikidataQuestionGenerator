package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import com.google.common.collect.ImmutableList;
import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.map.DefaultedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


/**
 *
 */
public class SharedBordersQuestion implements QuestionType {
	private static final Logger LOGGER = LoggerFactory.getLogger(SharedBordersQuestion.class);
	private static final String PROPERTY_CONTINENT = "P30";
	private static final String PROPERTY_INSTANCE_OF = "P31";
	private static final String PROPERTY_DISSOLVED_OR_ABOLISHED = "P576";
	private static final String PROPERTY_COUNTRY = "Q6256";
	private static final String PROPERTY_SHARES_BORDER = "P47";
	private static final Random RANDOM = new Random();
	private static final String WIKIDATA_SITE_URL = "http://www.wikidata.org/entity/";
	private Map<String, List<ItemDocument>> countriesByContinent = new HashMap<>();
	private Map<ItemDocument, List<String>> neighbours = DefaultedMap.<ItemDocument, List<String>>defaultedMap(new HashMap<>(), (Factory<List<String>>) ArrayList::new);
	private Map<ItemDocument, List<String>> transitiveNeighbours = DefaultedMap.<ItemDocument, List<String>>defaultedMap(new HashMap<>(), (Factory<List<String>>) ArrayList::new);
	

	public SharedBordersQuestion() {
		try (Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream("continents.csv"), "UTF-8")) {
			s.useDelimiter("[,\n]");
			s.nextLine(); // skip first line
			while (s.hasNext()) {
				String continentId = s.next();
				String label = s.next();
				countriesByContinent.put(continentId, new ArrayList<>());
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
		
		return Stream.generate(new BorderQuestionSupplier());
	}

	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		readDirectNeighbors(itemDocument);
		calculateTransitiveNeighbors();
	}

	private void calculateTransitiveNeighbors() {
		for (ItemDocument itemDocument : neighbours.keySet()) {

		}
	}

	private void readDirectNeighbors(ItemDocument itemDocument) {
		for (StatementGroup sg:itemDocument.getStatementGroups()){
			if (sg.getProperty().getId().equals(PROPERTY_CONTINENT)){
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							String continentId = ((ItemIdValue) v).getId();
							List<ItemDocument> countriesOfContinent = countriesByContinent.get(continentId);
							if (countriesOfContinent != null){
								LOGGER.info("{}: {}", itemDocument.getLabels().get("de").getText(), continentId);
								countriesOfContinent.add(itemDocument);
								Collection<String> neighbours = getNeighbours(itemDocument);
								this.neighbours.get(itemDocument).addAll(neighbours);
							}
						}
					}
				}
			}
		}
	}

	private Collection<String> getNeighbours(ItemDocument country) {
		Set<String>neighbours = new HashSet<>();
		for (StatementGroup sg:country.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_SHARES_BORDER)) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							neighbours.add(((ItemIdValue) v).getId());
						}
					}
				}
			}
		}
		return neighbours;
	}

	private class BorderQuestionSupplier implements Supplier<Question> {
		private final List<ItemDocument> countries;
		
		public BorderQuestionSupplier() {
			countries = countriesByContinent.values().stream().flatMap(Collection::stream).collect(toList());
		}

		@Override public Question get() {
			ItemDocument type = countries.get(RANDOM.nextInt(countries.size()));
			List<ItemDocument> country = countriesByContinent.get(type);
			String text = "Welches dieser Länder grenzt an " + continentLabels.get(type) + "?";
			String correctCountry = country.get(country.size() - 1).findLabel("de");

			Predicate<ItemDocument> pokemonHasType = i -> i.hasStatementValue(PROPERTY_SHARES_BORDER , new ItemIdValueImpl(country, WIKIDATA_SITE_URL));

			List<String> wrongCountries = RANDOM.ints(0, countries.size())
					.distinct()
					.mapToObj(countries::get)
					.filter(pokemonHasType.negate())
					.limit(3)
					.map(i -> i.findLabel("de"))
					.collect(toList());

			ImmutableList<String> answers = ImmutableList.<String>builder().add(correctCountry).addAll(wrongCountries).build();
			return new Question(text, answers);
		}
	}
}
