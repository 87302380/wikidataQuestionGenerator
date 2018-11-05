package de.ostfalia.teamprojekt.wwm.wikidatatest.processors;

import de.ostfalia.teamprojekt.wwm.wikidatatest.MockProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.LexemeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wikidata.wdtk.datamodel.helpers.Datamodel.makeDatatypeIdValue;
import static org.wikidata.wdtk.datamodel.helpers.Datamodel.makeItemDocument;
import static org.wikidata.wdtk.datamodel.helpers.Datamodel.makeLexemeDocument;
import static org.wikidata.wdtk.datamodel.helpers.Datamodel.makeMonolingualTextValue;
import static org.wikidata.wdtk.datamodel.helpers.Datamodel.makePropertyDocument;

class PredicateItemFilterTest {

	private MockProcessor mp;

	@BeforeEach
	void createLanguageFilter() {
		mp = new MockProcessor();
	}

	@Test
	void testPredicateTrue() {
		PredicateItemFilter f = new PredicateItemFilter(mp, i -> true);
		f.processItemDocument(makeItemDocument(ItemIdValue.NULL, null, null, null, null, emptyMap()));
		f.processItemDocument(makeItemDocument(ItemIdValue.NULL, null, null, null, null, emptyMap()));
		f.processItemDocument(makeItemDocument(ItemIdValue.NULL, null, null, null, null, emptyMap()));
		assertEquals(3, mp.processItemDocumentCount);
		assertEquals(0, mp.processPropertyDocumentCount);
		assertEquals(0, mp.processLexemeDocumentCount);
	}

	@Test
	void testPredicateFalse() {
		PredicateItemFilter f = new PredicateItemFilter(mp, i -> false);
		f.processItemDocument(makeItemDocument(ItemIdValue.NULL, null, null, null, null, emptyMap()));
		f.processItemDocument(makeItemDocument(ItemIdValue.NULL, null, null, null, null, emptyMap()));
		f.processItemDocument(makeItemDocument(ItemIdValue.NULL, null, null, null, null, emptyMap()));
		assertEquals(0, mp.processItemDocumentCount);
		assertEquals(0, mp.processPropertyDocumentCount);
		assertEquals(0, mp.processLexemeDocumentCount);
	}

	@Test
	void testLabelPredicate() {
		PredicateItemFilter f = new PredicateItemFilter(mp, i -> i.findLabel("de") != null);
		f.processItemDocument(makeItemDocument(ItemIdValue.NULL, singletonList(makeMonolingualTextValue("", "de")), null, null, null, emptyMap()));
		f.processItemDocument(makeItemDocument(ItemIdValue.NULL, null, null, null, null, emptyMap()));
		f.processItemDocument(makeItemDocument(ItemIdValue.NULL, null, null, null, null, emptyMap()));
		assertEquals(1, mp.processItemDocumentCount);
		assertEquals(0, mp.processPropertyDocumentCount);
		assertEquals(0, mp.processLexemeDocumentCount);
	}

	@Test
	void testProcessPropertyDocument() {
		new PredicateItemFilter(mp, i -> true).processPropertyDocument(makePropertyDocument(PropertyIdValue.NULL, null, null, null, null, makeDatatypeIdValue(DatatypeIdValue.DT_ITEM)));
		assertEquals(0, mp.processItemDocumentCount);
		assertEquals(1, mp.processPropertyDocumentCount);
		assertEquals(0, mp.processLexemeDocumentCount);
	}

	@Test
	void testProcessLexemeDocument() {
		new PredicateItemFilter(mp, i -> true).processLexemeDocument(makeLexemeDocument(
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

}
