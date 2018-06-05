GATEWAY_IPS=`busybox netstat -rn | busybox awk -F' ' '{print $2}' | busybox grep '^[0-9]*\.[0-9]*\.[0-9]*\.[0-9]*$' | busybox grep -v '\.0$' | busybox grep -v '\.255$' | busybox sort -u `
if [ -z "$GATEWAY_IPS" ];then
	GATEWAY_IPS=`busybox netstat -rn | busybox awk -F' ' '{print $1}' | busybox grep '^[0-9]*\.[0-9]*\.[0-9]*\.[0-9]*$' | busybox grep -v '\.0$' | busybox grep -v '\.255$' | busybox sort -u `
fi

for ii in $GATEWAY_IPS
do
	PING_RESULT=`busybox ping -W 1 -w 1 -c 1 $ii 2>&1 | busybox grep '1 packets transmitted'`
	TEST_RESULT=`echo $PING_RESULT | busybox awk -F',' '{print $2}' | busybox awk -F' ' '{print $1}'`
	if [ "$TEST_RESULT" == "1" ];then
		echo "GATEWAY_IP:[$ii]"
	fi
done
