# http-head-cache

Сценарии использования:
<ol>
<li> Данный модуль позволяет кэшировать запросы с сервера и повторном вызове по url, он возращает результат с файла, при этом проверяя актуальность на сервере с помощью http head по e-tag, то есть здесь возвращаются только загаловки без тела ответа.</li>
<li> Кэшировать результат ответа ресурса по url на определенное время, при последующем вызове по url , будет возращаться результат из кэша.</li>
<li> Возможно возвращать java объект, если на сервере лежит json файл.</li>
</ol>

##Диаграмма последовательности
![UseCaseNumber1](https://user-images.githubusercontent.com/48221408/134877536-9c414467-ca13-4fbb-b2c8-d77aa2fa167c.jpg)
##Пример 
```java
	static Gson GSON = new Gson();
	static Charset CHARSET = StandardCharsets.UTF_8;
	
	public static void main(String[] args) throws IOException {
		HttpService httpService = new HttpServiceImpl();
		FileService fileService = new FileServiceImpl(httpService, GSON, CHARSET);
		GsonService gsonService = new GsonServiceImpl(GSON, fileService);	
		String url = "https://gdev.by/repo/test.json";
		MyTestType myTest = gsonService.getObject(url, MyTestType.class);
	}
```

## Зависимости
*	Lombok
*	Gson
*	Apache Commons IO
*	Apache HttpClient
