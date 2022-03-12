# config-generator

[Russian version](https://github.com/gdevby/desktop-starter-launch-update-bootstrap/blob/master/config-generator/README_RU.md)<br>
This module creates json format configs to run the future java desktop application.<br>
It configures all the necessary files, java jre to run on a different OS, as well as resources (images...), dependencies, application startup arguments and jvm.<br>
You can try to create configs for test desktop java Hello World project.</br>
To test to functionality you need to run the following commands<br>
<ol>
<li>Create test directory</li>
<li>Clone the test project to the test directory using the command: git clone https://github.com/gdevby/starter-app.git</li>
<li>Clone the main project to the test directory using the command: git clone https://github.com/gdevby/desktop-starter-launch-update-bootstrap.git<br></li>
<li>In the project directory desktop-starter-launch-update-bootstrap run command mvn clean install</li>
<li>Go to directory desktop-starter-launch-update-bootstrap/config-generator</li>
<li>Run command java -jar target/config-generator-*.jar, where instead of * replace with version</li>
</ol>

Сonfig-generator has the following arguments:<br>

| Argument | Default | Description |
| :------: | :-----: | :---------: |
| -name| starter-app |Application name|  
| -version| 1.0 |Application version|  
| -mainClass| by.gdev.app.Main |Main class for running the application|  
| -appArguments|  |Application Arguments|  
| -jvmArguments| -Xmx512m,-Dfile.encoding=UTF8,-Djava.net.preferIPv4Stack=true |Arguments for java virtual machine|
| -appJar| starter-app-1.0.jar | The name of the jar file to run |  
| -javaFolder| ../../starter-app/example-compiled-app/jres_default |Directory where jvm is stored to create java config |  
| -javaConfig| src/test/resources |Directory where stored result jvm configuration, can be reused without generating each time specifying -skinJVMGeneration=true|  
| -resources| ../../starter-app/src/main/resources |Directory with the necessary resources to run the application|  
| -dependencies| ../../starter-app/example-compiled-app/target/dependencies |Directory with the necessary dependencies to run the application|  
| -appFolder| ../../starter-app/example-compiled-app/target |Directory where stored the jar file of the running application|  
| -url| https://raw.githubusercontent.com/gdevby/starter-app/master/example-compiled-app/server/ |Domain where configs will be available for download|  
| -skipJVMGeneration| false |flag to skip java generation|  
| -help|  |by specifying a flag, you can call help with a description of all commands|  

To further correct your application, you are advised to change the following arguments:
<ol>
	<li>-appJar</li>
	<li>-javaFolder</li>
	<li>-resources</li>
	<li>-dependencies</li>
	<li>-appFolder</li>
	<li>-url</li>
</ol>