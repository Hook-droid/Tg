package org.telegram.messenger;

import java.util.ArrayList;

public class LyrxFakeDevices {

    public static class Device {
        public final String label;
        public final String deviceModel;
        public final String systemVersion;
        public final String appVersion;

        Device(String label, String deviceModel, String systemVersion, String appVersion) {
            this.label = label;
            this.deviceModel = deviceModel;
            this.systemVersion = systemVersion;
            this.appVersion = appVersion;
        }
    }

    public static class Brand {
        public final String name;
        public final ArrayList<Device> devices = new ArrayList<>();

        Brand(String name) {
            this.name = name;
        }

        Brand add(String label, String model, String system, String app) {
            devices.add(new Device(label, model, system, app));
            return this;
        }
    }

    private static ArrayList<Brand> brands;

    public static ArrayList<Brand> getBrands() {
        if (brands != null) {
            return brands;
        }
        brands = new ArrayList<>();

        Brand apple = new Brand("iPhone");
        apple.add("iPhone 16 Pro Max", "iPhone 16 Pro Max", "iOS 18.5", "11.13.1 (26934)");
        apple.add("iPhone 16 Pro", "iPhone 16 Pro", "iOS 18.5", "11.13.1 (26934)");
        apple.add("iPhone 16", "iPhone 16", "iOS 18.4", "11.12.0 (26701)");
        apple.add("iPhone 15 Pro Max", "iPhone 15 Pro Max", "iOS 18.3", "11.11.0 (26502)");
        apple.add("iPhone 15", "iPhone 15", "iOS 17.6", "11.9.0 (26011)");
        apple.add("iPhone 14 Pro", "iPhone 14 Pro", "iOS 17.5", "11.8.0 (25810)");
        apple.add("iPhone 13", "iPhone 13", "iOS 17.2", "11.6.0 (25430)");
        apple.add("iPhone SE (3rd gen)", "iPhone SE 3", "iOS 17.1", "11.5.0 (25220)");
        brands.add(apple);

        Brand samsung = new Brand("Samsung");
        samsung.add("Galaxy S25 Ultra", "Samsung SM-S938B", "SDK 35", "12.9.2 (69919)");
        samsung.add("Galaxy S25", "Samsung SM-S931B", "SDK 35", "12.9.2 (69919)");
        samsung.add("Galaxy S24 Ultra", "Samsung SM-S928B", "SDK 34", "12.8.0 (68420)");
        samsung.add("Galaxy S24", "Samsung SM-S921B", "SDK 34", "12.8.0 (68420)");
        samsung.add("Galaxy S23 Ultra", "Samsung SM-S918B", "SDK 34", "12.6.1 (67210)");
        samsung.add("Galaxy Z Fold 6", "Samsung SM-F956B", "SDK 34", "12.8.0 (68420)");
        samsung.add("Galaxy Z Flip 6", "Samsung SM-F741B", "SDK 34", "12.8.0 (68420)");
        samsung.add("Galaxy A55", "Samsung SM-A556B", "SDK 34", "12.7.0 (67900)");
        brands.add(samsung);

        Brand xiaomi = new Brand("Xiaomi");
        xiaomi.add("Xiaomi 15 Ultra", "Xiaomi 25010PN30G", "SDK 35", "12.9.2 (69919)");
        xiaomi.add("Xiaomi 14 Pro", "Xiaomi 23116PN5BC", "SDK 34", "12.8.0 (68420)");
        xiaomi.add("Redmi Note 14 Pro", "Xiaomi 24094RAD4G", "SDK 34", "12.8.0 (68420)");
        xiaomi.add("Redmi Note 13 Pro", "Xiaomi 2312DRA50G", "SDK 34", "12.7.0 (67900)");
        xiaomi.add("POCO F6 Pro", "Xiaomi 23113RKC6G", "SDK 34", "12.8.0 (68420)");
        brands.add(xiaomi);

        Brand google = new Brand("Google Pixel");
        google.add("Pixel 9 Pro XL", "Google Pixel 9 Pro XL", "SDK 35", "12.9.2 (69919)");
        google.add("Pixel 9", "Google Pixel 9", "SDK 35", "12.9.2 (69919)");
        google.add("Pixel 8 Pro", "Google Pixel 8 Pro", "SDK 34", "12.8.0 (68420)");
        google.add("Pixel 7a", "Google Pixel 7a", "SDK 34", "12.7.0 (67900)");
        brands.add(google);

        Brand huawei = new Brand("Huawei");
        huawei.add("Mate 60 Pro", "HUAWEI ALN-AL00", "SDK 33", "12.5.0 (66300)");
        huawei.add("P60 Pro", "HUAWEI MNA-AL00", "SDK 33", "12.5.0 (66300)");
        huawei.add("Nova 12", "HUAWEI BLK-AL00", "SDK 33", "12.5.0 (66300)");
        brands.add(huawei);

        Brand oneplus = new Brand("OnePlus");
        oneplus.add("OnePlus 13", "OnePlus PJZ110", "SDK 35", "12.9.2 (69919)");
        oneplus.add("OnePlus 12", "OnePlus CPH2581", "SDK 34", "12.8.0 (68420)");
        oneplus.add("OnePlus Nord 4", "OnePlus CPH2661", "SDK 34", "12.7.0 (67900)");
        brands.add(oneplus);

        Brand nokia = new Brand("Nokia");
        nokia.add("Nokia XR21", "Nokia XR21", "SDK 33", "12.4.0 (65100)");
        nokia.add("Nokia G42", "Nokia G42 5G", "SDK 33", "12.4.0 (65100)");
        nokia.add("Nokia 3310 (2017)", "Nokia 3310", "SDK 19", "8.0.0 (18000)");
        brands.add(nokia);

        Brand desktop = new Brand("Desktop");
        desktop.add("MacBook Pro", "MacBook Pro", "macOS 15.3", "11.6.2 (263000)");
        desktop.add("Windows PC", "PC 64bit", "Windows 11", "5.10.2 x64");
        desktop.add("Linux", "PC 64bit", "Ubuntu 24.04", "5.10.2 x64");
        brands.add(desktop);

        return brands;
    }

