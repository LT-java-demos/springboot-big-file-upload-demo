package com.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class FileController {
    //模拟存储文件md5唯一标识，实际应该保存到数据库或者缓存中
    private static Map<String, Object> map = new ConcurrentHashMap<>();

    @Value("${file.upload}")
    private String fileUploadPath;

    @GetMapping("/")
    public String page() {
        return "upload";
    }

    /**
     * 检查文件是否存在，如果存在则实现秒传
     *
     * @param md5File 文件md5唯一标识
     * @return
     */
    @PostMapping("checkFile")
    @ResponseBody
    public Boolean checkFile(@RequestParam(value = "md5File") String md5File) {
        boolean exist = false;
        if (map.get(md5File) != null) {
            return true;
        }
        return exist;
    }

    /**
     * 检查分片存不存在
     *
     * @param md5File 文件md5唯一标识
     * @param chunk   分片
     * @return
     */
    @PostMapping("checkChunk")
    @ResponseBody
    public Boolean checkChunk(@RequestParam(value = "md5File") String md5File,
                              @RequestParam(value = "chunk") Integer chunk) {
        boolean exist = false;
        //分片存放目录
        String path = fileUploadPath + md5File + "/";
        //分片名
        String chunkName = chunk + ".tmp";
        File file = new File(path + chunkName);
        if (file.exists()) {
            exist = true;
        }
        return exist;
    }

    /**
     * 上传
     *
     * @param file
     * @param md5File 文件md5唯一标识
     * @param chunk   第几片，从0开始
     * @return
     */
    @PostMapping("upload")
    @ResponseBody
    public Boolean upload(@RequestParam(value = "file") MultipartFile file,
                          @RequestParam(value = "md5File") String md5File,
                          @RequestParam(value = "chunk", required = false) Integer chunk) {
        String path = fileUploadPath + md5File + "/";
        File dirfile = new File(path);
        //目录不存在，创建目录
        if (!dirfile.exists()) {
            dirfile.mkdirs();
        }
        String chunkName;
        //表示是小文件，还没有一片
        if (chunk == null) {
            chunkName = "0.tmp";
        } else {
            chunkName = chunk + ".tmp";
        }
        String filePath = path + chunkName;
        File savefile = new File(filePath);

        try {
            if (!savefile.exists()) {
                //文件不存在，则创建
                savefile.createNewFile();
            }
            //将文件保存
            file.transferTo(savefile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 合成分片
     *
     * @param chunks 分片数量
     * @param md5File 文件md5唯一标识
     * @param name 文件名
     * @return
     * @throws Exception
     */
    @PostMapping("merge")
    @ResponseBody
    public Boolean merge(@RequestParam(value = "chunks", required = false) Integer chunks,
                         @RequestParam(value = "md5File") String md5File,
                         @RequestParam(value = "name") String name) throws Exception {
        String path = fileUploadPath;
        //合成后的文件
        FileOutputStream fileOutputStream = new FileOutputStream(path + "/" + name);
        try {
            byte[] buf = new byte[1024];
            for (long i = 0; i < chunks; i++) {
                String chunkFile = i + ".tmp";
                File file = new File(path + md5File + "/" + chunkFile);
                InputStream inputStream = new FileInputStream(file);
                int len = 0;
                while ((len = inputStream.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, len);
                }
                inputStream.close();

            }
            map.put(md5File, name);
            //删除md5目录，及临时文件
//            File file = new File(path + md5File);
//            for (File f : file.listFiles()) {
//                f.delete();
//            }
//            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            fileOutputStream.close();
        }
        return true;
    }
}
