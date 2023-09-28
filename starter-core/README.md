# starter-core

[Russian version](https://github.com/gdevby/desktop-starter-launch-update-bootstrap/blob/master/starter-core/README_RU.md)<br>

This is the main module for running a desktop application. <br>
The lifecycle includes the following steps during application startup:
<ol>
<li>Environment definition (ОS).</li>
<li>Checking additional launch requirements (free disk space, access rights to the working directory, correct installation and access to the /temp directory, correct display of fonts, solving problems with the update KB4515384 (for Windows OS)).</li>
<li>Getting configs for launching an application, such as dependencies, resource files, launch options, launch arguments, application version.</li>
<li>Preparing resources for launch (updating the application version), validating current resources by hash sum, such as dependencies and resources, jre for different OS, and loading non-existent resources.</li>
<li>Launching the application itself.</li>
</ol>

*Can run offline, for this configs are cached, this allows you to store most configs on the server side.<br>

The by.dgev.Main class has the following arguments that can be changed:<br>

| Argument | Default | Description |
| :------: | :-----: | :---------: |
| -memory| 500 | The size of the required free disk space to download the application |  
| -uriAppConfig| http://127.0.0.1:81/starter-app/ | The URI by which appConfig.json is available, which contains all the information about the application being launched, this config is used by all applications by default. The URI must be specified without a version, see the description of the version parameter|  
| -workDirectory| starter  |The working directory where the files necessary for the application will be downloaded and where the application will run |  
| -version| 1.0 | Specifies the version of the application to run. Therefore, the config http://localhost:81/app/1.0/appConfig.json for version 1.0 will be used. This way we can install older versions of the application | 
|-cacheDirectory| starter/cache  | Directory for storing cached configs |  
|-urlConnection| http://www.google.com, http://www.baidu.com | List of sites to check Internet connection |
|-attempts| 3 | Number of attempts to restore the connection|
|-connectTimeout| 60000  |setting a value setConnectTimeout|
|-socketTimeout| 60000  |setting a value setSocketTimeout|
|-timeToLife| 600000 | File update time in seconds. After the expiration of this time, the file is not relevant|
|-stop|false| Argument to automatically close the application after installation. Used for tests|

There is a field in StarterAppConfig.prod, the purpose of which is to prevent the use of a signed starter application to launch a virus by changing the -uriAppConfig parameter in the starter-core module. It must be changed to true for production.