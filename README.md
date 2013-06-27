sesame-backup: a commandline backup tool for OpenRDF Sesame
===========================================================

Copyright InsightNG (http://www.insightng.com/) (c) 2012-2013. See LICENSE.txt for details.

Download / Usage instructions
------------------

You can either download a pre-packed binary release (see the Download section) or clone
the Git repository and bake your own.

The sesame-backup tool creates file backups of the contents every repository on the provided server, using TriG format, compressed. 

Usage:

<pre>
 bin/sesame-backup.sh [options] 
</pre>
Options:

<pre>
 --url=<server-url>
   URL of the Sesame Server to be backed up (default is http://localhost:8080/openrdf-sesame).
 --username=<server-username>
   Username to be passed to the server for authentication 
 --password=<server-password>
   Password to be passed to the server for authentication
 --dir=<backup-directory>
   Directory that the backup data is to be written to.
 --help Displays help message.
</pre>

Developer instructions
----------------------

This project uses Apache Maven (http://maven.apache.org/), and we recommend you
use Eclipse. To set up your development environment you can perform the following
steps:

1. Get a local copy of the git repository (`git clone ...`)
1. Install Eclipse (http://www.eclipse.org/)
1. Install EGit, the Eclipse plugin for Git (http://www.eclipse.org/egit/)
1. In your local git clone, perform `mvn eclipse:eclipse` to generate Eclipse IDE settings
1. In Eclipse, select 'Import' -> 'General' -> 'Existing project into Workspace' and select your local git repo for the project location. Make sure 'Copy projects into workspace' is *not* checked.
1. In the Eclipse package explorer, right-click on your newly checked out project, select 'Team' -> 'Share project', and select 'Git'. Click 'Next' and in the next screen click 'Finish'. Your Eclipse Git plugin will now be linked with your git repo, so you can commit, push, pull, etc directly from Eclipse.

To produce a new build from source, execute 'mvn package' in the project root. A distribution archive will be produced in the 'target/' directory. 
