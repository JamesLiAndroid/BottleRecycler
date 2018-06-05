if [ -f /sdcard/rvm/config.properties ];then
	RVM_CODE=`cat /sdcard/rvm/config.properties | busybox grep 'RVM.CODE=' | busybox grep -v grep | busybox awk -F'='  '{print $2}'`
	if [ -n "$RVM_CODE" ] && [ "$RVM_CODE" != "0" ];then
		cat /sdcard/rvm/config.properties > /data/data/com.incomrecycle.prms.rvm/config.properties
		sync
	fi
fi
