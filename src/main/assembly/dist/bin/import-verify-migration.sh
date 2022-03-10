#!/bin/sh

TEMPIN=file_migration.sql
INFILE=${1:-"file_migration.zip"}

unzip $INFILE

psql -U dd_manage_prestaging dd_verify_migration < $TEMPIN
rm $TEMPIN
