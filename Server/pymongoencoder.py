__author__ = 'steffen'

import json
from bson.objectid import ObjectId
import datetime


class PyMongoEncoder(json.JSONEncoder):
    def default(self, o):
        if (isinstance(o, ObjectId)):
            return str(o)
        if (isinstance(o, datetime.datetime)):
            return o.replace(tzinfo=datetime.timezone.utc).isoformat()

        super(PyMongoEncoder, self).default(o)
