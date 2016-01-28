# -*- coding: utf-8 -*-
from flask import Flask, request, Response
import database, instance
from log import logger
from bson import json_util
import json
from confighandler import ApiConfig
from instance import Instance
from pymongoencoder import PyMongoEncoder

_FLASK_NAME = 'flask_import_name'
_app = Flask(_FLASK_NAME)
NAME = 'name'
PROFILE = 'profile'
UPDATES = 'updates'
_db = database.DataBase()
_JSON_MIME = 'application/json'


@_app.route('/instances', methods=['POST'])
def post_instance():
    logger.debug('post_instance with following json: ' + str(request.get_json()))
    instancee = Instance(dict=request.get_json())
    _db.insert_instance(instancee)
    return Response(instancee.json_instance, mimetype=_JSON_MIME)


@_app.route('/instances/<instance_id>/updates', methods=['POST'])
def send_update(instance_id):
    logger.debug('posting update: ' + json_util.dumps(request.get_json()))
    instancee = Instance(id=instance_id)
    instancee.update(json.dumps(request.get_json()))
    _db.update_instance(instancee)
    return Response(instancee.json_instance, mimetype=_JSON_MIME)


@_app.route('/get_instances', methods=['GET'])
def get_instances():
    logger.info('getting all instances...')
    cursor = _db.get_all_instances()
    json = cursor_to_json(cursor)
    return Response(json, mimetype=_JSON_MIME)


def cursor_to_json(cursor):
    decoder = PyMongoEncoder()

    return json.dumps(cursor, cls=decoder)



config = ApiConfig()
_app.run(debug=True, host=config.host, port=config.port)
