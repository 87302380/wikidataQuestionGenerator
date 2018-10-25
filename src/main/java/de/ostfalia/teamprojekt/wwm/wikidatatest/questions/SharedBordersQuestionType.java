package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.wikidata.wdtk.datamodel.interfaces.*;


/**
 *
 */
public class SharedBordersQuestionType implements QuestionType {

	/**
	 * @param itemDocument
	 * @return
	 */
	@Override
	public boolean itemRelevant(ItemDocument itemDocument) {
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			// "P31" is "instance of" on Wikidata
			if (!sg.getProperty().getId().equals("P31")) {
				continue;
			}
			for (Statement s : sg.getStatements()) {
				if (s.getClaim().getMainSnak() instanceof ValueSnak) {
					Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
					// "Q6256" is "country" on Wikidata
					if (v instanceof ItemIdValue && ((ItemIdValue) v).getId().equals("Q6256")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public Question next() {
		return null;
	}
}
