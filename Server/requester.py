# -*- coding: utf-8 -*-
import requests, json, logging, sys
from log import logger


class Requester:
    _POST_INSTANCE_URL = 'http://127.0.0.1:5000/instances'
    _GET_URL = 'http://127.0.0.1:5000/get_instances'
    _instance = {}
    _POST_UPDATE_URL = 'http://127.0.0.1:5000/instances/569a4551b498136d83732a48/updates'
    _instance['name'] = 'instance_name18'
    _instance['profile'] = 'instance_profile4'
    _update = {
        "Herausgeber": "Xema",
        "Nummer": "1234-5678-9012-3456",
        "Deckung": 2e+6,
        "Waehrung": "EURO",
        "Inhaber": {
            "Name": "Mustermann",
            "Vorname": "Max",
            "maennlich": 'yooo',
            "Hobbys": ["Reiten", "Golfen", "Lesen"],
            "Alter": 42,
            "Kinder": [],
            "Partner": 'nopeeee'
        }}

    def post_instance(self):
        logger.info('Posting to: ' + self._POST_INSTANCE_URL)
        logger.debug(requests.post(self._POST_INSTANCE_URL, json=json.dumps(self._instance))._content)

    def get_it(self):
        instances = requests.get(self._GET_URL)._content
        for instance in instances:
            print(instance)

    def post_update(self):
        logger.info('Posting to: ' + self._POST_UPDATE_URL)
        logger.debug(requests.post(self._POST_UPDATE_URL, json=self._update)._content)


requester = Requester()
# requester.get_it()
logger.info('requester...')

requester.post_update()
# requester.post_instance()
