__author__ = 'steffen'
from flask import Flask, request, Response
import database, instance
from log import logger
from bson import json_util
from confighandler import ApiConfig
from instance import Instance

_FLASK_NAME = 'flask_import_name'
_app = Flask(_FLASK_NAME)
NAME = 'name'
PROFILE = 'profile'
UPDATES = 'updates'
_db = database.DataBase()
_JSON_MIME = 'application/json'


@_app.route('/instances', methods=['POST'])
def post_instance():
    logger.debug('post_instance with following json: ' + request.get_json())
    instancee = Instance(jsons=request.get_json())
    _db.insert_instance(instancee)
    return Response(instancee.json_instance, mimetype=_JSON_MIME)


@_app.route('/instances/<instance_id>/updates', methods=['POST'])
def send_update(instance_id):
    logger.debug('posting update: ' + json_util.dumps(request.get_json()))
    instancee = Instance(id=instance_id)
    instancee.update(json_util.dumps(request.get_json()))
    _db.update_instance(instancee)
    return Response(instancee.json_instance, mimetype=_JSON_MIME)


@_app.route('/get_instances', methods=['GET'])
def get_instances():
    logger.info('getting all instances...')
    return Response(_db.get_all_instances()).data


config = ApiConfig()
_app.run(debug=True, host=config.host, port=config.port)
