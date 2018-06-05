if [ -f /data/data/com.incomrecycle.prms.rvm/config.properties ];then
	RVM_CODE_OLD=`cat /data/data/com.incomrecycle.prms.rvm/config.properties | busybox grep 'RVM.CODE=' | busybox grep -v grep | busybox awk -F'='  '{print $2}'`
	if [ -n "$RVM_CODE_OLD" ] && [ "$RVM_CODE_OLD" != "0" ];then
		if [ -f /sdcard/rvm/config.properties ];then
			RVM_CODE=`cat /sdcard/rvm/config.properties | busybox grep 'RVM.CODE=' | busybox grep -v grep | busybox awk -F'='  '{print $2}'`
			if [ -z "$RVM_CODE" ] || [ "$RVM_CODE" == "0" ];then
				mkdir -p /sdcard/rvm/
				busybox cp /data/data/com.incomrecycle.prms.rvm/config.properties /sdcard/rvm/ 2>/dev/null
			fi
		else
			mkdir -p /sdcard/rvm/
			busybox cp /data/data/com.incomrecycle.prms.rvm/config.properties /sdcard/rvm/ 2>/dev/null
		fi
	fi
fi
