package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PokemonTypeQuestion implements QuestionType {
	
	private static final Logger                   LOGGER        = Logger.getLogger(PokemonTypeQuestion.class.getName());
	private static       Map<String, Set<String>> pokemonByType = new HashMap<>();
	
	public PokemonTypeQuestion() {
		if (pokemonByType.isEmpty()) {
			try (Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream("pokemontypes.csv"), "UTF-8")) {
				s.useDelimiter(",");
				s.nextLine(); // skip first line
				while (s.hasNext()) {
					pokemonByType.put(s.next(), new HashSet<>(1000));
					s.nextLine();
				}
			}
		}
	}
	
	@Override public boolean itemRelevant(final ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			// subclass of
			if (sg.getProperty().getId().equals("P279")) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						// "Q3966183" is "pokemon species" on Wikidata
						if (v instanceof ItemIdValue && pokemonByType.containsKey(((ItemIdValue) v).getId())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	@Override public void processItemDocument(final ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			// subclass of
			if (sg.getProperty().getId().equals("P279")) {
				for (Statement s : sg.getStatements()) {
					if (s.getClaim().getMainSnak() instanceof ValueSnak) {
						Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
						if (v instanceof ItemIdValue) {
							String pokemonTypeId = ((ItemIdValue) v).getId();
							pokemonByType.get(pokemonTypeId).add(itemDocument.getLabels().get("de").getText());
							LOGGER.log(Level.INFO, itemDocument.getLabels().get("de").getText() + ": " + pokemonTypeId);
						}
					}
				}
			}
		}
	}
	
	@Override public boolean hasNext() {
		return false;
	}
	
	@Override public Question next() {
		return null;
	}
}
