package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.stream.Stream;


/**
 *
 */
public class SharedBordersQuestionType implements QuestionType {
	private static final String PROPERTY_INSTANCE_OF = "P31";
	private static final String PROPERTY_DISSOLVED_OR_ABOLISHED = "P576";
	private static final String PROPERTY_COUNTRY = "Q6256";

	/**
	 * @param itemDocument
	 * @return
	 */
	@Override
	public boolean itemRelevant(ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {

			if (sg.getProperty().getId().equals(PROPERTY_DISSOLVED_OR_ABOLISHED)){
				return false;
			}

			if (!sg.getProperty().getId().equals(PROPERTY_INSTANCE_OF)) {
				continue;
			}

			for (Statement s : sg.getStatements()) {
				if (s.getClaim().getMainSnak() instanceof ValueSnak) {
					Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();

					if (v instanceof ItemIdValue && ((ItemIdValue) v).getId().equals(PROPERTY_COUNTRY)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override public Stream<Question> generateQuestions() {
		return Stream.empty();
	}

}
