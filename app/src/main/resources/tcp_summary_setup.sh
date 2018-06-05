SOURCE_SHELL_SCRIPT=$SOURCE_SHELL_SCRIPT$
TARGET_SHELL_SCRIPT=/system/bin/tcp_summary.sh
UPDATE_ENABLE=FALSE
if [ -f $TARGET_SHELL_SCRIPT ];then
	DIFF_COUNT=`busybox diff $TARGET_SHELL_SCRIPT $SOURCE_SHELL_SCRIPT | busybox wc -l`
	if [ "$DIFF_COUNT" != "0" ];then
		UPDATE_ENABLE=TRUE
	fi
else
	UPDATE_ENABLE=TRUE
fi
if [ "$UPDATE_ENABLE" == "TRUE" ];then
	TCP_SUMMARY_PID=`busybox ps auxwww | busybox grep 'tcp_summary' | busybox grep RUN | busybox grep -v grep | busybox awk -F' ' '{print $1}'`
	if [ -n "$TCP_SUMMARY_PID" ];then
		kill -9 $TCP_SUMMARY_PID 2>/dev/null
	fi
	mount -wo remount /system
	busybox cp $SOURCE_SHELL_SCRIPT $TARGET_SHELL_SCRIPT
	chmod 555 $TARGET_SHELL_SCRIPT
	mount -ro remount /system
fi
$TARGET_SHELL_SCRIPT