    public static String currentLabel() {
        String saved = SharedConfig.lyrxFakeDeviceLabel;
        if (saved == null || saved.length() == 0) {
            return null;
        }
        return saved;
    }

    public static void select(Device device) {
        if (device == null) {
            SharedConfig.lyrxFakeDeviceLabel = "";
            SharedConfig.lyrxFakeDeviceModel = "";
            SharedConfig.lyrxFakeDeviceSystem = "";
            SharedConfig.lyrxFakeDeviceApp = "";
        } else {
            SharedConfig.lyrxFakeDeviceLabel = device.label;
            SharedConfig.lyrxFakeDeviceModel = device.deviceModel;
            SharedConfig.lyrxFakeDeviceSystem = device.systemVersion;
            SharedConfig.lyrxFakeDeviceApp = device.appVersion;
        }
        MessagesController.getGlobalMainSettings().edit()
                .putString("lyrxFakeDeviceLabel", SharedConfig.lyrxFakeDeviceLabel)
                .putString("lyrxFakeDeviceModel", SharedConfig.lyrxFakeDeviceModel)
                .putString("lyrxFakeDeviceSystem", SharedConfig.lyrxFakeDeviceSystem)
                .putString("lyrxFakeDeviceApp", SharedConfig.lyrxFakeDeviceApp)
                .commit();
    }

    public static String getFakeModel() {
        try {
            android.content.SharedPreferences p = ApplicationLoader.applicationContext
                    .getSharedPreferences("mainconfig", android.content.Context.MODE_PRIVATE);
            return p.getString("lyrxFakeDeviceModel", "");
        } catch (Throwable e) {
            return "";
        }
    }

    public static String getFakeSystem() {
        try {
            android.content.SharedPreferences p = ApplicationLoader.applicationContext
                    .getSharedPreferences("mainconfig", android.content.Context.MODE_PRIVATE);
            return p.getString("lyrxFakeDeviceSystem", "");
        } catch (Throwable e) {
            return "";
        }
    }

    public static String getFakeApp() {
        try {
            android.content.SharedPreferences p = ApplicationLoader.applicationContext
                    .getSharedPreferences("mainconfig", android.content.Context.MODE_PRIVATE);
            return p.getString("lyrxFakeDeviceApp", "");
        } catch (Throwable e) {
            return "";
        }
    }

    public static void restartApp(android.app.Activity activity) {
        try {
            if (activity == null) {
                return;
            }
            android.content.pm.PackageManager pm = activity.getPackageManager();
            android.content.Intent intent = pm.getLaunchIntentForPackage(activity.getPackageName());
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            activity.finishAffinity();
            activity.startActivity(intent);
        } catch (Throwable ignore) {
        }
        System.exit(0);
    }
}
