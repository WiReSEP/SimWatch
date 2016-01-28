# -*- coding: utf-8 -*-
import requests, json, logging, sys
from log import logger
from bson.objectid import ObjectId
from bson import json_util
import datetime
import random



class Requester:
    _POST_INSTANCE_URL = 'http://127.0.0.1:5000/instance'
    _GET_URL = 'http://127.0.0.1:5000/instance'
    _instance = {}
    _GET_INSTANCE_URL = None
    _instance['name'] = 'instance_name112777'
    _instance['profile'] = {"profilekey: ": "profilevalue"}
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
        logger.info('response: {}'.format(response))
        return response

    def get_every_instance(self):
        logger.info('Getting every instance...')
        response = requests.get(self._GET_URL).json()
        logger.info(str(response))


    def post_update(self, url):
        logger.info('Posting update to: ' + url)
        response = requests.post(url, json=self._update).json()
        tempjson = json_util.dumps(response)
        formatted_response = json_util.loads(tempjson)
        logger.info('response: {}'.format(formatted_response))
        return formatted_response

    def get_instance_by_id(self, id):
        response = requests.get(self.create_url(['http://127.0.0.1:5000/instance/', id])).json()
        logger.info(str(response))

    def get_profile_by_id(self, id):
        response = requests.get(self.create_url(['http://127.0.0.1:5000/profile/', id])).json()
        logger.info(str(response))

    def get_instances_id(self):
        response = requests.get('http://127.0.0.1:5000/instance/ids').json()
        logger.info(str(response))
        return response

    def get_update_from_instance_since_date(self, id):
        yesterday = (datetime.datetime.now() - datetime.timedelta(days=1)).isoformat()
        response = requests.get(self.create_url(['http://127.0.0.1:5000/instance/', id, '/', yesterday])).json()
        logger.info(str(response))

    def test_api(self):
        logger.info('execute test')
        logger.info('getting every instance...')
        self.get_every_instance()
        logger.info('posting instance...')
        response = self.post_instance()
        id = response["_id"]
        profile_id = response["profile_id"]
        url = self.create_url(['http://127.0.0.1:5000/instance/', id, '/updates'])
        self.post_update(url)
        logger.info('now getting this instance by id...')
        self.get_instance_by_id(id)
        logger.info('now getting profile by id...')
        self.get_profile_by_id(profile_id)
        logger.info('now getting id from every instance available...')
        id_list = self.get_instances_id()
        logger.info('now getting every update from random instance since yesterday')
        self.get_update_from_instance_since_date(random.choice(id_list))

    def create_url(self, stringlist):
        url = ''
        for string in stringlist:
            url += string

        return url


requester = Requester()
requester.test_api()
# requester.get_it()
