package com.incomrecycle.prms.rvm.gui;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.utils.DateUtils;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class BroadcastTaskCommonService {
    private static List<HashMap<String, String>> allAdList = new ArrayList();
    private static List<HashMap<String, String>> homePageLeftBottomList = new ArrayList();
    private static int lastModifiedTime = 0;

    public static void init(String confPath) {
        Exception e;
        Throwable th;
        if (allAdList == null) {
            allAdList = new ArrayList();
        }
        InputStream fis = null;
        InputStreamReader isr = null;
        String file_path = confPath;
        File file = new File(confPath);
        if (StringUtils.isBlank(confPath) || !file.isFile()) {
            file_path = SysConfig.get("BROADCAST.FILE");
        }
        File file2 = new File(file_path);
        if (file2.isFile()) {
            int thisFileModifyTime = Integer.parseInt(DateUtils.formatDatetime(new Date(file2.lastModified()), "yyyyMMddHH"));
            if (thisFileModifyTime > lastModifiedTime || allAdList.size() <= 0) {
                lastModifiedTime = thisFileModifyTime;
                allAdList.clear();
                try {
                    InputStream fis2 = new FileInputStream(file2);
                    try {
                        InputStreamReader isr2 = new InputStreamReader(new FileInputStream(file2), "UTF-8");
                        try {
                            HashMap<String, String> broadcast;
                            BufferedReader br = new BufferedReader(isr2);
                            HashMap<String, String> broadcast2 = null;
                            while (true) {
                                try {
                                    String ADInfo = br.readLine();
                                    if (ADInfo == null) {
                                        break;
                                    } else if (!(ADInfo.length() == 0 || ADInfo.startsWith("#"))) {
                                        int idx = ADInfo.indexOf("=");
                                        if (idx != -1) {
                                            if (broadcast2 != null) {
                                                broadcast2.put(ADInfo.substring(0, idx).trim(), ADInfo.substring(idx + 1).trim());
                                                broadcast = broadcast2;
                                            }
                                            broadcast = broadcast2;
                                        } else {
                                            ADInfo = ADInfo.trim();
                                            int startIdx = ADInfo.indexOf("[");
                                            int endIdx = ADInfo.indexOf("]");
                                            if (startIdx >= 0 && endIdx == ADInfo.length() - 1) {
                                                if (startIdx != 0) {
                                                    ADInfo = ADInfo.substring(startIdx);
                                                }
                                                if (broadcast2 != null && broadcast2.size() > 0) {
                                                    allAdList.add(broadcast2);
                                                }
                                                broadcast = new HashMap();
                                                broadcast.put("name", ADInfo);
                                            }
                                            broadcast = broadcast2;
                                        }
                                        broadcast2 = broadcast;
                                    }
                                } catch (Exception e2) {
                                    e = e2;
                                    isr = isr2;
                                    fis = fis2;
                                    broadcast = broadcast2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    isr = isr2;
                                    fis = fis2;
                                    broadcast = broadcast2;
                                }
                            }
                            if (broadcast2 != null) {
                                if (broadcast2.size() > 0) {
                                    allAdList.add(broadcast2);
                                }
                            }
                            IOUtils.close(fis2);
                            if (isr2 != null) {
                                try {
                                    isr2.close();
                                    isr = isr2;
                                    fis = fis2;
                                    broadcast = broadcast2;
                                    return;
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                    isr = isr2;
                                    fis = fis2;
                                    broadcast = broadcast2;
                                    return;
                                }
                            }
                            fis = fis2;
                            broadcast = broadcast2;
                        } catch (Exception e4) {
                            e = e4;
                            isr = isr2;
                            fis = fis2;
                        } catch (Throwable th3) {
                            th = th3;
                            isr = isr2;
                            fis = fis2;
                        }
                    } catch (Exception e5) {
                        e = e5;
                        fis = fis2;
                        try {
                            e.printStackTrace();
                            IOUtils.close(fis);
                            if (isr != null) {
                                try {
                                    isr.close();
                                } catch (IOException e32) {
                                    e32.printStackTrace();
                                }
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            IOUtils.close(fis);
                            if (isr != null) {
                                try {
                                    isr.close();
                                } catch (IOException e322) {
                                    e322.printStackTrace();
                                }
                            }
//                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        fis = fis2;
                        IOUtils.close(fis);
                        if (isr != null) {
                            isr.close();
                        }
//                        throw th;
                    }
                } catch (Exception e6) {
                    e = e6;
                    e.printStackTrace();
                    IOUtils.close(fis);
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static List getHomePageLeftTopList(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                List homePageLeftTopList = new ArrayList();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0) {
                        String begin = (String) map.get(AllAdvertisement.BEGIN_DATE);
                        String end = (String) map.get(AllAdvertisement.END_DATE);
                        if (!(StringUtils.isBlank(begin) || StringUtils.isBlank(end))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date begin_date = sdf.parse(begin);
                            Date end_date = sdf.parse(end);
                            Date today = new Date();
                            if (AllAdvertisement.HOMEPAGE_LEFT_TOP_LABEL.equals(map.get("name")) && today.before(end_date) && today.after(begin_date)) {
                                homePageLeftTopList.add(map);
                            }
                        }
                    }
                }
                List<HashMap<String, String>> sortAfterList = new ArrayList(sortList(homePageLeftTopList));
                homePageLeftTopList.clear();
                homePageLeftTopList.addAll(sortAfterList);
                return homePageLeftTopList;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List getRebateProcessList(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                List rebateProcessList = new ArrayList();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0) {
                        String begin = (String) map.get(AllAdvertisement.BEGIN_DATE);
                        String end = (String) map.get(AllAdvertisement.END_DATE);
                        if (!(StringUtils.isBlank(begin) || StringUtils.isBlank(end))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date begin_date = sdf.parse(begin);
                            Date end_date = sdf.parse(end);
                            Date today = new Date();
                            if (AllAdvertisement.HOMEPAGE_REBATE_PROCESS_LABEL.equals(map.get("name")) && today.before(end_date) && today.after(begin_date)) {
                                rebateProcessList.add(map);
                            }
                        }
                    }
                }
                List<HashMap<String, String>> sortAfterList = new ArrayList(sortList(rebateProcessList));
                rebateProcessList.clear();
                rebateProcessList.addAll(sortAfterList);
                return rebateProcessList;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List getRebateProcessChangeList(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                List rebateProcessList = new ArrayList();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0) {
                        String begin = (String) map.get(AllAdvertisement.BEGIN_DATE);
                        String end = (String) map.get(AllAdvertisement.END_DATE);
                        if (!(StringUtils.isBlank(begin) || StringUtils.isBlank(end))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date begin_date = sdf.parse(begin);
                            Date end_date = sdf.parse(end);
                            Date today = new Date();
                            if (AllAdvertisement.HOMEPAGE_REBATE_PROCESS_CHANGE_LABEL.equals(map.get("name")) && today.before(end_date) && today.after(begin_date)) {
                                rebateProcessList.add(map);
                            }
                        }
                    }
                }
                List<HashMap<String, String>> sortAfterList = new ArrayList(sortList(rebateProcessList));
                rebateProcessList.clear();
                rebateProcessList.addAll(sortAfterList);
                return rebateProcessList;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List getHomePageLeftBottomList(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                homePageLeftBottomList.clear();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0) {
                        String begin = (String) map.get(AllAdvertisement.BEGIN_DATE);
                        String end = (String) map.get(AllAdvertisement.END_DATE);
                        if (!(StringUtils.isBlank(begin) || StringUtils.isBlank(end))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date begin_date = sdf.parse(begin);
                            Date end_date = sdf.parse(end);
                            Date today = new Date();
                            if (AllAdvertisement.HOMEPAGE_LEFT_BOTTOM_LABEL.equals(map.get("name")) && today.before(end_date) && today.after(begin_date)) {
                                homePageLeftBottomList.add(map);
                            }
                        }
                    }
                }
                List<HashMap<String, String>> sortAfterList = sortList(homePageLeftBottomList);
                homePageLeftBottomList.clear();
                homePageLeftBottomList.addAll(sortAfterList);
                return homePageLeftBottomList;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List getHomePageRightTopList(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                List homePageRightTopList = new ArrayList();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0) {
                        Date today = new Date();
                        String begin = (String) map.get(AllAdvertisement.BEGIN_DATE);
                        String end = (String) map.get(AllAdvertisement.END_DATE);
                        if (!(StringUtils.isBlank(begin) || StringUtils.isBlank(end))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date begin_date = sdf.parse(begin);
                            Date end_date = sdf.parse(end);
                            if (AllAdvertisement.HOMEPAGE_RIGHT_TOP_LABEL.equals(map.get("name")) && today.before(end_date) && today.after(begin_date)) {
                                homePageRightTopList.add(map);
                            }
                        }
                    }
                }
                List<HashMap<String, String>> sortAfterList = new ArrayList(sortList(homePageRightTopList));
                homePageRightTopList.clear();
                homePageRightTopList.addAll(sortAfterList);
                return homePageRightTopList;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static List<HashMap<String, String>> sortList(List<HashMap<String, String>> sortList) {
        List<HashMap<String, String>> temp = new ArrayList();
        int srcListSize = 0;
        if (sortList != null) {
            srcListSize = sortList.size();
        }
        if (srcListSize > 0) {
            HashMap<String, Integer> hsmpPrity = new HashMap();
            for (int i = 0; i < srcListSize; i++) {
                String playOrder = (String) ((HashMap) sortList.get(i)).get(AllAdvertisement.PLAY_ORDER);
                if (StringUtils.isBlank(playOrder)) {
                    hsmpPrity.put(i + "", Integer.valueOf(1));
                } else {
                    try {
                        hsmpPrity.put(i + "", Integer.valueOf(Integer.parseInt(playOrder)));
                    } catch (Exception e) {
                        hsmpPrity.put(i + "", Integer.valueOf(1));
                    }
                }
            }
            List<Entry<String, Integer>> listPX = new ArrayList(hsmpPrity.entrySet());
            Collections.sort(listPX, new Comparator<Entry<String, Integer>>() {
                public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                    return ((Integer) o1.getValue()).intValue() - ((Integer) o2.getValue()).intValue();
                }
            });
            for (Entry<String, Integer> e2 : listPX) {
                try {
                    temp.add(sortList.get(Integer.parseInt((String) e2.getKey())));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return temp;
    }

    public static List getHomePageLeftBottomPicList(String confPath) {
        getHomePageLeftBottomList(confPath);
        List<HashMap<String, String>> listPicture = new ArrayList();
        for (int i = 0; i < homePageLeftBottomList.size(); i++) {
            HashMap map = (HashMap) homePageLeftBottomList.get(i);
            try {
                if (map.get(AllAdvertisement.MEDIA_TYPE).equals("PICTURE")) {
                    listPicture.add(map);
                }
            } catch (Exception e) {
            }
        }
        return listPicture;
    }

    public static List getHomePageLeftBottomVideoList(String confPath) {
        getHomePageLeftBottomList(confPath);
        List<HashMap<String, String>> listVideo = new ArrayList();
        for (int i = 0; i < homePageLeftBottomList.size(); i++) {
            HashMap map = (HashMap) homePageLeftBottomList.get(i);
            try {
                if (map.get(AllAdvertisement.MEDIA_TYPE).equals(AllAdvertisement.MEDIA_TYPE_MOVIE)) {
                    listVideo.add(map);
                }
            } catch (Exception e) {
            }
        }
        return listVideo;
    }

    public static List getPutBottlePicList(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                List putBottlePicList = new ArrayList();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0) {
                        String begin = (String) map.get(AllAdvertisement.BEGIN_DATE);
                        String end = (String) map.get(AllAdvertisement.END_DATE);
                        if (!(StringUtils.isBlank(begin) || StringUtils.isBlank(end))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date begin_date = sdf.parse(begin);
                            Date end_date = sdf.parse(end);
                            Date today = new Date();
                            if (AllAdvertisement.HOMEPAGE_PUT_BOTTLE_LABEL.equals(map.get("name")) && today.before(end_date) && today.after(begin_date)) {
                                putBottlePicList.add(map);
                            }
                        }
                    }
                }
                List<HashMap<String, String>> sortAfterList = sortList(putBottlePicList);
                putBottlePicList.clear();
                putBottlePicList.addAll(sortAfterList);
                return putBottlePicList;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List getExtVendingWay(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                List arrayList = new ArrayList();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0) {
                        String begin = (String) map.get(AllAdvertisement.BEGIN_DATE);
                        String end = (String) map.get(AllAdvertisement.END_DATE);
                        if (!(StringUtils.isBlank(begin) || StringUtils.isBlank(end))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date begin_date = sdf.parse(begin);
                            Date end_date = sdf.parse(end);
                            Date today = new Date();
                            if (AllAdvertisement.HOMEPAGE_EXT_VENDING_LABEL.equals(map.get("name")) && today.before(end_date) && today.after(begin_date)) {
                                arrayList.add(map);
                            }
                        }
                    }
                }
                return arrayList;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List getVendingWayFlag(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                List arrayList = new ArrayList();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0) {
                        String begin = (String) map.get(AllAdvertisement.BEGIN_DATE);
                        String end = (String) map.get(AllAdvertisement.END_DATE);
                        if (!(StringUtils.isBlank(begin) || StringUtils.isBlank(end))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date begin_date = sdf.parse(begin);
                            Date end_date = sdf.parse(end);
                            Date today = new Date();
                            if (AllAdvertisement.HOMEPAGE_VENDING_LABEL.equals(map.get("name")) && today.before(end_date) && today.after(begin_date)) {
                                arrayList.add(map);
                            }
                        }
                    }
                }
                return arrayList;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List getHomePageCenterTopList(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                List homePageCenterTopList = new ArrayList();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0) {
                        Date today = new Date();
                        String begin = (String) map.get(AllAdvertisement.BEGIN_DATE);
                        String end = (String) map.get(AllAdvertisement.END_DATE);
                        if (!(StringUtils.isBlank(begin) || StringUtils.isBlank(end))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date begin_date = sdf.parse(begin);
                            Date end_date = sdf.parse(end);
                            if (AllAdvertisement.HOMEPAGE_CENTER_TOP_LABEL.equals(map.get("name")) && today.before(end_date) && today.after(begin_date)) {
                                homePageCenterTopList.add(map);
                            }
                        }
                    }
                }
                List<HashMap<String, String>> sortAfterList = new ArrayList(sortList(homePageCenterTopList));
                homePageCenterTopList.clear();
                homePageCenterTopList.addAll(sortAfterList);
                return homePageCenterTopList;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List getBackgroundPicList(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                List putBottlePicList = new ArrayList();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0) {
                        String begin = (String) map.get(AllAdvertisement.BEGIN_DATE);
                        String end = (String) map.get(AllAdvertisement.END_DATE);
                        if (!(StringUtils.isBlank(begin) || StringUtils.isBlank(end))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date begin_date = sdf.parse(begin);
                            Date end_date = sdf.parse(end);
                            Date today = new Date();
                            if (AllAdvertisement.BACKGROUND_LABEL.equals(map.get("name")) && today.before(end_date) && today.after(begin_date)) {
                                putBottlePicList.add(map);
                            }
                        }
                    }
                }
                List<HashMap<String, String>> sortAfterList = sortList(putBottlePicList);
                putBottlePicList.clear();
                putBottlePicList.addAll(sortAfterList);
                return putBottlePicList;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List getAdVersionList(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                homePageLeftBottomList.clear();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0 && AllAdvertisement.AD_VERSION.equals(map.get("name"))) {
                        homePageLeftBottomList.add(map);
                    }
                }
                List<HashMap<String, String>> sortAfterList = sortList(homePageLeftBottomList);
                homePageLeftBottomList.clear();
                homePageLeftBottomList.addAll(sortAfterList);
                return homePageLeftBottomList;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List getHomePageFullAdList(String confPath) {
        try {
            init(confPath);
            int allAdListSize = allAdList.size();
            if (allAdListSize > 0) {
                List homePageLeftTopList = new ArrayList();
                for (int i = 0; i < allAdListSize; i++) {
                    HashMap map = (HashMap) allAdList.get(i);
                    if (map != null && map.size() > 0) {
                        String begin = (String) map.get(AllAdvertisement.BEGIN_DATE);
                        String end = (String) map.get(AllAdvertisement.END_DATE);
                        if (!(StringUtils.isBlank(begin) || StringUtils.isBlank(end))) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date begin_date = sdf.parse(begin);
                            Date end_date = sdf.parse(end);
                            Date today = new Date();
                            if (AllAdvertisement.HOMEPAGE_FULL_AD_LABEL.equals(map.get("name")) && today.before(end_date) && today.after(begin_date)) {
                                String iconPath = (String) map.get(AllAdvertisement.ICON);
                                String picPath = (String) map.get(AllAdvertisement.MAIN_PICTURE_PATH);
                                String moviePath = (String) map.get(AllAdvertisement.MAIN_MOVIE_PATH);
                                if (!StringUtils.isBlank(iconPath) && new File(iconPath).isFile() && ((!StringUtils.isBlank(picPath) && new File(picPath).isFile()) || (!StringUtils.isBlank(moviePath) && new File(moviePath).isFile()))) {
                                    homePageLeftTopList.add(map);
                                }
                            }
                        }
                    }
                }
                List<HashMap<String, String>> sortAfterList = new ArrayList(sortList(homePageLeftTopList));
                homePageLeftTopList.clear();
                homePageLeftTopList.addAll(sortAfterList);
                return homePageLeftTopList;
            }
        } catch (Exception e) {
        }
        return null;
    }
}
