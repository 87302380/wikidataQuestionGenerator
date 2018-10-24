package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;

import java.util.Iterator;

public interface QuestionType extends EntityDocumentProcessor, Iterator<Question> {
	
	public boolean itemRelevant(ItemDocument itemDocument);
	
}
