dd-verify-migration
=========================

SYNOPSIS
--------

    dd-verify-migration { server | check  | load-from-fedora | load-from-vault | load-from-dataverse } ...

DESCRIPTION
-----------

Database for comparing expected and actual files and datasets after migration to data station


ARGUMENTS
---------

      positional arguments:
         {server,check,load-from-fedora,load-from-dataverse,load-from-vault} ...
                                     available commands

      named arguments:      
        -h, --help                   show this help message and exit
        -v, --version                show the application version and exit

    load-from-dataverse [-h] [-d DOI | --csv CSV] [file]
      
      Load actual tables with file info from dataverse
      
      positional arguments:
        file                         application configuration file (default: etc/config.yml)

      named arguments:      
        -d DOI, --doi DOI            The DOI for which to load the files,
                                     for example: 'doi:10.17026/dans-xtz-qa6j'
        --csv CSV                    CSV file produced by easy-fedora-to-bag
        -h, --help                   show this help message and exit

    load-from-vault [-h] (-u UUIDS | -U UUID | -s STORE) [file]
         
      Load expected table with info from manifest-sha1.txt of bags in the vault

      positional arguments:
        file                         application configuration file (default: etc/config.yml)

      named arguments:     
        -u UUIDS, --uuids UUIDS      file with UUIDs of a bag in the vault
        -U UUID, --UUID UUID         UUID of a bag in the vault
        -s STORE, --store STORE      name of a bag store in the vault
        -h, --help                   show this help message and exit

    load-from-fedora [-c [FILE]] [-h] csv [csv ...]

      Load expected tables with info from easy_files in fs-rdb and transformation rules

      positional arguments:
        csv                          CSV file produced by easy-fedora-to-bag

      named arguments:
        -c [FILE], --config [FILE]   application configuration file (default: etc/config.yml)
        -h, --help                   show this help message and exit

EXAMPLES
--------

         dd-verify-migration load-from-dataverse
         dd-verify-migration load-from-vault -u uuids.txt
         dd-verify-migration load-from-fedora easy-fedora-to-bag-log.csv

INSTALLATION AND CONFIGURATION
------------------------------
Currently, this project is built as an RPM package for RHEL7/CentOS7 and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/dd-verify-migration` and the configuration files to `/etc/opt/dans.knaw.nl/dd-verify-migration`. 

For installation on systems that do no support RPM and/or systemd:

1. Build the tarball (see next section).
2. Extract it to some location on your system, for example `/opt/dans.knaw.nl/dd-verify-migration`.
3. Start the service with the following command
   ```
   /opt/dans.knaw.nl/dd-verify-migration/bin/dd-verify-migration server /opt/dans.knaw.nl/dd-verify-migration/cfg/config.yml 
   ```

The file `/opt/dans.knaw.nl/dd-verify-migration/cfg/account-substitues.csv` will be delivered with just a header line.
The content of the file should equal the substitution file configured for `easy-convert-bag-to-deposit`.
To ignore substitution for testing purposes: add just one line with identical values in both columns.

BUILDING FROM SOURCE
--------------------
Prerequisites:

* Java 8 or higher
* Maven 3.3.3 or higher
* RPM

Steps:
    
    git clone https://github.com/DANS-KNAW/dd-verify-migration.git
    cd dd-verify-migration 
    mvn clean install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM 
packaging will be activated. If `rpm` is available, but at a different path, then activate it by using
Maven's `-P` switch: `mvn -Pprm install`.

Alternatively, to build the tarball execute:

    mvn clean install assembly:single
