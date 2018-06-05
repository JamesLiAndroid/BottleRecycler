package android_serialport_api;

import android.util.Log;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

public class SerialPortFinder {
    private static final String TAG = "SerialPort";
    private Vector<Driver> mDrivers = null;

    public class Driver {
        private String mDeviceRoot;
        Vector<File> mDevices = null;
        private String mDriverName;

        public Driver(String name, String root) {
            this.mDriverName = name;
            this.mDeviceRoot = root;
        }

        public Vector<File> getDevices() {
            if (this.mDevices == null) {
                this.mDevices = new Vector();
                File[] files = new File("/dev").listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getAbsolutePath().startsWith(this.mDeviceRoot)) {
                        Log.d(SerialPortFinder.TAG, "Found new device: " + files[i]);
                        this.mDevices.add(files[i]);
                    }
                }
            }
            return this.mDevices;
        }

        public String getName() {
            return this.mDriverName;
        }
    }

    Vector<Driver> getDrivers() throws IOException {
        if (this.mDrivers == null) {
            this.mDrivers = new Vector();
            LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));
            while (true) {
                String l = r.readLine();
                if (l == null) {
                    break;
                }
                String drivername = l.substring(0, 21).trim();
                String[] w = l.split(" +");
                if (w.length >= 5 && w[w.length - 1].equals("serial")) {
                    Log.d(TAG, "Found new driver " + drivername + " on " + w[w.length - 4]);
                    this.mDrivers.add(new Driver(drivername, w[w.length - 4]));
                }
            }
            r.close();
        }
        return this.mDrivers;
    }

    public String[] getAllDevices() {
        Vector<String> devices = new Vector<>();
        try {
            Iterator<Driver> itdriv = getDrivers().iterator();
            while (itdriv.hasNext()) {
                Driver driver = itdriv.next();
                Iterator<File> itdev = ((Driver) itdriv.next()).getDevices().iterator();
                while (itdev.hasNext()) {
                    String device = ((File) itdev.next()).getName();
                    devices.add(String.format("%s (%s)", new Object[]{device, driver.getName()}));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (String[]) devices.toArray(new String[devices.size()]);
    }

    public String[] getAllDevicesPath() {
        Vector<String> devices = new Vector();
        try {
            Iterator<Driver> itdriv = getDrivers().iterator();
            while (itdriv.hasNext()) {
                Iterator<File> itdev = ((Driver) itdriv.next()).getDevices().iterator();
                while (itdev.hasNext()) {
                    devices.add(((File) itdev.next()).getAbsolutePath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (String[]) devices.toArray(new String[devices.size()]);
    }
}