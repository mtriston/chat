# Chat
## Сервер и клиент многопользовательского чата.

Для реализации проекта использовались:
* Sockets, Threads
* Spring: spring-context, spring-jbdc, spring-security
* Библиотеки GSON, HikariCP
* PostreSQL

Жизненный цикл программы:
* Регистрация или вход
  * Пароли хранятся в захешированном состоянии.
  * Проверка login на уникальность 
* Выбор или создание комнаты
* Чат
  * При подключении отображается история 30 последних сообщений в комнате.
  * Общение происходит в рамках одной комнаты
  * Сообщения передаются в JSON формате.

## База данных
Для работы требуется `PosgreSQL`.  
username/password необходимо указать `resources/db.properties`.  
При первом запуске сервера будет создана необходимая схема с именем `chat`.  
Структура таблиц:

![Screenshot from 2021-07-11 23-26-41](https://user-images.githubusercontent.com/52173536/125209319-96591f80-e2a0-11eb-93f1-f8af1b4847c3.png)

## Как использовать
В папке программы (Server/Client)
```bash
mvn install exec:java
```
либо
### Server
```bash
mvn
java -jar target/socket-server.jar --port=8081
```
### Client
```bash
mvn
java -jar target/socket-client.jar --port=8081
```
