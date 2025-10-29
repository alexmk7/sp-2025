## Импортирование проекта

Проект проще всего открыть с помощью Intellij IDEA. В меню:
`File -> New -> Project from Existing Sources`,  выбрать директорию с проектом, выбрать `gradle` и импортировать.

Небольшой проект, пример использования [VK Streaming API](https://vk.com/dev/streaming_api),
`Apache Kafka` и `Spark Streaming`.

С помощью `VK Streaming` можно в реальном времени получать сообщения в `VK` по каким-то ключевым словам. Всё это попадает в `Каfka`, оттуда в `Spark Streaming`, где каждые 40 секунд с перекрытием в 10 секунд считается статистика самых популярных слов в сообщениях.

## Файлы
`KafkaConfig.kt` - утилитарный класс, константы и создание параметров для клиента Кафки

`KafkaConsumer.kt` - consumer, читает сообщения из топика `vk-input` и выводит на экран

`KafkaVKProducer.kt` - producer, получает посты через vk streaming и публикует их в топике `vk-input`

`SparkConsumer.kt` - пример интеграции Kafka и Spark Streaming. Читаем тексты из топика `vk-input`, разбиваем на
слова и составляем статистику слова по окнам длиной 40 секунд с промежутком 10 секунд, выводим на экран 5 самых частотных слов

## Запуск

Для запуска нужно [создать](https://vk.com/editapp?act=create) своё приложение в `VK`. Нужно выбрать standalone-приложение и получить `appId` и `secretKey`. Эти данные нужно записать в файл 
`src/main/resources/vk_credentials.conf`
 
в виде

>app_id=...
> 
>access_token=... 

Затем:

1. Запускаем Kafka в контейнере
```bash
docker run --rm --name kafka_sandbox -v `pwd`:/course \
       -p 2181:2181 -p 9092:9092 \
       -e ADVERTISED_HOST=127.0.0.1 \
       -e NUM_PARTITIONS=2 \
       johnnypark/kafka-zookeeper
```       
2. Запускаем producer: `KafkaVKProducer.kt`
3. Запускам потоковыю обработку через Spark `SparkConsumer.kt`