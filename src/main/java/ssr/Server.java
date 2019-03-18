/**
 * 
 */
/**
 * @author Administrator
 *
 */
package ssr;


import ssr.com.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
1.创建一个ServerSocket对象；
2.调用ServerSocket对象的accept方法，等待连接，连接成功会返回一个Socket对象，否则一直阻塞等待；
3.从Socket对象中获取InputStream和OutputStream字节流，这两个流分别对应request请求和response响应；
4.处理请求：读取InputStream字节流信息，转成字符串形式，并解析，这里的解析比较简单，仅仅获取uri(统一资源标识符)信息;
5.处理响应：根据解析出来的uri信息，从WEB_ROOT目录中寻找请求的资源资源文件, 读取资源文件，并将其写入到OutputStream字节流中；
6.关闭Socket对象；
7.转到步骤2，继续等待连接请求；
*/

public class Server {

    static final int listenPort=8888;

    static final String listenIP="0.0.0.0";

    public static void main(String[] args) {

        // 开启客户端代理服务器
        Server server = new Server();

        // 等待连接请求
        server.await();
    }
    
    public void await() {
        // 创建一个ServerSocket对象
        ServerSocket serverSocket = null;

        try {
            //服务器套接字对象
            serverSocket = new ServerSocket(listenPort, 1, InetAddress.getByName(listenIP));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // 循环等待一个请求
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                socket.setKeepAlive(true);

                //加入任务列表，等待处理
                ServerProxy sp = new ServerProxy(socket);
                Thread t = new Thread(sp);
                t.start();

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}