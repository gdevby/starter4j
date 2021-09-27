# http-head-cache

Данный модуль позволяет определить актуальность данных хранящихся в файле, путем сравнивания  "ETag", "Content-Length" и "Last-Modified".<br>
Для начала работы необходимо вызвать метод getObject(url, Class)

```java
		HttpServiceImpl httpService = new HttpServiceImpl();
		FileServiceImpl fileService = new FileServiceImpl(httpService, GSON, CHARSET);
		GsonServiceImpl gsonService = new GsonServiceImpl(GSON, fileService);	
		String url = "http://localhost:81/test.json";
		MyTestType myTest = gsonService.getObject(url, MyTestType.class);
```
где: <br>
url - url файла хранящегося на сервера,<br>
Class - класс описывающий данные в файле.<br>

При первом запуске сохраняются метаданне описываюище файл и сам файл.При последующем запуске проверяются метаданные файла, которые мы получаем с сервера и те которые сохранены локально. Если метаданные равны возвращается локальный файл, в противном случае получаем файл с сервера, сохраняем и уже получаем актуальный файл.
При изменных метаданных файл изменяет свое содержимое.

## Зависимости
	* Lombok
	* Gson
	* Apache Commons IO
	* Apache HttpClient
