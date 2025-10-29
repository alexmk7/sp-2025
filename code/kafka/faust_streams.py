import configparser
import random
import re
from typing import AsyncIterable

import faust
from aiohttp.web import Request


def gen_text() -> str:
    words = list({"hello", "world", "hey", "no", "yes"})
    return " ".join(random.choice(words) for _ in range(random.randint(2, 10)))


class Text(faust.Record):
    text: str


def main(topic: str, bootstrap_servers: str):
    app = faust.App("hello-app", broker=bootstrap_servers)

    text_topic = app.topic(topic, key_type=bytes, value_type=Text, partitions=1)

    counts = app.Table(
        "word-counts", key_type=str, value_type=int, default=int, partitions=1
    ).tumbling(size=10, expires=20, key_index=True)

    @app.page("/count/{word}/")
    @app.table_route(table=counts, match_info="word")
    async def get_count(web, request: Request, word: str):
        return web.json(
            {
                word: counts[word].now(),
            }
        )

    @app.agent(text_topic)
    async def text_topic_listener(texts: AsyncIterable[str]):
        async for text in texts:
            for word in re.findall("\w+", text.lower()):
                counts[word] += 1
            print(list(counts.items()))

    @app.timer(interval=4.0)
    async def text_sender(app: faust.App):        
        await text_topic.send(
            value=Text(text=gen_text()),
        )

    app.main()


if __name__ == "__main__":
    cfg = configparser.ConfigParser()
    cfg.read("properties.ini")

    main("test-topic", cfg["Kafka"]["bootstrap_servers"])
