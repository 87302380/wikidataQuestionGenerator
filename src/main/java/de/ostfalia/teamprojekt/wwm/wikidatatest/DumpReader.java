package de.ostfalia.teamprojekt.wwm.wikidatatest;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.LexemeDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;
import org.wikidata.wdtk.util.CompressionType;
import org.wikidata.wdtk.util.DirectoryManager;
import org.wikidata.wdtk.util.DirectoryManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DumpReader {

	private final DumpProcessingController dumpProcessingController;
	private final MwLocalDumpFile dumpFile;

	public DumpReader(String filename, EntityDocumentProcessor callback) {
		dumpProcessingController = new DumpProcessingController("wikidatawiki");
		dumpProcessingController.registerEntityDocumentProcessor(new OnlyEntityProcessor(new ProgressCountProcessor(callback)), null, true);
		if (filename.endsWith(".bz2")) {
			dumpFile = new Bzip2DumpFile(filename);
		} else if (filename.endsWith(".json")) {
			dumpFile = new UncompressedJsonDumpFile(filename);
		} else {
			dumpFile = new MwLocalDumpFile(filename);
		}
	}

	public void start() {
		dumpProcessingController.processDump(dumpFile);
	}

	private static class ProgressCountProcessor implements EntityDocumentProcessor {
		private final EntityDocumentProcessor next;
		private long count = 0;

		public ProgressCountProcessor(EntityDocumentProcessor next) {
			this.next = next;
		}

		@Override public void processItemDocument(final ItemDocument itemDocument) {
			count++;
			if (count % 10_000 == 0) {
				Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, count + " items processed");
			}
			next.processItemDocument(itemDocument);
		}

		@Override public void processPropertyDocument(final PropertyDocument propertyDocument) {
			next.processPropertyDocument(propertyDocument);
		}

		@Override public void processLexemeDocument(final LexemeDocument lexemeDocument) {
			next.processLexemeDocument(lexemeDocument);
		}
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




	public static class Bzip2DumpFile extends MwLocalDumpFile {

		final DirectoryManager directoryManager;

		Bzip2DumpFile(String fileName) {
			super(fileName);
			DirectoryManager tmp;
			// create a DirectoryManager the same way super does. Should not throw an exception.
			try {
				tmp = DirectoryManagerFactory.createDirectoryManager(getPath().getParent(), true);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			directoryManager = tmp;
		}

		@Override public InputStream getDumpFileStream() throws IOException {
			// mostly copied from super
			if (!isAvailable()) {
				throw new IOException("Local dump file \"" + getPath().toString() + "\" is not available for reading.");
			}
			return this.directoryManager.getInputStreamForFile(getPath().getFileName().toString(), CompressionType.BZ2);
		}
	}

	public static class UncompressedJsonDumpFile extends MwLocalDumpFile {

		final DirectoryManager directoryManager;

		UncompressedJsonDumpFile(String fileName) {
			super(fileName);
			DirectoryManager tmp;
			// create a DirectoryManager the same way super does. Should not throw an exception.
			try {
				tmp = DirectoryManagerFactory.createDirectoryManager(getPath().getParent(), true);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			directoryManager = tmp;
		}

		@Override public InputStream getDumpFileStream() throws IOException {
			// mostly copied from super
			if (!isAvailable()) {
				throw new IOException("Local dump file \"" + getPath().toString() + "\" is not available for reading.");
			}
			return this.directoryManager.getInputStreamForFile(getPath().getFileName().toString(), CompressionType.NONE);
		}
	}

}
