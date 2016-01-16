__author__ = 'steffen'

from pymongo import MongoClient
import datetime, instance
from log import logger
from bson.objectid import ObjectId
import confighandler

NAME = 'name'
PROFILE = 'profile'
DATE = 'date'
UPDATES = 'updates'
ID = '_id'


class DataBase:
    confighandler = confighandler.ApiConfig()
    client = MongoClient(host=confighandler.mongo_host, port=confighandler.port)
    db = client['test_database']
    instances = db['instances']
    profiles = db['profiles']
    logger.info('initiated database')
    # instances.delete_many({})
    # profiles.delete_many({})

    def insert_instance(self, instancee):
        self.instances.insert_one(instancee.dict_instance)
        logger.info(ID + ": " + str(instancee.dict_instance[ID]))
        return instancee

    def update_instance(self, instancee):
        logger.info('Storing update in database now...')
        self.instances.update_one({ID: ObjectId(instancee.id)}, {'$set': {UPDATES: instancee.dict_instance[UPDATES]}},
                                  upsert=False)
        dict_instance = self.get_instance(instancee.id)
        logger.info('updates after saving: ' + str(dict_instance[UPDATES]))
        logger.info('instance updated: ' + instancee.id)
        return dict_instance

    def get_all_instances(self):
        logger.info('getting all instances...')
        return self.instances.find({})

    def get_instance(self, id):
        logger.info('getting instance: ' + id)
        return self.instances.find_one({ID: ObjectId(id)})

    def insert_profile(self, profile):
        logger.info('inserting profile: ' + profile)
        profilee = {PROFILE: profile}
        return self.profiles.insert_one(profilee).inserted_id

    def get_all_profiles(self):
        logger.info('getting all profiles...')
        return self.instances.find({})

    def get_profile(self, id=None, profile=None):
        if id is not None:
            logger.info('getting profile: ' + str(id))
            return self.profiles.find_one({ID: ObjectId(id)})
        else:
            logger.info('getting profle: ' + profile)
            return self.profiles.find_one({PROFILE: profile})
            # todo für markus: instance by id, von einer instanz alle updates ab datum, profil by id, alle instanzen id ausgabe


"""b = DataBase()
cursor = db.get_all_instances()
for object in cursor:
        print(str(object))
        print(str(object[ID]))"""
