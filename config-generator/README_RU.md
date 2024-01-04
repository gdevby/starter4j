# config-generator

Данный модуль создает конфиги формата json для запуска будущего java desktop приложения. <br>
Он конфигурирует все необходимые файлы, java jre для запуска на различный ОС, а так же ресурсы (картинки...), зависимости, аргументы запуска приложения и jvm.<br>

Вы можете попробовать создать конфиги для тестового desktop java Hello World проекта.</br>
Для проверки работоспособности вам необходимо выполнить следующие команды:<br>
<ol>
<li>Создать тестовую директорию</li>
<li>Клонировать в тестовую директорию тестовый проект при помощи команды: git clone https://github.com/gdevby/starter-app.git</li>
<li>Клонировать в тестовую директорию основной проект при помощи команды: git clone https://github.com/gdevby/desktop-starter-launch-update-bootstrap.git<br></li>
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
| -appArguments| currentAppVersion={currentAppVersion} |Aргументы приложения, эти аргументы передаются в запускаемое приложение|  
| -jvmArguments| -Xmx512m,-Dfile.encoding=UTF8,-Djava.net.preferIPv4Stack=true |Aргументы для java виртуальной машины|
| -appJar| starter-app-1.0.jar | Название запускаемого jar файла |  
| -javaFolder| ../../starter-app/example-compiled-app/jres_default |Каталог, в котором хранится jvm для создания конфигурации java |  
| -javaConfig| src/test/resources |Директория, где храниться результат jvm конфигурации, можно переиспользовать без генерации каждый раз указав -skinJVMGeneration=true|  
| -resources| ../../starter-app/src/main/resources |Директория с необходимыми ресурсами для запуска приложения|  
| -dependencies| ../../starter-app/example-compiled-app/target/dependencies |Директория с необходимыми зависимостями для запуска приложения|  
| -appFolder| ../../starter-app/example-compiled-app/target |Директория, где хранится jar файл запускаемого приложения|  
| -url| https://raw.githubusercontent.com/gdevby/starter-app/master/example-compiled-app/server/ |Домен по которому будут доступны конфиги для скачивания|  
| -skipJVMGeneration| false |флаг позволяющий пропустить генерацию java|  
| -help|  |указав флаг можно вызвать помощь с описание всех команд|  

Для дальнейшей корректной вашего приложения вам рекомендуется изменить следующие аргументы:
<ol>
	<li>-appJar</li>
	<li>-javaFolder</li>
	<li>-resources</li>
	<li>-dependencies</li>
	<li>-appFolder</li>
	<li>-url</li>
	<li>-mainClass</li>
</ol>