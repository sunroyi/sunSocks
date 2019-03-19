/**
 * 
 */
/**
 * @author Administrator
 *
 */
package ssr;


import com.ice.jni.registry.RegistryException;
import ssr.com.*;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
1.创建一个ServerSocket对象；
2.调用ServerSocket对象的accept方法，等待连接，连接成功会返回一个Socket对象，否则一直阻塞等待；
3.从Socket对象中获取InputStream和OutputStream字节流，这两个流分别对应request请求和response响应；
4.处理请求：读取InputStream字节流信息，转成字符串形式，并解析，这里的解析比较简单，仅仅获取uri(统一资源标识符)信息;
5.处理响应：根据解析出来的uri信息，从WEB_ROOT目录中寻找请求的资源资源文件, 读取资源文件，并将其写入到OutputStream字节流中；
6.关闭Socket对象；
7.转到步骤2，继续等待连接请求；
*/

public class Client implements ActionListener{

    //Specify the look and feel to use.  Valid values:  s
    //null (use the default), "Metal", "System", "Motif", "GTK+"
    final static String LOOKANDFEEL = "System";
    JButton jbutton;
    JLabel lblServer;
    JLabel lblPort;
    JLabel lblPassword;
    JTextField txtServer;
    JTextField txtPort;
    JTextField txtPassword;
    Boolean blnStart = false;

    static final int workerNumber = 4;//线程池保留数量，服务器为8核cpu，合适的数量应该小于8

    static final int maxPoolSize=256;//最大线程数量，即最大并发量

    static final int maxWorkerInQueue = 2500;// 最大工作队列数量

    static final int waitTime = 10;// 超时等待时间

    static final int listenPort=8788;

    static final String listenIP="127.0.0.1";

    static final String foreignIP="*.*.*.*";

    static final ThreadPoolExecutor tpe = new ThreadPoolExecutor(workerNumber,
            maxPoolSize, waitTime, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(maxWorkerInQueue));

    public Client(){
        doShutDownWork();
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Client client = new Client();
                client.createAndShowGUI();
            }
        });
    }

    private void doShutDownWork() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Proxy proxy = new Proxy();
                try {
                    // 设置代理服务器
                    proxy.disableProxy();
                } catch (RegistryException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if (!blnStart)
        {
            jbutton.setText("Stop Proxy");
            blnStart = true;

            ThreadClient threadClient = new ThreadClient();
            threadClient.start();
        }else{
            // 退出
            System.exit(1);
        }
    }

    class ThreadClient extends Thread{
        @Override
        public void run() {
            try {
                // 设置代理服务器
                Proxy proxy = new Proxy();
                // IE代理服务器
                proxy.changeProxy(listenIP, listenPort);
            } catch (Exception ex) {
                System.out.println("PC Proxy Server Setting Error:" + ex.getMessage());
            }

            try {
                // 开启本地代理服务器
                Client client = new Client();

                // 等待连接请求
                client.await(txtServer.getText(), txtPort.getText());
            } catch (Exception ex) {
                System.out.println("Proxy Client Error:" + ex.getMessage());
            }
        }
    }

    private static void initLookAndFeel() {
        String lookAndFeel = null;

        if (LOOKANDFEEL != null) {
            if (LOOKANDFEEL.equals("Metal")) {
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("System")) {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("Motif")) {
                lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            } else if (LOOKANDFEEL.equals("GTK+")) { //new in 1.4.2
                lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            } else {
                System.err.println("Unexpected value of LOOKANDFEEL specified: "
                        + LOOKANDFEEL);
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }

            try {
                UIManager.setLookAndFeel(lookAndFeel);
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:"
                        + lookAndFeel);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel ("
                        + lookAndFeel
                        + ") on this platform.");
                System.err.println("Using the default look and feel.");
            } catch (Exception e) {
                System.err.println("Couldn't get specified look and feel ("
                        + lookAndFeel
                        + "), for some reason.");
                System.err.println("Using the default look and feel.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI() {
        //Set the look and feel.---设置外观，可以忽略
        initLookAndFeel();

        //Make sure we have nice window decorations.
        //设置为false的话，即为不改变外观
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //----------------------Pannel Components---------------------------
        Panel pn = new Panel(null);
        pn.setSize(800, 600);

        lblServer = new JLabel("Server");
        lblServer.setBounds(50, 30, 100, 30);
        pn.add(lblServer);

        txtServer = new JTextField(listenIP);
        txtServer.setBounds(150, 30, 100, 30);
        pn.add(txtServer);

        JLabel lblForeignIP = new JLabel(foreignIP);
        lblForeignIP.setBounds(300, 30, 100, 30);
        pn.add(lblForeignIP);

        lblPort = new JLabel("Port");
        lblPort.setBounds(50, 80, 100, 30);
        pn.add(lblPort);

        txtPort = new JTextField("8888");
        txtPort.setBounds(150, 80, 100, 30);
        pn.add(txtPort);

        lblPassword = new JLabel("Password");
        lblPassword.setBounds(50, 130, 100, 30);
        pn.add(lblPassword);

        txtPassword = new JTextField("sun");
        txtPassword.setBounds(150, 130, 100, 30);
        pn.add(txtPassword);

        jbutton = new JButton("Start Proxy");
        jbutton.setMnemonic(KeyEvent.VK_I);
        jbutton.addActionListener(this);
        jbutton.setBounds(100, 180, 200, 30);
        pn.add(jbutton);
        //----------------------Pannel Components---------------------------

        //Display the window.
        frame.add(pn);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(400, 280);
    }
    
    public void await(String serverIP, String serverPort) throws IOException {
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
                ClinetProxy cp = new ClinetProxy(socket, serverIP, serverPort);
                Thread t = new Thread(cp);
                t.start();

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
