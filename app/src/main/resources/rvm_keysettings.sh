COMMAND=$COMMAND$
SYSTEM_KEY_FILE=/system/etc/rvm_keysettings.properties
SDCARD_KEY_FILE=/sdcard/rvm_keysettings.properties

if [ "$COMMAND" == "init" ];then
	if [ ! -f $SYSTEM_KEY_FILE ];then
		if [ -f $SDCARD_KEY_FILE ];then
			KEY_COUNT=`cat $SDCARD_KEY_FILE | busybox grep -v '^#' | busybox grep '=' | busybox wc -l`
			if [ "$KEY_COUNT" != "0" ];then
				mount -wo remount /system
				busybox cp $SDCARD_KEY_FILE $SYSTEM_KEY_FILE
				mount -ro remount /system
				sync
			fi
		fi
	fi
fi


if [ "$COMMAND" == "restore" ];then
	if [ -f $SYSTEM_KEY_FILE ];then
		busybox cp $SYSTEM_KEY_FILE $SDCARD_KEY_FILE
	fi
fi

if [ "$COMMAND" == "backup" ];then
	if [ -f $SDCARD_KEY_FILE ];then
		KEY_COUNT=`cat $SDCARD_KEY_FILE | busybox grep -v '^#' | busybox grep '=' | busybox wc -l`
		if [ "$KEY_COUNT" != "0" ];then
			UPDATE_ENABLE=FALSE
			if [ -f $SYSTEM_KEY_FILE ];then
				DIFF_COUNT=`busybox comm -3 $SYSTEM_KEY_FILE $SDCARD_KEY_FILE | busybox grep -v '#' | busybox grep '=' | busybox wc -l`
				if [ "$DIFF_COUNT" != "0" ];then
					UPDATE_ENABLE=TRUE
				fi
			else
				UPDATE_ENABLE=TRUE
			fi
			if [ "$UPDATE_ENABLE" == "TRUE" ];then
				mount -wo remount /system
				busybox cp $SDCARD_KEY_FILE $SYSTEM_KEY_FILE
				mount -ro remount /system
				sync
			fi
		fi
	fi
fi
