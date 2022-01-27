# http-upload

Данный модуль позволяет следующие сценарии использования:

<ol>
	<li> Кэшировать результат ответа ресурса по url, при повторном вызове по url возращается результат сохраненный в папке кэша, при этом проверяется актуальность файла  с помощью http head по e-tag, то есть проверка происходит по заголовкам http.</li>
	<li> Кэшировать результат ответа ресурса по url на определенное время, при последующем вызове по url будет возращаться результат из кэша.</li>
	<li> Возможно возвращать java объект, если на сервере лежит json файл.</li>
</ol>

## Диаграмма последовательности
Для первого сценария:
![top](https://user-images.githubusercontent.com/48221408/135266529-7da025a8-9fc7-47ff-8753-5b424182b4bf.png)
Для второго сценария:
![r](https://user-images.githubusercontent.com/48221408/135227297-e5bad530-76ed-498c-ab2b-0fc50a75fc7d.png)
## Пример 
<a href="https://github.com/gdevby/desktop-starter-launch-update-bootstrap/blob/master/http-head-cache/src/test/java/by/gdev/http/head/cache/GsonServiceImplTest1.java">GsonServiceImplTest1.java</a><br>

## Зависимости
*	Lombok
*	Gson
*	Apache Commons IO
*	Apache HttpClient