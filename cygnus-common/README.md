#<a name="top"></a>cygnus-common
Content:

* [Welcome to cygnus-common](#section1)
* [Basic operation](#section2)
    * [Hardware requirements](#section2.1)
    * [Installation (CentOS/RedHat)](#section2.2)
    * [Configuration](#section2.3)
    * [Running](#section2.4)
    * [Unit testing](#section2.5)
    * [Management API overview](#section2.6)
* [Features summary](#section3)
* [Reporting issues and contact information](#section4)

#<a name="section1"></a>Welcome to cygnus-common
cygnus-common is the base for any Cygnus agent (e.g. cygnus-ngsi). Cygnus agents are based on [Apache Flume](http://flume.apache.org/) agents, which are basically composed of a source in charge of receiving the data, a channel where the source puts the data once it has been transformed into a Flume event, and a sink, which takes Flume events from the channel in order to persist the data within its body into a third-party storage.

cygnus-common provides a set of extensions for Apache Flume, for instance, defining how a Http source handler must look like or adding channels suitable for reading Cygnus-like counters. But not only Flume extensions, but interesting functionality for any agent in terms of a common Management Interface, common backend classes for HDFS, MySQL, MongoDB, PostgreSQL and many others, unified logging classes and error handling, etc.

[Top](#top)

##<a name="section2"></a>Basic operation
###<a name="section2.1"></a>Hardware requirements
* RAM: 1 GB, specially if abusing of the batching mechanism.
* HDD: A few GB may be enough unless the channel types are configured as `FileChannel` type.

[Top](#top)

###<a name="section2.2"></a>Installation (CentOS/RedHat)
Simply configure the FIWARE repository if not yet configured:

    $ cat > /etc/yum.repos.d/fiware.repo <<EOL
    [Fiware]
    name=FIWARE repository
    baseurl=http://repositories.testbed.fi-ware.eu/repo/rpm/x86_64/
    gpgcheck=0
    enabled=1
    EOL

And use your applications manager in order to install the latest version of cygnus-common:

    $ yum install cygnus-common

The above will install cygnus-common in `/usr/cygnus/`.

[Top](#top)

###<a name="section2.3"></a>Configuration
Configuring cygnus-common is just configuring Apache Flume since no agent-related functionality is added (that's something agents as cygnus-ngsi do). Please, check [this](https://flume.apache.org/FlumeUserGuide.html#setup) official guidelines.

[Top](#top)

###<a name="section2.4"></a>Running
cygnus-common can be run as a service by simply typing:

    $ service cygnus-common start

Logs are written in `/var/log/cygnus/cygnus.log`, and the PID of the process will be at `/var/run/cygnus/cygnus_1.pid`.

[Top](#top)

###<a name="section2.5"></a>Unit testing
Running the tests require [Apache Maven](https://maven.apache.org/) installed and cygnus-common sources downloaded.

    $ git clone https://github.com/telefonicaid/fiware-cygnus.git
    $ cd fiware-cygnus/cygnus-common
    $ mvn test

[Top](#top)

###<a name="section2.6"></a>Management API overview
Run the following `curl` in order to get the version (assuming cygnus-common runs on `localhost`):

```
$ curl -X GET "http://localhost:8081/v1/version"
{
    "success": "true",
    "version": "0.12.0_SNAPSHOT.52399574ea8503aa8038ad14850380d77529b550"
}
```

Many other operations, like getting/putting/updating/deleting the grouping rules can be found in Management Interface [documentation](./doc/installation_and_administration_guide/management_interface.md).

[Top](#top)

##<a name="section3"></a>Features summary
<table>
  <tr><td rowspan="6">Management Interface</td><td>GET /version</td><td>0.5.0</td></tr>
  <tr><td>GET /stats</td><td>0.13.0</td></tr>
  <tr><td>GET /groupingrules</td><td>0.13.0</td></tr>
  <tr><td>POST /groupingrules</td><td>0.13.0</td></tr>
  <tr><td>PUT /groupingrules</td><td>0.13.0</td></tr>
  <tr><td>DELETE /groupingrules</td><td>0.13.0</td></tr>
  <tr><td rowspan="4">Flume extensions</td><td>RoundRobinChannelSelector</td><td>0.6.0</td></tr>
  <tr><td>CygnusMemoryChannel</td><td>0.13.0</td></tr>
  <tr><td>CygnusHandler</td><td>0.14.0</td></tr>
  <tr><td>CygnusSink</td><td>0.14.0</td></tr>
  <tr><td rowspan="6">Operation</td><td>RPM building framework</td><td>0.3.0</td></tr>
  <tr><td>TDAF-like logs</td><td>0.4.0</td></tr>
  <tr><td>Multi-instances</td><td>0.7.0</td></tr>
  <tr><td>start/stop/status per instance</td><td>0.7.1</td></tr>
  <tr><td>Ordered death of Cygnus</td><td>0.8.0</td></tr>
  <tr><td>Polling interval parameter</td><td>0.8.0</td></tr>
</table>

[Top](#top)

##<a name="section4"></a>Reporting issues and contact information
Any doubt you may have, please refer to the [Cygnus Core Team](./reporting_issues_and_contact.md).

[Top](#top)
