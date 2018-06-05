package com.incomrecycle.prms.rvm.gui.camera;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.zxing.LuminanceSource;
import com.google.zxing.PlanarYUVLuminanceSource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class CameraManager {
    private static final int CAPTURE_MODE_PICTURE = 1;
    private static final int CAPTURE_MODE_PREVIEW = 0;
    private static int globalMgrSeq = 0;
    private static final List<CameraManager> listCameraManager = new ArrayList();
    private static final Logger logger = LoggerFactory.getLogger("CameraManager");
    private Camera camera = null;
    private CameraEventCallback cameraEventCallback = null;
    private int cameraIdx = -1;
    private int captureMode = 0;
    private List<CameraData> listCameraData = new ArrayList();
    private int mgrSeq;
    private PictureCallback pictureCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.startPreview();
            if (!CameraManager.this.pictureTaking) {
                CameraManager.this.pictureTaking = true;
                CameraData cameraData = new CameraData();
                cameraData.data = new byte[data.length];
                System.arraycopy(data, 0, cameraData.data, 0, data.length);
                Parameters parameters = camera.getParameters();
                cameraData.width = parameters.getPreviewSize().width;
                cameraData.height = parameters.getPreviewSize().height;
                cameraData.format = parameters.getPreviewFormat();
                cameraData.mode = 1;
                synchronized (CameraManager.this.listCameraData) {
                    CameraManager.this.pictureTaking = false;
                    CameraManager.this.listCameraData.add(cameraData);
                    CameraManager.this.listCameraData.notify();
                }
                if (CameraManager.this.cameraEventCallback != null) {
                    CameraManager.this.cameraEventCallback.onReceive(CameraManager.this.cameraIdx, cameraData);
                }
            }
        }
    };
    private boolean pictureEnable = true;
    private boolean pictureTaking = false;
    private PreviewCallback previewCallback = new PreviewCallback() {
        private byte[] data = null;

        public void onPreviewFrame(byte[] data, Camera camera) {
            if (!CameraManager.this.pictureTaking) {
                CameraManager.this.pictureTaking = true;
                CameraData cameraData = new CameraData();
                cameraData.data = new byte[data.length];
                System.arraycopy(data, 0, cameraData.data, 0, data.length);
                try {
                    Parameters parameters = camera.getParameters();
                    cameraData.width = parameters.getPreviewSize().width;
                    cameraData.height = parameters.getPreviewSize().height;
                    cameraData.format = parameters.getPreviewFormat();
                    cameraData.mode = 0;
                    synchronized (CameraManager.this.listCameraData) {
                        CameraManager.this.pictureTaking = false;
                        CameraManager.this.listCameraData.add(cameraData);
                        CameraManager.this.listCameraData.notify();
                    }
                    if (CameraManager.this.cameraEventCallback != null) {
                        CameraManager.this.cameraEventCallback.onReceive(CameraManager.this.cameraIdx, cameraData);
                    }
                } catch (Exception e) {
                }
            }
        }
    };
    private boolean previewing = false;

    public static class CameraData {
        byte[] data;
        int format;
        int height;
        int mode;
        int width;
    }

    public interface CameraEventCallback {
        void onClose(int i);

        void onOpen(int i);

        void onReceive(int i, CameraData cameraData);

        void onStartPreview(int i);

        void onStopPreview(int i);
    }

    private static class DebugCameraEventCallback implements CameraEventCallback {
        private CameraManager cameraManager;

        public DebugCameraEventCallback(CameraManager cameraManager) {
            this.cameraManager = cameraManager;
        }

        public void onOpen(int cameraIdx) {
            CameraManager.logger.debug("CAMERA:(" + this.cameraManager.mgrSeq + ")" + cameraIdx + ":OPEN");
        }

        public void onClose(int cameraIdx) {
            CameraManager.logger.debug("CAMERA:(" + this.cameraManager.mgrSeq + ")" + cameraIdx + ":CLOSE");
        }

        public void onStartPreview(int cameraIdx) {
            CameraManager.logger.debug("CAMERA:(" + this.cameraManager.mgrSeq + ")" + cameraIdx + ":STARTPREVIEW");
        }

        public void onReceive(int cameraIdx, CameraData cameraData) {
            CameraManager.logger.debug("CAMERA:(" + this.cameraManager.mgrSeq + ")" + cameraIdx + ":RECEIVE");
        }

        public void onStopPreview(int cameraIdx) {
            CameraManager.logger.debug("CAMERA:(" + this.cameraManager.mgrSeq + ")" + cameraIdx + ":STOPPREVIEW");
        }
    }

    public static void clearCameraManager() {
        List<CameraManager> list = new ArrayList();
        list.addAll(listCameraManager);
        for (int i = 0; i < list.size(); i++) {
            ((CameraManager) list.get(i)).closeDriver();
        }
    }

    public static boolean clearCameraManager(int iCameraIdx) {
        for (int i = 0; i < listCameraManager.size(); i++) {
            CameraManager cameraManager = (CameraManager) listCameraManager.get(i);
            if (iCameraIdx == cameraManager.cameraIdx) {
                cameraManager.closeDriver();
                return true;
            }
        }
        return false;
    }

    public CameraManager() {
        int i = globalMgrSeq;
        globalMgrSeq = i + 1;
        this.mgrSeq = i;
    }

    public void openDriver(int iCameraIdx, SurfaceHolder holder) throws IOException {
        if (this.camera == null) {
            boolean needReconnect = clearCameraManager(iCameraIdx);
            if (needReconnect) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    throw new IOException(e.getMessage());
                }
            }
            int cameraCount = Camera.getNumberOfCameras();
            if (iCameraIdx >= cameraCount) {
                throw new IOException("Camera[" + iCameraIdx + "] not found");
            }
            if (iCameraIdx < 0) {
                for (int i = 0; i < cameraCount; i++) {
                    Camera open = Camera.open();
                    this.camera = open;
                    if (open != null) {
                        break;
                    }
                }
            } else {
                this.camera = Camera.open(iCameraIdx);
            }
            if (this.camera == null) {
                throw new IOException();
            }
            if (needReconnect) {
                this.camera.reconnect();
            }
            this.cameraIdx = iCameraIdx;
            listCameraManager.add(this);
            if (this.cameraEventCallback != null) {
                this.cameraEventCallback.onOpen(iCameraIdx);
            }
            this.camera.setPreviewDisplay(holder);
            this.previewing = false;
            this.pictureEnable = true;
            this.pictureTaking = false;
        }
    }

    public boolean isOpen() {
        return this.camera != null;
    }

    public void closeDriver() {
        for (int i = 0; i < listCameraManager.size(); i++) {
            if (((CameraManager) listCameraManager.get(i)) == this) {
                listCameraManager.remove(i);
                break;
            }
        }
        if (this.camera != null) {
            this.camera.setPreviewCallback(null);
            this.camera.stopPreview();
            this.camera.release();
            this.previewing = false;
            this.camera = null;
            if (this.cameraEventCallback != null) {
                this.cameraEventCallback.onStopPreview(this.cameraIdx);
            }
            if (this.cameraEventCallback != null) {
                this.cameraEventCallback.onClose(this.cameraIdx);
            }
        }
    }

    public void startPreview() {
        if (this.camera != null && !this.previewing) {
            this.camera.startPreview();
            this.previewing = true;
            if (this.cameraEventCallback != null) {
                this.cameraEventCallback.onStartPreview(this.cameraIdx);
            }
        }
    }

    public void stopPreview() {
        if (this.camera != null && this.previewing) {
            this.camera.stopPreview();
            this.previewing = false;
            if (this.cameraEventCallback != null) {
                this.cameraEventCallback.onStopPreview(this.cameraIdx);
            }
        }
    }

    public boolean isPreviewing() {
        return this.camera != null && this.previewing;
    }

    public Bitmap takePicture() {
        Bitmap bitmap = null;
        if (this.camera != null && this.previewing && this.pictureEnable) {
            synchronized (this.listCameraData) {
                this.pictureEnable = false;
                try {
                    if (this.captureMode == 0) {
                        this.camera.setOneShotPreviewCallback(this.previewCallback);
                    }
                    if (this.captureMode == 1) {
                        this.camera.takePicture(null, null, this.pictureCallback);
                    }
                    try {
                        this.listCameraData.wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.pictureEnable = true;
                    if (this.listCameraData.size() == 0) {
                    } else {
                        CameraData cameraData = (CameraData) this.listCameraData.get(0);
                        this.listCameraData.remove(0);
                        if (cameraData.mode == 1) {
                            Options opts = new Options();
                            opts.inSampleSize = 2;
                            opts.inPreferredConfig = Config.ARGB_8888;
                            bitmap = BitmapFactory.decodeByteArray(cameraData.data, 0, cameraData.data.length, opts);
                        } else {
                            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(cameraData.data, cameraData.width, cameraData.height, 0, 0, cameraData.width, cameraData.height, false);
                            bitmap = toBitmap(source, source.renderThumbnail(), source.getThumbnailWidth(), source.getThumbnailHeight());
                        }
                    }
                } catch (Exception e2) {
                    this.pictureEnable = true;
                }
            }
        }
        return bitmap;
    }

    private Bitmap toBitmap(LuminanceSource source, int[] pixels, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public boolean takePictureAsFile(String filename) {
        boolean z = false;
        if (this.camera != null && this.previewing && this.pictureEnable) {
            synchronized (this.listCameraData) {
                this.pictureEnable = false;
                try {
                    if (this.captureMode == 0) {
                        this.camera.setOneShotPreviewCallback(this.previewCallback);
                    }
                    if (this.captureMode == 1) {
                        this.camera.takePicture(null, null, this.pictureCallback);
                    }
                    try {
                        this.listCameraData.wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.pictureEnable = true;
                    if (this.listCameraData.size() == 0) {
                    } else {
                        CameraData cameraData = (CameraData) this.listCameraData.get(0);
                        this.listCameraData.remove(0);
                        FileOutputStream fos = null;
                        z = false;
                        FileOutputStream fos2;
                        if (cameraData.mode == 1) {
                            try {
                                fos2 = new FileOutputStream(filename);
                                try {
                                    fos2.write(cameraData.data);
                                    z = true;
                                    fos = fos2;
                                } catch (Exception e2) {
                                    fos = fos2;
                                }
                            } catch (Exception e3) {
                            }
                        } else {
                            YuvImage image = new YuvImage(cameraData.data, cameraData.format, cameraData.width, cameraData.height, null);
                            try {
                                fos2 = new FileOutputStream(filename);
                                try {
                                    image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, fos2);
                                    z = true;
                                    fos = fos2;
                                } catch (Exception e4) {
                                    fos = fos2;
                                }
                            } catch (Exception e5) {
                            }
                        }
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (Exception e6) {
                            }
                        }
                    }
                } catch (Exception e7) {
                    this.pictureEnable = true;
                }
            }
        }
        return z;
    }

    public void setEventCallback(CameraEventCallback cameraEventCallback) {
        this.cameraEventCallback = cameraEventCallback;
    }
}
