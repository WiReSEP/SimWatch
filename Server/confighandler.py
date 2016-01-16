__author__ = 'steffen'

import yaml, log
from log import logger

PORT = 'port'
HOST = 'host'


class ApiConfig:
    host = None
    port = None

    def __init__(self):
        with open("api_config", 'r') as yaml_file:
            self._config = yaml.load(yaml_file)
        self.port = self._config[PORT]
        self.host = self._config[HOST]
        logger.info('Imported api_config - PORT: ' + str(self.port) + ' HOST: ' + str(self.host))
