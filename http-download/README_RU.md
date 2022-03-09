# http-download

Данный модуль позволяет два сценарии использования:<br>
<br>
Первый:
	<li> Кэшировать результат ответа ресурса по url, при повторном вызове по url возращается результат сохраненный в папке кэша, при этом проверяется актуальность файла  с помощью http head по e-tag, то есть проверка происходит по заголовкам http.</li>
Диаграмма последовательности:
  ![top](https://user-images.githubusercontent.com/48221408/135266529-7da025a8-9fc7-47ff-8753-5b424182b4bf.png)
	<li> Кэшировать результат ответа ресурса по url на определенное время, при последующем вызове по url будет возращаться результат из кэша.</li> 
Диаграмма последовательности:
![r](https://user-images.githubusercontent.com/48221408/135227297-e5bad530-76ed-498c-ab2b-0fc50a75fc7d.png)
	<li> Возможно возвращать java объект, если на сервере лежит json файл.</li>
Пример:
[GsonServiceImplTest1.java](https://github.com/gdevby/desktop-starter-launch-update-bootstrap/blob/master/http-download/src/test/java/by/gdev/http/head/cache/GsonServiceImplTest1.java)<br>
<br>
Второй:
<li> Загружать файлы многопоточно по URI, из нескольких URL, после валидируем их, возможно реализовать разархиварование с помощью обработчиков  </li>

Пример:
[DownloadTest.java](https://github.com/gdevby/desktop-starter-launch-update-bootstrap/blob/master/starter-core/src/test/java/by/gdev/core/DownloadTest.java)