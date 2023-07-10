#!/bin/bash

LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8 MAVEN_OPTS="--enable-preview" mvn compile exec:java
