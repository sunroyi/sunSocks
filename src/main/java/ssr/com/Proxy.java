package ssr.com;

import com.ice.jni.registry.NoSuchKeyException;
import com.ice.jni.registry.NoSuchValueException;
import com.ice.jni.registry.RegistryException;

public class Proxy {
    private static String folder = "SOFTWARE";
    private static String subKeyNode = "Microsoft\\Windows\\CurrentVersion\\Internet Settings";
    private static String subKeyNameServer = "ProxyServer";
    private static String subKeyNameEnable = "ProxyEnable";
    private static String subKeyNameOverride = "ProxyOverride";
    private static String subKeyOverrideValue = "<local>";

    private int originProxyEnable;
    private String originProxyServer;
    private String originProxyOverride;
    private Register register = new Register();

    public boolean backToOriginValue() {

        try {
            register.setIntValue(folder, subKeyNode, subKeyNameEnable,
                    originProxyEnable);
            register.setValue(folder, subKeyNode, subKeyNameServer,
                    originProxyServer);
            try {
                System.out.println("backed key: "
                        + register.getValue(folder, subKeyNode,
                        subKeyNameServer)
                        + " "
                        + register.getIntValue(folder, subKeyNode,
                        subKeyNameEnable));
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            register.setValue(folder, subKeyNode, subKeyNameOverride,
                    originProxyOverride);
        } catch (NoSuchKeyException e) {
            e.printStackTrace();
            return false;
        } catch (RegistryException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean changeProxy(String proxyIp, int proxyPort) {
        try {
            enableProxy();
            setProxy(proxyIp, proxyPort);
            try {
                System.out.println("after change key: "
                        + register.getValue(folder, subKeyNode,
                        subKeyNameServer)
                        + " "
                        + register.getIntValue(folder, subKeyNode,
                        subKeyNameEnable));
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            setOverride();
        } catch (NoSuchKeyException e) {
            e.printStackTrace();
            return false;
        } catch (RegistryException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean saveOriginValue() {
        try {
            originProxyServer = register.getValue(folder, subKeyNode,
                    subKeyNameServer);
            originProxyEnable = register.getIntValue(folder, subKeyNode,
                    subKeyNameEnable);
            System.out.println("save origin value: " + originProxyServer + " "
                    + originProxyEnable);
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchKeyException e) {
            e.printStackTrace();
            return false;
        } catch (RegistryException e) {
            e.printStackTrace();
            return false;
        }

        // 没有勾选跳过本地代理服务器时，没有proxyoverride，此时保存为“”，并且返回true
        try {
            originProxyOverride = register.getValue(folder, subKeyNode,
                    subKeyNameOverride);
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchKeyException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchValueException e) {
            originProxyOverride = "";
            return true;
        } catch (RegistryException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void enableProxy() throws NoSuchKeyException, RegistryException {
        register.setIntValue(folder, subKeyNode, subKeyNameEnable, 1);
    }

    public void disableProxy() throws NoSuchKeyException, RegistryException {
        register.setIntValue(folder, subKeyNode, subKeyNameEnable, 0);
    }

    private void setProxy(String ip, int port) throws NoSuchKeyException,
            RegistryException {
        register.setValue(folder, subKeyNode, subKeyNameServer, ip + ":" + port);
    }

    private void setOverride() throws NoSuchKeyException, RegistryException {
        register.setValue(folder, subKeyNode, subKeyNameOverride,
                subKeyOverrideValue);
    }

    public void setRegister(Register register) {
        this.register = register;
    }

}  