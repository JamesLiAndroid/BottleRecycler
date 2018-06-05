package com.incomrecycle.prms.rvm.gui.activity.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.incomrecycle.common.utils.IOUtils;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.RVMMainADActivity;
import com.incomrecycle.prms.rvm.common.SysDef.AllAdvertisement;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class MianActivityIconAdapter extends BaseAdapter {
    private static int select_item = 1;
    private List<HashMap> listItem;
    private Context mContext;
    private LayoutInflater mInflater = ((LayoutInflater) this.mContext.getSystemService("layout_inflater"));

    public MianActivityIconAdapter(Context context, List list) {
        this.listItem = list;
        this.mContext = context;
    }

    public int getCount() {
        return this.listItem.size();
    }

    public Object getItem(int position) {
        return this.listItem.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = this.mInflater.inflate(R.layout.frame_ad_item, null);
        ImageView img = (ImageView) convertView.findViewById(R.id.icon_img);
        select_item = RVMMainADActivity.indexOfListMedia;
        LayoutParams lp;
        if (select_item == position) {
            lp = img.getLayoutParams();
            lp.width = FTPCodes.FILE_STATUS_OK;
            lp.height = FTPCodes.FILE_STATUS_OK;
            img.setLayoutParams(lp);
        } else {
            lp = img.getLayoutParams();
            lp.width = FTPCodes.RESTART_MARKER;
            lp.height = FTPCodes.RESTART_MARKER;
            img.setLayoutParams(lp);
        }
        img.setImageBitmap(getLoacalBitmap((String) ((HashMap) this.listItem.get(position)).get(AllAdvertisement.ICON)));
        return convertView;
    }

    public static Bitmap getLoacalBitmap(String url) {
        try {
            InputStream fis = new FileInputStream(url);
            Bitmap btmap = BitmapFactory.decodeStream(fis);
            IOUtils.close(fis);
            return btmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
