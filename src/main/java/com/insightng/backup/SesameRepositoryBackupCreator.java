package com.insightng.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.trig.TriGWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a backup of the given repository on the Sesame server
 * 
 * @author Hayden Smith
 * @since 1.2
 */
public class SesameRepositoryBackupCreator implements Runnable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String repositoryId;
	private File backupFile;
	private Repository repository;

	public SesameRepositoryBackupCreator(final String repositoryId, final File backupFile,
			final Repository repository) {
		this.repositoryId = repositoryId;
		this.backupFile = backupFile;
		this.repository = repository;
	}

	@Override
	public void run() {
		if (backupToFile()) {
			compressFile();
		}
	}

	private boolean backupToFile() {
		logger.info("backing up repository {} to file {}", repositoryId, backupFile.getAbsolutePath());
		if (this.backupFile.exists()) {
			logger.error("Unable to backup repository " + repositoryId + ": file "
					+ this.backupFile.getAbsolutePath() + " already exists");
			return false;
		}

		try {
			if (!this.backupFile.createNewFile()) {
				logger.error("Unable to backup repository " + repositoryId + ": unable to create file "
						+ this.backupFile.getAbsolutePath());
				return false;
			}
		} catch (IOException e) {
			logger.error("Unable to backup repository " + repositoryId + ": unable to create file "
					+ this.backupFile.getAbsolutePath(), e);
			return false;
		}

		FileOutputStream outputStream = null;

		boolean errorDuringBackup = false;

		try {
			outputStream = new FileOutputStream(backupFile);
			final RDFWriter trigWriter = new TriGWriter(outputStream);
			final RepositoryConnection conn = this.repository.getConnection();

			try {
				// Export the contents of the repository to a TriG-formatted
				// file
				conn.export(trigWriter);
			} catch (RDFHandlerException e) {
				logger.error("Unable to backup repository " + repositoryId, e);

				if (!backupFile.delete()) {
					logger.error("Unable to remove incomplete backup file " + backupFile.getAbsolutePath());
				}
			} finally {
				conn.close();
			}
		} catch (FileNotFoundException e) {
			logger.error("Unable to backup repository " + repositoryId + " due to missing backup file "
					+ backupFile.getAbsolutePath(), e);
			errorDuringBackup = true;
		} catch (RepositoryException e) {
			logger.error("Unable to open connection to repository " + repositoryId, e);
			errorDuringBackup = true;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.error("Unable to close backup file " + backupFile.getAbsolutePath(), e);
					errorDuringBackup = true;
				}
			}
		}

		return !errorDuringBackup;
	}

	private void compressFile() {
		logger.info("compressing file {}", repositoryId, backupFile.getAbsolutePath());

		File gzipBackupFile = new File(backupFile.getParent(), backupFile.getName() + ".gz");

		if (gzipBackupFile.exists()) {
			logger.error("Compressed backup file " + gzipBackupFile.getAbsolutePath() + " already exists");
			return;
		}

		try {
			if (!gzipBackupFile.createNewFile()) {
				logger.error("Unable to create compressed backup file " + gzipBackupFile.getAbsolutePath());
				return;
			}
		} catch (IOException e) {
			logger.error("Unable to create compressed backup file " + gzipBackupFile.getAbsolutePath(), e);
			return;
		}

		FileInputStream inputStream = null;
		GZIPOutputStream gzipOutputStream = null;

		try {
			inputStream = new FileInputStream(backupFile);
		} catch (FileNotFoundException e) {
			logger.error("Could not find backup file " + backupFile.getAbsolutePath(), e);
			return;
		}

		try {
			gzipOutputStream = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(
					gzipBackupFile)));

			// TODO Use Channels?

			int bufferSize = 4096;
			byte[] buffer = new byte[bufferSize];

			int bytesRead = 0;

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				gzipOutputStream.write(buffer, 0, bytesRead);
			}
		} catch (FileNotFoundException e) {
			logger.error("Could not find backup file " + gzipBackupFile.getAbsolutePath(), e);
		} catch (IOException e) {
			logger.error("Could not compress backup file " + gzipBackupFile.getAbsolutePath(), e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("Unable to close uncompressed backup file " + backupFile.getAbsolutePath(),
							e);
				}
			}

			if (gzipOutputStream != null) {
				try {
					gzipOutputStream.close();
				} catch (IOException e) {
					logger.error(
							"Unable to close compressed backup file " + gzipBackupFile.getAbsolutePath(), e);
				}
			}

			if (!backupFile.delete()) {
				logger.warn("Unable to delete uncompressed backup file " + backupFile.getAbsolutePath());
			}
		}
	}
}
