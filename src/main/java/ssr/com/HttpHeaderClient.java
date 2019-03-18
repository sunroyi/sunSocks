package ssr.com;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析头部信息
 *
 */
public final class HttpHeaderClient {

    private List<String> header=new ArrayList<String>();

    private String method;
    private String host;
    private String port;

    public static final int MAXLINESIZE = 4096;

    public static final String METHOD_GET="GET";
    public static final String METHOD_POST="POST";
    public static final String METHOD_CONNECT="CONNECT";

    private HttpHeaderClient(){}

    /**
     * 从数据流中读取请求头部信息，必须在放在流开启之后，任何数据读取之前
     * @param in
     * @return
     * @throws IOException
     */
    public static final HttpHeaderClient readHeader(InputStream in) throws IOException {
        HttpHeaderClient header = new HttpHeaderClient();
        StringBuilder sb = new StringBuilder();
        //先读出交互协议来，
        char c = 0;
        while ((c = (char) in.read()) != '\n') {
            sb.append(c);
            if (sb.length() == MAXLINESIZE) {//不接受过长的头部字段
                break;
            }
        }
        //如能识别出请求方式则则继续，不能则退出
        if(header.addHeaderMethod(sb.toString())!=null){
            do {
                sb = new StringBuilder();
                while ((c = (char) in.read()) != '\n') {
                    sb.append(c);
                    if (sb.length() == MAXLINESIZE) {//不接受过长的头部字段
                        break;
                    }
                }
                if (sb.length() > 1 && header.notTooLong()) {//如果头部包含信息过多，抛弃剩下的部分
                    header.addHeaderString(sb.substring(0, sb.length() - 1));
                } else {
                    break;
                }
            } while (true);
        }

        return header;
    }

    /**
     *
     * @param str
     */
    private void addHeaderString(String str){
        str=str.replaceAll("\r", "");
        header.add(str);
        if(str.startsWith("Host")){//解析主机和端口
            String[] hosts= str.split(":");
            host=hosts[1].trim();
            if(method.endsWith(METHOD_CONNECT)){
                port=hosts.length==3?hosts[2]:"443";//https默认端口为443
            }else if(method.endsWith(METHOD_GET)||method.endsWith(METHOD_POST)){
                port=hosts.length==3?hosts[2]:"80";//http默认端口为80
            }
            System.out.println("Header:" + str);
        }
    }

    /**
     * 判定请求方式
     * @param str
     * @return
     */
    private String addHeaderMethod(String str){
        str=str.replaceAll("\r", "");
        header.add(str);
        if(str.startsWith(METHOD_CONNECT)){//https链接请求代理
            method=METHOD_CONNECT;
        }else if(str.startsWith(METHOD_GET)){//http GET请求
            method=METHOD_GET;
        }else if(str.startsWith(METHOD_POST)){//http POST请求
            method=METHOD_POST;
        }
        return method;
    }


    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        for(String str : header){
            sb.append(str).append("\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }

    public boolean notTooLong(){
        return header.size()<=16;
    }


    public List<String> getHeader() {
        return header;
    }


    public void setHeader(List<String> header) {
        this.header = header;
    }


    public String getMethod() {
        return method;
    }


    public void setMethod(String method) {
        this.method = method;
    }

    public String getHost() {
        return host;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public String getPort() {
        return port;
    }


    public void setPort(String port) {
        this.port = port;
    }

}