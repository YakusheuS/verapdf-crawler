# verapdf-crawler
###Pre-requisites
In order to install Logius crawler you'll need 
 * Java 7 and Java 8, which can be downloaded [from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html), or for 
 Linux users [OpenJDK](http://openjdk.java.net/install/index.html).
 * Heritrix 3 need to be installed, [web page with details](https://webarchive.jira.com/wiki/display/Heritrix)
 * Verapdf need to be installed, [you can download the latest version here](http://downloads.verapdf.org/gf/)
 
###Installing Logius web application
You need to dowload and build modules LogiusWebApp and HeritrixExtention with maven. You should run the following command from the 
directory that contains both downloaded modules

	mvn clean install

After that you will get two jar files "your_directory/LogiusWebApp/target/LogiusWebApp-1.0-SNAPSHOT.jar" and "your_directory/HeritrixExtention/target/HeritrixExtention.jar". The file "your_directory/HeritrixExtention/target/HeritrixExtention.jar" should be placed in "lib/" directory in your Heritrix installation directory.

###Starting heritrix
You need to start heritrix application. Firtly you need to ensure that java 7 will be used to run heritrix, and secondly you need to run 
heritrix with necessary arguments:

	export JAVA_HOME=your_path_to_java7
	heritrix_directory/bin/heritrix -a your_login:your_password -p 8443
    
Here your_login and your_password are the credentials you shoul pass to LogiusWebApp afterwards.

###Configuring Logius application
  You need to create the directory with configuration files, suppose you name it data/. You need to move files "crawled_urls.txt"(make sure that this file contains only one line "id,crawlURL,jobURL,startTime,finishTime"), "sample_configuration.cxml" and "sample_report.ods" from "LogiusWebApp/src/main/resources/" to your "data\" directory. You need to configure the file "LogiusWebApp/src/main/resources/config.yml" accordingly. You may change parameters server:applicationConnectors:port: and server:adminConnectors:port:. You need to configure emailServer: as a email server which will send notifications about finished jobs (it should be gmail server and port: 587 should remain intact). The following settings must be provided: resourcePath(path to your "data/" directory), heritrixLogin and heritrixPassword (login and password you used to start heritrix) and verapdfPath(path to verapdf shell script, i.e. "/home/user/verapdf/verapdf").
  
###Running Logius application
First you need to ensure that you are running Logius application with java 8 and then you can start the application using commands:
  
  	export JAVA_HOME=your_path_to_java8
	java -jar "your_directory/LogiusWebApp/target/LogiusWebApp-1.0-SNAPSHOT.jar -server "path_to_your_config_file/config.yml"