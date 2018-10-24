package de.ostfalia.teamprojekt.wwm.wikidatatest;

import org.wikidata.wdtk.datamodel.helpers.JsonSerializer;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DumpWriter implements AutoCloseable {
	
	private final JsonSerializer jsonSerializer;
	private final String filename;
	
	public DumpWriter(final String filename) throws IOException {
		OutputStream out = new BufferedOutputStream(IoHelpers.openExampleFileOuputStream(filename));
		this.filename = filename;
		this.jsonSerializer = new JsonSerializer(out);
	}
	
	public void serialize(ItemDocument itemDocument) {
		jsonSerializer.processItemDocument(itemDocument);
	}
	
	@Override public void close() {
		System.out.println("Serialized " + this.jsonSerializer.getEntityDocumentCount() + " item documents to JSON file " + filename + ".");
		this.jsonSerializer.close();
	}
}
