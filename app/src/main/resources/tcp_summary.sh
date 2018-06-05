#! /system/bin/sh

EXEC_FLAG=$1
EXEC_FLAG2=$2

SUMMARY_PATH=/sdcard/rvm/tcp_summary
SUMMARY_FILE_OLDVERSION=${SUMMARY_PATH}/tcp_summary
SUMMARY_FILE_PREFIX=${SUMMARY_PATH}/tcp_summary
SUMMARY_FILE_OLD=${SUMMARY_FILE_PREFIX}.old
SUMMARY_FILE_NEW=${SUMMARY_FILE_PREFIX}.new
SUMMARY_FILE_197001=${SUMMARY_FILE_PREFIX}.197001
TCP_FLOW_SIZE=
SUMMARY_TIMES=0

record_tcp_flux() {
	SUMMARY_MONTH=`date '+%Y%m'`
	SUMMARY_FILE=${SUMMARY_FILE_PREFIX}.${SUMMARY_MONTH}
	
	if [ "$SUMMARY_MONTH" == "197001" ];then
		TCP_SUMMARY_MONTH_LIST=`busybox ls -ltr ${SUMMARY_FILE_PREFIX}.?????? 2>/dev/null | busybox awk -F' ' '{print $9}'`
		for dd in $TCP_SUMMARY_MONTH_LIST
		do
			SUMMARY_FILE=$dd
		done
	else
		if [ ! -f ${SUMMARY_FILE} ];then
			if [ -f ${SUMMARY_FILE_197001} ];then
				busybox mv ${SUMMARY_FILE_197001} ${SUMMARY_FILE}
			fi
		fi
	fi

	TCP_SUMMARY_MONTH_LIST=`busybox ls -tl ${SUMMARY_FILE_PREFIX}.?????? 2>/dev/null | busybox awk -F' ' '{print $9}'`
	for dd in $TCP_SUMMARY_MONTH_LIST
	do
		if [ "$dd" != "$SUMMARY_FILE" ];then
			busybox rm -rf $dd
		fi
	done
	if [ -f $SUMMARY_FILE ];then
		if [ -z "${TCP_FLOW_SIZE}" ];then
			TCP_FLOW_SIZE=`busybox grep '^TCP_FLOW_SIZE:' ${SUMMARY_FILE} 2>/dev/null | busybox awk -F':' '{print $2}'`
		fi
		if [ -z "${TCP_FLOW_SIZE}" ];then
			TCP_FLOW_SIZE=0
		fi
	else
		TCP_FLOW_SIZE=0
	fi
	
	cat /proc/net/dev | busybox grep -v '|' | busybox awk -F' ' '{print $1""$2":"$10}' | busybox grep -v '^lo:' | busybox grep -v ':0:0$' > ${SUMMARY_FILE_NEW}
	
	VALID_DEV_LIST=`netcfg | busybox grep -v '127.0.0..' | busybox grep -v '0.0.0.0' | busybox awk -F' ' '{print $1}'`

	DELTA_TCP_DATA=0
	NEW_TCP_DATA_LIST=`cat $SUMMARY_FILE_NEW`
	if [ -n "$NEW_TCP_DATA_LIST" ];then
		for ii in $NEW_TCP_DATA_LIST
		do
			TCP_ID=`echo "$ii" | busybox awk -F':' '{print $1}'`
			NEW_TCP_VALUE_RX=`echo "$ii" | busybox awk -F':' '{print $2}'`
			NEW_TCP_VALUE_TX=`echo "$ii" | busybox awk -F':' '{print $3}'`
			IS_VALID_DEV=FALSE
			for iii in $VALID_DEV_LIST
			do
				if [ "$iii" == "$TCP_ID" ];then
					IS_VALID_DEV=TRUE
					break
				fi
			done
			if [ "$IS_VALID_DEV" == "FALSE" ];then
				continue
			fi
			OLD_TCP_VALUE_RX=0
			OLD_TCP_VALUE_TX=0
			if [ -f $SUMMARY_FILE_OLD ];then
				TMP_TCP_VALUE_RX=`busybox grep "^${TCP_ID}:" ${SUMMARY_FILE_OLD} | busybox awk -F':' '{print $2}'`
				TMP_TCP_VALUE_TX=`busybox grep "^${TCP_ID}:" ${SUMMARY_FILE_OLD} | busybox awk -F':' '{print $3}'`
				if [ -n "$TMP_TCP_VALUE_RX" ];then
					OLD_TCP_VALUE_RX=$TMP_TCP_VALUE_RX
				fi
				if [ -n "$TMP_TCP_VALUE_TX" ];then
					OLD_TCP_VALUE_TX=$TMP_TCP_VALUE_TX
				fi
			fi
			DELTA_VALUE_RX=0
			DELTA_VALUE_TX=0
			if [ $NEW_TCP_VALUE_RX -lt $OLD_TCP_VALUE_RX ];then
				DELTA_VALUE_RX=$NEW_TCP_VALUE_RX
			else
				DELTA_VALUE_RX=`busybox expr $NEW_TCP_VALUE_RX - $OLD_TCP_VALUE_RX`
			fi
			if [ $NEW_TCP_VALUE_TX -lt $OLD_TCP_VALUE_TX ];then
				DELTA_VALUE_TX=$NEW_TCP_VALUE_TX
			else
				DELTA_VALUE_TX=`busybox expr $NEW_TCP_VALUE_TX - $OLD_TCP_VALUE_TX`
			fi
			DELTA_TCP_DATA=`busybox expr $DELTA_TCP_DATA + $DELTA_VALUE_RX + $DELTA_VALUE_TX`
		done
	fi

	TCP_FLOW_SIZE=`busybox expr $TCP_FLOW_SIZE + $DELTA_TCP_DATA`
	echo "TCP_FLOW_SIZE:${TCP_FLOW_SIZE}" > ${SUMMARY_FILE}

	busybox mv ${SUMMARY_FILE_NEW} ${SUMMARY_FILE_OLD}
	SUMMARY_TIMES=`busybox expr $SUMMARY_TIMES + 1`
	if [ "$SUMMARY_TIMES" == "10" ];then
		busybox cp ${SUMMARY_FILE} ${SUMMARY_FILE}.1.BACKUP
	fi
	if [ "$SUMMARY_TIMES" == "20" ];then
		busybox cp ${SUMMARY_FILE} ${SUMMARY_FILE}.2.BACKUP
		SUMMARY_TIMES=0
	fi
}

