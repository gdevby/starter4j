# config-generator

Данный модуль создает конфиги формата json для запуска будующего java desktop приложения. <br>
Он конфигурирует все необходимые файлы, java jre для запуска на различный ОС, а так же ресурсы (картинки...), зависимости, аргументы запуска приложения и jvm.<br>

Вы можете попробовать создать конфиги для тестового desktop java Hello World проекта.</br>
Для проверки работоспособности вам необходимо выполнить следующий команды:<br>
<ol>
<li>Создать тестовую дирректорию</li>
<li>Клонировать в тестовую дирректорию тестовый проет при помощи команды: git clone https://github.com/gdevby/starter-app.git</li>
<li>Клонировать в тестовую дирректорию основной проет при помощи команды: git clone https://github.com/gdevby/desktop-starter-launch-update-bootstrap.git<br></li>
<li>В директории проекта desktop-starter-launch-update-bootstrap выполнить команду mvn clean install</li>
<li>Перейти в директорию desktop-starter-launch-update-bootstrap/config-generator</li>
<li>Выполнить команду java -jar target/config-generator-*.jar, где вместо * заменить на версию</li>

</ol>
Сonfig-generator имеет следующие аргументы:<br>

| Аргумент | По умолчанию  | Описание  |
| :------: | :-----------: | :-------: |
| -name| starter-app |Название приложения|  
| -version| 1.0 |Версия приложения|  
| -mainClass| by.gdev.app.Main |Главный класс для запуска приложения|  
| -app|  |Aргументы приложения|  
| -jvm|   |Aргументы для java виртуальной машины|
| -file| starter-app-1.0.jar |запускаемый jar файл |  
| -javaFolder| ../../starter-app/jvms |Каталог, в котором хранится jvm для создания конфигурации java |  
| -javaConfig| src/test/resources |Сохраненный результат jvm конфигурации, можно переиспользовать без генерации каждый раз указав -flag=true|  
| -resources| ../../starter-app/src/main/resources/resources |Дирректория с необходимыми ресурсами для запуска приложения|  
| -dependencies| ../../starter-app/dep |Дирректория с необходимыми зависимостями для запуска приложения|  
| -appFolder| ../../starter-app |Каталог для сохраненной сгенерированной конфигурации нового приложения|  
| -domain| http://localhost:81/ |Домен по которому будут доступны конфиги для скачивания|  
| -flag| false |флаг позволяющий пропустить генерацию java|  
| -help| false |указав флаг true можно вызвать помощь с описание всех команд|  

Для дальнейшей корректной вашего приложения вам рекомендуеться изменить следующие аргументы:
<ol>
	<li>-javafolder</li>
	<li>-file</li>
	<li>-config</li>
	<li>-resources</li>
	<li>-dependencies</li>
	<li>-appfolder</li>
	<li>-domain</li>
</ol>