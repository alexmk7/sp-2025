## Импортирование проекта

Проект проще всего открыть с помощью Intellij IDEA. В меню: 
`File -> New -> Project from Existing Sources`,  выбрать директорию с проектом, выбрать `gradle` и импортировать.

## Файлы
`KafkaConfig.kt` - утилитарный класс, константы и создание параметров для клиента Кафки 

`KafkaWordsProducer.kt` - producer, генерирует текст из случайных слов и добавляет их в topic `wordcount-input`

`KafkaWordsConsumer.kt` - consumer, читает сообщения из топиков  `wordcount-input`, `wordcount-output` и выводит на экран

`KafkaStreamsWordCount.kt` - пример KafkaStreaming, читает данные из топика `wordcount-input` считает статистику слов и 
записывает изменения в топик `wordcount-output`

`SparkConsumer.kt` - пример интеграции Kafka и Spark Streaming. Читаем тексты из топика `wordcount-input`, разбиваем на 
слова и составляем статистику слова по окнам длиной 10 секунд с промежутком 2 секунды, выводим на экран 5 самых частотных слов   

## Запуск

1. Запускаем Kafka в контейнере
```bash
docker run --rm --name kafka_sandbox -v `pwd`:/course \
       -p 2181:2181 -p 9092:9092 \
       -e ADVERTISED_HOST=127.0.0.1 \
       -e NUM_PARTITIONS=2 \
       johnnypark/kafka-zookeeper
```       
2. Запускаем producer: `KafkaWordsProducer.kt`
3. Запускам обработку потока: `KafkaStreamsWordCount.kt`
4. Запускам потоковыю обработку через Spark `SparkConsumer.kt`