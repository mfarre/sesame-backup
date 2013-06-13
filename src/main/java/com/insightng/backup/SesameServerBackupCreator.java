package com.insightng.backup;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a backup of all the repositories on the Sesame server
 * 
 * @author Hayden Smith
 * 
 */
public class SesameServerBackupCreator {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final int NUM_THREADS = 5;

	private ExecutorService executor;
	private String sesameServerUrl;
	private String sesameUsername;
	private String sesamePassword;
	private File backupDir;
	private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");

	public SesameServerBackupCreator(final String sesameServerUrl, final String sesameUsername,
			final String sesamePassword, final File backupDir) {
		this.sesameServerUrl = sesameServerUrl;
		this.sesameUsername = sesameUsername;
		this.sesamePassword = sesamePassword;
		this.backupDir = backupDir;
	}

	public void start() {
		Collection<RepositoryInfo> repositories = null;

		RepositoryManager repManager = null;
		try {
			repManager = getRepositoryManager(sesameServerUrl, sesameUsername, sesamePassword);
		} catch (RepositoryException e) {
			logger.error("Unable to create repository manager", e);
			return;
		}

		try {
			repositories = repManager.getAllRepositoryInfos(true);
		} catch (RepositoryException e) {
			logger.error("Unable to retrieve repository information", e);
			return;
		}

		executor = Executors.newFixedThreadPool(NUM_THREADS);

		for (RepositoryInfo info : repositories) {
			try {
				final Repository repository = repManager.getRepository(info.getId());

				if (repository == null) {
					throw new RepositoryException("Unable to retrieve repository " + info.getId());
				}

				final File repoBackupDir = new File(backupDir, info.getId());
				if (!repoBackupDir.exists()) {
					repoBackupDir.mkdir();
				} else if (!repoBackupDir.isDirectory()) {
					logger.error("Repository backup dir {} is not a directory",
							repoBackupDir.getAbsolutePath());
				} else if (!repoBackupDir.canWrite()) {
					logger.error("Unable to write to repository backup dir {}",
							repoBackupDir.getAbsolutePath());
				}

				final Calendar cal = Calendar.getInstance();
				final File backupFile = new File(repoBackupDir, dateFormat.format(cal.getTime())
						+ info.getId() + ".trig");

				executor.execute(new SesameRepositoryBackupCreator(info.getId(), backupFile, repository));
			} catch (Exception e) {
				logger.error("Unable to backup repository " + info.getId(), e);
			}
		}

		executor.shutdown();
	}

	/**
	 * Create a repository manager for the given URL and set the username and
	 * password for the server
	 * 
	 * @param sesameServerUrl
	 *            the url of the Sesame server.
	 * @param sesameUsername
	 *            the username with which to authenticate. May be null.
	 * @param sesamePassword
	 *            the password with which to authenticate. May be null.
	 * @return a RepositoryManager for the given Sesame server.
	 * @throws RepositoryException
	 */
	private RepositoryManager getRepositoryManager(final String sesameServerUrl, final String sesameUsername,
			final String sesamePassword) throws RepositoryException {
		RemoteRepositoryManager repManager = new RemoteRepositoryManager(sesameServerUrl);
		if (sesameUsername != null && sesamePassword != null) {
			repManager.setUsernameAndPassword(sesameUsername, sesamePassword);
		}
		repManager.initialize();
		return repManager;
	}
}
