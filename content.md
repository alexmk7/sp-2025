
# Apache Kafka

### Концепции и архитектура

* Сommit log как модель данных.
* Брокеры, partition, replica/leader–follower.
* Контроллеры Kafka.
* Кафка с ZooKeeper vs Kraft (новая архитектура).

### Производители и потребители

* Производители (producers) и потребители (consumers).
* Гарантии доставки сообщений: at-most/at-least/exactly-once.
* Consumer groups, масштибирование и ребалансировка.
* Offset management: committed offsets.

### Особенности Kafka

* Retention: по времени, по размеру, key-based state retention.
* Сегментация хранилища: сегменты, индекс, time index.
* Идепотентный производитель.
* Транзакции: атомарная запись между партициями.
* Exactly Once Semantics (EOS) в Kafka Streams и производителях.

### Event sourcing 

* Потоки как первичные данные, таблицы как производные данные (KTable/KStream модель).
* Changelog topics.
* Приложения на основе архитектуры event sourcing.
* RocksDB как временное хранилище.

### ksqlDB

* Потоки и таблицы в SQL.
* Потоковые запросы (push queries).


### Faust Streams

* Архитектура: agent, table, changelog.
* Использование как Python-аналог Kafka Streams.
* Ограничения (нет развития) и замены.

### FastStream

* Современный фреймворк на Python.
* Producers/consumers.

### Quix Streams

* Event-driven Python SDK.
* Streaming features: windowing, state, joins.
* Интеграция с Kafka.
* Производственный фреймворк для ML/real-time analytics.

# Spark Structured Streaming

### Источники и приёмники

* Source и sink.
* Kafka source.
* File source.
* Rate generator для тестированиz.
* Генерация "искусственных" данных.

### Модель данных

* Micro-batch 
* "Неограниченные" DataFrames
* Watermarks и задержка данных.
* Триггеры.
* Stateful aggregations.
* Stream-stream joins.

