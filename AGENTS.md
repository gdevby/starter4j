# AGENTS.md — starter4j

Инструкции для AI-агентов, работающих с репозиторием **starter4j** ([gdevby/starter4j](https://github.com/gdevby/starter4j)).

## Обзор проекта

**starter4j** — Java-лаунчер для desktop-приложений (JavaFX / Swing) с автообновлением, проверкой окружения и загрузкой зависимостей с сервера. Альтернатива launch4j с другой моделью: JAR не упаковывается в EXE; установщик скачивает лаунчер и запускает приложение по конфигам с сервера.

| Параметр | Значение |
|---|---|
| Статус | beta |
| Лицензия | MIT |
| Java | 25 |
| Версия артефактов | `${revision}` в корневом `pom.xml` (сейчас `1.25`) |
| groupId | `by.gdev` |
| Платформы | Windows, Linux, macOS |
| Тестовое приложение | [gdevby/starter-app](https://github.com/gdevby/starter-app) |

Документация проекта:
- [README.md](README.md) / [README_RU.md](README_RU.md)
- [GitHub Wiki](https://github.com/gdevby/starter4j/wiki) (дублируется на EN и RU)

## Архитектура

Maven multi-module проект. Зависимости между модулями:

```
starter4j/
├── starter-core/                    # Лаунчер: JavaFX UI, оркестрация запуска
├── config-generator/                # Генерация JSON-конфигов для деплоя на сервер
├── http-download/                   # HTTP-кэш, многопоточная загрузка, валидация hash
├── desktop-common-util/             # OS, пути, GPU, JSON, FileMapperService
├── desktop-common-util-additional/  # Простой системы, автозапуск
└── pom.xml                          # Родительский POM, revision, deploy-профиль
```

### Жизненный цикл (разработчик → пользователь)

```mermaid
flowchart LR
  A[Сборка приложения] --> B[config-generator]
  B --> C[JSON + JRE + ресурсы на сервер]
  C --> D[Установщик / starter-core.jar]
  D --> E[Проверки окружения]
  E --> F[Загрузка и валидация файлов]
  F --> G[Запуск desktop-приложения]
```

1. **Подготовка:** `mvn clean install`, затем `config-generator` создаёт конфиги в `target/out/<appName>/`.
2. **Деплой:** каталог приложения и `jres_default` выкладываются на HTTP-сервер (нужны GET, HEAD, докачка/resume).
3. **Запуск:** пользователь стартует установщик или `starter-core-*.jar`; лаунчер скачивает `appConfig.json`, JRE, зависимости и запускает целевое приложение.

Пример структуры на сервере: [starter-app/example-compiled-app/server](https://github.com/gdevby/starter-app/tree/master/example-compiled-app/server).

### Ключевые классы

| Класс | Модуль | Назначение |
|---|---|---|
| `by.gdev.Launcher` | starter-core | Точка входа JAR (`Main-Class` в shade-профиле) |
| `by.gdev.Main` | starter-core | JavaFX `Application`; парсит аргументы, инициализирует UI и `Starter` |
| `by.gdev.component.Starter` | starter-core | Основной pipeline: кэш, self-update, валидация, загрузка, запуск процесса |
| `by.gdev.model.StarterAppConfig` | starter-core | CLI-параметры лаунчера; `DEFAULT_CONFIG`, `URI_APP_CONFIG`, флаг `prod` |
| `by.gdev.model.AppConfig` | starter-core | Конфиг целевого приложения с сервера |
| `by.gdev.handler.*` | starter-core | Валидация окружения, обновления, self-update core |
| `by.gdev.subscruber.ConsoleSubscriber` | starter-core | EventBus: лог загрузки, завершение по фразе из дочернего процесса |
| `by.gdev.ui.subscriber.ViewSubscriber` | starter-core | EventBus: отображение ошибок и UI-событий |
| `by.gdev.generator.AppConfigCreator` | config-generator | Генерация конфигов приложения |
| `by.gdev.http.download.impl.*` | http-download | HTTP-клиент, загрузчик, кэш, Gson |
| `by.gdev.util.DesktopUtil`, `OSInfo` | desktop-common-util | Кроссплатформенные пути и определение ОС |

### Pipeline запуска (`Main.work`)

Порядок вызовов в `Starter`:

1. `cleanCache()` — очистка устаревшего кэша
2. `updateApplication()` — self-update лаунчера (`starterUpdate.json`)
3. `validateEnvironmentAndAppRequirements()` — диск, temp, права, шрифты, KB4515384 (Windows)
4. `prepareResources()` — загрузка/обновление JRE, jar, ресурсов; проверка SHA1
5. `runApp()` — запуск целевого процесса через `JavaProcessHelper`

Коммуникация между компонентами — **Google EventBus** (`ExceptionMessage`, `DownloaderStatus`, `StarterAppProcess`).

## Конфигурация и сервер

### Файлы на сервере

| Файл | Назначение |
|---|---|
| `{appName}/appConfig.json` | Актуальный конфиг (последняя версия) |
| `{appName}/{version}/appConfig.json` | Конфиг конкретной версии (`-version`) |
| `starterUpdate.json` | Self-update лаунчера по ОС (sha1 + uri) |
| `domainAvailabilityV1.json` | Карта доступных доменов/зеркал |
| `jre_default/` | JRE для разных OS/arch |

### Важные параметры `StarterAppConfig`

- `-uriAppConfig` — URI каталога с `appConfig.json` (должен заканчиваться на `/`). **Только для тестов.**
- **Production:** изменить `StarterAppConfig.DEFAULT_CONFIG` / `URI_APP_CONFIG` и пересобрать `starter-core`.
- `prod = true` — запрещает подмену `-uriAppConfig`; URI должен совпадать с `URI_APP_CONFIG` (защита от запуска вирусов через подписанный лаунчер).
- Рабочая директория (приоритет): `-workDirectory` → `installer.properties` (`work.dir=...`) → системный путь через `DesktopUtil.getSystemPath`.
- `-testURLs` — проверка доступности серверов перед запросами (в README иногда `-urlConnection` — в коде это `-testURLs`).

### Сигнал успешного запуска дочернего приложения

Лаунчер завершается, когда дочерний процесс пишет в stdout/log строку **`starter can be closed`**. Обработка в `ConsoleSubscriber`. Флаг `-ignoreExitPhraseFromChildApp` отключает это поведение.

### Особые случаи (уже реализованы в коде)

- Путь к JAR с символом `!` — блокируется в `Main.checkOnInvalidPath()`.
- Имя пользователя Windows с кириллицей — смена рабочей директории (см. README).
- Офлайн-режим — конфиги кэшируются локально.

## Сборка и тестирование

### Требования

- JDK 25
- Maven 3.x
- Для полного E2E-теста: nginx на `http://localhost:81/` (GET, HEAD, resume)
- Тестовый проект: `git clone https://github.com/gdevby/starter-app.git`
- Для нативной сборки (`-Pnative`): GraalVM Native Image с поддержкой JavaFX; переменная окружения **`GRAALVM_HOME`** должна указывать на корень дистрибутива (например, Liberica NIK 25 на Java 25). На Windows также нужен MSVC toolchain для линковки.

### Команды

```bash
# Сборка всех модулей
mvn clean install

# Fat JAR лаунчера (shade-профиль)
cd starter-core
mvn clean package -Pshade
# java -jar target/starter-core-*.jar -uriAppConfig http://localhost:81/starter-app/

# Генерация конфигов
cd config-generator
mvn clean package -Pshade
java -jar target/config-generator-*.jar -url http://localhost:81/

# Тесты модуля
mvn test -pl starter-core
mvn test -pl http-download
mvn test -pl config-generator

# Публикация в Maven Central (нужны GPG + settings.xml)
mvn clean deploy -P deploy
```

### Нативная сборка (GraalVM Native Image)

Профиль **`native`** в `starter-core` использует [native-maven-plugin](https://graalvm.github.io/native-build-tools/latest/maven-plugin.html) (`org.graalvm.buildtools:native-maven-plugin`) для сборки standalone-исполняемого файла вместо fat JAR.

**Требования (Windows):**

- Установлен GraalVM-дистрибутив **с JavaFX** (обычный GraalVM CE без JavaFX не подойдёт).
- Переменная **`GRAALVM_HOME`** — путь к корню дистрибутива; **`JAVA_HOME`** — тот же путь (или `%GRAALVM_HOME%`).
- Проверенный вариант: **Liberica NIK 23** (Java 21, full / с JavaFX).
- MSVC / Windows SDK — для линковки native-image.

**Команда:**

```bash
cd starter-core
mvn clean package -Pnative
```

**Результат:** `starter-core/target/starter-core-<version>.exe` (имя задаётся `imageName` в профиле).

**Конфигурация профиля** (`starter-core/pom.xml`):

- `mainClass`: `by.gdev.Launcher`
- `-march=compatibility`, `--enable-url-protocols=https`
- Windows: `-H:NativeLinkerOption=/SUBSYSTEM:WINDOWS`, `/ENTRY:mainCRTStartup` (GUI-приложение без консоли)

**Рефлексия и ресурсы:** предконфигурация native-image в `starter-core/src/main/resources/META-INF/native-image/` (`reflect-config.json`, `resource-config.json`, `proxy-config.json`, `serialization-config.json`, `jni-config.json`, `predefined-classes-config.json`). При добавлении reflection-heavy зависимостей или новых ресурсов может потребоваться обновление этих файлов.

Профили **`shade`** (fat JAR) и **`native`** (exe) взаимоисключающие — активируется один из них при `package`.

`starter-core` в тестах поднимает MockServer на порту **1080** (`mockserver-maven-plugin`).

### Локальный прогон (из wiki)

```bash
git clone https://github.com/gdevby/starter4j.git
git clone https://github.com/gdevby/starter-app.git
cd starter4j && mvn clean install
cd config-generator
java -jar target/config-generator-*.jar -url http://localhost:81/
# Загрузить target/out/starter-app/ на сервер
cd ../starter-core
java -jar target/starter-core-*.jar -uriAppConfig http://localhost:81/starter-app/
```

## Установщики и JRE

Wiki-инструкции (EN/RU дублируют друг друга):

| Тема | Wiki |
|---|---|
| Создание конфигов | [Create config file](https://github.com/gdevby/starter4j/wiki/Create-config-file) |
| Установщики Win/Linux/macOS | [Creating installers](https://github.com/gdevby/starter4j/wiki/Creating-installers) |
| JRE через jlink + JavaFX | [Создание JRE из jdk и javafx](https://github.com/gdevby/starter4j/wiki/%D0%A1%D0%BE%D0%B7%D0%B4%D0%B0%D0%BD%D0%B8%D0%B5-JRE-%D0%B8%D0%B7-jdk-%D0%B8-javafx.) |
| Maven Central | [Публикация в Central Repository](https://github.com/gdevby/starter4j/wiki/%D0%9F%D1%83%D0%B1%D0%BB%D0%B8%D0%BA%D0%B0%D1%86%D0%B8%D1%8F-%D0%B2-Central-Repository) |

- **Windows:** NSIS + плагины Inetc, Nsis7z; пример скриптов в [starter-app/example-compiled-app](https://github.com/gdevby/starter-app/tree/master/example-compiled-app). Альтернатива JAR-лаунчеру — нативный exe через `-Pnative` (см. раздел «Нативная сборка»).
- **Linux:** `dpkg-deb`, структура DEB-пакета описана в wiki.
- **macOS:** `javapackager -deploy -native dmg` (нужен JDK с javapackager).

Для установщиков старых версий приложения используйте `-version <old_version>` при сборке.

## Правила для агентов

### Область изменений

| Задача | Куда смотреть / править |
|---|---|
| Логика запуска, UI лаунчера | `starter-core/src/main/java/by/gdev/` |
| Нативная сборка, native-image | `starter-core/pom.xml` (профиль `native`), `META-INF/native-image/` |
| CLI-параметры лаунчера | `StarterAppConfig.java` |
| Генерация конфигов | `config-generator/` |
| HTTP-загрузка и кэш | `http-download/` |
| OS-утилиты, пути, GPU | `desktop-common-util/` |
| Автозапуск, downtime | `desktop-common-util-additional/` |
| Локализация UI | `starter-core/src/main/resources/application_*.properties` |
| Логирование | `logback.xml`, `logbackFull.xml` |
| Версия и deploy | корневой `pom.xml` (`revision`, профиль `deploy`) |

### Что нельзя ломать без явной необходимости

- Контракт **`starter can be closed`** — дочерние приложения и документация на него опираются.
- Флаг **`prod`** и проверка `URI_APP_CONFIG` в `Main.work()` — механизм безопасности production-сборок.
- Формат JSON-конфигов на сервере (`appConfig.json`, `starterUpdate.json`) — обратная совместимость с уже развёрнутыми клиентами.
- Требование trailing `/` у URI сервера (нормализация есть, но URL в конфигах должны быть согласованы).
- EventBus-подписчики (`ConsoleSubscriber`, `ViewSubscriber`) — единственный канал ошибок и прогресса в UI.

### Стиль кода

- Java 25, Maven, Lombok — следовать существующим паттернам.
- Пакет: `by.gdev.*` (и подпакеты модулей).
- CLI: JCommander (`@Parameter` в config-классах).
- JSON: Gson (`Main.GSON`, `GsonServiceImpl`).
- UI: JavaFX (`StarterStatusStage`, `UpdateStage`).
- HTTP: Apache HttpClient 5.
- **Сохранять существующие опечатки в именах пакетов/классов** при точечных правках: `subscruber`, `exeption`, `ValidatedPartionSize` — не переименовывать без отдельной задачи рефакторинга.
- Минимальный diff: не рефакторить несвязанный код.

### Тестирование изменений

| Изменение | Минимальная проверка |
|---|---|
| starter-core | `mvn test -pl starter-core`; ручной запуск с `-uriAppConfig` и starter-app |
| http-download | `mvn test -pl http-download` |
| config-generator | `mvn test -pl config-generator`; проверить `target/out/` |
| DEFAULT_CONFIG / prod | Сборка shade JAR + smoke-тест с локальным сервером |
| Нативная сборка (`-Pnative`) | `mvn clean package -Pnative` в `starter-core`; smoke-тест exe с `-uriAppConfig` |
| Локализация | Проверить ключи во всех `application_*.properties` |

## Полезные файлы

| Файл | Описание |
|---|---|
| [README.md](README.md) | Обзор, жизненный цикл, сравнение с launch4j |
| [starter-core/README.md](starter-core/README.md) | Аргументы CLI, self-update, prod |
| [config-generator/README.md](config-generator/README.md) | Аргументы генератора конфигов |
| [http-download/README.md](http-download/README.md) | Сценарии кэширования и загрузки |
| [desktop-common-util/README.md](desktop-common-util/README.md) | OS-утилиты |
| [pom.xml](pom.xml) | Модули, версии, Central Publishing |
| [GitHub Wiki](https://github.com/gdevby/starter4j/wiki) | Пошаговые инструкции деплоя и установщиков |

## Связанные репозитории

- **origin:** `https://github.com/gdevby/starter4j.git` — публичный GitHub
- **starter-app:** тестовое Hello World приложение, пример server layout и установщиков
