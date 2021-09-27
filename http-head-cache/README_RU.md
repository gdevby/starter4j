# http-head-cache

Данный модуль позволяет следующие сценарии использования:
<ol>
	<li> Кэшировать запросы с сервера, при повторном вызове по url возращается результат сохраненный в папке кэша, при этом проверяется актуальность файла  с помощью http head по e-tag, то есть проверка происходит по заголовкам http.</li>
	<li> Кэшировать результат ответа ресурса по url на определенное время, при последующем вызове по url будет возращаться результат из кэша.</li>
	<li> Возможно возвращать java объект, если на сервере лежит json файл.</li>
</ol>

## Диаграмма последовательности
![2](https://user-images.githubusercontent.com/48221408/134901731-4cd3c891-b172-4f8c-98f5-03db69cb5dad.png)

## Пример 
<a href="https://github.com/gdevby/desktop-starter-launch-update-bootstrap/blob/master/http-head-cache/src/test/java/by/gdev/http/head/cache/MainTest.java">MainTest.java</a>

## Зависимости
*	Lombok
*	Gson
*	Apache Commons IO
*	Apache HttpClient


