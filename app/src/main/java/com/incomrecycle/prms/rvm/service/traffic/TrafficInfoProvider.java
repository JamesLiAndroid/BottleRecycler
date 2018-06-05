package com.incomrecycle.prms.rvm.service.traffic;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import com.incomrecycle.prms.rvm.RVMApplication;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.GET_PERMISSIONS;

public class TrafficInfoProvider {
    private static final int UNSUPPORTED = -1;
    private Context context = RVMApplication.getApplication();
    private PackageManager pm = this.context.getPackageManager();

    public List<TrafficInfo> getTrafficInfos() {
        List<PackageInfo> packinfos = this.pm.getInstalledPackages(GET_PERMISSIONS);
        List<TrafficInfo> trafficInfos = new ArrayList();
        for (PackageInfo packinfo : packinfos) {
            String[] permissions = packinfo.requestedPermissions;
            if (permissions != null && permissions.length > 0) {
                for (String permission : permissions) {
                    if ("android.permission.INTERNET".equals(permission)) {
                        TrafficInfo trafficInfo = new TrafficInfo();
                        trafficInfo.setPackname(packinfo.packageName);
                        trafficInfo.setAppname(packinfo.applicationInfo.loadLabel(this.pm).toString());
                        int uid = packinfo.applicationInfo.uid;
                        trafficInfo.setUID(uid);
                        trafficInfo.setRx(TrafficStats.getUidRxBytes(uid));
                        trafficInfo.setTx(TrafficStats.getUidTxBytes(uid));
                        trafficInfos.add(trafficInfo);
                        break;
                    }
                }
            }
        }
        return trafficInfos;
    }

    public long getTrafficInfo(int uid) {
        long rcvTraffic = getRcvTraffic(uid);
        long sndTraffic = getSndTraffic(uid);
        if (rcvTraffic == -1 || sndTraffic == -1) {
            return -1;
        }
        return rcvTraffic + sndTraffic;
    }

    public long getRcvTraffic(int uid) {
        IOException e;
        Throwable th;
        long rcvTraffic = TrafficStats.getUidRxBytes(uid);
        if (rcvTraffic == -1) {
            return -1;
        }
        RandomAccessFile rafRcv = null;
        RandomAccessFile rafSnd = null;
        try {
            RandomAccessFile rafRcv2 = new RandomAccessFile("/proc/uid_stat/" + uid + "/tcp_rcv", "r");
            try {
                rcvTraffic = Long.parseLong(rafRcv2.readLine());
                if (rafRcv2 != null) {
                    try {
                        rafRcv2.close();
                    } catch (IOException e2) {
                        rafRcv = rafRcv2;
                    }
                }
                if (rafSnd != null) {
                    rafSnd.close();
                }
                rafRcv = rafRcv2;
            } catch (FileNotFoundException e3) {
                rafRcv = rafRcv2;
                rcvTraffic = -1;
                if (rafRcv != null) {
                    try {
                        rafRcv.close();
                    } catch (IOException e4) {
                    }
                }
                if (rafSnd != null) {
                    rafSnd.close();
                }
                return rcvTraffic;
            } catch (IOException e5) {
                e = e5;
                rafRcv = rafRcv2;
                try {
                    e.printStackTrace();
                    if (rafRcv != null) {
                        try {
                            rafRcv.close();
                        } catch (IOException e6) {
                        }
                    }
                    if (rafSnd != null) {
                        rafSnd.close();
                    }
                    return rcvTraffic;
                } catch (Throwable th2) {
                    if (rafRcv != null) {
                        try {
                            rafRcv.close();
                        } catch (IOException e7) {
                            throw th2;
                        }
                    }
                    if (rafSnd != null) {
                        rafSnd.close();
                    }
                    throw th2;
                }
            } catch (Throwable th3) {
                rafRcv = rafRcv2;
                if (rafRcv != null) {
                    rafRcv.close();
                }
                if (rafSnd != null) {
                    rafSnd.close();
                }
                throw th3;
            }
        } catch (FileNotFoundException e8) {
            rcvTraffic = -1;
            if (rafRcv != null) {
                try {
                    rafRcv.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (rafSnd != null) {
                try {
                    rafSnd.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return rcvTraffic;
        } catch (IOException e9) {
            e = e9;
            e.printStackTrace();
            if (rafRcv != null) {
                try {
                    rafRcv.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (rafSnd != null) {
                try {
                    rafSnd.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return rcvTraffic;
        }
        return rcvTraffic;
    }

    public long getSndTraffic(int uid) {
        IOException e;
        Throwable th;
        long sndTraffic = TrafficStats.getUidTxBytes(uid);
        if (sndTraffic == -1) {
            return -1;
        }
        RandomAccessFile rafRcv = null;
        RandomAccessFile rafSnd = null;
        try {
            RandomAccessFile rafSnd2 = new RandomAccessFile("/proc/uid_stat/" + uid + "/tcp_snd", "r");
            try {
                sndTraffic = Long.parseLong(rafSnd2.readLine());
                if (rafRcv != null) {
                    try {
                        rafRcv.close();
                    } catch (IOException e2) {
                        rafSnd = rafSnd2;
                    }
                }
                if (rafSnd2 != null) {
                    rafSnd2.close();
                }
                rafSnd = rafSnd2;
            } catch (FileNotFoundException e3) {
                rafSnd = rafSnd2;
                sndTraffic = -1;
                if (rafRcv != null) {
                    try {
                        rafRcv.close();
                    } catch (IOException e4) {
                    }
                }
                if (rafSnd != null) {
                    rafSnd.close();
                }
                return sndTraffic;
            } catch (IOException e5) {
                e = e5;
                rafSnd = rafSnd2;
                try {
                    e.printStackTrace();
                    if (rafRcv != null) {
                        try {
                            rafRcv.close();
                        } catch (IOException e6) {
                        }
                    }
                    if (rafSnd != null) {
                        rafSnd.close();
                    }
                    return sndTraffic;
                } catch (Throwable th2) {
                    if (rafRcv != null) {
                        try {
                            rafRcv.close();
                        } catch (IOException e7) {
                            throw th2;
                        }
                    }
                    if (rafSnd != null) {
                        rafSnd.close();
                    }
                    throw th2;
                }
            } catch (Throwable th3) {
                rafSnd = rafSnd2;
                if (rafRcv != null) {
                    rafRcv.close();
                }
                if (rafSnd != null) {
                    rafSnd.close();
                }
                throw th3;
            }
        } catch (FileNotFoundException e8) {
            sndTraffic = -1;
            if (rafRcv != null) {
                try {
                    rafRcv.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (rafSnd != null) {
                try {
                    rafSnd.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return sndTraffic;
        } catch (IOException e9) {
            e = e9;
            e.printStackTrace();
            if (rafRcv != null) {
                try {
                    rafRcv.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (rafSnd != null) {
                try {
                    rafSnd.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return sndTraffic;
        }
        return sndTraffic;
    }

    public static long getNetworkRxBytes() {
        return TrafficStats.getTotalRxBytes();
    }

    public static long getNetworkTxBytes() {
        return TrafficStats.getTotalTxBytes();
    }
}
