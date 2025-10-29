from confluent_kafka import Consumer, TopicPartition


KAFKA_BROKER = "localhost:9092"
TOPIC = "telemetry"


def main(broker: str, topic: str):
    consumer = Consumer({
        "bootstrap.servers": broker,
        "group.id": "group1",
        "auto.offset.reset": "latest"
        })

    consumer.subscribe([topic])

    # tp = TopicPartition(topic=topic, partition=1, offset=0)
    # consumer.assign([tp])
    # consumer.seek(tp)

    try:
        while True: 
            msg = consumer.consume(num_messages=1, timeout=1.0)            
            for m in msg:
                print(m.value()) 

    finally:
        consumer.close()


if __name__ == "__main__":
    main(KAFKA_BROKER, TOPIC)
