# -*- coding: utf-8 -*-
from flask import Flask, request, Response
import database, instance
from log import logger
from bson import json_util
import json
from confighandler import ApiConfig
from instance import Instance
from attachment import Attachment
from pymongoencoder import PyMongoEncoder
from dateutil import parser

_FLASK_NAME = 'flask_import_name'
_app = Flask(_FLASK_NAME)
NAME = 'name'
PROFILE = 'profile'
UPDATES = 'updates'
_db = database.DataBase()
_JSON_MIME = 'application/json'
_BINARY_MIME = 'application/octet-stream'


@_app.route('/instance', methods=['POST'])
def post_instance():
    logger.debug('post_instance with following json: ' + str(request.get_json()))
    instancee = Instance(dict=request.get_json())
    _db.insert_instance(instancee)
    return Response(instancee.json_instance, mimetype=_JSON_MIME)


@_app.route('/instance/<instance_id>/updates', methods=['POST'])
def send_update(instance_id):
    logger.debug('posting update: ' + json_util.dumps(request.get_json()))
    instancee = Instance(id=instance_id)
    instancee.update(json.dumps(request.get_json()))
    _db.update_instance(instancee)
    return Response(instancee.json_instance, mimetype=_JSON_MIME)


@_app.route('/instance/<instance_id>/attachment/<property_name>', methods=['POST'])
def upload_attachment(instance_id, property_name):
    logger.debug('receiving attachement \'' + property_name + '\' for instance ' + instance_id)
    attachment = Attachment(instance_id, property_name, request)
    if attachment.save():
        status = 200
    else:
        status = 400
    return Response(attachment.json_status, mimetype=_JSON_MIME, status=status)


@_app.route('/instance/<instance_id>/attachment/<property_name>', methods=['GET'])
def load_attachment(instance_id, property_name):
    logger.debug('serving attachement \'' + property_name + '\' for instance ' + instance_id)
    attachment = Attachment(instance_id, property_name, request)
    generator = attachment.load()
    if generator is not None:
        return Response(generator(), mimetype=_BINARY_MIME, status=200)
    else:
        return Response(attachment.json_status, mimetype=_JSON_MIME, status=404)


@_app.route('/instance', methods=['GET'])
def get_instances():
    logger.info('getting all instances...')
    cursor = _db.get_all_instances()
    list = cursor_to_list(cursor)
    return Response(to_json(list), mimetype=_JSON_MIME)


@_app.route('/instance/ids', methods=['GET'])
def get_instances_id():
    logger.info('gettting id from every instance...')
    cursor = _db.get_all_instance_ids()
    id_list = []
    for doc in cursor:
        id_list.append(doc['_id'])
    return Response(to_json(id_list), mimetype=_JSON_MIME)


@_app.route('/instance/<id>/<date>', methods=['GET'])
def get_instance_since(id, date):
    logger.info('getting every update from %s since %s', id, date)
    cursor = _db.get_instance(id)
    update_list = []
    updates = cursor[UPDATES]
    for update in updates:
        if update['date'] >= parser.parse(date):
            update_list.append(update)
    return Response(to_json(update_list), mimetype=_JSON_MIME)


@_app.route('/instance/<id>', methods=['GET'])
def get_instance(id):
    logger.info('getting instance: ' + id)
    cursor = _db.get_instance(id)
    return Response(to_json(cursor), mimetype=_JSON_MIME)


@_app.route('/profile/<id>', methods=['GET'])
def get_profile(id):
    logger.info('getting profile: ' + id)
    cursor = _db.get_profile(id)
    profile = cursor['profile']
    return Response(to_json(profile), mimetype=_JSON_MIME)


def to_json(obj):
    return json.dumps(obj, cls=PyMongoEncoder)


def cursor_to_list(cursor):
    list = []
    for doc in cursor:
        list.append(doc)
    return list


config = ApiConfig()
_app.run(debug=True, host=config.host, port=config.port)
