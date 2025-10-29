import asyncio

from faststream import FastStream
from faststream.kafka import KafkaBroker


broker = KafkaBroker("localhost:9092")
app = FastStream(broker)

@broker.subscriber("words", "words_new", group_id="group2")
@broker.publisher("out-channel")
async def handle_msg(message: str) -> str:
    return str(len(message.split(" ")))


if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    loop.run_until_complete(app.start())
    loop.run_forever()
    