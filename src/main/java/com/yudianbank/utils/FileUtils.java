package com.yudianbank.utils;

import com.google.common.collect.Lists;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author yudian-it
 * @date 2017/11/13
 */
@Component
public class FileUtils {

    final String REDIS_FILE_PREVIEW_PDF_KEY = "converted-preview-pdf-file";

    @Autowired
    RedissonClient redissonClient;
    @Value("${file.dir}")
    String fileDir;

    @Value("${converted.file.charset}")
    String charset;

    /**
     * 已转换过的文件集合(redis缓存)
     * @return
     */
    public Map<String, String> listConvertedFiles() {
        RMapCache<String, String> convertedList = redissonClient.getMapCache(REDIS_FILE_PREVIEW_PDF_KEY);
        return convertedList;
    }

    /**
     * 已转换过的文件，根据文件名获取
     * @return
     */
    public String getConvertedFile(String key) {
        RMapCache<String, String> convertedList = redissonClient.getMapCache(REDIS_FILE_PREVIEW_PDF_KEY);
        return convertedList.get(key);
    }

    /**
     * 从url中剥离出文件名
     * @param url
     *      格式如：http://keking.ufile.ucloud.com.cn/20171113164107_月度绩效表模板(新).xls?UCloudPublicKey=ucloudtangshd@weifenf.com14355492830001993909323&Expires=&Signature=I D1NOFtAJSPT16E6imv6JWuq0k=
     * @return
     */
    public String getFileNameFromURL(String url) {
        // 因为url的参数中可能会存在/的情况，所以直接url.lastIndexOf("/")会有问题
        // 所以先从？处将url截断，然后运用url.lastIndexOf("/")获取文件名
        //String noQueryUrl = url.substring(0, url.indexOf("?") != -1 ? url.indexOf("?"): url.length());
       // String fileName = url.substring(url.lastIndexOf("/") + 1);
       // return fileName;
        //1.先判断是否存在fileName ，如果存在取出fileName。不存在，则直接取链接后面的
        Map<String, String> stringStringMap = urlSplit(url);
        if(stringStringMap.containsKey("fileName")){
            return stringStringMap.get("fileName");
        }else{
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            return fileName;
        }
    }

    /**
     * 获取链接中的selectDay,用于缓存存储。
     * @param url
     * @return
     */
    public String getFileSelectDay(String url) {

        //1.selectDay ，selectDay。不存在，则直接存当日
        Map<String, String> stringStringMap = urlSplit(url);
        if(stringStringMap.containsKey("selectDay")){
            return stringStringMap.get("selectDay");
        }else{
            Date date =new Date();
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
            return "2021-10-10";
        }
    }

    /**
     * 获取文件后缀
     * @param fileName
     * @return
     */
    public String getSuffixFromFileName(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return suffix;
    }

    /**
     * 从路径中获取
     * @param path
     *      类似这种：C:\Users\yudian-it\Downloads
     * @return
     */
    public String getFileNameFromPath(String path) {
        return path.substring(path.lastIndexOf(File.separator) + 1);
    }

    public List<String> listPictureTypes(){
        List<String> list = Lists.newArrayList();
        list.add("jpg");
        list.add("jpeg");
        list.add("png");
        list.add("gif");
        list.add("bmp");
        return list;
    }

    public List<String> listArchiveTypes(){
        List<String> list = Lists.newArrayList();
        list.add("rar");
        list.add("zip");
        list.add("jar");
        list.add("7-zip");
        list.add("tar");
        list.add("gzip");
        list.add("7z");
        return list;
    }

    public List<String> listOfficeTypes() {
        List<String> list = Lists.newArrayList();
        list.add("docx");
        list.add("doc");
        list.add("xls");
        list.add("xlsx");
        list.add("ppt");
        list.add("pptx");
        return list;
    }

    /**
     * 获取相对路径
     * @param absolutePath
     * @return
     */
    public String getRelativePath(String absolutePath) {
        return absolutePath.substring(fileDir.length());
    }

    public void addConvertedFile(String fileName, String value){
        RMapCache<String, String> convertedList = redissonClient.getMapCache(REDIS_FILE_PREVIEW_PDF_KEY);
        convertedList.fastPut(fileName, value);
    }

    /**
     * 判断文件编码格式
     * @param path
     * @return
     */
    public String getFileEncodeUTFGBK(String path){
        String enc = Charset.forName("GBK").name();
        File file = new File(path);
        InputStream in= null;
        try {
            in = new FileInputStream(file);
            byte[] b = new byte[3];
            in.read(b);
            in.close();
            if (b[0] == -17 && b[1] == -69 && b[2] == -65) {
                enc = Charset.forName("UTF-8").name();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("文件编码格式为:" + enc);
        return enc;
    }

    /**
     * 对转换后的文件进行操作(改变编码方式)
     * @param outFilePath
     */
    public void doActionConvertedFile(String outFilePath) {
        StringBuffer sb = new StringBuffer();
        try (InputStream inputStream = new FileInputStream(outFilePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))){
            String line;
            while(null != (line = reader.readLine())){
                if (line.contains("charset=gb2312")) {
                    line = line.replace("charset=gb2312", "charset=utf-8");
                }
                sb.append(line);
            }
            // 添加sheet控制头
            sb.append("<script src=\"js/jquery-3.0.0.min.js\" type=\"text/javascript\"></script>");
            sb.append("<script src=\"js/excel.header.js\" type=\"text/javascript\"></script>");
            sb.append("<link rel=\"stylesheet\" href=\"css/http_cdn.static.runoob.com_libs_bootstrap_3.3.7_css_bootstrap.css\">");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 重新写入文件
        try(FileOutputStream fos = new FileOutputStream(outFilePath);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))){
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/////////////////////////////////////////////////////////////////////
    /**
     * 去掉url中的路径，留下请求参数部分
     * @param url url地址
     * @return url请求参数部分
     */
    private  String truncateUrlPage(String url) {
        String strAllParam = null;
        String[] arrSplit = null;
        url = url.trim();
        arrSplit = url.split("[?]");
        if (url.length() > 1) {
            if (arrSplit.length > 1) {
                for (int i = 1; i < arrSplit.length; i++) {
                    strAllParam = arrSplit[i];
                }
            }
        }
        return strAllParam;
    }

    /**
     * 将参数存入map集合
     * @param url  url地址
     * @return url请求参数部分存入map集合
     */
    public  Map<String, String> urlSplit(String url) {
        Map<String, String> mapRequest = new HashMap<String, String>();
        String[] arrSplit = null;
        String strUrlParam = truncateUrlPage(url);
        if (strUrlParam == null) {
            return mapRequest;
        }
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");
            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            } else {
                if (arrSplitEqual[0] != "") {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

//将链接param 拼接起来 Map<String, String>
        public static String createLinkStringByGet(Map<String, String> params) throws UnsupportedEncodingException {
                List<String> keys = new ArrayList<String>(params.keySet());
                Collections.sort(keys);
                String prestr = "?";
                for (int i = 0; i < keys.size(); i++) {
                    String key = keys.get(i);
                    String value = params.get(key);
                    value = URLEncoder.encode(value, "UTF-8");
                    if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                    prestr = prestr + key + "=" + value;
                    } else {
                    prestr = prestr + key + "=" + value + "&";
                    }
            }
            return prestr;
        }
}
