package Spider;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Spider.Spider.getPage;
import static Spider.Spider.getYun;

public class PanSouSou {
    private static String prefix = "http://www.pansoso.com/";
    private static ArrayList<Resource> searchResult = new ArrayList<>();
    private static ArrayList<GetResult> getOperations = new ArrayList<>(); //用来保存针对不同url的get操作

    //用来得到一整个页面上的资源的类
    private static class GetResult extends Thread {
        private URL url;

        GetResult(URL url) {
            this.url = url;
    }

        public void run() {
            if (isInterrupted())
                return;

            String page = getPage(url);

            if (page == null)
                return;

            //查找没有结果
            if (page.contains("页面不存在") || page.contains("Bad Request") || page.contains("没有找到相关网盘资源")) {
                return;
            }

            Pattern pattern = Pattern.compile("/[a-z]/[0-9]*?/", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(page);
            ArrayList<GetPiece> getPieces = new ArrayList<>();
            try {
                while (matcher.find()) {
                    GetPiece getPiece = new GetPiece(new URL(prefix + matcher.group(0)));
                    getPieces.add(getPiece);
                    getPiece.setPriority(10);
                    if (isInterrupted())
                        return;
                    getPiece.start();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                for (GetPiece temp : getPieces) {
                    if(isInterrupted()) {
                        temp.interrupt();
                        continue;
                    }
                    temp.join(1000);
                    temp.interrupt();
                }
            } catch (InterruptedException e) {
                for (GetPiece temp : getPieces){
                    temp.interrupt();
                }
            }
        }
    }

    //用来得到一个资源的类
    private static class GetPiece extends Thread {
        private URL url;

        GetPiece(URL url) {
            this.url = url;
        }

        public void run() {
            if (isInterrupted())
                return;

            String reName = "未知", reType = "未知", reUrl = "未知", reUploader = "未知", reUploadTime = "未知", reSize = "未知";

            String page = getPage(url);
            if (page == null)
                return;

            //获得name
            Pattern pattern = Pattern.compile("</title>.*?/><meta", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(page);
            if (matcher.find()) {
                reName = matcher.group(0);
                reName = reName.substring(reName.indexOf("content=\""));
                reName = reName.replace("content=\"", "");
                reName = reName.replace("/><meta", "");
                reName = reName.trim();
            } else return;

            //获取type
            if(reName.lastIndexOf(".") > 0)
                if(Resource.fileFormats.contains(reName.substring(reName.lastIndexOf(".")+1)))
                    reType = reName.substring(reName.lastIndexOf(".")+1);

            //获得uploader
            pattern = Pattern.compile("分享达人：.*?</dd>", Pattern.DOTALL);
            matcher = pattern.matcher(page);
            if (matcher.find()) {
                reUploader = matcher.group(0);
                reUploader = reUploader.replace("分享达人：", "");
                reUploader = reUploader.replace("</dd>", "");
                reUploader = reUploader.trim();
            } else return;

            //获得uploadTime
            pattern = Pattern.compile("分享时间：.*?</dd>", Pattern.DOTALL);
            matcher = pattern.matcher(page);
            if (matcher.find()) {
                reUploadTime = matcher.group(0);
                reUploadTime = reUploadTime.replace("分享时间：", "");
                reUploadTime = reUploadTime.replace("</dd>", "");
                reUploadTime = reUploadTime.trim();
            } else return;

            //获得size
            pattern = Pattern.compile("文件大小：.*?</dd>", Pattern.DOTALL);
            matcher = pattern.matcher(page);
            if (matcher.find()) {
                reSize = matcher.group(0);
                reSize = reSize.replace("文件大小：", "");
                reSize = reSize.replace("</dd>", "");
                reSize = reSize.trim();
            } else return;


            //获得url，需要进入下一个界面之后再匹配
            pattern = Pattern.compile("相关搜索.*?class=\"red\".*?百度网盘", Pattern.DOTALL);
            matcher = pattern.matcher(page);
            String tempUrl1 = "";
            try {
                if (matcher.find()) {
                    tempUrl1 = matcher.group(0);
                    tempUrl1 = tempUrl1.substring(tempUrl1.lastIndexOf("href"));
                    tempUrl1 = tempUrl1.replace("href=\"", "");
                    tempUrl1 = tempUrl1.substring(0, tempUrl1.indexOf("target"));
                    tempUrl1 = tempUrl1.trim();
                    if(tempUrl1.charAt(tempUrl1.length()-1) == '"')
                        tempUrl1 = tempUrl1.substring(0, tempUrl1.length() - 1);

                    if (isInterrupted())
                        return;

                    String subPage1 = getPage(new URL(tempUrl1));
                    if (subPage1 == null) {
                        return;
                    }
                    pattern = Pattern.compile("href=\".*?百度网盘", Pattern.DOTALL);
                    matcher = pattern.matcher(subPage1);
                    if (matcher.find()) {
                        reUrl = matcher.group(0);
                        reUrl = reUrl.substring(reUrl.lastIndexOf("href=\""));
                        reUrl = reUrl.replace("href=\"", "");
                        reUrl = reUrl.substring(0, reUrl.indexOf("target"));
                        reUrl = reUrl.trim();
                        if(reUrl.charAt(reUrl.length()-1) == '"')
                            reUrl = reUrl.substring(0, reUrl.length() - 1);
                    } else {
                        return;
                    }

                    //还要再进入下一界面去资源是否有效
                    Resource result = getYun(new URL(reUrl));
                    result.setSize(reSize);
                    if(result.getName().equals("未知"))
                        result.setName(reName);
                    if(result.getType().equals("未知"))
                        result.setType(reType);
                    if(result.getUploader().equals("未知"))
                        result.setUploader(reUploader);
                    if(result.getUploadTime().equals("未知"))
                        result.setUploadTime(reUploadTime);

                    searchResult.add(result);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class getResources extends Thread {
        private String key;
        private int pageNum = 1;  //用来指示爬取的页数


        public getResources(String key, int pageNum){
            this.key = key;
            this.pageNum = pageNum;
        }

        public void run() {
            searchResult.clear();
            getOperations.clear();
            try {
//                for (int i = 1; i <= pageNum; i++) {
                    GetResult getResult = new GetResult(new URL(prefix + "zh/" + key + "_" + Integer.toString(pageNum)));
                    getOperations.add(getResult);
                    getResult.setPriority(10);
                    getResult.start();
//                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                for (GetResult temp : getOperations) {
                    temp.join(2500);
                    temp.interrupt();
                }
            } catch (InterruptedException e) {
                for (GetResult temp : getOperations) {
                    temp.interrupt();
                }
            }
        }
    }

    public static ArrayList<Resource> getResult(){
        return searchResult;
    }

}
