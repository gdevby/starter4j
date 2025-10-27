# starter-core

[Russian version](https://github.com/gdevby/starter4j/blob/master/starter-core/README_RU.md)<br>

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
| -appName| starter | Installing directory for the application. |  
| -memory| 500 | The size of the required free disk space to download the application |  
| -uriAppConfig| http://127.0.0.1:80/starter-app/, http://127.0.0.1:81/starter-app/  | The URI by which appConfig.json is available, which contains all the information about the application being launched, this config is used by all applications by default. The URI must be specified without a version, see the description of the version parameter|  
| -workDirectory| starter  |The working directory where the files necessary for the application will be downloaded and where the application will run |  
| -version| null | Specifies the version of the application to run. Therefore, the config http://localhost:81/app/1.0/appConfig.json for version 1.0 will be used. This way we can install older versions of the application | 
|-urlConnection| http://www.google.com, http://www.baidu.com | List of url which use to do requests. When some url or servers are not available, it doesn't do request. It will skip for download file and to do requests. If we have server file http://example.com/repo than this field should be http://example.com") |
|-attempts| 3 | Number of attempts to restore the connection|
|-connectTimeout| 60000  |setting a value setConnectTimeout|
|-socketTimeout| 60000  |setting a value setSocketTimeout|
|-timeToLife| 600000 | File update time in seconds. After the expiration of this time, the file is not relevant|
|-cleaningOldCacheFiles| 10 | Number of days between automatic clearing of outdated cache files|
|-stop|false| Argument to automatically close the application after installation. Used for tests|
|-logURIService| null | Log service which can save logs and return code. User can send code for support. Doesn't implement a backend. To activate we need to use parameter ExceptionMessage#logButton=true, See ViewSubscriber#doRequest|


There is a field in StarterAppConfig.prod, the purpose of which is to prevent the use of a signed starter application to launch a virus by changing the -uriAppConfig parameter in the starter-core module. It must be changed to true for production.

Starter-core supports self-updating. To do this, you need to create a starterUpdate.json file. Example file contents:
```
{
  "LINUX":{
	"sha1":"e5b5bbce5daf4b8f016bec661122f48e1bd03292",
	"uri":"http://127.0.0.1:81/starter-core-1.1.jar"
  },
  "WINDOWS":{
	"sha1":"574f3fc6667dc31d94d2167ab904aac27edadd09 ",
	"uri":"http://127.0.0.1:81/starter-core-1.1.jar"
  }
}
```
where:<br>
LINUX - operating system<br>
sha1 - hash sum of the file being updated<br>
uri - uri to download file<br>