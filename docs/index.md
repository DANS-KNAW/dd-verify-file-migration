dd-verify-migration
===================

SYNOPSIS
--------

```text   
dd-verify-migration { server | check  | load-from-dataverse } ...
```

DESCRIPTION
-----------

Database for comparing expected and actual files and datasets after migration to data station.


ARGUMENTS
---------
    
```text
positional arguments:
{server,check,load-from-dataverse} ...

positional arguments:
  {server,check,load-from-dataverse}
                         available commands

named arguments:
  -h, --help             show this help message and exit
  -v, --version          show the application version and exit
```


EXAMPLES
--------

```text
dd-verify-migration load-from-dataverse
```

INSTALLATION AND CONFIGURATION
------------------------------
Currently, this project is built as an RPM package for RHEL7/CentOS7 and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/dd-verify-migration` and the configuration files to `/etc/opt/dans.knaw.nl/dd-verify-migration`. 

For installation on systems that do no support RPM and/or systemd:

1. Build the tarball (see next section).
2. Extract it to some location on your system, for example `/opt/dans.knaw.nl/dd-verify-migration`.
3. Start the service with the following command
        
        opt/dans.knaw.nl/dd-verify-migration/bin/dd-verify-migration server \
         /opt/dans.knaw.nl/dd-verify-migration/cfg/config.yml 
        

The file `/opt/dans.knaw.nl/dd-verify-migration/cfg/account-substitutes.csv` will be delivered with just a header line.
The content of the file should equal the substitution file configured for `easy-convert-bag-to-deposit`.
To ignore substitution for testing purposes: add just one line with identical values in both columns.

BUILDING FROM SOURCE
--------------------
Prerequisites:

* Java 11 or higher
* Maven 3.3.3 or higher
* RPM

Steps:
    
```text
git clone https://github.com/DANS-KNAW/dd-verify-migration.git
cd dd-verify-migration 
mvn clean install
```    

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM 
packaging will be activated. If `rpm` is available, but at a different path, then activate it by using
Maven's `-P` switch: `mvn -Pprm install`.

Alternatively, to build the tarball execute:

```text
mvn clean install assembly:single
```
