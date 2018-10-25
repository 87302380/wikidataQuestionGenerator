package de.ostfalia.teamprojekt.wwm.wikidatatest.model;

import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;

import java.util.List;

public class Question {
	
	public String questionText;
	public PropertyIdValue propertyAskedOn;
	// the right answer should be first
	public List<String> answers;
	
}
