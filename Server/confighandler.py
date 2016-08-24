# -*- coding: utf-8 -*-
import os
import yaml, log
from log import logger

config_keys = ['port', 'host', 'mongo_port', 'mongo_host', 'mongo_name']

class ApiConfig:
    def __init__(self):
        with open(os.path.join(os.path.dirname(__file__), "api_config"), 'r') as yaml_file:
            self._config = yaml.load(yaml_file)
        for key in config_keys:
          setattr(self, key, self._config.get(key))
          
        logger.info('API configuration read')
