package eu.jendrik.wikidatatest;

/*
 * #%L
 * Wikidata Toolkit Examples
 * %%
 * Copyright (C) 2014 Wikidata Toolkit Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class IoHelpers {
	
	private IoHelpers() {}
	
	/**
	 * The directory where to place files created by the example applications.
	 */
	private static final String EXAMPLE_OUTPUT_DIRECTORY = "results";
	
	static void configureLogging() {
		// Create the appender that will write log messages to the console.
		ConsoleAppender consoleAppender = new ConsoleAppender();
		// Define the pattern of log messages.
		// Insert the string "%c{1}:%L" to also show class name and line.
		consoleAppender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %c{1}:%L %-5p - %m%n"));
		// Change to Level.ERROR for fewer messages:
		consoleAppender.setThreshold(Level.INFO);
		
		consoleAppender.activateOptions();
		Logger.getRootLogger().addAppender(consoleAppender);
	}
	
	/**
	 * Opens a new FileOutputStream for a file of the given name in the example
	 * output directory ({@link IoHelpers#EXAMPLE_OUTPUT_DIRECTORY}). Any
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
		Path filePath = directoryPath.resolve(filename);
		return new FileOutputStream(filePath.toFile());
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
}
