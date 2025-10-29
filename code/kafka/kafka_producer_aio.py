import asyncio
import random
from confluent_kafka.experimental.aio import AIOProducer


KAFKA_BROKER = "localhost:9092"
TOPIC = "words"


def gen_text() -> str:
    words = list({"hello", "world", "hey", "no", "yes"})
    return " ".join(random.choice(words) for _ in range(random.randint(2, 10)))


async def producer_1(broker: str, topic: str):
    producer = AIOProducer({"bootstrap.servers": broker})

    try:
        while True:
            text = gen_text()
            await producer.produce(topic, key=str(hash(text) % 3).encode("utf-8"), value=text.encode("utf8"))

            print(f"Sent to {topic}: {text}")

            sleep_time = random.uniform(0.5, 3.0)
            await asyncio.sleep(sleep_time)
    finally:
        producer.flush()      


async def main(broker: str, topic: str):
    await asyncio.gather(
        producer_1(broker, "words"),
        producer_1(broker, "words_new"),
    )


if __name__ == "__main__":
    asyncio.run(main(KAFKA_BROKER, TOPIC))
