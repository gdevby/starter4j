# desktop-desktop.starter
[Russian version](https://github.com/gdevby/starter4j/blob/master/README_RU.md)<br>
Status: beta<br>
We want to create java desktop starter which can help to run java app faster and better. There is functionality to update your default application with low traffic and check the environment (memory amount, correct temp directory, etc.). We have detailed instructions to create installers for Windows, Linux, MacOS that will let you install java if your computer doesn't have it.<br>
Allows you to easily change your application configs on the server side.<br>
This solution is created on our 6-year experience in this area.<br>
<br>
**It works for: Linux, MacOS, Windows.**<br>
<br>
The project has the following life cycle:<br>
You need to prepare files from your desktop application:
<ol>
<li>Preparing files using config-generator</li>
<li>Uploading files to your server</li>
</ol>

To run your desktop app will have next steps:
<ol>
<li>Collects environment data</li>
<li>Performs checks (availability of free space, correctness of the temp directory, access rights to the working directory, problems with display fonts)</li>
<li>Downloads files (images, .jar, .zip, dependencies)</li>
<li>Checks the correctness of files by hash (for example, java files)</li>
<li>If necessary, updates your application to the user's choice (a selection window appears)</li>
<img align="middle" width="500" src="https://user-images.githubusercontent.com/48221408/155071002-1ffdd120-b8f0-4865-8401-75ccf3440cc2.jpg" alt="fork this repository" />
<li>Launch your application</li>
</ol>

Additional features of the starter:<br>
Solves the problem of changing the encoding if the username is in russian language, usually in this case it is impossible to run the application, we change the working directory to C:\program_name and display a message to the user about this.<br>
<br>
You can check how the test desktop application installers work by downloading ready-made for:
1) [Windows Installer](https://github.com/gdevby/starter-app/raw/master/example-compiled-app/os_installer/StarterInstaller.exe)
2) **Linux**<br>
Installation is done through the terminal, to do this, open the terminal and run the following command
```
wget https://github.com/gdevby/starter-app/blob/master/example-compiled-app/os_installer/installDebPackage.sh -O - | sh
```
3) **MacOs**<br>
Installation is carried out through the terminal and from the browser. To run from a browser, you need to sign code signing your installer. In this example, we'll show you how to run it from the terminal. Open a terminal and run the command
```
curl --remote-name https://raw.githubusercontent.com/gdevby/starter-app/master/example-compiled-app/os_installer/starter-1.0.dmg && chmod +x ./starter-1.0.dmg && open -W ./starter-1.0.dmg 
```

These modules can help you develop desktop applications:
1) Java paths, OS identification, user directories, in details [desktop-common-util]()
2) Multi-threaded download files and cache configs for desktop application, in detail [http-download](https://github.com/gdevby/starter4j/blob/master/http-download/README.md )
3) Get information on GPU, downtime and system activity, in detail [desktop-common-util-additional]()

This project solves similar problems as launch4j, but it solves differently:
1) Doesn't pack jar into exe. An installer is created that will download the launcher and run your jar file
2) There is an update functionality, therefore it suits better for projects with a large audience
3) It also provides ready-made functionality for desktop applications (validation)
4) Solves some bugs with java

link to the next [instruction](https://github.com/gdevby/starter4j/wiki/Create-config-file)
