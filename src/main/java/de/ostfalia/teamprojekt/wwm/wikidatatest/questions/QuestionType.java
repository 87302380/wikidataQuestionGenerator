package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;

import java.io.IOException;
import java.util.stream.Stream;

public interface QuestionType extends EntityDocumentProcessor {

	boolean itemRelevant(ItemDocument itemDocument);

	Stream<Question> generateQuestions() throws IOException;
}
