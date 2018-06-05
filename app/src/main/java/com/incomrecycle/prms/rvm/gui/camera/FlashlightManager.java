package com.incomrecycle.prms.rvm.gui.camera;

import android.os.IBinder;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FlashlightManager {
    private static final String TAG = FlashlightManager.class.getSimpleName();
    private static final Object iHardwareService = getHardwareService();
    private static final Method setFlashEnabledMethod = getSetFlashEnabledMethod(iHardwareService);

    static {
        if (iHardwareService == null) {
            Log.v(TAG, "This device does supports control of a flashlight");
        } else {
            Log.v(TAG, "This device does not support control of a flashlight");
        }
    }

    private FlashlightManager() {
    }

    static void enableFlashlight() {
        setFlashlight(true);
    }

    static void disableFlashlight() {
        setFlashlight(false);
    }

    private static Object getHardwareService() {
        final Class<?> maybeForName = maybeForName("android.os.ServiceManager");
        if (maybeForName != null) {
            final Method maybeGetMethod = maybeGetMethod(maybeForName, "getService", String.class);
            if (maybeGetMethod != null) {
                final Object invoke = invoke(maybeGetMethod, null, "hardware");
                if (invoke != null) {
                    final Class<?> maybeForName2 = maybeForName("android.os.IHardwareService$Stub");
                    if (maybeForName2 != null) {
                        final Method maybeGetMethod2 = maybeGetMethod(maybeForName2, "asInterface", IBinder.class);
                        if (maybeGetMethod2 != null) {
                            return invoke(maybeGetMethod2, null, invoke);
                        }
                    }
                }
            }
        }
        return null;
//        Class<?> serviceManagerClass = maybeForName("android.os.ServiceManager");
//        if (serviceManagerClass == null) {
//            return null;
//        }
//        Method getServiceMethod = maybeGetMethod(serviceManagerClass, "getService", String.class);
//        if (getServiceMethod == null || invoke(getServiceMethod, null, "hardware") == null) {
//            return null;
//        }
//        Class<?> iHardwareServiceStubClass = maybeForName("android.os.IHardwareService$Stub");
//        if (iHardwareServiceStubClass == null) {
//            return null;
//        }
//        Method asInterfaceMethod = maybeGetMethod(iHardwareServiceStubClass, "asInterface", IBinder.class);
//        if (asInterfaceMethod == null) {
//            return null;
//        }
//        return invoke(asInterfaceMethod, null, hardwareService);
    }

    private static Method getSetFlashEnabledMethod(Object iHardwareService) {
        if (iHardwareService == null) {
            return null;
        }
        return maybeGetMethod(iHardwareService.getClass(), "setFlashlightEnabled", Boolean.TYPE);
    }

    private static Class<?> maybeForName(String name) {
        Class<?> cls = null;
        try {
            cls = Class.forName(name);
        } catch (ClassNotFoundException e) {
        } catch (RuntimeException re) {
            Log.w(TAG, "Unexpected error while finding class " + name, re);
        }
        return cls;
    }

    private static Method maybeGetMethod(Class<?> clazz, String name, Class<?>... argClasses) {
        Method method = null;
        try {
            method = clazz.getMethod(name, argClasses);
        } catch (NoSuchMethodException e) {
        } catch (RuntimeException re) {
            Log.w(TAG, "Unexpected error while finding method " + name, re);
        }
        return method;
    }

    private static Object invoke(Method method, Object instance, Object... args) {
        Object obj = null;
        try {
            obj = method.invoke(instance, args);
        } catch (IllegalAccessException e) {
            Log.w(TAG, "Unexpected error while invoking " + method, e);
        } catch (InvocationTargetException e2) {
            Log.w(TAG, "Unexpected error while invoking " + method, e2.getCause());
        } catch (RuntimeException re) {
            Log.w(TAG, "Unexpected error while invoking " + method, re);
        }
        return obj;
    }

    private static void setFlashlight(boolean active) {
        if (iHardwareService != null) {
            invoke(setFlashEnabledMethod, iHardwareService, Boolean.valueOf(active));
        }
    }
}
