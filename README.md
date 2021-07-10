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

## Как использовать
### сервер
```bash
cd Chat/Server
mvn
java -jar target/socket-server.jar --port=8081
```
### клиент
```bash
cd Chat/Сlient
mvn
java -jar target/socket-client.jar --port=8081
