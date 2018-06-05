package com.incomrecycle.prms.rvm.common;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.SysGlobal;
import com.incomrecycle.common.utils.NetworkUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.CardType;
import com.incomrecycle.prms.rvm.service.comm.entity.trafficcard.FrameDataFormat.ServerFrameType;
import java.util.ArrayList;
import java.util.List;

public class NetworkStateMgr {
    public static final String NETWORK_ERROR = "NETWORK_ERROR";
    public static final String NETWORK_FAILED = "NETWORK_FAILED";
    public static final String NETWORK_SUCCESS = "NETWORK_SUCCESS";
    private static NetworkStateMgr mgr = null;
    private boolean isRunning = false;
    private List<OnStateChanged> listOnStateChanged = new ArrayList();
    private String state = NETWORK_SUCCESS;

    public interface OnStateChanged {
        void apply(String str);
    }

    public static NetworkStateMgr getMgr() {
        if (mgr == null) {
            mgr = new NetworkStateMgr();
        }
        return mgr;
    }

    private NetworkStateMgr() {
    }

    public String getNetworkState() {
        return this.state;
    }

    public void start() {
        if (!this.isRunning) {
            this.isRunning = true;
            SysGlobal.execute(new Runnable() {
                public void run() {
                    String newState = NetworkStateMgr.this.state;
                    String TIMEOUT = SysConfig.get("RVM.QUERY.NETWORK.STATE.TIME");
                    if (StringUtils.isBlank(TIMEOUT)) {
                        TIMEOUT = CardType.MSG_BDJ;
                    }
                    int timeout = Integer.valueOf(TIMEOUT).intValue() * ServerFrameType.DATA_TRANSFER_REQ;
                    while (true) {
                        newState = NetworkStateMgr.this.getNetState();
                        if (!(newState == null || NetworkStateMgr.this.state.equalsIgnoreCase(newState))) {
                            NetworkStateMgr.this.state = newState;
                            for (int i = 0; i < NetworkStateMgr.this.listOnStateChanged.size(); i++) {
                                ((OnStateChanged) NetworkStateMgr.this.listOnStateChanged.get(i)).apply(NetworkStateMgr.this.state);
                            }
                        }
                        try {
                            Thread.sleep((long) timeout);
                        } catch (Exception e) {
                        }
                    }
                }
            });
        }
    }

    public void add(OnStateChanged onStateChanged) {
        this.listOnStateChanged.add(onStateChanged);
    }

    public void remove(OnStateChanged onStateChanged) {
        if (onStateChanged != null) {
            this.listOnStateChanged.remove(onStateChanged);
        }
    }

    private String getNetState() {
        List<String> listGatewayIp = NetworkUtils.getGatewayIp("get_gateway.sh");
        if (listGatewayIp == null) {
            listGatewayIp = new ArrayList();
        }
        if (listGatewayIp.size() == 0 && !StringUtils.isBlank(SysConfig.get("NETWORK.GATEWAY"))) {
            listGatewayIp.add(SysConfig.get("NETWORK.GATEWAY"));
        }
        String netState = null;
        for (int i = 0; i < listGatewayIp.size(); i++) {
            netState = NetworkUtils.ipState((String) listGatewayIp.get(i));
            if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase(netState)) {
                break;
            }
        }
        if (!NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase(netState)) {
            return NETWORK_ERROR;
        }
        String DNS_IPS = SysConfig.get("NETWORK.DNS");
        if (!StringUtils.isBlank(DNS_IPS)) {
            boolean isSuccessful = false;
            String[] DNS_IP_LIST = DNS_IPS.split(";");
            for (String ipState : DNS_IP_LIST) {
                if (NetworkUtils.NET_STATE_SUCCESS.equalsIgnoreCase(NetworkUtils.ipState(ipState))) {
                    isSuccessful = true;
                    break;
                }
            }
            if (!isSuccessful) {
                return NETWORK_FAILED;
            }
        }
        return NETWORK_SUCCESS;
    }
}