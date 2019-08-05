package cn.szetop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * 创建人: ChenSheng
 * 创建时间: 2019/8/5
 */
public class Spider {
    public static String path = "https://pnr.sz.gov.cn/ywzy/fdtz/cggbcx/ftq_6606/";
    public static int num = -1,sum = 0;

    /**
     * 定义四个文件(链接存储,图片存储,文件存储,错误链接存储)
     */
    public static File aLinkFile,imgLinkFile,docLinkFile,errorLinkFile;

    /**
     * @param path 目标地址
     */
    public static void getAllLinks(String path) {
        Document doc = null;
        try {
            doc = Jsoup.parse(HttpUtil.get(path));
        } catch (Exception e) {
            //接收到错误链接(404)页面
            writeTxtFile(errorLinkFile,path+"\r\n");  //写入错误链接收集文件
            num++;
            if(sum>num) {  //如果文件总数(sum)大于num(当前坐标)则继续遍历
                getAllLinks(getFileLine(aLinkFile,num));
            }
            return;
        }
        Elements aLinks = doc.select("a[href]");
        Elements imgLinks = doc.select("img[src]");
        System.out.println("开始链接"+path);
        for (Element element : aLinks) {
            String url = element.attr("href");
            //判断链接是否包含http和https
            if (!url.contains("http://")&&!url.contains("https://")) {
                url = Spider.path + url;
            }
            //判断文件中是否有这个链接
            if (!readTxtFile(aLinkFile).contains(url)&&!url.contains("javascript")) {
                //路径必须包含网页主链接-----防止爬入别的网站
                if(url.contains(Spider.path)) {
                    //判断a标签的内容是文件还是子链接
                    if(url.contains(".doc")||url.contains(".exl")||url.contains(".pdf")) {
                        //写入文件中,文件名+文件链接
                        writeTxtFile(docLinkFile,element.text()+"\r\n\t"+url+"\r\n");
                    }else {
                        //将链接写入文件
                        writeTxtFile(aLinkFile,url+"\r\n");
                        sum++;  //链接总数+1
                    }
                    System.out.println("\t"+element.text()+":\t"+url);
                }
            }
        }
        //同时抓取该页面图片链接
        for (Element element : imgLinks) {
            String srcStr = element.attr("src");
            //判断是否包含http和https
            if (!srcStr.contains("http")&&!srcStr.contains("https")) {
                srcStr = Spider.path + srcStr;
            }
            if(!readTxtFile(imgLinkFile).contains(srcStr)) {
                //将图片链接写进文中
                writeTxtFile(imgLinkFile,srcStr+"\r\n");
            }
        }
        num++;
        if(sum>num) {
            getAllLinks(getFileLine(aLinkFile,num));
        }
    }

    /**
     * 读取文件内容
     * @param file  文件类
     * @return  文件内容
     */

    private static String readTxtFile(File file) {
        String result = "";  //读取结果
        String thisLine = ""; //每次读取的行

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                while((thisLine = reader.readLine())!=null) {
                    result += thisLine + "\n";
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入内容
     * @param file  文件类
     * @param urlStr  要写入的文本
     */
    private static void writeTxtFile(File file, String urlStr) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(urlStr);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件指定行数的数据,用于爬虫获取当前要爬的链接
     * @param file   目标文件
     * @param num    指定的行数
     */
    private static String getFileLine(File file, int num) {
        String thisLine = "";
        int thisNum = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while((thisLine = reader.readLine()) != null) {
                if(num == thisNum) {
                    return thisLine;
                }

                thisNum++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 获取文件总行数(有多少链接)
     * @param file 文件类
     * @return  总行数
     */
    public static int getFileCount(File file) {
        int count = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while(reader.readLine() != null) {  //遍历文件行
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }


    public static void main(String[] args) {
        aLinkFile = new File("F:/Spider/ALinks.txt");
        imgLinkFile = new File("F:/Spider/ImgLinks.txt");
        docLinkFile = new File("F:/Spider/DocLinks.txt");
        errorLinkFile = new File("F:/Spider/ErrorLinks.txt");
        //用数组存储文件对象
        File[] files = new File[] {aLinkFile,imgLinkFile,docLinkFile,errorLinkFile};
        try {
            for (File file : files) {
                if(file.exists())   //如果文件存在
                    file.delete();   //则先删除
                file.createNewFile();  //再创建
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long startTime = System.currentTimeMillis();   //获取开始时间
        Spider.getAllLinks(path);   //开始爬取目标内容
        System.out.println(""
                + "----------爬取结束-----------"
                + "\n目标网址:"+path
                + "\n链接总数:"+sum+"条"
                + "\n图片总数:"+getFileCount(imgLinkFile)+"张"
                + "\n文件总数:"+getFileCount(docLinkFile)+"份" );
        writeTxtFile(aLinkFile, "链接总数:"+getFileCount(aLinkFile)+"条");
        writeTxtFile(imgLinkFile, "图片总数:"+getFileCount(imgLinkFile)+"张");
        writeTxtFile(docLinkFile, "文件总数:"+getFileCount(docLinkFile)+"份");
        writeTxtFile(errorLinkFile, "问题链接总数:"+getFileCount(errorLinkFile)+"条");
        long endTime = System.currentTimeMillis();    //获取结束时间
        System.out.println("\n程序运行时间:"+(endTime - startTime) + "ms");   //输出程序运行时间

    }
}
