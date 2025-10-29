import json
import random
import time
from datetime import datetime
from confluent_kafka import Producer


KAFKA_BROKER = "localhost:9092"
TOPIC = "telemetry"


def main(broker: str, topic: str):
    producer = Producer({"bootstrap.servers": broker})

    DEVICE_IDS = ["sensor-1", "sensor-2", "sensor-3"]

    def generate_telemetry() -> dict[str, object]:
        return {
            "timestamp": datetime.now().isoformat(),
            "device_id": random.choice(DEVICE_IDS),
            "temperature": round(random.uniform(20, 30), 2),
            "humidity": round(random.uniform(30, 60), 2),
            "voltage": round(random.uniform(3.2, 4.2), 2),
        }

    try:
        while True:
            telemetry = generate_telemetry()
            producer.produce(
                topic,
                key=str(telemetry["device_id"]).encode("utf-8"),
                value=json.dumps(telemetry).encode("utf-8"),
            )

            print(f"Sent: {telemetry}")

            sleep_time = random.uniform(0.5, 3.0)
            time.sleep(sleep_time)
    finally:
        producer.flush()


if __name__ == "__main__":
    main(KAFKA_BROKER, TOPIC)
