# -*- coding: utf-8 -*-

import json, os, errno, fcntl, re
from log import logger

ATTACHMENT_DIRECTORY = './attachments'
INSTANCE_ID_FORMAT = r'^[0-9a-f]+$'
PROPERTY_NAME_FORMAT = r'^[\w_.]+$'
BUFFER_SIZE = 4096


class Attachment():
    def __init__(self, instance_id, property_name, request):
        self.instance_id = instance_id
        self.property_name = property_name
        self.request = request
        self.status = {'success': False, 'messages': []}

    def is_valid(self):
        """
        Check if the following criteria are met:
         - attachment directory exists
         - instance id format is valid
         - property name format is valid

        :return: True if all criteria are met, else False
        """
        valid = True
        if not os.path.isdir(ATTACHMENT_DIRECTORY):
            logger.info('Attachment directory does not exist')
            self.status['messages'].append('Attachment directory does not exist')
            valid = False
        if not re.match(INSTANCE_ID_FORMAT, self.instance_id):
            logger.info('Invalid instance id format')
            self.status['messages'].append('Invalid instance id format')
            valid = False
        if not re.match(PROPERTY_NAME_FORMAT, self.property_name):
            logger.info('Invalid poroperty name format')
            self.status['messages'].append('Invalid property name format')
            valid = False
        return valid

    def save(self):
        """
        Save the attachment into a file.

        Make sure the attachment is valid and the directory for the simulation exists.
        Open and exclusively lock the file.
        Write the request body to the file.
        Unlock the file.

        :return: True if successful, else False
        """
        if not self.is_valid():
            return False
        sim_dir = ATTACHMENT_DIRECTORY + '/' + self.instance_id
        try:
            os.makedirs(sim_dir)
        except OSError as ex:
            if ex.errno != errno.EEXIST:
                raise
        with open(sim_dir + '/' + self.property_name, 'bw') as file:
            fcntl.lockf(file, fcntl.LOCK_EX)
            while True:
                chunk = self.request.stream.read(BUFFER_SIZE)
                if not chunk:
                    break
                file.write(chunk)
            fcntl.lockf(file, fcntl.LOCK_UN)
        self.status['success'] = True
        return True

    def load(self):
        """
        Load the attachment.

        Make sure the request is valid and the file exists
        Open and lock the file.
        Let Flask serve the file in chunks.
        Unlock and close the file.

        :return: generator that yields the binary file if successful, else None
        """
        if not self.is_valid():
            return None
        file_path = ATTACHMENT_DIRECTORY + '/' + self.instance_id + '/' + self.property_name
        if not os.path.exists(file_path):
            logger.info('Attachment file does not exist')
            self.status['messages'].append('Attachment file does not exist')
            return None
        f =  open(file_path, 'br')
        fcntl.lockf(f, fcntl.LOCK_SH)
        def generate():
            while True:
                chunk = f.read(BUFFER_SIZE)
                if not chunk:
                    break
                yield chunk
            fcntl.lockf(f, fcntl.LOCK_UN)
            f.close()
        self.status['success'] = True
        return generate

    @property
    def json_status(self):
        return json.dumps(self.status)
