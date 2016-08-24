# SimWatch
_SimWatch_ is a tool to watch and monitor the progress of simulations via smartphones. 

# Installation
This guide describes the required steps to install and run the SimWatch API server. The server is written in Python and can be served with any webserver that supports [FastCGI](https://en.wikipedia.org/wiki/FastCGI).

## MongoDB
To store the state of all monitored simulations, SimWatch uses _MongoDB_. Refer to the [official MongoDB installation guide](https://docs.mongodb.com/manual/administration/install-on-linux/) to set it up.

## Installing the API Server
The following pakages are needed to install and run the API:
 * _virtualenv_
 * _python3_
 * _mod_fastcgi_ (when using Apache)

**Note:** *The commands in this guide should be executed as the user that will be used to run the simwatch FastCGI process to make sure all created files have sufficent access permissions*

**Download the SimWatch API Server**

Download the `Server` folder from this repository (or clone it) somewhere on the filesystem. The user which runs the server process (Apache) needs write permissions for this directory. All binary data (like images) the simulations send to the server are stored here, make sure there is enough storage space available (100 MB are propably sufficient).

**Create a virtual environment**

To avoid having to install all SimWatch python dependencies globally on the system, a _virtual python environment_ will be used instead. Create the virtual environment in any directory (however not within the downloaded source directory) like this:

```shell
virtualenv -p python3 /some/path/simwatch_venv
```

**Install SimWatch python dependencies**

```shell
cd /some/path/simwatch_venv
. bin/activate
# the shell prompt changes, indicating the virtual enviroment is now active
bin/pip install -r /path/to/SimWatch/Server/requirements.txt
```

**Configure the API Server**

```shell
cd /path/to/SimWatch/Server
# copy example config to use it as template
cp api_config_example api_config
```
Open the file `api_config` in a text editor and verify that all options are set correctly.


**Configuring the webserver**

The first step is to generate a FastCGI script that is used by the webserver to launch the SimWatch server. The launch script loads the virtual python environment and therefor needs to be re-written whenever the virtual environment is moved to another directory.
```shell
/path/to/SimWatch/Server/create_fastcgi_script.sh /some/path/simwatch_venv
```

The next step is to configure the webserver to use the generated FastCGI script to serve HTTP requests. The following snipped shows how to set up a [virtual host on Apache](https://httpd.apache.org/docs/2.2/en/vhosts/), for other webservers refer to their FastCGI documentation.
```
LoadModule fastcgi_module modules/mod_fastcgi.so
FastCgiServer /path/to/SimWatch/Server/simwatch.fcgi -idle-timeout 300 -processes 1

<VirtualHost *:80>
    ServerName simwatch.example.com
    DocumentRoot /path/to/SimWatch/Server
    
    AddHandler fastcgi-script fcgi
    ScriptAlias / /path/to/SimWatch/Server/simwatch.fcgi/
    <Location />
        SetHandler fastcgi-script
        Require all granted
    </Location>
</VirtualHost>
```

