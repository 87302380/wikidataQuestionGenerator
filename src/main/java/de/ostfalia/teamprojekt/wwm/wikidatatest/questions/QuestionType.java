package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

public interface QuestionType extends EntityDocumentProcessor {

	boolean canGenerateQuestions();

	default void onStartDumpReading() {}

	Stream<Question> generateQuestions(Optional<Integer> difficulty) throws IOException;
}
