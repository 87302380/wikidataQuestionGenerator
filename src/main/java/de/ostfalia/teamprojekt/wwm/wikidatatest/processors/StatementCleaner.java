package de.ostfalia.teamprojekt.wwm.wikidatatest.processors;

import de.ostfalia.teamprojekt.wwm.wikidatatest.CorrectSerializeStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.implementation.StatementGroupImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.LexemeDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Removes qualifiers and references from Statements.
 */
public class StatementCleaner implements EntityDocumentProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(StatementCleaner.class);
	private static final String PROPERTY_END_TIME = "P582";
	private final EntityDocumentProcessor next;

	public StatementCleaner(EntityDocumentProcessor next) {
		this.next = Objects.requireNonNull(next);
	}

	@Override public void processItemDocument(ItemDocument itemDocument) {
		// create new statement groups, because we have to change the statements (remove the references and qualifiers)
		List<StatementGroup> newStatementGroups = new ArrayList<>();
		for (final StatementGroup sg : itemDocument.getStatementGroups()) {
			List<Statement> newStatements = new ArrayList<>();
			for (final Statement s : sg.getStatements()) {
				if (hasEnded(s)) {
					continue;
				}
				// create a new statement with the same value, but ignore the rest
				// not all kinds of values can be build (todo: investigate why)
				Statement newS;
				try {
					newS = StatementBuilder.forSubjectAndProperty(itemDocument.getEntityId(), s.getMainSnak().getPropertyId())
					                       .withValue(s.getValue())
					                       .build();
				} catch (UnsupportedOperationException e) {
					LOGGER.warn("could not create a Statement with a value of type " + s.getValue().getClass(), e);
					continue;
				}
				newS = new CorrectSerializeStatement(newS);
				newStatements.add(newS);
			}
			if (!newStatements.isEmpty()) {
				newStatementGroups.add(new StatementGroupImpl(newStatements));
			}
		}

		// create a new item document with the nwe statement groups
		itemDocument = new ItemDocumentImpl(
				itemDocument.getEntityId(),
				new ArrayList<>(itemDocument.getLabels().values()),
				Collections.emptyList(),
				Collections.emptyList(),
				newStatementGroups,
				Collections.emptyList(),
				0
		);

		next.processItemDocument(itemDocument);
	}

	private boolean hasEnded(final Statement s) {
		Iterator<Snak> qualifiers = s.getAllQualifiers();
		while (qualifiers.hasNext()) {
			Snak qualifier = qualifiers.next();
			if (qualifier.getPropertyId().getId().equals(PROPERTY_END_TIME) && qualifier instanceof ValueSnak) {
				Value value = ((ValueSnak) qualifier).getValue();
				if (value instanceof TimeValue) {
					TimeValue timeValue = (TimeValue) value;
					int year = (int) timeValue.getYear();
					LocalDate date = LocalDate.of(year, 12, 31);
					if (date.isBefore(LocalDate.now())) {
						// the statement has an end date which is in the current year
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override public void processPropertyDocument(final PropertyDocument propertyDocument) {
		next.processPropertyDocument(propertyDocument);
	}

	@Override public void processLexemeDocument(final LexemeDocument lexemeDocument) {
		next.processLexemeDocument(lexemeDocument);
	}
}
