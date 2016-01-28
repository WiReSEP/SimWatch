__author__ = 'steffen'

import json
from bson.objectid import ObjectId
from datetime import datetime


class PyMongoEncoder(json.JSONEncoder):
    def default(self, o):
        if (isinstance(o, ObjectId)):
            return str(o)
        if (isinstance(o, datetime)):
            return o.isoformat()

        super(PyMongoEncoder, self).default(o)
