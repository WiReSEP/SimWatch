# -*- coding: utf-8 -*-
import logging, sys

logger = logging.getLogger('logger')
logger.setLevel(logging.DEBUG)
handler = logging.StreamHandler(stream=sys.stdout)
handler.setLevel(logging.DEBUG)
logger.addHandler(handler)
