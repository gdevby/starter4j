# http-download

Данный модуль позволяет два сценарии использования:<br>
<br>
Первый:
	<li> Кэшировать результат ответа ресурса по url, при повторном вызове по url возращается результат сохраненный в папке кэша, при этом проверяется актуальность файла  с помощью http head по e-tag, то есть проверка происходит по заголовкам http.</li>
Диаграмма последовательности:
![first_ru](https://user-images.githubusercontent.com/48221408/157828914-b91b6808-3979-4d52-a767-e187066d1cf6.jpg)
	<li> Кэшировать результат ответа ресурса по url на определенное время, при последующем вызове по url будет возращаться результат из кэша.</li> 
Диаграмма последовательности:
![second_ru](https://user-images.githubusercontent.com/48221408/157828926-b813725a-94f7-4d42-b4b1-dbbe52ff17b8.jpg)
	<li> Возможно возвращать java объект, если на сервере лежит json файл.</li>
Пример:
[GsonServiceImplTest1.java](https://github.com/gdevby/starter4j/blob/master/http-download/src/test/java/by/gdev/http/head/cache/GsonServiceImplTest1.java)<br>
<br>
Второй:
<li> Загружать файлы многопоточно по URI, из нескольких URL, после валидируем их, возможно реализовать разархиварование с помощью обработчиков  </li>

Пример:
[DownloadTest.java](https://github.com/gdevby/starter4j/blob/master/starter-core/src/test/java/by/gdev/core/DownloadTest.java)