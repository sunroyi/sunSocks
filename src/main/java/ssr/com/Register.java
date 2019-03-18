package ssr.com;

import com.ice.jni.registry.RegDWordValue;
import com.ice.jni.registry.RegStringValue;
import com.ice.jni.registry.Registry;
import com.ice.jni.registry.RegistryException;
import com.ice.jni.registry.RegistryKey;
import ssr.Client;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


public class Register {

    // 把ICE_JNIRegistry.dll在的路径加载到java.library.path中，这里是放在classpath下面了
    static {
        // ①编译成Jar包后的DLL路径设置
        String basePath = new Register().getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            basePath = URLDecoder.decode(basePath,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(basePath.endsWith(".jar")){
            basePath = basePath.substring(0,basePath.lastIndexOf("/")+1);
        }
        File f = new File(basePath);
        basePath = f.getAbsolutePath();  //得到windows下的正确路径
        System.setProperty("java.library.path", basePath+"/");

        // ②本机调试用
        //System.setProperty("java.library.path", Register.class.getResource("/").getPath());
    }

    public String getValue(String folder, String subKeyNode, String subKeyName)
            throws SecurityException,
            IllegalArgumentException,
            RegistryException {
        RegistryKey software = Registry.HKEY_CURRENT_USER.openSubKey(folder);
        RegistryKey subKey = software.openSubKey(subKeyNode);
        String value = subKey.getStringValue(subKeyName);
        subKey.closeKey();
        return value;
    }

    public int getIntValue(String folder, String subKeyNode, String subKeyName)
            throws SecurityException,
            IllegalArgumentException,
            RegistryException {
        RegistryKey software = Registry.HKEY_CURRENT_USER.openSubKey(folder);
        RegistryKey subKey = software.openSubKey(subKeyNode);
        int value = ((RegDWordValue) subKey.getValue(subKeyName)).getData();
        subKey.closeKey();
        return value;
    }

    public boolean setIntValue(String folder, String subKeyNode,
                               String subKeyName, int subKeyValue) throws RegistryException {
        RegistryKey software = Registry.HKEY_CURRENT_USER.openSubKey(folder);
        RegistryKey subKey = software.createSubKey(subKeyNode, "");
        RegDWordValue value = new RegDWordValue(subKey, subKeyName);
        value.setData(subKeyValue);
        subKey.setValue(value);
        subKey.flushKey();
        subKey.closeKey();
        return true;
    }

    public boolean setValue(String folder, String subKeyNode,
                            String subKeyName, String subKeyValue) throws RegistryException {
        RegistryKey software = Registry.HKEY_CURRENT_USER.openSubKey(folder);
        RegistryKey subKey = software.createSubKey(subKeyNode, "");
        subKey.setValue(new RegStringValue(subKey, subKeyName, subKeyValue));
        subKey.flushKey();
        subKey.closeKey();
        return true;
    }
}
