package ssr.com;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 将客户端发送过来的数据转发给请求的服务器端，并将服务器返回的数据转发给客户端
 *
 */
public class ClinetProxy implements Runnable {
    private Socket socketIn;
    private Socket socketOut;

    private long totalUpload=0l;//总计上行比特数
    private long totalDownload=0l;//总计下行比特数

    private String serverIP;
    private String serverPort;

    public ClinetProxy(Socket socket, String serverIP, String serverPort) {
        this.socketIn = socket;

        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    /** 已连接到请求的服务器 */
    private static final String AUTHORED = "HTTP/1.1 200 Connection established\r\n\r\n";
    /** 内部错误 */
    private static final String SERVERERROR = "HTTP/1.1 500 Connection FAILED\r\n\r\n";

    @Override
    public void run() {

        StringBuilder builder=new StringBuilder();
        try {
            builder.append("\r\n").append("Request Time  :" + sdf.format(new Date()));

            InputStream isIn = socketIn.getInputStream();
            OutputStream osIn = socketIn.getOutputStream();

            // 设置外部服务器主机和端口
            socketOut = new Socket(serverIP, Integer.parseInt(serverPort));
            socketOut.setKeepAlive(true);
            InputStream isOut = socketOut.getInputStream();
            OutputStream osOut = socketOut.getOutputStream();

            //新开一个线程将返回的数据转发给客户端
            //由于涉及到TCP协议，必须采用异步的方式，同时收发数据，并不是等所有Input结束后再Output
            Thread stc = new ServerToClientDataSendThread(isOut, osIn);
            stc.start();

            // 对header加密
            HttpHeaderClient header = HttpHeaderClient.readHeader(isIn);
            byte[] headerData=header.toString().getBytes();
            osOut.write(codeUtils.enCode(headerData));
            osOut.flush();

            //读取客户端请求过来的数据转发给服务器
            Thread cts = new ClientToServerDataSendThread(isIn, osOut);
            cts.start();
            cts.join();

            //等待向客户端转发的线程结束
            stc.join();  // 将服务器返回的数据写回客户端，主线程等待ot结束再关闭。
        } catch (Exception e) {
            e.printStackTrace();
            if(!socketIn.isOutputShutdown()){
                //如果还可以返回错误状态的话，返回内部错误
                try {
                    socketIn.getOutputStream().write(SERVERERROR.getBytes());
                } catch (IOException e1) {}
            }
        } finally {
            try {
                if (socketIn != null) {
                    socketIn.close();
                }
            } catch (IOException e) {}
            if (socketOut != null) {
                try {
                    socketOut.close();
                } catch (IOException e) {}
            }
            //纪录上下行数据量和最后结束时间并打印
            builder.append("\r\n").append("Up    Bytes  :" + totalUpload);
            builder.append("\r\n").append("Down  Bytes  :" + totalDownload);
            builder.append("\r\n").append("Closed Time  :" + sdf.format(new Date()));
            builder.append("\r\n");
            logRequestMsg(builder.toString());
        }
    }

    /**
     * 避免多线程竞争把日志打串行了
     * @param msg
     */
    private synchronized void logRequestMsg(String msg){
        System.out.println(msg);
    }

    /**
     * 将客户端返回的数据转发给服务器端
     *
     */
    class ClientToServerDataSendThread extends Thread {
        private InputStream isIn;
        private OutputStream osOut;

        ClientToServerDataSendThread(InputStream isIn, OutputStream osOut) {
            this.isIn = isIn;
            this.osOut = osOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[4096];
            try {
                int len;
                while ((len = isIn.read(buffer)) != -1) {
                    if (len > 0) {
                        osOut.write(buffer, 0, len);
                    }
                    totalUpload+=len;
                    if (socketIn.isClosed() || socketOut.isClosed()) {
                        break;
                    }
                }
                osOut.flush();
                osOut.close();
            } catch (Exception e) {
                try {
                    socketOut.close();// 尝试关闭远程服务器连接，中断转发线程的读阻塞状态
                } catch (IOException e1) {
                    System.out.println(e.getMessage());
                }
                System.out.println(e.getMessage());
            }finally{
            }
        }
    }

    /**
     * 将服务器端返回的数据转发给客户端
     *
     */
    class ServerToClientDataSendThread extends Thread {
        private InputStream isOut;
        private OutputStream osIn;

        ServerToClientDataSendThread(InputStream isOut, OutputStream osIn) {
            this.isOut = isOut;
            this.osIn = osIn;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[4096];
            try {
                int len;
                while ((len = isOut.read(buffer)) != -1) {
                    if (len > 0) {
                        // logData(buffer, 0, len);
                        osIn.write(buffer, 0, len);
                        totalDownload+=len;
                    }
                    if (socketIn.isOutputShutdown() || socketOut.isClosed()) {
                        break;
                    }
                }
                osIn.flush();
                osIn.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

}