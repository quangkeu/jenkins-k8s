# SSDC-CORE

This project contains core classes for SSDC Java-based projects. It includes:

* Generic data access layer for SQL-based databases (_SsdcDao_)
* Cache support using Redis (see _SsdcRepository_)
* Generic Rest API endpoint for CRUD 
* Other common dependencies


* Clone and build at local to use: (//TODO setup local _Nexus_ repository for SSDC)
    ```
    git clone http://git.vnpt-technology.vn/scm/ssdc_ump/ssdc-core.git
    mvn clean install
    ```
* Add dependency to project's pom.xml
    ```
    <dependency>
      <groupId>vn.vnpt.ssdc</groupId>
      <artifactId>ssdc-core</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
    ```

For more information, please check repo __ump-backend__ to see how to use this library.

