package com.incomrecycle.prms.rvm.util;

import com.incomrecycle.common.utils.StringUtils;
import com.incomrecycle.prms.rvm.gui.CommonServiceHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class QuerySortMedia {
    public List<HashMap<String, String>> QueryLocalMedia(String LocalActivityName, String MeidaType) {
        List<HashMap<String, String>> listURL = new ArrayList();
        List<HashMap<String, String>> listMedia = new ArrayList();
        HashMap<String, Object> hsmpResult = new HashMap();
        HashMap<String, Integer> hsmpPrity = new HashMap();
        try {
            hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryMedia", null);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (hsmpResult != null && hsmpResult.size() > 0) {
            listURL = (List) hsmpResult.get("RVM_MEDIA_LIST");
            int num = listURL.size();
            if (listURL != null && num > 0) {
                for (int i = 0; i < num; i++) {
                    HashMap<String, String> hsmpMedia = (HashMap) listURL.get(i);
                    String MEDIA_TYPE = (String) hsmpMedia.get("MEDIA_TYPE");
                    String FILE_PATH = (String) hsmpMedia.get("FILE_PATH");
                    String MEDIA_PLAY_LOCAL = (String) hsmpMedia.get("MEDIA_PLAY_LOCAL");
                    String MEDIA_PRIORITY = (String) hsmpMedia.get("MEDIA_PRIORITY");
                    int priority = 0;
                    try {
                        if (!StringUtils.isBlank(MEDIA_PRIORITY)) {
                            priority = Integer.parseInt(MEDIA_PRIORITY);
                        }
                    } catch (Exception e) {
                        priority = 0;
                    }
                    if (!StringUtils.isBlank(FILE_PATH) && new File(FILE_PATH).isFile() && MEDIA_TYPE.equalsIgnoreCase(MeidaType) && MEDIA_PLAY_LOCAL.equalsIgnoreCase(LocalActivityName)) {
                        try {
                            hsmpPrity.put(i + "", Integer.valueOf(priority));
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                List<Entry<String, Integer>> arrayList = new ArrayList(hsmpPrity.entrySet());
                Collections.sort(arrayList, new Comparator<Entry<String, Integer>>() {
                    public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                        return ((Integer) o1.getValue()).intValue() - ((Integer) o2.getValue()).intValue();
                    }
                });
                for (Entry<String, Integer> e3 : arrayList) {
                    try {
                        listMedia.add(listURL.get(Integer.parseInt((String) e3.getKey())));
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                }
            }
        }
        return listMedia;
    }

    public List<HashMap<String, String>> QueryLocalMedia(String LocalActivityName) {
        List<HashMap<String, String>> listURL = new ArrayList();
        List<HashMap<String, String>> listMedia = new ArrayList();
        HashMap<String, Object> hsmpResult = new HashMap();
        HashMap<String, Integer> hsmpPrity = new HashMap();
        try {
            hsmpResult = CommonServiceHelper.getGUICommonService().execute("GUIQueryCommonService", "queryMedia", null);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (hsmpResult != null && hsmpResult.size() > 0) {
            listURL = (List) hsmpResult.get("RVM_MEDIA_LIST");
            int num = listURL.size();
            if (listURL != null && num > 0) {
                for (int i = 0; i < num; i++) {
                    HashMap<String, String> hsmpMedia = (HashMap) listURL.get(i);
                    String FILE_PATH = (String) hsmpMedia.get("FILE_PATH");
                    String MEDIA_PLAY_LOCAL = (String) hsmpMedia.get("MEDIA_PLAY_LOCAL");
                    String MEDIA_PRIORITY = (String) hsmpMedia.get("MEDIA_PRIORITY");
                    int priority = 0;
                    try {
                        if (!StringUtils.isBlank(MEDIA_PRIORITY)) {
                            priority = Integer.parseInt(MEDIA_PRIORITY);
                        }
                    } catch (Exception e) {
                        priority = 0;
                    }
                    if (!StringUtils.isBlank(FILE_PATH) && new File(FILE_PATH).isFile() && MEDIA_PLAY_LOCAL.equalsIgnoreCase(LocalActivityName)) {
                        try {
                            hsmpPrity.put(i + "", Integer.valueOf(priority));
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                List<Entry<String, Integer>> listPX = new ArrayList(hsmpPrity.entrySet());
                Collections.sort(listPX, new Comparator<Entry<String, Integer>>() {
                    public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                        return ((Integer) o1.getValue()).intValue() - ((Integer) o2.getValue()).intValue();
                    }
                });
                for (Entry<String, Integer> e3 : listPX) {
                    try {
                        listMedia.add(listURL.get(Integer.parseInt((String) e3.getKey())));
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                }
            }
        }
        return listMedia;
    }
}
