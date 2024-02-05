package com.example.demo.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.demo.common.R;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/project")
public class projectController {

    private String parentFolderPath = "/home/wuqingfu"; // 父文件夹路径 D:\111  /home/wuqingfu
    private String folderName = "user001-workdir"; // 要创建的文件夹名称
    private String yamlName = "example.yaml";
    private String pythonName = "skypilot_test.py";
    private String HOST = "192.168.96.177";
    private Integer PORT = 12345;


    @PutMapping("/mkdir")
    public R<String> createFolder(HttpSession session) {
        File parentFolder = new File(parentFolderPath);
        boolean result;
        if (!parentFolder.exists()) {
            result = parentFolder.mkdir();
            if (!result) {
                return R.error("无法创建父文件夹");
            }
        }
        if (parentFolder.exists() && parentFolder.isDirectory()) {
            File newFolder = new File(parentFolder, folderName);
            if (newFolder.mkdir()) {
                try {
                    File yamlFile = new File(newFolder, yamlName);
                    FileWriter writer = new FileWriter(yamlFile);
                    writer.write("#我是yaml\n");
                    writer.write("resources:\n" +
                            "  # Optional; if left out, automatically pick the cheapest cloud.\n" +
                            "  cloud: kubernetes\n" +
                            "  # 1x NVIDIA V100 GPU\n");
                    writer.write("workdir: .\n");
                    writer.write("setup: |\n" +
                            "  echo \"Running setup.\"\n");
                    writer.write("run: |\n" +
                            "  echo \"Hello, SkyPilot!\"\n" +
                            "  conda env list\n");
                    writer.close();
                    return R.success("创建成功并写入 YAML 文件");
                } catch (IOException e) {
                    e.printStackTrace();
                    return R.error("写入 YAML 文件时发生错误");
                }
            } else {
                return R.error("创建失败");
            }
        } else {
            return R.error("父文件夹不存在或不是一个有效的文件夹");
        }
    }

    @PostMapping("/upload")
    public R<String> upload(MultipartFile[] files) {
        File usrFolder = new File(parentFolderPath, folderName);
        if (usrFolder.exists()) {
            for (MultipartFile file : files) {
                String filename = StringUtils.cleanPath(file.getOriginalFilename());
                try {
                    File newFile = new File(usrFolder, filename);
                    file.transferTo(newFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    return R.error("上传文件失败");
                }
            }
            return R.success("上传成功");
        } else {
            return R.error("上传失败");
        }
    }

    @PutMapping("/runTask")
    public R<String> runCMD(HttpSession session) {
        String content = parentFolderPath + "/" + folderName + "/" + yamlName;
        Logger log = Logger.getLogger(this.getClass().getName());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("yamlAddr", content);
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
        } catch (IOException e) {
            e.printStackTrace();
            return R.success("提交失败！");
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
        return R.success("提交成功！");
    }

    //    linux python
//    @PutMapping("/runTask")
//    public R<String> runCMD(HttpSession session) {
//        try {
//            String filepath = parentFolderPath + "/" + folderName + "/" + pythonName;
//            ProcessBuilder processBuilder = new ProcessBuilder("/home/wuqingfu/anaconda3/envs/sky/bin/python", filepath);
//            processBuilder.redirectErrorStream(true);
//            Process process = processBuilder.start();
//            process.waitFor();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return R.success("执行成功");
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            return R.error("执行失败");
//        }
//    }

//    linux bash
//    @PutMapping("/runTask")
//    public R<String> runCMD(HttpSession session) {
//
//        String activateConda = "conda activate sky";
//        // 构建命令 "cd user001-workdir"
//        String changeDirCommand = "cd " + parentFolderPath + "/" + folderName;
//
//        // 构建命令 "sky launch -c mycluster hello_sky.yaml"
//        String launchCommand = "sky launch -c mycluster " + yamlName;
//
//        try {
//            // 执行 "cd user001-workdir" 命令
//            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", activateConda);
//            processBuilder.redirectErrorStream(true);
//            Process process = processBuilder.start();
//            process.waitFor();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            // 执行 "sky launch -c mycluster hello_sky.yaml" 命令
//            processBuilder = new ProcessBuilder("bash", "-c", changeDirCommand);
//            processBuilder.redirectErrorStream(true);
//            process = processBuilder.start();
//            process.waitFor();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            // 执行 "sky launch -c mycluster hello_sky.yaml" 命令
//            processBuilder = new ProcessBuilder("bash", "-c", launchCommand);
//            processBuilder.redirectErrorStream(true);
//            process = processBuilder.start();
//            process.waitFor();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            // 读取命令执行结果
////            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
////            StringBuilder output = new StringBuilder();
////            String line;
////            while ((line = reader.readLine()) != null) {
////                output.append(line).append("\n");
////            }
////            reader.close();
//
//            int exitCode = process.waitFor();
//            if (exitCode == 0) {
//                return R.success("命令执行成功\n");
////                return R.success("命令执行成功\n" + output.toString());
//            } else {
//                return R.error("命令执行失败\n");
////                return R.error("命令执行失败\n" + output.toString());
//            }
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            return R.error("命令执行时发生错误");
//        }
//    }

//    windows cmd
//    @PutMapping("/runTask")
//    public R<String> runCMD(HttpSession session) {
//        String changeDirCommand0 = "dir";
//
//        String changeDirCommand1 = "cd " + parentFolderPath + "/" + folderName;
//
//        // 构建命令 "sky launch -c mycluster hello_sky.yaml"
//        String changeDirCommand2 = "dir";
//
//        try {
//            // 执行 "cd user001-workdir" 命令
//            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", changeDirCommand0);
//            processBuilder.redirectErrorStream(true);
//            Process process = processBuilder.start();
//            process.waitFor();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            // 执行 "sky launch -c mycluster hello_sky.yaml" 命令
//            processBuilder = new ProcessBuilder("cmd.exe", "/c", changeDirCommand1);
//            processBuilder.redirectErrorStream(true);
//            process = processBuilder.start();
//            process.waitFor();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            processBuilder = new ProcessBuilder("cmd.exe", "/c", changeDirCommand2);
//            processBuilder.redirectErrorStream(true);
//            process = processBuilder.start();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            // 读取命令执行结果
////            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
////            StringBuilder output = new StringBuilder();
////            String line;
////            while ((line = reader.readLine()) != null) {
////                output.append(line).append("\n");
////            }
////            reader.close();
//
//            int exitCode = process.waitFor();
//            if (exitCode == 0) {
//                return R.success("命令执行成功\n");
//                //                return R.success("命令执行成功\n" + output.toString());
//            } else {
//                return R.error("命令执行失败\n");
////                return R.error("命令执行失败\n" + output.toString());
//            }
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            return R.error("命令执行时发生错误");
//        }
//    }
}
