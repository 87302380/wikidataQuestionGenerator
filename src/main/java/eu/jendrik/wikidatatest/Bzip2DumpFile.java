package eu.jendrik.wikidatatest;

import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;
import org.wikidata.wdtk.util.CompressionType;
import org.wikidata.wdtk.util.DirectoryManager;
import org.wikidata.wdtk.util.DirectoryManagerFactory;

import java.io.IOException;
import java.io.InputStream;

public class Bzip2DumpFile extends MwLocalDumpFile {
	
	final         DirectoryManager directoryManager;
	
	Bzip2DumpFile(String fileName) {
		super(fileName);
		DirectoryManager tmp = null;
		// create a DirectoryManager the same way super does. Should not throw an exception.
		try {
			tmp = DirectoryManagerFactory.createDirectoryManager(getPath().getParent(), true);
		} catch (IOException ignored) {}
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
