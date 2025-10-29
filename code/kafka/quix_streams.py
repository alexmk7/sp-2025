import os
import re
from quixstreams import Application

app = Application(
    broker_address="localhost:9092",
    consumer_group="group3",
    auto_offset_reset="earliest"
)

words_topic = app.topic(name="words", value_deserializer="str")
word_length_topic = app.topic(name="word_length_topic")

sdf = app.dataframe(topic=words_topic)

sdf = sdf.apply(lambda text: len(text))
sdf = sdf.hopping_window(duration_ms=5000, step_ms=1000).mean().current()

sdf = sdf.to_topic(word_length_topic)

if __name__ == "__main__":
    # Start message processing
    app.run(sdf)