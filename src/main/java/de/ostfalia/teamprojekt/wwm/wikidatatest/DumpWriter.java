package de.ostfalia.teamprojekt.wwm.wikidatatest;

import org.wikidata.wdtk.datamodel.helpers.JsonSerializer;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.LexemeDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DumpWriter implements EntityDocumentProcessor, AutoCloseable {

	/**
	 * The directory where to place files created by the example applications.
	 */
	private static final String EXAMPLE_OUTPUT_DIRECTORY = "results";

	private final JsonSerializer jsonSerializer;
	private final String filename;

	public DumpWriter(final String filename) throws IOException {
		OutputStream out = new BufferedOutputStream(openExampleFileOuputStream(filename));
		this.filename = filename;
		this.jsonSerializer = new JsonSerializer(out);
		this.jsonSerializer.open();
	}

	/**
	 * Opens a new FileOutputStream for a file of the given name in the example
	 * output directory ({@link DumpWriter#EXAMPLE_OUTPUT_DIRECTORY}). Any
	 * file of this name that exists already will be replaced. The caller is
	 * responsible for eventually closing the stream.
	 *
	 * @param filename
	 *            the name of the file to write to
	 * @return FileOutputStream for the file
	 * @throws IOException
	 *             if the file or example output directory could not be created
	 */
	static FileOutputStream openExampleFileOuputStream(String filename) throws IOException {
		Path directoryPath = Paths.get(EXAMPLE_OUTPUT_DIRECTORY);

		createDirectory(directoryPath);
		File filePath = directoryPath.resolve(filename).toFile();
		if (filePath.exists()) {
			throw new FileAlreadyExistsException(filePath.toString());
		}
		return new FileOutputStream(filePath);
	}

	/**
	 * Create a directory at the given path if it does not exist yet.
	 *
	 * @param path
	 *            the path to the directory
	 * @throws IOException
	 *             if it was not possible to create a directory at the given
	 *             path
	 */
	private static void createDirectory(Path path) throws IOException {
		try {
			Files.createDirectory(path);
		} catch (FileAlreadyExistsException e) {
			if (!path.toFile().isDirectory()) {
				throw e;
			}
		}
	}

	@Override public void close() {
		System.out.println("Serialized " + this.jsonSerializer.getEntityDocumentCount() + " item documents to JSON file " + filename + ".");
		this.jsonSerializer.close();
	}

	@Override public void processItemDocument(final ItemDocument itemDocument) {
		jsonSerializer.processItemDocument(itemDocument);
	}

	@Override public void processPropertyDocument(final PropertyDocument propertyDocument) {
		jsonSerializer.processPropertyDocument(propertyDocument);
	}

	@Override public void processLexemeDocument(final LexemeDocument lexemeDocument) {
		jsonSerializer.processLexemeDocument(lexemeDocument);
	}
}
