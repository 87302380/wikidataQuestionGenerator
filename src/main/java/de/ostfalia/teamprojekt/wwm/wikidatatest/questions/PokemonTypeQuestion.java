package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import com.google.common.collect.ImmutableList;
import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.apache.commons.compress.utils.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

public class PokemonTypeQuestion implements QuestionType {

	private static final Logger LOGGER = LoggerFactory.getLogger(PokemonTypeQuestion.class);
	private static final String PROPERTY_INSTANCE_OF = "P31";
	private static final String PROPERTY_FROM_FICTIONAL_UNIVERSE = "P1080";
	private static final String PROPERTY_DIFFERENT_FROM = "P1889";
	private static final String PROPERTY_BULBAPEDIA_ARTICLE = "P4845";
	private static final Set<String> IGNORE_STATEMENTS_WHEN_COUNTING = Sets.newHashSet(
			PROPERTY_FROM_FICTIONAL_UNIVERSE, PROPERTY_DIFFERENT_FROM, PROPERTY_BULBAPEDIA_ARTICLE
	);
	private static final Comparator<ItemDocument> STATEMENT_COUNT_COMPARATOR = Comparator.comparingInt(PokemonTypeQuestion::numberOfKnowledgeStatements);
	private static final Random RANDOM = new Random();
	private static final String WIKIDATA_SITE_URL = "http://www.wikidata.org/entity/";
	private static final int ESTIMATED_NUMBER_OF_WELL_KNOWN_POKEMON_PER_TYPE = 50;

	private Map<String, List<ItemDocument>> pokemonByType = new HashMap<>();
	private Map<String, String> typeLabels = new HashMap<>();

	public PokemonTypeQuestion() {
		try (Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream("pokemontypes.csv"), "UTF-8")) {
			s.useDelimiter("[,\n]");
			s.nextLine(); // skip first line
			while (s.hasNext()) {
				String typeId = s.next();
				String label = s.next();
				pokemonByType.put(typeId, new ArrayList<>());
				typeLabels.put(typeId, label);
			}
		}
	}

	private static int numberOfKnowledgeStatements(final ItemDocument itemDocument) {
		int count = 0;
		Iterator<Statement> it = itemDocument.getAllStatements();
		while (it.hasNext()) {
			Statement s = it.next();
			if (!IGNORE_STATEMENTS_WHEN_COUNTING.contains(s.getStatementId())) {
				count++;
			}
		}
		return count;
	}

	@Override public boolean itemRelevant(final ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_INSTANCE_OF)) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue && pokemonByType.containsKey(((ItemIdValue) v).getId())) {
							// german label might not exist
							//LOGGER.log(Level.INFO, itemDocument.getLabels().get("de").getText() + ": " + ((ItemIdValue) v).getId());
							LOGGER.info("{} is of type {}", itemDocument.getEntityId().getId(), ((ItemIdValue) v).getId());
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override public Stream<Question> generateQuestions() {
		for (final List<ItemDocument> pokemon : pokemonByType.values()) {
			pokemon.sort(STATEMENT_COUNT_COMPARATOR.reversed());
		}

		List<ItemDocument> wellKnownPokemon = pokemonByType.values()
				.stream()
				.map(Collection::stream)
				.map(x -> x.limit(ESTIMATED_NUMBER_OF_WELL_KNOWN_POKEMON_PER_TYPE))
				.flatMap(Function.identity())
				.distinct()
				.collect(toList());

		Iterator<Question> questions = new PokemonQuestionIterator(wellKnownPokemon);

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(questions, 0), false);
	}

	@Override public void processItemDocument(final ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_INSTANCE_OF)) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							String pokemonTypeId = ((ItemIdValue) v).getId();
							List<ItemDocument> pokemonWithThisType = pokemonByType.get(pokemonTypeId);
							if (pokemonWithThisType != null) {
								// instance of something that is a subclass of pokemon
								LOGGER.info("{}: {}", itemDocument.getLabels().get("de").getText(), pokemonTypeId);
								pokemonByType.get(pokemonTypeId).add(itemDocument);
							}
						}
					}
				}
			}
		}
	}

	private class PokemonQuestionIterator implements Iterator<Question> {
		private final List<ItemDocument> wellKnownPokemon;
		private Iterator<Entry<String, List<ItemDocument>>> pokemonByTypeIterator;

		public PokemonQuestionIterator(final List<ItemDocument> wellKnownPokemon) {
			this.wellKnownPokemon = wellKnownPokemon;
			pokemonByTypeIterator = pokemonByType.entrySet().iterator();
		}

		@Override public boolean hasNext() {
			return pokemonByTypeIterator.hasNext();
		}

		@Override public Question next() {
			Entry<String, List<ItemDocument>> entry = pokemonByTypeIterator.next();
			String type = entry.getKey();
			List<ItemDocument> pokemon = entry.getValue();
			String text = "Welches dieser Pokemon ist ein " + typeLabels.get(type) + "?";
			String correctPokemon = pokemon.get(Math.min(pokemon.size(), RANDOM.nextInt(ESTIMATED_NUMBER_OF_WELL_KNOWN_POKEMON_PER_TYPE))).findLabel("de");

			Predicate<ItemDocument> pokemonHasType = i -> i.hasStatementValue(PROPERTY_INSTANCE_OF, new ItemIdValueImpl(type, WIKIDATA_SITE_URL));

			List<String> wrongPokemon = RANDOM.ints(0, wellKnownPokemon.size())
					.distinct()
					.mapToObj(wellKnownPokemon::get)
					.filter(pokemonHasType.negate())
					.limit(3)
					.map(i -> i.findLabel("de"))
					.collect(toList());

			ImmutableList<String> answers = ImmutableList.<String>builder().add(correctPokemon).addAll(wrongPokemon).build();
			return new Question(text, answers);
		}
	}
}
