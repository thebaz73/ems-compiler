# EMS - Enterprise Monitoring System

## EMS Java MIB JavaBean Compiler

### Overview
A management information base (MIB) is a database used for managing the entities in a communications network. Most often associated with the Simple Network Management Protocol (SNMP), the term is also used more generically in contexts such as in OSI/ISO Network management model. While intended to refer to the complete collection of management information available on an entity, it is often used to refer to a particular subset, more correctly referred to as MIB-module.

### Project scope
Objects in the MIB are defined using a subset of Abstract Syntax Notation One (ASN.1) called "Structure of Management Information Version 2 (SMIv2)" [RFC 2578](http://tools.ietf.org/html/rfc2578). The software that performs the parsing is a MIB compiler. Current project scope is to create POJOs that, according to JavaBean specification, is capable to use the [EMS Java SNMP Toolkit for POJOs](https://github.com/thebaz73/ems-tookit) in order to allows SNMP communication. 

### Technologies
Current project uses following core technologies:
* Java
* Maven
* Template engine (Velocity)

### Usage
Clone project and compile.

```
usage: [mibc]
 -d,--dir <arg>       A pipe (|) separated list of MIB file's repositories
 -h,--help            Print help for this application
 -k,--keep            Keep sources
 -l,--lib <arg>       Library directory
 -m,--mib <arg>       The MIB file name
 -n,--name <arg>      The ManagedObject class name
 -o,--output <arg>    Output directory
 -p,--package <arg>   The ManagedObject package name
 -v,--version <arg>   The MIB file version [1|2]
 ```
 
 **NOTE: Tested with Java6**
