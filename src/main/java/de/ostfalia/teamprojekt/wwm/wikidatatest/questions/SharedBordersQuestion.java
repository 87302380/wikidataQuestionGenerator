package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


public class SharedBordersQuestion implements QuestionType {

	private static final Logger LOGGER = LoggerFactory.getLogger(SharedBordersQuestion.class);
	private static final String PROPERTY_INSTANCE_OF = "P31";
	private static final String PROPERTY_COUNTRY = "Q6256";
	private static final String PROPERTY_SHARES_BORDER = "P47";
	private static final Random RANDOM = new Random();
	private final Set<Country> countries = new HashSet<>();

	@Override public boolean canGenerateQuestions() {
		return true;
	}

	@Override
	public Stream<Question> generateQuestions() {
		// not in the dump, but neighbor of another country (eg greenland, soviet union)
		Set<ItemIdValue> invalidCountryIds = new HashSet<>();
		for (Iterator<Country> iterator = countries.iterator(); iterator.hasNext(); ) {
			final Country country = iterator.next();
			if (country.itemDocument == null) {
				iterator.remove();
				invalidCountryIds.add(country.id);
			}
		}
		countries.forEach(c -> c.adjacentCountries.removeIf(invalidCountryIds::contains));
		for (Country country : countries) {
			calculateTransitiveNeighbors(country);
		}
		return Stream.generate(new BorderQuestionSupplier(countries));
	}

	private void calculateTransitiveNeighbors(Country c) {
		for (ItemIdValue neighborId : c.adjacentCountries) {
			Optional<Country> neighbor = getCountryForId(neighborId);
			if (!neighbor.isPresent()) {
				// just created, is invalid
				continue;
			}
			Set<ItemIdValue> neighbourNeighbours = getNeighbours(neighbor.get().itemDocument);
			neighbourNeighbours.removeIf(c.adjacentCountries::contains);
			c.transitiveNeighbors.addAll(neighbourNeighbours);
		}
		c.transitiveNeighbors.removeIf(n -> n.equals(c.id));
	}

	private Optional<Country> getCountryForId(final ItemIdValue id) {
		return countries.stream().filter(c -> Objects.equals(c.id, id)).findFirst();
	}

	private static Set<ItemIdValue> getNeighbours(ItemDocument country) {
		Set<ItemIdValue> neighbours = new HashSet<>();
		for (StatementGroup sg : country.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_SHARES_BORDER)) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							neighbours.add((ItemIdValue) v);
						}
					}
				}
			}
		}
		return neighbours;
	}

	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_INSTANCE_OF)) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							ItemIdValue value = ((ItemIdValue) v);
							if (!value.getId().equals(PROPERTY_COUNTRY)) {
								continue;
							}
							LOGGER.info("{}: {}", itemDocument.getLabels().get("de").getText(), value);

							Country currentlyWorkingOn = createOrGetCountryForId(itemDocument.getEntityId());

							//TODO: Warum verschwindet Name?
							// Mit der aktuellen Impl müsste Finden von Relatives und das Bilden von Sätzen mit Nomen viel einfacher sein
							currentlyWorkingOn.name = itemDocument.findLabel("de");
							currentlyWorkingOn.itemDocument = itemDocument;

							Set<ItemIdValue> neighbours = getNeighbours(itemDocument);
							currentlyWorkingOn.adjacentCountries.addAll(neighbours);
							for (ItemIdValue neighbour : neighbours) {
								Country countryForId = createOrGetCountryForId(neighbour);
								countryForId.adjacentCountries.add(itemDocument.getEntityId());
							}
						}
					}
				}
			}
		}
	}

	private Country createOrGetCountryForId(ItemIdValue id) {
		Optional<Country> country = getCountryForId(id);
		if (country.isPresent()) {
			// Dieses Country wurde schon mal gesehen (entweder als Item direkt, oder als Relationship)
			LOGGER.info("found country {}", id);
			return country.get();
		} else {
			// Dieses Country wurde noch nie zuvor gesehen, wird angelegt
			LOGGER.info("creating country {}", id);
			Country c = new Country();
			c.id = id;
			countries.add(c);
			return c;
		}
	}


	private static class BorderQuestionSupplier implements Supplier<Question> {
		private final List<Country> countries;

		BorderQuestionSupplier(Set<Country> countries) {
			this.countries = new ArrayList<>(countries);
		}

		@Override
		public Question get() {
			final Country country = getRandomCountry();

			String correctAnswer = findCountryById(Iterators.get(country.adjacentCountries.iterator(), country.adjacentCountries.size() - 1)).orElseThrow(
					IllegalStateException::new).name;

			String text = "Welches dieser Länder grenzt an " + country.name + "?";

			List<ItemIdValue> transitiveNeighbors = new ArrayList<>(country.transitiveNeighbors);

			List<String> wrongAnswers = RANDOM.ints(0, transitiveNeighbors.size())
					.distinct()
					.mapToObj(transitiveNeighbors::get)
					.map(this::findCountryById)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.limit(3)
					.map(c -> c.name)
					.collect(toList());

			ImmutableList<String> answers = ImmutableList.<String>builder().add(correctAnswer).addAll(wrongAnswers).build();
			return new Question(text, answers);
		}

		private Country getRandomCountry() {
			Country tmp;
			do {
				tmp = countries.get(RANDOM.nextInt(countries.size()));
			} while (tmp.adjacentCountries.isEmpty());
			return tmp;
		}

		private Optional<Country> findCountryById(final ItemIdValue id) {
			return countries.stream().filter(c -> c.id.equals(id)).findFirst();
		}
	}




	private static class Country {
		// Wird gesetzt, sobald Nachbarn gefunden werden
		ItemDocument itemDocument;
		ItemIdValue id;
		Set<ItemIdValue> adjacentCountries = new HashSet<>();
		Set<ItemIdValue> transitiveNeighbors = new HashSet<>();

		String name;

		@Override
		public int hashCode() {
			return Objects.hash(id);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Country country = (Country) o;
			return Objects.equals(id, country.id);
		}
	}
}
