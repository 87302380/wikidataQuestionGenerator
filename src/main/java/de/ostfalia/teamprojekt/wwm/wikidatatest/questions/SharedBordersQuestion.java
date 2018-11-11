package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.map.DefaultedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


public class SharedBordersQuestion implements QuestionType {

	class Country {
		// Wird gesetzt, sobald Nachbarn gefunden werden
		ItemIdValue id;
		Set<ItemIdValue> adjacentCountries = new HashSet<>();

		String name;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Country country = (Country) o;
			return Objects.equals(id, country.id);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(SharedBordersQuestion.class);
	private static final String PROPERTY_CONTINENT = "P30";
	private static final String PROPERTY_INSTANCE_OF = "P31";
	private static final String PROPERTY_DISSOLVED_OR_ABOLISHED = "P576";
	private static final String PROPERTY_COUNTRY = "Q6256";
	private static final String PROPERTY_SHARES_BORDER = "P47";
	private static final Random RANDOM = new Random();
	private static final String WIKIDATA_SITE_URL = "http://www.wikidata.org/entity/";

	private Set<Country> countries = new HashSet<>();

	public SharedBordersQuestion() {

	}

	@Override
	public boolean itemRelevant(ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {

			if (sg.getProperty().getId().equals(PROPERTY_DISSOLVED_OR_ABOLISHED)) {
				return false;
			}

			if (!sg.getProperty().getId().equals(PROPERTY_INSTANCE_OF)) {
				continue;
			}

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

	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_CONTINENT)) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							ItemIdValue continentId = ((ItemIdValue) v);
							LOGGER.info("{}: {}", itemDocument.getLabels().get("de").getText(), continentId);

							Country currentlyWorkingOn = getCountryForId(continentId);
							//currentlyWorkingOn.itemDocument = itemDocument;

							//TODO: Warum verschwindet Name?
							// Mit der aktuellen Impl müsste Finden von Relatives und das Bilden von Sätzen mit Nomen viel einfacher sein
							currentlyWorkingOn.name = itemDocument.findLabel("de") + "+";

							Set<ItemIdValue> neighbours = getNeighbours(itemDocument);
							for (ItemIdValue neighbour : neighbours) {
								Country countryForId = getCountryForId(neighbour);
								countryForId.adjacentCountries.add(continentId);
							}
						}
					}
				}
			}
		}
	}

	private Country getCountryForId(ItemIdValue id) {
		Optional<Country> country = countries.stream().filter(c -> Objects.equals(c.id, id)).findFirst();
		if(country.isPresent()) {
			// Dieses Country wurde schon mal gesehen (entweder als Item direkt, oder als Relationship)
			return country.get();
		} else {
			// Dieses Country wurde noch nie zuvor gesehen, wird angelegt
			Country c = new Country();
			c.id = id;
			countries.add(c);
			return c;
		}
	}

	private void calculateTransitiveNeighbors() {
		/*
		// Für jedes Land...
		for (Map.Entry<ItemDocument, List<String>> item : this.neighbours.entrySet()) {
			Set<String> transitiveNeighbors = new HashSet<>();

			// Für jeden Nachbar des Lands...
			List<String> idOfMyNeighbors = item.getValue();
			for (String idOfMyNeighbor : idOfMyNeighbors) {
				// ... werden die Nachbarn errechnet (Als die Nachbarn meiner Nachbarn)
				ItemDocument neighbor = getNeighborForId(idOfMyNeighbor);
				transitiveNeighbors.addAll(getNeighbours(neighbor));
			}

			// Hinweis: Die Nachbarn meiner Nachbarn beinhalten auf jeden Fall mich selbst.
			transitiveNeighbours.put(item.getKey(), new ArrayList<>(transitiveNeighbors));
		}
		*/
	}

	private Set<ItemIdValue> getNeighbours(ItemDocument country) {
		Set<ItemIdValue> neighbours = new HashSet<>();
		for (StatementGroup sg : country.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_SHARES_BORDER)) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							neighbours.add((ItemIdValue)v);
						}
					}
				}
			}
		}
		return neighbours;
	}

	@Override
	public Stream<Question> generateQuestions() {

		calculateTransitiveNeighbors();
		return Stream.generate(new BorderQuestionSupplier());
	}

	private class BorderQuestionSupplier implements Supplier<Question> {
		private final List<ItemDocument> countries = null;

		public BorderQuestionSupplier() {
			//countries = countriesByContinent.values().stream().flatMap(Collection::stream).collect(toList());
		}

		@Override
		public Question get() {
			/*
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
			*/
			return null;
		}
	}
}
