package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class SubclassOfQuestionType implements QuestionType {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubclassOfQuestionType.class);
	private static final String PROPERTY_INSTANCE_OF = "P31";
	private static final String PROPERTY_SUBCLASS_OF = "P279";
	private static final Comparator<ItemDocument> STATEMENT_COUNT_COMPARATOR = Comparator.comparingInt(i -> Iterators.size(i.getAllStatements()));
	private static final Random RANDOM = new Random();
	private static final int ESTIMATED_NUMBER_OF_WELL_KNOWN_POKEMON_PER_TYPE = 50;

	private Set<Category> categories = new HashSet<>();
	private final Map<String, List<ItemDocument>> pokemonByType = new HashMap<>();
	private final Map<String, String> typeLabels = new HashMap<>();
	private int numberOfDumpReading = 0;

	public SubclassOfQuestionType() { }

	@Override public boolean canGenerateQuestions() {
		return numberOfDumpReading == 2;
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

		return Stream.generate(new PokemonQuestionSupplier(wellKnownPokemon));
	}

	@Override public void onStartDumpReading() {
		numberOfDumpReading++;
		if (numberOfDumpReading == 2) {
			LOGGER.info("found {} categories", categories.size());
			categories = categories.stream().filter(c -> c.subCategories.size() >= 2).collect(toSet());
			LOGGER.info("found {} categories with more than 2 subcategories", categories.size());
			LOGGER.info("found {} categories without an itemdocument", categories.stream().filter(c -> c.itemDocument == null).count());
			categories.stream().filter(c -> c.itemDocument != null).sorted(Comparator.comparingInt(c -> c.subCategories.size())).limit(1000).forEachOrdered(c -> System.out.println(c.itemDocument.findLabel("de")));
		}
	}

	@Override public void processItemDocument(final ItemDocument itemDocument) {
		if (numberOfDumpReading == 1) {
			Optional<Category> category = categories.stream().filter(c -> c.getId() == itemDocument.getEntityId()).findAny();
			category.ifPresent(c -> c.itemDocument = itemDocument);
			for (StatementGroup sg : itemDocument.getStatementGroups()) {
				if (sg.getProperty().getId().equals(PROPERTY_SUBCLASS_OF)) {
					Set<Category> parents = new HashSet<>();
					for (Statement s : sg.getStatements()) {
						if (s.getClaim().getMainSnak() instanceof ValueSnak) {
							Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
							if (v instanceof ItemIdValue) {
								ItemIdValue value = ((ItemIdValue) v);
								Category parent = findOrCreateCategoryWithId(value);
								parents.add(parent);
//								LOGGER.info("{} is subclass of {}", itemDocument.getLabels().get("de").getText(), value);
							}
						}
					}
					SubCategory subCategoryForItemDocument = new SubCategory();
					subCategoryForItemDocument.itemDocument = itemDocument;
					subCategoryForItemDocument.parents = parents;
					parents.forEach(p -> p.subCategories.add(subCategoryForItemDocument));
				}
			}
		} else if (numberOfDumpReading == 2) {
			for (StatementGroup sg : itemDocument.getStatementGroups()) {
				if (sg.getProperty().getId().equals(PROPERTY_INSTANCE_OF)) {
					for (Statement s : sg.getStatements()) {
						if (s.getClaim().getMainSnak() instanceof ValueSnak) {
							Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
							if (v instanceof ItemIdValue) {
								ItemIdValue value = ((ItemIdValue) v);
								Optional<SubCategory> subCategory = findSubCategory(value);
								subCategory.ifPresent(sc -> sc.instances.add(itemDocument));
							}
						}
					}
				}
			}
		}
	}

	private Category findOrCreateCategoryWithId(final ItemIdValue idValue) {
		Optional<Category> optCategory = categories.stream().filter(c -> c.getId().equals(idValue)).findFirst();
		if (optCategory.isPresent()) {
			return optCategory.get();
		} else {
			Category c = new Category();
			c.id = idValue;
			categories.add(c);
			return c;
		}
	}

	private Optional<SubCategory> findSubCategory(final ItemIdValue value) {
		return categories.stream().flatMap(c -> c.subCategories.stream()).filter(s -> s.itemDocument.getEntityId().equals(value)).findFirst();
	}




	private class PokemonQuestionSupplier implements Supplier<Question> {
		private final ImmutableList<String> types;
		private final List<ItemDocument> wellKnownPokemon;

		private PokemonQuestionSupplier(final List<ItemDocument> wellKnownPokemon) {
			types = ImmutableList.copyOf(typeLabels.keySet());
			this.wellKnownPokemon = wellKnownPokemon;
		}

		@Override public Question get() {
			String type = types.get(RANDOM.nextInt(types.size()));
			List<ItemDocument> pokemon = pokemonByType.get(type);
			String text = "Welches dieser Pokemon ist ein " + typeLabels.get(type) + "?";
			String correctPokemon = pokemon.get(Math.min(pokemon.size() - 1, RANDOM.nextInt(ESTIMATED_NUMBER_OF_WELL_KNOWN_POKEMON_PER_TYPE))).findLabel("de");

			Predicate<ItemDocument> pokemonHasType = i -> i.hasStatementValue(PROPERTY_INSTANCE_OF, Datamodel.makeWikidataItemIdValue(type));

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




	private class Category {
		private final Set<SubCategory> subCategories = new HashSet<>();
		private ItemDocument itemDocument;
		private ItemIdValue id; // only used when we haven't encountered this
		private ItemIdValue getId() {
			return id == null ? itemDocument.getEntityId() : id;
		}
	}




	private class SubCategory {
		private final Set<ItemDocument> instances = new HashSet<>();
		private ItemDocument itemDocument;
		private Set<Category> parents;

		private Set<SubCategory> siblings() {
			return parents.stream().flatMap(c -> c.subCategories.stream()).filter(c -> c != this).collect(toSet());
		}
	}
}
