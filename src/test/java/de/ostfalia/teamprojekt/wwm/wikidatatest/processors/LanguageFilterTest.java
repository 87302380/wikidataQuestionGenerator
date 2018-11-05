package de.ostfalia.teamprojekt.wwm.wikidatatest.processors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.LexemeDocument;
import org.wikidata.wdtk.datamodel.interfaces.LexemeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;

import java.util.Arrays;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.wikidata.wdtk.datamodel.helpers.Datamodel.makeDatatypeIdValue;
import static org.wikidata.wdtk.datamodel.helpers.Datamodel.makeItemDocument;
import static org.wikidata.wdtk.datamodel.helpers.Datamodel.makeLexemeDocument;
import static org.wikidata.wdtk.datamodel.helpers.Datamodel.makeMonolingualTextValue;
import static org.wikidata.wdtk.datamodel.helpers.Datamodel.makePropertyDocument;

class LanguageFilterTest {

	private MockProcessor mp;
	private LanguageFilter lf;

	@BeforeEach
	void createLanguageFilter() {
		mp = new MockProcessor();
		lf = new LanguageFilter(mp);
	}

	@Test
	void testProcessItemDocumentNullLabels() {
		lf.processItemDocument(makeItemDocument(ItemIdValue.NULL, null, null, null, null, emptyMap()));
		assertEquals(0, mp.processItemDocumentCount);
		assertEquals(0, mp.processPropertyDocumentCount);
		assertEquals(0, mp.processLexemeDocumentCount);
	}

	@Test
	void testProcessItemDocumentEmptyLabels() {
		lf.processItemDocument(makeItemDocument(ItemIdValue.NULL, emptyList(), null, null, null, emptyMap()));
		assertEquals(0, mp.processItemDocumentCount);
		assertEquals(0, mp.processPropertyDocumentCount);
		assertEquals(0, mp.processLexemeDocumentCount);
	}

	@Test
	void testProcessItemDocumentEnglishLabel() {
		lf.processItemDocument(makeItemDocument(ItemIdValue.NULL, singletonList(makeMonolingualTextValue("", "en")), null, null, null, emptyMap()));
		assertEquals(0, mp.processItemDocumentCount);
		assertEquals(0, mp.processPropertyDocumentCount);
		assertEquals(0, mp.processLexemeDocumentCount);
	}

	@Test
	void testProcessItemDocumentGermanLabel() {
		lf.processItemDocument(makeItemDocument(ItemIdValue.NULL, singletonList(makeMonolingualTextValue("", "de")), null, null, null, emptyMap()));
		assertEquals(1, mp.processItemDocumentCount);
		assertEquals(0, mp.processPropertyDocumentCount);
		assertEquals(0, mp.processLexemeDocumentCount);
	}

	@Test
	void testProcessItemDocumentGermanAndEnglishLabel() {
		MockProcessor mp = new MockProcessor() {
			@Override public void processItemDocument(final ItemDocument itemDocument) {
				super.processItemDocument(itemDocument);
				assertEquals("", itemDocument.findLabel("de"));
				assertNull(itemDocument.findLabel("en"));
			}
		};
		LanguageFilter lf = new LanguageFilter(mp);
		lf.processItemDocument(makeItemDocument(
				ItemIdValue.NULL,
				Arrays.asList(makeMonolingualTextValue("", "de"), makeMonolingualTextValue("", "en")),
				null,
				null,
				null,
				emptyMap()
		));
		assertEquals(1, mp.processItemDocumentCount);
		assertEquals(0, mp.processPropertyDocumentCount);
		assertEquals(0, mp.processLexemeDocumentCount);
	}

	@Test
	void testProcessPropertyDocument() {
		lf.processPropertyDocument(makePropertyDocument(PropertyIdValue.NULL, null, null, null, null, makeDatatypeIdValue(DatatypeIdValue.DT_ITEM)));
		assertEquals(0, mp.processItemDocumentCount);
		assertEquals(1, mp.processPropertyDocumentCount);
		assertEquals(0, mp.processLexemeDocumentCount);
	}

	@Test
	void testProcessLexemeDocument() {
		lf.processLexemeDocument(makeLexemeDocument(
				LexemeIdValue.NULL,
				ItemIdValue.NULL,
				ItemIdValue.NULL,
				singletonList(makeMonolingualTextValue("", "de")),
				null,
				null,
				null
		));
		assertEquals(0, mp.processItemDocumentCount);
		assertEquals(0, mp.processPropertyDocumentCount);
		assertEquals(1, mp.processLexemeDocumentCount);
	}

	class MockProcessor implements EntityDocumentProcessor {
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
}
