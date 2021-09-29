# http-head-cache

Данный модуль позволяет следующие сценарии использования:
<ol>
	<li> Кэшировать результат ответа ресурса по url, при повторном вызове по url возращается результат сохраненный в папке кэша, при этом проверяется актуальность файла  с помощью http head по e-tag, то есть проверка происходит по заголовкам http.</li>
	![2](https://user-images.githubusercontent.com/48221408/134901731-4cd3c891-b172-4f8c-98f5-03db69cb5dad.png)
	<li> Кэшировать результат ответа ресурса по url на определенное время, при последующем вызове по url будет возращаться результат из кэша.</li>
	![r](https://user-images.githubusercontent.com/48221408/135227297-e5bad530-76ed-498c-ab2b-0fc50a75fc7d.png)
	<li> Возможно возвращать java объект, если на сервере лежит json файл.</li>
</ol>

## Пример 
<a href="https://github.com/gdevby/desktop-starter-launch-update-bootstrap/blob/master/http-head-cache/src/test/java/by/gdev/http/head/cache/GsonServiceImplTest1.java">GsonServiceImplTest1.java</a><br>
<a href="https://github.com/gdevby/desktop-starter-launch-update-bootstrap/blob/master/http-head-cache/src/test/java/by/gdev/http/head/cache/GsonServiceImplTest2.java">GsonServiceImplTest2.java</a>

## Зависимости
*	Lombok
*	Gson
*	Apache Commons IO
*	Apache HttpClient


