package de.ostfalia.teamprojekt.wwm.wikidatatest.model;

import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;

import java.util.Set;

public class Question {
	
	public String          questionText;
	public PropertyIdValue propertyAskedOn;
	public Set<String>     answers;
	
}
