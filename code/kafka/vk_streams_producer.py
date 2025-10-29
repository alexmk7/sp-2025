import asyncio
import configparser
import json
import logging
import urllib.parse
from dataclasses import dataclass
from typing import (Any, Awaitable, Callable, Iterable, List, Mapping,
                    Optional, Tuple)

import httpx
import websockets
from confluent_kafka import Consumer, Producer, TopicPartition
from websockets.typing import Data

LOG = logging.getLogger(__name__)


@dataclass
class Rule:
    tag: str
    value: str


@dataclass
class Event:
    author: str
    message: str


class VkHttpException(Exception):
    def __init__(
        self,
        message: str,
        error_code: int,
    ):
        self.message, self.error_code = message, error_code

    def __str__(self):
        return f"VK error '{self.error_code}': {self.message}"


class VkStreaming:
    def __init__(self, endpoint: str, key: str):
        self.key = key
        self.endpoint = endpoint

    async def _api_request(
        self,
        path: str,
        args: Optional[Mapping[str, str]] = None,
        payload: Mapping[str, Any] = None,
        method: str = "get",
    ) -> Mapping[str, Any]:
        args = args or dict()
        if "key" not in args:
            args = dict(args)
            args["key"] = self.key

        url = f"https://{self.endpoint}/{path}/?{urllib.parse.urlencode(args)}"

        async with httpx.AsyncClient() as client:
            match method:
                case "get":
                    r = await client.get(url)
                case "post":
                    r = await client.post(url, json=payload)
                case "delete":
                    r = await client.request(method="delete", url=url, json=payload)

            self.raise_for_status(r)

            return r.json()

    async def get_rules(self) -> List[Rule]:
        resp = await self._api_request("rules", method="get")
        return [Rule(**entry) for entry in resp["rules"]]

    async def add_rule(self, rule: Rule):
        await self._api_request(
            "rules",
            payload={"rule": {"value": rule.value, "tag": rule.tag}},
            method="post",
        )

    async def delete_rule(self, tag: str):
        await self._api_request("rules", payload={"tag": tag}, method="delete")

    async def delete_all_rules(self):
        rules = await self.get_rules()
        for rule in rules:
            await self.delete_rule(rule.tag)

    async def start(
        self,
    ) -> Iterable[Event]:  # , callback: Callable[[int], Awaitable[None]]):
        url = f"wss://{self.endpoint}/stream?key={self.key}"
        async for websocket in websockets.connect(url):
            try:
                async for message in websocket:
                    data = json.loads(message)
                    yield Event(
                        '"' + data["event"]["event_url"] + '"',
                        '"' + data["event"]["text"] + '"',
                    )

            except websockets.ConnectionClosed:
                asyncio.sleep(5)
                continue

    def start_sync(self) -> Iterable[Data]:
        ait = self.start().__aiter__()
        loop = asyncio.new_event_loop()

        async def get_next():
            try:
                obj = await ait.__anext__()
                return False, obj
            except StopAsyncIteration:
                return True, None

        while True:
            done, obj = loop.run_until_complete(get_next())
            if done:
                break
            yield obj

    @classmethod
    async def create(cls, access_token: str) -> "VkStreaming":
        url = f"https://api.vk.com/method/streaming.getServerUrl?v=5.199&access_token={access_token}"
        async with httpx.AsyncClient() as client:
            r = await client.get(url)
            r.raise_for_status()

            resp_json = r.json()["response"]
            return VkStreaming(endpoint=resp_json["endpoint"], key=resp_json["key"])

    @classmethod
    def raise_for_status(cls, r: httpx.Response):
        if r.status_code != 200:
            raise VkHttpException(
                f"Http error, status code is {r.status_code}", r.status_code
            )

        resp_json = r.json()
        if resp_json["code"] != 200:
            raise VkHttpException(
                message=resp_json["error"]["message"],
                error_code=resp_json["error"]["error_code"],
            )


def main(access_token: str, topic: str, bootstrap_servers: str):
    with asyncio.Runner() as r:
        vs = r.run(VkStreaming.create(access_token))

        rules = r.run(vs.get_rules())

        print(rules)

        producer = Producer({"bootstrap.servers": bootstrap_servers})

        for data in vs.start_sync():
            producer.produce(
                topic,
                key=data.author.encode("utf-8"),
                value=data.message.encode("utf-8"),
            )
            print(data)


if __name__ == "__main__":
    logging.basicConfig(format="%(levelname)s:%(message)s", level=logging.ERROR)

    cfg = configparser.ConfigParser()
    cfg.read("properties.ini")

    main(
        cfg["VK"]["access_token"],
        cfg["Kafka"]["topic"],
        cfg["Kafka"]["bootstrap_servers"],
    )
