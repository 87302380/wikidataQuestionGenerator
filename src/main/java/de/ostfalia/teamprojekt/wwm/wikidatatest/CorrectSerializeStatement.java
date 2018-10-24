package de.ostfalia.teamprojekt.wwm.wikidatatest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.wikidata.wdtk.datamodel.implementation.StatementImpl;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.Value;

/**
 * This is a wrapper around {@link StatementImpl}, which doesn't serialize
 * {@link #getValue()}.
 *
 * {@code getValue()} just returns {@code getMainSnak().getValue()}, which would
 * result in a duplicate serialization.
 */
public class CorrectSerializeStatement extends StatementImpl {
	
	public CorrectSerializeStatement(Statement s) {
		super(s.getStatementId(), s.getRank(), s.getMainSnak(), s.getQualifiers(), s.getReferences(), s.getSubject());
	}
	
	@Override
	@JsonIgnore
	public Value getValue() {
		return super.getValue();
	}
}