TCP_SUMMARY_RUNNINT_COUNT=`busybox ps auxwww | busybox grep 'tcp_summary' | busybox grep RUN | busybox grep -v grep | busybox wc -l 2>/dev/null`
if [ "$EXEC_FLAG" == "RUN" ];then
	TCP_SUMMARY_RUNNINT_COUNT=`busybox expr $TCP_SUMMARY_RUNNINT_COUNT - 1`
fi

if [ "$TCP_SUMMARY_RUNNINT_COUNT" == "0" ];then
	if [ -f /system/norco/norco_env.sh ];then
		CMD_STR=`busybox grep '/system/bin/tcp_summary.sh' /system/norco/norco_env.sh`
		if [ -z "$CMD_STR" ];then
			mount -wo remount /system/norco
			echo "" >> /system/norco/norco_env.sh
			echo 'busybox nohup /system/bin/tcp_summary.sh RUN 2>/dev/null 1>/dev/null &' >> /system/norco/norco_env.sh
			mount -ro remount /system/norco
		fi
	fi
	
	if [ "$EXEC_FLAG" == "RUN" ];then
		SUMMARY_MONTH=`date '+%Y%m'`
		SUMMARY_FILE=${SUMMARY_FILE_PREFIX}.${SUMMARY_MONTH}
		if [ "$EXEC_FLAG2" != "RELOAD" ];then
			busybox rm -rf $SUMMARY_FILE_OLD
			busybox rm -rf $SUMMARY_FILE_NEW

			if [ ! -f ${SUMMARY_FILE} ];then
				SUMMARY_FILE_BACKUP=
				if [ "${SUMMARY_MONTH}" == "197001" ];then
					TCP_SUMMARY_MONTH_LIST=`busybox ls -ltr ${SUMMARY_FILE_PREFIX}.*.BACKUP 2>/dev/null | busybox awk -F' ' '{print $9}'`
					for tt in $TCP_SUMMARY_MONTH_LIST
					do
						SUMMARY_FILE_BACKUP=$tt
					done
				else
					TCP_SUMMARY_MONTH_LIST=`busybox ls -ltr ${SUMMARY_FILE}.*.BACKUP 2>/dev/null | busybox awk -F' ' '{print $9}'`
					for tt in $TCP_SUMMARY_MONTH_LIST
					do
						SUMMARY_FILE_BACKUP=$tt
					done
				fi
				if [ -n "${SUMMARY_FILE_BACKUP}" ];then
					busybox cp ${SUMMARY_FILE_BACKUP} ${SUMMARY_FILE}
				fi
			fi
			busybox rm -rf ${SUMMARY_FILE}.*.BACKUP
		fi
		if [ -f ${SUMMARY_FILE_OLDVERSION} ];then
			if [ ! -f ${SUMMARY_FILE} ];then
				OLD_FLOW_SIZE=`cat ${SUMMARY_FILE_OLDVERSION} | busybox grep "tcp_total,${SUMMARY_MONTH}" | busybox awk -F ':' '{print $2}'`
				if [ -n "$OLD_FLOW_SIZE" ];then
					TCP_FLOW_SIZE=$OLD_FLOW_SIZE
					echo "TCP_FLOW_SIZE:${TCP_FLOW_SIZE}" > ${SUMMARY_FILE}
					cat /proc/net/dev | busybox grep -v '|' | busybox awk -F' ' '{print $1""$2":"$10}' | busybox grep -v '^lo:' | busybox grep -v ':0:0$' > ${SUMMARY_FILE_OLD}
				fi
			fi
			busybox rm -rf ${SUMMARY_FILE_OLDVERSION}
			busybox rm -rf ${SUMMARY_FILE_OLDVERSION}.1
			busybox rm -rf ${SUMMARY_FILE_OLDVERSION}.2
		fi
		
		busybox mkdir -p ${SUMMARY_PATH}
		while [ 1 == 1 ]
		do
			record_tcp_flux
			sleep 30s
		done
	else
		busybox nohup /system/bin/tcp_summary.sh RUN RELOAD 2>/dev/null 1>/dev/null &
	fi
fi
