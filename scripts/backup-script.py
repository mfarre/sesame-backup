import os
import subprocess
import sys

#python backup.py /path/to/backup/folder max_backup_files
if len(sys.argv) < 3 :
	print "Usage: python backup.py <backup folder> <max backup files>"
	quit()

max_back = int(sys.argv[2])

#we get files sorted from oldest to newest
files = sorted(os.listdir(sys.argv[1]))

while(len(files) >= max_back):
	print "Removing "+sys.argv[1]+"/"+files[0]
	subprocess.call(["rm",sys.argv[1]+"/"+files[0]])	
	files = sorted(os.listdir(sys.argv[1]))

subprocess.call(["java","-jar","sesame-backup.jar"])


