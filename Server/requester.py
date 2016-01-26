# -*- coding: utf-8 -*-
import requests, json, logging, sys
from log import logger
from bson.objectid import ObjectId
from bson import json_util



class Requester:
    _POST_INSTANCE_URL = 'http://127.0.0.1:5000/instances'
    _GET_URL = 'http://127.0.0.1:5000/get_instances'
    _instance = {}
    _instance['name'] = 'instance_name112777'
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
        response = requests.post(self._POST_INSTANCE_URL, json=self._instance).json()
        tempjson = json_util.dumps(response)
        formatted_response = json_util.loads(tempjson)
        logger.info('response: {}'.format(formatted_response))
        return formatted_response

    def get_it(self):
        instances = requests.get(self._GET_URL)._content
        for instance in instances:
            print(instance)

    def post_update(self, url):
        logger.info('Posting to: ' + url)
        response = requests.post(url, json=self._update).json()
        tempjson = json_util.dumps(response)
        formatted_response = json_util.loads(tempjson)
        logger.info('response: {}'.format(formatted_response))
        return formatted_response

    def test_istance_update(self):
        logger.info('execute test')
        response = self.post_instance()
        id = response["_id"]
        logger.info("ID: {}".format(str(id)))
        url = '{}{}{}'.format('http://127.0.0.1:5000/instances/', id, '/updates')
        self.post_update(url)





requester = Requester()
requester.test_istance_update()
