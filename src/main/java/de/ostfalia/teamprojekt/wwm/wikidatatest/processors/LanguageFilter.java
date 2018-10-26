package de.ostfalia.teamprojekt.wwm.wikidatatest.processors;

import org.wikidata.wdtk.datamodel.helpers.DatamodelFilter;
import org.wikidata.wdtk.datamodel.implementation.DataObjectFactoryImpl;
import org.wikidata.wdtk.datamodel.interfaces.DocumentDataFilter;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.LexemeDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

import java.util.Collections;
import java.util.Objects;

public class LanguageFilter implements EntityDocumentProcessor {

	private static final String LANGUAGE = "de";
	private final DatamodelFilter datamodelFilter;
	private final EntityDocumentProcessor next;

	public LanguageFilter(EntityDocumentProcessor next) {
		this.next = Objects.requireNonNull(next);
		DocumentDataFilter filter = new DocumentDataFilter();
		filter.setLanguageFilter(Collections.singleton(LANGUAGE));
		filter.setSiteLinkFilter(Collections.emptySet());
		this.datamodelFilter = new DatamodelFilter(new DataObjectFactoryImpl(), filter);
	}

	@Override public void processItemDocument(ItemDocument itemDocument) {
		itemDocument = datamodelFilter.filter(itemDocument);
		// it may be possible that no german label is given. in that case the item should be ignored
		if (itemDocument.findLabel(LANGUAGE) != null) {
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
