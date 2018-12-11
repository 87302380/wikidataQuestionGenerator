package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import com.google.common.collect.ImmutableList;
//import com.google.common.collect.Iterators;
import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.apache.commons.collections4.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

//import static java.util.stream.Collectors.toList;


public class SharedBordersQuestion implements QuestionType {

	private static final Logger LOGGER = LoggerFactory.getLogger(SharedBordersQuestion.class);
//	private static final String PROPERTY_INSTANCE_OF = "P31";
//	private static final String PROPERTY_COUNTRY = "Q6256";
	private static final String PROPERTY_SHARES_BORDER = "P47";
	private static final String PROPERTY_CONSTRAINT = "P2302";
	private static final String ITEM_TYPE_CONSTRAINT = "Q21503250";
	private static final String ITEM_VALUE_TYPE_CONSTRAINT = "Q21510865";
	private static final Random RANDOM = new Random();
	private final Map<String, ItemDocument> itemMap = new HashMap<>();
	private ArrayList<PropertyDocument> propertyList = new ArrayList<>();
	private int counter = 0;

//	private static Set<ItemIdValue> getNeighbours(ItemDocument country) {
//		Set<ItemIdValue> neighbours = new HashSet<>();
//		for (StatementGroup sg : country.getStatementGroups()) {
//			if (sg.getProperty().getId().equals(PROPERTY_SHARES_BORDER)) {
//				for (Statement s : sg.getStatements()) {
//					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
//						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
//						if (v instanceof ItemIdValue) {
//							neighbours.add((ItemIdValue) v);
//						}
//					}
//				}
//			}
//		}
//		return neighbours;
//	}

	@Override
	public void onStartDumpReading() {
		counter++;
	}

	@Override
	public boolean canGenerateQuestions() {
		return counter == 2;
	}

	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {
		if (counter == 2) {
			return;
		}
		for (StatementGroup sg : propertyDocument.getStatementGroups()) {
			if (sg.getProperty().getId().equals(PROPERTY_CONSTRAINT)) {
				Set<ItemIdValue> typeConstraint = null;
				Set<ItemIdValue> valueConstraint = null;

				for (Statement s : sg.getStatements()) {
					Value value = s.getValue();
					if (value instanceof ItemIdValue) {
						ItemIdValue v = (ItemIdValue) value;
						if (v.getId().equals(ITEM_TYPE_CONSTRAINT)) {
							typeConstraint = getQualifierIds(s);
						}
						if (v.getId().equals(ITEM_VALUE_TYPE_CONSTRAINT)) {
							valueConstraint = getQualifierIds(s);
						}
					}
				}
				if (typeConstraint != null && valueConstraint != null) {
//					Set<ItemIdValue> intersection = SetUtils.intersection(typeConstraint, valueConstraint);
//					if (!intersection.isEmpty()) {
					if (typeConstraint.equals(valueConstraint)){
						propertyList.add(propertyDocument);
					}
				}
			}
		}
	}

	private Set<ItemIdValue> getQualifierIds(Statement s) {
		Set<ItemIdValue> result;
		result = new HashSet<>();
		for (SnakGroup snakGroup : s.getQualifiers()) {
			if (snakGroup.getProperty().getId().equals("P2308")) {
				for (Snak snak : snakGroup.getSnaks()) {
					if (snak instanceof ValueSnak) {
						Value snakValue = ((ValueSnak) snak).getValue();
						if (snakValue instanceof ItemIdValue) {
							result.add(((ItemIdValue) snakValue));
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		if (counter == 1) {
			return;
		}
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			for (PropertyDocument propertyDocument : propertyList) {
				if (!sg.getProperty().getId().equals(propertyDocument.getEntityId().getId())) {
					continue;
				}
				itemMap.put(itemDocument.getEntityId().getId(), itemDocument);
				return;
			}
		}
	}

	@Override
	public Stream<Question> generateQuestions() {
		ArrayList<ItemDocument> items = new ArrayList<>(itemMap.values());
//		// not in the dump, but neighbor of another country (eg greenland, soviet union)
//		Set<ItemIdValue> invalidCountryIds = new HashSet<>();
//		for (Iterator<Country> iterator = countries.iterator(); iterator.hasNext(); ) {
//			final Country country = iterator.next();
//			if (country.itemDocument == null) {
//				iterator.remove();
//				invalidCountryIds.add(country.id);
//			}
//		}
//		countries.forEach(c -> c.adjacentCountries.removeIf(invalidCountryIds::contains));
//		for (Country country : countries) {
//			calculateTransitiveNeighbors(country);
//		}
		return Stream.generate(new QuestionSupplier(items));
	}

//	private void calculateTransitiveNeighbors(Country c) {
//		for (ItemIdValue neighborId : c.adjacentCountries) {
//			Optional<Country> neighbor = getCountryForId(neighborId);
//			if (!neighbor.isPresent()) {
//				// just created, is invalid
//				continue;
//			}
//			Set<ItemIdValue> neighbourNeighbours = getNeighbours(neighbor.get().itemDocument);
//			neighbourNeighbours.removeIf(c.adjacentCountries::contains);
//			c.transitiveNeighbors.addAll(neighbourNeighbours);
//		}
//		c.transitiveNeighbors.removeIf(n -> n.equals(c.id));
//	}


	private class QuestionSupplier implements Supplier<Question> {
		private final List<ItemDocument> items;
		private final List<PropertyDocument> properties;

		QuestionSupplier(ArrayList<ItemDocument> items) {
			this.items = new ArrayList<>(items);
			this.properties = new ArrayList<>(propertyList);
		}

		@Override
		public Question get() {
			final ItemDocument item = items.get(RANDOM.nextInt(items.size()));
			final PropertyDocument property = getRandomProperty(item);

			String correctAnswer =
					item.findStatementGroup(property.getEntityId().getId()).getSubject().getId();
//					findCountryById(Iterators.get(country.adjacentCountries.iterator(), country.adjacentCountries.size() - 1)).orElseThrow(
//					IllegalStateException::new).name;

			String text = item.getEntityId() + " grenzt an " + "... ?";

			/*List<ItemIdValue> transitiveNeighbors = new ArrayList<>(country.transitiveNeighbors);

			List<String> wrongAnswers = RANDOM.ints(0, transitiveNeighbors.size())
					.distinct()
					.mapToObj(transitiveNeighbors::get)
					.map(this::findCountryById)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.limit(3)
					.map(c -> c.name)
					.collect(toList());*/

			ImmutableList<String> answers = ImmutableList.<String>builder().add(correctAnswer)/*.addAll(wrongAnswers)*/.build();
			return new Question(text, answers, 1);
		}

		private PropertyDocument getRandomProperty(ItemDocument item) {
			PropertyDocument property ;
			boolean found = false;
			do {
				property = properties.get(RANDOM.nextInt(items.size()-1));
				for (StatementGroup sg : item.getStatementGroups()) {
					if (!sg.getProperty().getId().equals(property.getEntityId().getId())) {
						continue;
					}
					found = true;
					break;
				}
			}while (found);
			return property;
		}

	}
}
