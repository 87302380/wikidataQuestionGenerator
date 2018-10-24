package de.ostfalia.teamprojekt.wwm.wikidatatest.processors;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.LexemeDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

import java.util.Objects;
import java.util.function.Predicate;

public class PredicateItemFilter implements EntityDocumentProcessor {
	
	private final EntityDocumentProcessor next;
	private final Predicate<ItemDocument> predicate;
	
	public PredicateItemFilter(EntityDocumentProcessor next, Predicate<ItemDocument> predicate) {
		this.next = Objects.requireNonNull(next);
		this.predicate = Objects.requireNonNull(predicate);
	}
	
	@Override public void processItemDocument(final ItemDocument itemDocument) {
		if (predicate.test(itemDocument)) {
			next.processItemDocument(itemDocument);
		}
	}
	
	@Override public void processPropertyDocument(final PropertyDocument propertyDocument) {
		next.processPropertyDocument(propertyDocument);
	}
	
	@Override public void processLexemeDocument(final LexemeDocument lexemeDocument) {
		next.processLexemeDocument(lexemeDocument);
	}
}
