package com.incomrecycle.prms.rvm.gui.activity.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.util.AttributeSet;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyGifView extends View {
    private static final int what_update = 1;
    private DrawableObj gifDrawableObj = null;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                MyGifView.this.invalidate();
            }
        }
    };
    private boolean hasDrawn = false;
    private String is_scale = null;
    private Movie movie = null;
    private long movieStart;

    private static class DrawableObj {
        Drawable drawable;
        Bitmap drawableBitmap;

        private DrawableObj() {
            this.drawable = null;
            this.drawableBitmap = null;
        }
    }

    public MyGifView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.is_scale = attributeSet.getAttributeValue(null, "is_scale");
        String filename = attributeSet.getAttributeValue(null, "filename");
        String str_res_id = attributeSet.getAttributeValue(null, "res_id");
        if (str_res_id != null) {
            str_res_id = str_res_id.trim();
            if (str_res_id.length() == 0) {
                str_res_id = null;
            }
        }
        int res_id = -1;
        if (str_res_id != null) {
            res_id = attributeSet.getAttributeResourceValue(null, "res_id", 0);
        }
        updateResource(res_id, filename);
    }

    private static byte[] read(InputStream is) {
        byte[] buff = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true) {
            try {
                int readLen = is.read(buff);
                if (readLen <= 0) {
                    break;
                }
                baos.write(buff, 0, readLen);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return baos.toByteArray();
    }

    private static byte[] readFile(String filename) {
        Throwable th;
        byte[] bArr = null;
        File file = new File(filename);
        if (file.isFile()) {
            InputStream is = null;
            try {
                InputStream is2 = new FileInputStream(file);
                if (is2 == null) {
                    close(is2);
                } else {
                    try {
                        bArr = read(is2);
                        close(is2);
                    } catch (Exception e) {
                        is = is2;
                        close(is);
                        return bArr;
                    } catch (Throwable th2) {
                        th = th2;
                        is = is2;
                        close(is);
                        throw th;
                    }
                }
            } catch (Exception e2) {
                close(is);
                return bArr;
            } catch (Throwable th3) {
                close(is);
            }
        }
        return bArr;
    }

    private static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    public void updateResource(int res_id, String filename) {
        if (filename != null) {
            filename = filename.trim();
            if (filename.length() == 0) {
                filename = null;
            }
        }
        Movie movie = null;
        DrawableObj drawableObj = null;
        if (filename != null && new File(filename).isFile()) {
            try {
                byte[] data = readFile(filename);
                movie = Movie.decodeByteArray(data, 0, data.length);
            } catch (Exception e) {
            }
            if (movie == null) {
                drawableObj = fitSizeImg(filename);
            }
        }
        if (movie == null && drawableObj == null && res_id != -1) {
            InputStream is = getResources().openRawResource(res_id);
            if (is != null) {
                try {
                    movie = Movie.decodeStream(is);
                } catch (Exception e2) {
                }
                if (movie == null) {
                    try {
                        drawableObj = fitSizeImg(is);
                    } catch (Exception e3) {
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e4) {
                    }
                }
            }
        }
        if (drawableObj != null || movie != null) {
            synchronized (this) {
                DrawableObj oldDrawableObj = this.gifDrawableObj;
                this.gifDrawableObj = drawableObj;
                if (!(oldDrawableObj == null || oldDrawableObj.drawableBitmap == null)) {
                    oldDrawableObj.drawableBitmap.recycle();
                }
                this.movie = movie;
            }
            if (this.hasDrawn) {
                Message msg = new Message();
                msg.what = 1;
                this.handler.sendMessage(msg);
            }
        }
    }

    public static DrawableObj fitSizeImg(InputStream is) {
        try {
            int resSize = is.available();
            Options opts = new Options();
            opts.inTempStorage = new byte[16384];
            opts.inPreferredConfig = Config.RGB_565;
            if (resSize < 20480) {
                opts.inSampleSize = 1;
            } else if (resSize < 51200) {
                opts.inSampleSize = 1;
            } else if (resSize < 102400) {
                opts.inSampleSize = 1;
            } else if (resSize < 307200) {
                opts.inSampleSize = 1;
            } else if (resSize < 614400) {
                opts.inSampleSize = 1;
            } else if (resSize < 819200) {
                opts.inSampleSize = 2;
            } else if (resSize < AccessibilityEventCompat.TYPE_TOUCH_INTERACTION_START) {
                opts.inSampleSize = 4;
            } else if (resSize < 1310720) {
                opts.inSampleSize = 6;
            } else {
                opts.inSampleSize = 8;
            }
            opts.inDither = false;
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            if (is != null) {
                DrawableObj drawableObj = new DrawableObj();
                drawableObj.drawableBitmap = BitmapFactory.decodeStream(is, null, opts);
                is.close();
                drawableObj.drawable = new BitmapDrawable(drawableObj.drawableBitmap);
                return drawableObj;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DrawableObj fitSizeImg(String path) {
        Exception e;
        if (path == null || path.length() < 1) {
            return null;
        }
        File file = new File(path);
        Options opts = new Options();
        opts.inTempStorage = new byte[16384];
        opts.inPreferredConfig = Config.RGB_565;
        if (file.length() < 20480) {
            opts.inSampleSize = 1;
        } else if (file.length() < 51200) {
            opts.inSampleSize = 1;
        } else if (file.length() < 102400) {
            opts.inSampleSize = 1;
        } else if (file.length() < 307200) {
            opts.inSampleSize = 1;
        } else if (file.length() < 614400) {
            opts.inSampleSize = 1;
        } else if (file.length() < 819200) {
            opts.inSampleSize = 2;
        } else if (file.length() < 1048576) {
            opts.inSampleSize = 4;
        } else if (file.length() < 1310720) {
            opts.inSampleSize = 6;
        } else {
            opts.inSampleSize = 8;
        }
        opts.inDither = false;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        try {
            FileInputStream fs = new FileInputStream(file);
            if (fs != null) {
                try {
                    DrawableObj drawableObj = new DrawableObj();
                    drawableObj.drawableBitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, opts);
                    fs.close();
                    drawableObj.drawable = new BitmapDrawable(drawableObj.drawableBitmap);
                    return drawableObj;
                } catch (Exception e2) {
                    e = e2;
                    FileInputStream fileInputStream = fs;
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            return null;
        }
    }

    protected void onDraw(Canvas canvas) {
        this.hasDrawn = true;
        synchronized (this) {
            if (this.movie != null) {
                long curTime = SystemClock.uptimeMillis();
                if (this.movieStart == 0) {
                    this.movieStart = curTime;
                }
                int duraction = this.movie.duration();
                Bitmap bitmap;
                Drawable drawable;
                if (duraction != 0) {
                    this.movie.setTime((int) ((curTime - this.movieStart) % ((long) duraction)));
                    if ("true".equalsIgnoreCase(this.is_scale)) {
                        bitmap = Bitmap.createBitmap(this.movie.width(), this.movie.height(), Config.ARGB_8888);
                        this.movie.draw(new Canvas(bitmap), 0.0f, 0.0f);
                        drawable = new BitmapDrawable(null, bitmap);
                        drawable.setBounds(0, 0, getWidth(), getHeight());
                        drawable.draw(canvas);
                        if (bitmap != null) {
                            bitmap.recycle();
                        }
                    } else {
                        this.movie.draw(canvas, 0.0f, 0.0f);
                    }
                    Message msg = new Message();
                    msg.what = 1;
                    this.handler.sendMessageDelayed(msg, 80);
                } else if ("true".equalsIgnoreCase(this.is_scale)) {
                    bitmap = Bitmap.createBitmap(this.movie.width(), this.movie.height(), Config.ARGB_8888);
                    this.movie.draw(new Canvas(bitmap), 0.0f, 0.0f);
                    drawable = new BitmapDrawable(null, bitmap);
                    drawable.setBounds(0, 0, getWidth(), getHeight());
                    drawable.draw(canvas);
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                } else {
                    this.movie.draw(canvas, 0.0f, 0.0f);
                }
            }
            if (this.gifDrawableObj != null) {
                if ("true".equalsIgnoreCase(this.is_scale)) {
                    this.gifDrawableObj.drawable.setBounds(0, 0, getWidth(), getHeight());
                } else {
                    this.gifDrawableObj.drawable.setBounds(0, 0, this.gifDrawableObj.drawable.getIntrinsicWidth(), this.gifDrawableObj.drawable.getIntrinsicHeight());
                }
                this.gifDrawableObj.drawable.draw(canvas);
            }
        }
        super.onDraw(canvas);
    }

    protected void onDetachedFromWindow() {
        if (this.gifDrawableObj != null) {
            this.gifDrawableObj.drawable.setCallback(null);
            if (this.gifDrawableObj.drawableBitmap != null) {
                this.gifDrawableObj.drawableBitmap.recycle();
                this.gifDrawableObj.drawableBitmap = null;
            }
            this.gifDrawableObj = null;
        }
        if (this.movie != null) {
            this.movie = null;
        }
        super.onDetachedFromWindow();
    }
}
