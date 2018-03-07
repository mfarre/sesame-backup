package com.insightng.backup;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Entry point for running the backup process from the command line
 * 
 * @author Hayden Smith
 */
public class BackupData {

	private static final String DEFAULT_SESAME_SERVER_URL = "http://localhost:8080/rdf4j-server";

	private static final String URL_OPTION = "url";
	private static final String USERNAME_OPTION = "username";
	private static final String PASSWORD_OPTION = "password";
	private static final String DIR_OPTION = "dir";
	private static final String HELP_OPTION = "help";

	private static String sesameServerUrl = null;
	private static String sesameUsername = null;
	private static String sesamePassword = null;
	private static File backupDir = null;

	private static final String HELP_MESSAGE = "" + "Usage:\n" + "------\n" + "\n" + "Options:\n" + "\n"
			+ "--url=<sesame-server-url>:\t\tURL of the sesame server to be backed up\n"
			+ "--username=<server-username>:\t\tUsername to be passed to server for authentication\n"
			+ "--password=<server-password>:\t\tPassword to be passed to server for authentication\n"
			+ "--dir=<backup-directory>:\t\tDirectory that the backups are to be written to\n"
			+ "--help:\t\t\tDisplay this help message\n";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Map<String, String> options = argumentsToMap(args);

		sesameServerUrl = DEFAULT_SESAME_SERVER_URL;

		boolean configurationError = false;

		if (options.containsKey(HELP_OPTION)) {
			System.out.println(HELP_MESSAGE);
			System.exit(0);
		}

		if (options.containsKey(URL_OPTION)) {
			sesameServerUrl = options.get(URL_OPTION);
		}

		if (options.containsKey(USERNAME_OPTION)) {
			sesameUsername = options.get(USERNAME_OPTION);
		}

		if (options.containsKey(PASSWORD_OPTION)) {
			sesamePassword = options.get(PASSWORD_OPTION);
		}

		if (options.containsKey(DIR_OPTION)) {
			backupDir = new File(options.get(DIR_OPTION));

			if (!backupDir.exists()) {
				System.err.println("Backup path " + backupDir.getAbsolutePath() + " does not exist");
				configurationError = true;
			} else if (!backupDir.isDirectory()) {
				System.err.println("Backup path " + backupDir.getAbsolutePath() + " is not a directory");
				configurationError = true;
			} else if (!backupDir.canExecute()) {
				System.err.println("Cannot open backup directory " + backupDir.getAbsolutePath());
				configurationError = true;
			}
		}

		if (!configurationError) {
			final SesameServerBackupCreator serverBackupCreator = new SesameServerBackupCreator(
					sesameServerUrl, sesameUsername, sesamePassword, backupDir);
			serverBackupCreator.start();
		} else {
			// TODO Error codes
			System.exit(-1);
		}
	}

	private static Map<String, String> argumentsToMap(String[] args) {
		Map<String, String> options = new HashMap<String, String>();

		for (String arg : args) {
			if (!arg.startsWith("--")) {
				System.err.println("Unknown argument: " + arg);
				continue;
			}

			int valueIndex = arg.indexOf('=');
			if (valueIndex >= 2) {
				String option = arg.substring(2, valueIndex);
				String value = arg.substring(valueIndex + 1);

				options.put(option, value);
			} else {
				String option = arg.substring(2);
				options.put(option, null);
			}
		}

		return options;
	}
}
