# -*- coding: utf-8 -*-
import yaml, log
from log import logger

PORT = 'port'
HOST = 'host'
MONGO_PORT = 'mongo_port'
MONGO_HOST = 'mongo_host'



class ApiConfig:
    host = None
    port = None

    def __init__(self):
        with open("api_config", 'r') as yaml_file:
            self._config = yaml.load(yaml_file)
        self.port = self._config[PORT]
        self.host = self._config[HOST]
        self.mongo_port = self._config[MONGO_PORT]
        self.mongo_host = self._config[MONGO_HOST]
        logger.info('Imported api_config - PORT: ' + str(self.port) + ' HOST: ' + str(self.host))
