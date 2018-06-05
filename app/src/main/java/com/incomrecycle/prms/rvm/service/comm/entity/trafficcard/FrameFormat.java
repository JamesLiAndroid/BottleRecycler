package com.incomrecycle.prms.rvm.service.comm.entity.trafficcard;

import com.incomrecycle.common.utils.EncryptUtils;
import java.util.ArrayList;
import java.util.List;

public class FrameFormat {
    private List<FrameItem> listFrameItem = new ArrayList();

    public void add(FrameItem frameItem) {
        frameItem.setPos(getFrameLen());
        this.listFrameItem.add(frameItem);
    }

    public void setLen(String name, int len) {
        int pos = 0;
        boolean hasFound = false;
        for (int i = 0; i < this.listFrameItem.size(); i++) {
            FrameItem frameItem = (FrameItem) this.listFrameItem.get(i);
            if (!hasFound && frameItem.getName().equalsIgnoreCase(name)) {
                hasFound = true;
                frameItem.setLen(len);
            }
            frameItem.setPos(pos);
            pos += frameItem.getLen();
        }
    }

    public FrameItem get(String name) {
        for (int i = 0; i < this.listFrameItem.size(); i++) {
            FrameItem frameItem = (FrameItem) this.listFrameItem.get(i);
            if (frameItem.getName().equalsIgnoreCase(name)) {
                return frameItem;
            }
        }
        return null;
    }

    public int getPos(String name) {
        for (int i = 0; i < this.listFrameItem.size(); i++) {
            FrameItem frameItem = (FrameItem) this.listFrameItem.get(i);
            if (frameItem.getName().equalsIgnoreCase(name)) {
                return frameItem.getPos();
            }
        }
        return -1;
    }

    public int getLen(String name) {
        for (int i = 0; i < this.listFrameItem.size(); i++) {
            FrameItem frameItem = (FrameItem) this.listFrameItem.get(i);
            if (frameItem.getName().equalsIgnoreCase(name)) {
                return frameItem.getLen();
            }
        }
        return 0;
    }

    public int getFrameLen() {
        if (this.listFrameItem.size() == 0) {
            return 0;
        }
        FrameItem lastFrameItem = (FrameItem) this.listFrameItem.get(this.listFrameItem.size() - 1);
        return lastFrameItem.getPos() + lastFrameItem.getLen();
    }

    public String dump(byte[] data) {
        if (data == null) {
            return "NULL";
        }
        return dump(data, 0, data.length);
    }

    public String dump(byte[] data, int pos, int len) {
        if (data == null) {
            return "NULL";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.listFrameItem.size(); i++) {
            FrameItem frameItem = (FrameItem) this.listFrameItem.get(i);
            sb.append(frameItem.getName());
            sb.append("(" + frameItem.getLen() + "):");
            int l = frameItem.getLen();
            if (frameItem.getPos() + l > len) {
                l = len - frameItem.getPos();
            }
            sb.append(EncryptUtils.byte2hex(data, frameItem.getPos() + pos, l));
            sb.append("\n");
        }
        return sb.toString();
    }
}
