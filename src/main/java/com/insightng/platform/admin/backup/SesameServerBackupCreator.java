package com.insightng.platform.admin.backup;

import java.io.File;
import java.net.URLEncoder;
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
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

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

				final Calendar cal = Calendar.getInstance();
				final File backupFile = new File(backupDir, URLEncoder.encode(
						info.getId() + "_" + dateFormat.format(cal.getTime()) + ".trig", "UTF-8"));

				executor.execute(new SesameRepositoryBackupCreator(info.getId(), backupFile, repository));
			} catch (Exception e) {
				logger.error("Unable to backup repository " + info.getId(), e);
			}
		}
	}

	/**
	 * Create a repository manager for the given URL and set the username and
	 * password for the server
	 * 
	 * @param sesameServerUrl
	 * @param sesameUsername
	 * @param sesamePassword
	 * @return
	 * @throws RepositoryException
	 */
	private RepositoryManager getRepositoryManager(final String sesameServerUrl, final String sesameUsername,
			final String sesamePassword) throws RepositoryException {
		RemoteRepositoryManager repManager = new RemoteRepositoryManager(sesameServerUrl);
		repManager.setUsernameAndPassword(sesameUsername, sesamePassword);
		repManager.initialize();
		return repManager;
	}
}
