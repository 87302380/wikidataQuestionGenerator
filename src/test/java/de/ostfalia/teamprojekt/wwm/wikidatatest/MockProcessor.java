package de.ostfalia.teamprojekt.wwm.wikidatatest;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.LexemeDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

public class MockProcessor implements EntityDocumentProcessor {
	public int processItemDocumentCount, processPropertyDocumentCount, processLexemeDocumentCount;

	@Override public void processItemDocument(final ItemDocument itemDocument) {
		processItemDocumentCount++;
	}

	@Override public void processPropertyDocument(final PropertyDocument propertyDocument) {
		processPropertyDocumentCount++;
	}

	@Override public void processLexemeDocument(final LexemeDocument lexemeDocument) {
		processLexemeDocumentCount++;
	}
}
