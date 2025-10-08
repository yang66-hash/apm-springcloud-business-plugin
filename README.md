# apm-springcloud-business-plugins
Example of instrumentation which plugs in to the Elastic Java Agent and instruments Spring Cloud microservices.

## Overview

The [Elastic APM Java Agent](https://github.com/elastic/apm-agent-java/) is a Java agent that automatically measures the performance of your application and tracks errors. [Full documentation is available](https://www.elastic.co/guide/en/apm/agent/java/current/intro.html).

## projects-introduction

This project is a plugin designed to detect issues within microservices built on Spring Cloud. While it may not directly perform detection, it gathers metrics from the business layer of Spring Cloud microservices and utilizes these metrics to formulate detection rules.

## Building

The full project can be built by cloning to your local system, changing to the root directory, and running `mvn clean package`.

Prerequisites: git, maven and JDK 11+ installed

```aidl
git clone https://github.com/yang66-hash/apm-springcloud-business-plugin.git
cd apm-springcloud-business-plugin
mvn clean package
```

## Running

You need an Elastic APM Java Agent jar (the latest version is recommended, but at least version 1.31.0 to instrument traces and at least 1.39.0 to instrument metrics). Additionally, an Elastic APM server is recommended, the collected metrics will be sent to the APM Server, stored in Elasticsearch and visualize on Kibana.

The latest agent version can be found in [GitHub](https://github.com/elastic/apm-agent-java). You can download using any of the standard download mechanisms, eg

```aidl
wget https://github.com/elastic/apm-agent-java?tab=readme-ov-file
```
Setting following environment variables(or you can set System properties) when running microservice modules or jars.
Put this plugin jar to the 'ELASTIC_APM_PLUGINS_DIR' directory.

```aidl
ELASTIC_APM_APPLICATION_PACKAGES=com.yang.xingdiancloud; //directories set want to monitoring, splitting in ',' 
ELASTIC_APM_ENABLE_EXPERIMENTAL_INSTRUMENTATIONS=true; //open plugins function
ELASTIC_APM_PLUGINS_DIR=D://apm/plugins; //set the path where you put the plugins
ELASTIC_APM_SERVER_URL=http://118.25.105.124:8200; //set the APM Server 
ELASTIC_APM_SERVICE_NAME=cloud-user-service //set the service name of this application
```
Then you can use this plugin when you use apm java agent as normal, the metrics will be sent with the metrics and traces agent monitoring to APM Server together per 30 seconds.
