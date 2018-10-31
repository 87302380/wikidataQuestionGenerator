package de.ostfalia.teamprojekt.wwm.wikidatatest.questions;

import de.ostfalia.teamprojekt.wwm.wikidatatest.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.*;

public class MaerchenFigurQuestion implements QuestionType {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaerchenFigurQuestion.class);
    private static Map<String, Set<String>> MaerchenFigur = new HashMap<>();

    public MaerchenFigurQuestion() {
        if (MaerchenFigur.isEmpty()) {
            try (Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream("maerchenList.csv"), "UTF-8")) {
                s.useDelimiter(",");
                s.nextLine(); // skip first line
                while (s.hasNext()) {
                    MaerchenFigur.put(s.next(), new HashSet<>(1000));
                    s.nextLine();
                }
            }
        }
    }

    @Override public boolean itemRelevant(final ItemDocument itemDocument) {
        for (StatementGroup sg : itemDocument.getStatementGroups()) {
            if (sg.getProperty().getId().equals("P1441")) {
                for (Statement s : sg.getStatements()) {
                    if (s.getClaim().getMainSnak() instanceof ValueSnak) {
                        Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
                        if (v instanceof ItemIdValue && MaerchenFigur.containsKey(((ItemIdValue) v).getId())) {
                            // german label might not exist
                            //LOGGER.log(Level.INFO, itemDocument.getLabels().get("de").getText() + ": " + ((ItemIdValue) v).getId());
                            LOGGER.info(itemDocument.getEntityId().getId() + " is of type " + ((ItemIdValue) v).getId());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override public void processItemDocument(final ItemDocument itemDocument) {
        for (StatementGroup sg : itemDocument.getStatementGroups()) {
            if (sg.getProperty().getId().equals("P1441")) {
                for (Statement s : sg.getStatements()) {
                    if (s.getClaim().getMainSnak() instanceof ValueSnak) {
                        Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
                        if (v instanceof ItemIdValue) {
                            String maerchen = ((ItemIdValue) v).getId();
                            MaerchenFigur.get(maerchen).add(itemDocument.getLabels().get("de").getText());
                            LOGGER.info(itemDocument.getLabels().get("de").getText() + ": " + maerchen);
                        }
                    }
                }
            }
        }
    }

    @Override public boolean hasNext() {
        return false;
    }

    @Override public Question next() {
        return null;
    }

}
