# -*- coding: utf-8 -*-
import json, logging, datetime, database
from log import logger
from bson import json_util

PROFILE = 'profile'
NAME = 'name'
DATE = 'date'
UPDATES = 'updates'
PROFILE_ID = 'profile_id'


class Instance():
    def __init__(self, dict=None, id=None):
        self.db = database.DataBase()
        if dict is None:
            self.dict_instance = self.db.get_instance(id)

            self.id = id
            if self.dict_instance is None:
                logger.info('dict_instance is None!')
            else:
                logger.info('created instance from id! Name: ' + str(self.dict_instance[NAME]))
        else:
            self.dict_instance = dict
            logger.info('assessing profile id...')
            profile_dict = self.db.get_profile(profile=self.dict_instance[PROFILE])
            if profile_dict is None:
                profile_id = self.db.insert_profile(self.dict_instance[PROFILE])
                logger.info('profile didnt exist yet. Created new profile with id: ' + str(profile_id))
            else:
                profile_id = profile_dict[database.ID]
                logger.info('profile existed already. Id: ' + str(profile_id))

            self.dict_instance[DATE] = datetime.datetime.now()
            self.dict_instance[UPDATES] = []
            self.dict_instance[PROFILE_ID] = profile_id
            logger.info('created instance...')
            logger.info(NAME + ": " + self.dict_instance[NAME])
            logger.info(PROFILE + ": " + self.dict_instance[PROFILE])

    def update(self, update):
        dict_update = json.loads(update)
        dict_update[DATE] = datetime.datetime.now()
        if self.dict_instance is not None:
            logger.debug('Update to insert: ' + str(update))
            logger.debug('Updates before updating: ' + str(self.dict_instance[UPDATES]))
            current_updates = self.dict_instance[UPDATES]
            current_updates.append(dict_update)
            self.dict_instance[UPDATES] = current_updates
            logger.debug('Updates after updating: ' + str(self.dict_instance[UPDATES]))
            logger.debug('Update needs to be saved now!')
        else:
            logger.debug('dict_instance is None!')

    @property
    def json_instance(self):
        return json.dumps(self.dict_instance, default=json_util.default)
