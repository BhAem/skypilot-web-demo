package com.example.demo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class PythonSocket {

    public PythonSocket() {

    }

    public Object remoteCall(String content) {
        String HOST = "192.168.96.1";
        Integer PORT = 12345;
        Logger log = Logger.getLogger(this.getClass().getName());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", content);
        String str = jsonObject.toJSONString();
        System.out.println("发送内容(JSONObject)：" + jsonObject);
        System.out.println("发送内容(String)：" + str);
        // 访问服务进程的套接字
        Socket socket = null;
        // List<Question> questions = new ArrayList<>();
        log.info("调用远程接口:host=>" + HOST + ",port=>" + PORT);
        try {
            // 初始化套接字，设置访问服务的主机和进程端口号，HOST是访问python进程的主机名称，可以是IP地址或者域名，PORT是python进程绑定的端口号
            socket = new Socket(HOST, PORT);
            // 获取输出流对象
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os);
            // 发送内容
            out.print(str);
            // 告诉服务进程，内容发送完毕，可以开始处理
            out.print("over");
            // 获取服务进程的输入流
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String tmp = null;
            StringBuilder sb = new StringBuilder();
            // 读取内容
            while ((tmp = br.readLine()) != null)
                sb.append(tmp).append('\n');
            // 解析结果
            System.out.println("接收数据：" + sb.toString());

            // 解析成数组
            // JSONArray res = JSON.parseArray(sb.toString());

            // 解析成对象
            JSONObject res = JSON.parseObject(sb.toString());
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("远程接口调用结束");
        }
        return null;
    }
}