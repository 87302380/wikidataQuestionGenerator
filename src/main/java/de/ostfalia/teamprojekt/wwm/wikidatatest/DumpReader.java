package de.ostfalia.teamprojekt.wwm.wikidatatest;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;

public class DumpReader {
	
	private final DumpProcessingController dumpProcessingController;
	private final MwLocalDumpFile          mwDumpFile;
	
	public DumpReader(String filename, EntityDocumentProcessor callback) {
		dumpProcessingController = new DumpProcessingController("wikidatawiki");
		dumpProcessingController.registerEntityDocumentProcessor(new OnlyEntityProcessor(callback), null, true);
		mwDumpFile = new MwLocalDumpFile(filename, DumpContentType.JSON, "20181004", null);
	}
	
	public void start() {
		dumpProcessingController.processDump(mwDumpFile);
	}
	
	/**
	 * This classes processes only entities and ignores the other methods in {@link EntityDocumentProcessor}
	 */
	private static class OnlyEntityProcessor implements EntityDocumentProcessor {
		
		private final EntityDocumentProcessor callback;
		
		public OnlyEntityProcessor(EntityDocumentProcessor callback) {
			this.callback = callback;
		}
		
		@Override public void processItemDocument(final ItemDocument itemDocument) {
			callback.processItemDocument(itemDocument);
		}
	}
	
}
