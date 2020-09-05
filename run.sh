
source env.rc

JAR=jars

# ============================
log() {
  [ -n "$LOGILE" ] && echo $1 >>$LOGFILE
  echo "$1"
}

logfail() {
  log "$1"
  log "Exit immediately"
  exit 1
}

# ===========================

required_var() {
  local -r VARIABLE=$1
  [ -z "${!VARIABLE}" ] && logfail "Need to set environment variable $VARIABLE"
}

required_listofvars() {
  local -r listv=$1
  for value in $listv; do required_var $value; done
}

# =============================

verify() {
  LOGDIR=${LOGDIR:-/tmp/export}
  mkdir -p $LOGDIR
  LOGFILE=$LOGDIR/exportdb.log
  required_listofvars PROP
}

javacall() {
  local CLASSPATH=$1
  while read JAR
  do
     CLASSPATH=$JAR:$CLASSPATH;
  done <<!
     $( ls ./jdbc/*.jar)
!
  echo "-cp $CLASSPATH"
}

setjdbc() {
   local -r CLASS=$1
   JAVACMD="java $JVMPARS `javacall target/ExportDB-1.0-SNAPSHOT-jar-with-dependencies.jar` $CLASS -p $PROP"
}

# ============================
# tasks
# ============================

# extract list of schema
# $1 - output file
extractschemas() {
  setjdbc com.export.db2.main.ExportSchemas
  echo $JAVACMD
  ! $JAVACMD -o $1  && logfail "Failed"
}

# extract list of tables in the schema
# $1 : output file
# $2 : schemaname
exportschema() {
  setjdbc com.export.db2.main.ExportSchema
  ! $JAVACMD -o $1 -s $2  && logfail "Failed"
}

# export table
# $1: table name
# $2: directory for export
exporttable() {
  mkdir -p $2
  setjdbc com.export.db2.main.ExportMain
  ! $JAVACMD -t $1 -d $2  && logfail "Failed"
}

# export list
# $1: list of table
# $2: directory for export

exportlist() {
  local -r LIST=$1
  local -r DIR=$2
  while read line; do
    if [ -n "$line" ]; then
      exporttable $line $DIR
    fi
  done < $LIST
}

# =============================
# generate db2 load commands
# =============================

# makes DB2 load statement
# args:
#  $1: load/import
#  $2: table name
#  $3: file name with exported rows
#  $4: directory for exported blob data, if exists add blob statement
#  $5: basefilename for creating msg and dump file
#  $6: output SQL file
#  $7: dumpdir
#  $8: msgdir

makeload() {
  local -r LOAD=$1
  local -r tablename=$2
  local -r filename=$3
  local -r blobdir=$4
  local -r basefilename=$5
  local -r LOADSQL=$6
  local -r DUMPDIR=$7
  local -r MSGDIR=$8

  if [ $LOAD == "load" ]; then
    log "Create LOAD for $tablename"
    echo "LOAD $CLIENT FROM $filename" >>$LOADSQL
  else
    log "Create IMPORT for $tablename"
    echo "IMPORT FROM $filename" >>$LOADSQL
  fi
  echo "OF DEL" >>$LOADSQL

  if [ -d $blobdir ]; then
    log "Blob dir exists: add LOBS FROM $blobdir"
    echo "LOBS FROM $blobdir" >>$LOADSQL
  fi
  echo 'MODIFIED BY LOBSINFILE  CODEPAGE=1208  COLDEL~ USEDEFAULTS CHARDEL"" DELPRIORITYCHAR' >>$LOADSQL
  [ -z "$CLIENT" ] && echo "DUMPFILE=$DUMPDIR/$basefilename" >>$LOADSQL
  echo "MESSAGES $MSGDIR/$basefilename" >>$LOADSQL
  echo "REPLACE INTO $tablename" >>$LOADSQL
  echo ";" >>$LOADSQL
  echo >>$LOADSQL

#  echo "SET INTEGRITY FOR $tablename   IMMEDIATE CHECKED;" >>$LOADSQL
#  echo >>$LOADSQL
}

changetocan() {
  local tablename=$1
  local nametolow=${tablename,,}
  local namecan=${nametolow/./_}
  echo $namecan
}

# makes DB2 load statement, prepares data for makeload
# args:
#  $1: load/import
#  $2: tablename
#  $3: directory with exported data
#  $4: output SQL LOAD file
#  $5: dumpdir
#  $6: msgdir
addloadstatement() {
  local -r LOAD=$1
  local -r tablename=$2
  local -r EXPORT_DIR=$3
  local -r LOADSQL=$4
  local -r namecan=`changetocan $tablename`
  local -r filename=$EXPORT_DIR/$namecan.txt
  local -r blobdir=$EXPORT_DIR/$namecan

  # DB2 load dump directory for rejected rows
  local -r DUMPDIR=$5
  local -r MSGDIR=$6

  # prepare load
  makeload $LOAD $tablename $filename $blobdir "$namecan.txt" $LOADSQL $DUMPDIR $MSGDIR
}

# create load statements for list of tables
# $1: load/import
# $2: list of tables
# $3: directory for exported data
# $4: file for SQL LOAD command

createloadstatements() {
  local -r LOAD=$1
  local -r LIST=$2
  local -r DIR=$3
  local -r SQLOUT=$4

  rm -f $SQLOUT

  local -r MSGDIR=$DIR/msg
  # DB2 load dump directory for rejected rows
  local -r DUMPDIR=$DIR/dump

  rm -rf $MSGDIR $DUMPDIR
  mkdir -p $MSGDIR $DUMPDIR

  while read line; do
    if [ -n "$line" ]; then
      addloadstatement $LOAD $line $DIR $SQLOUT $DUMPDIR $MSGDIR
    fi
  done < $LIST
}


# ============================

printhelp() {
  echo "run.sh action /parameters/"
  echo ""
  echo "action: extractschemas /output_file_name/"
  echo " Extract schema names from database to the file"
  echo " Example : run.sh extractschemas /tmp/schemas"
  echo ""
  echo "action: extracttables /output_file_name/ /schema/"
  echo " Extract list of tables in the schema"
  echo " Example : run.sh extracttables /tmp/schemas DB2INST1"
  echo ""
  echo "action: exporttable /table_name/ /directory/"
  echo " Export single table"
  echo " Example : run.sh exporttable DB2INST1.CUSTOMER /tmp/export"
  echo ""
  echo "action: exportlist /file_of_tables/ /directory/"
  echo " Export list of tables reaf from table"
  echo " Example : run.sh exportlist /tmp/listoftable /tmp/export"
  echo ""
  echo "action: createload /file_of_tables/ /directory/ /output_sql/"
  echo " Create SQL LOAD command file"
  echo " Example : run.sh createload /tmp/listoftable /tmp/export /tmp/load.db2"
  echo ""
  echo "action: createimport /file_of_tables/ /directory/ /output_sql/"
  echo " Create SQL IMPORT command file"
  echo " Example : run.sh createimport /tmp/listoftable /tmp/export /tmp/load.db2"
  echo ""

}

main() {
  local -r action=$1
  shift

  case $action in
      -h|--help|-?) printhelp; exit 10;;
      extractschemas) extractschemas $@;;
      extracttables) exportschema $@;;
      exporttable) exporttable $@;;
      exportlist) exportlist $@;;
      createload) createloadstatements load $@;;
      createimport) createloadstatements import $@;;
      *) printhelp; exit 10;;
  esac

  exit 0

}

main $@