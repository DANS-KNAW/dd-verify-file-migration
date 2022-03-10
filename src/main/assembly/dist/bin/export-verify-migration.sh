#!/bin/sh

TEMPOUT=file_migration.sql
OUTFILE=${1:-"file_migration.zip"}

pg_dump --data -U dd_verify_migration dd_verify_migration > $TEMPOUT
zip $OUTFILE $TEMPOUT
rm $TEMPOUT
