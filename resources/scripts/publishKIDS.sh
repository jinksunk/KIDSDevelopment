#!/bin/bash
#
# Put the KIDS file in place on solomon, overwriting the existing one if present.
# Must be run from a system with an SSH-key for solomon.
#

FILELOC="../ontologies/KIDS/kids.owl"
DSTLOC="/srv/www/solomon.cs.iastate.edu/ontologies/KIDS.owl"
PUBHOST="solomon.cs.iastate.edu"

/usr/bin/scp $FILELOC $PUBHOST:$DSTLOC
