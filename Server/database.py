# -*- coding: utf-8 -*-
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
STATUS = 'status'
ERROR = 'error'


class DataBase:
    confighandler = confighandler.ApiConfig()
    client = MongoClient(host=confighandler.mongo_host, port=confighandler.mongo_port)
    db = client[confighandler.mongo_name]
    instances = db['instances']
    profiles = db['profiles']
    logger.info('initiated database')
    instances.delete_many({})
    #profiles.delete_many({})

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

    def get_all_instance_ids(self):
        logger.info('getting all instance ids...')
        return self.instances.find({}, {ID: 1})

    def get_all_instance_status(self):
        logger.info('getting all instance status...')
        return self.instances.find({}, {ID: 1, STATUS: 1, ERROR:1})

    def insert_profile(self, profile):
        logger.info('inserting profile: ' + str(profile))

        return self.profiles.insert_one(profile).inserted_id

    def get_all_profiles(self):
        logger.info('getting all profiles...')
        return self.instances.find({})

    def delete_instance(self, id):

        logger.info('deleting instance: '+ id)
        obj_id = ObjectId(id)
        dict_id = {"_id" : obj_id}
        response = self.instances.delete_one(dict_id)
        logger.info('response: ' + str(response))
        return 'instance {} deleted'.format(id)

    def get_profile(self, id=None, profile=None):
        if id is not None:
            logger.info('getting profile: ' + str(id))
            return self.profiles.find_one({ID: ObjectId(id)})
        else:
            logger.info('getting profle: ' + str(profile))
            return self.profiles.find_one({PROFILE: profile})
            # todo für markus: von einer instanz alle updates ab datum
