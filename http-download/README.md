# http-download

[Russian version](https://github.com/gdevby/desktop-starter-launch-update-bootstrap/blob/master/http-download/README_RU.md)<br>
This module allows two usage scenarios:<br>
<br>
First:
	<li>Cache the result of the response by url, when called again by url, the result is returned saved in the cache folder, while checking the relevance of the file using the http head by e-tag, that is, the check is performed by http headers.</li>
Sequence Diagram :
![first_en](https://user-images.githubusercontent.com/48221408/157828755-0850855c-cdb2-4566-856d-e0e89b3ec3d3.jpg)
	<li> Cache the result of the responce by url for a certain time, the next call by url will retutn the result from the cache.</li> 
Sequence Diagram :
![second_en](https://user-images.githubusercontent.com/48221408/157828762-9af6fb90-0f99-4bfb-9e45-5ba295d3bded.jpg)
	<li> It is possible to return a java object if there is a json file on the server</li>
Example:
[GsonServiceImplTest1.java](https://github.com/gdevby/desktop-starter-launch-update-bootstrap/blob/master/http-download/src/test/java/by/gdev/http/head/cache/GsonServiceImplTest1.java)<br>
<br>
Second:
<li>Download files multi-threaded by URI, from several URL, after validate them, it is possable to implement unzipping using handlers</li>

Example:
[DownloadTest.java](https://github.com/gdevby/desktop-starter-launch-update-bootstrap/blob/master/starter-core/src/test/java/by/gdev/core/DownloadTest.java)
