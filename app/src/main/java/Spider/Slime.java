package Spider;

import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Spider.Spider.getPage;
import static Spider.Spider.getYun;

public class Slime {
    private static ArrayList<Resource> searchResult = new ArrayList<>();
    private static ArrayList<GetResult> getOperations = new ArrayList<>(); //用来保存针对不同url的get操作

    private static class GetPiece extends Thread {
        private String info;

        GetPiece(String info) {
            this.info = info;
        }

        public void run() {
            if (isInterrupted())
                return;

            try {
//                info.print();
                String reName = "未知", reType = "未知", reUrl = "未知", reUploader = "未知", reUploadTime = "未知", reSize = "未知";

                if(info.contains("href")) {
                    reUrl = info.substring(info.indexOf("href=\"") + 6, info.indexOf("\">"));
                    reName = info.substring(info.indexOf(">")+1, info.indexOf("</a>"));
                    reUrl = reUrl.trim();
                    reName = reName.trim();
                }
                else return;
                if(reName.lastIndexOf(".") > 0)
                    if(Resource.fileFormats.contains(reName.substring(reName.lastIndexOf(".")+1)))
                        reType = reName.substring(reName.lastIndexOf(".")+1);
                if(info.contains("ftype")){
                    reType = info.substring(info.indexOf("ftype\">"));
                    reType = reType.substring(7, reType.indexOf("</span>"));
                    reType = reType.trim();
                }
                if(info.contains("size")){
                    reSize = info.substring(info.indexOf("size\">"));
                    reSize = reSize.substring(6, reSize.indexOf("</span>"));
                    reSize = reSize.trim();
                }
                if(info.contains("upload")){
                    reUploadTime = info.substring(info.indexOf("size\">"));
                    reUploadTime = reUploadTime.substring(8, reUploadTime.indexOf("</span>"));
                    reUploadTime = reUploadTime.replace("上传: ", "");
                    reUploadTime = reUploadTime.trim();
                }
                if(info.contains("查看用户")){
                    reUploader = info.substring(info.indexOf("查看用户"), info.indexOf("的所有分享"));
                    reUploader = reUploader.replace("查看用户", "");
                    reUploader = reUploader.trim();
                }
                if (isInterrupted())
                    return;

                Resource result = getYun(new URL(reUrl));
                result.setSize(reSize);
                if (result.getName().equals("未知"))
                    result.setName(reName);
                if (result.getType().equals("未知"))
                    result.setType(reType);
                if (result.getUploadTime().equals("未知"))
                    result.setUploadTime(reUploadTime);
                if(result.getUploader().equals("未知"))
                    result.setUploader(reUploader);

                if (isInterrupted())
                    return;
//                result.print();
                searchResult.add(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
            if (page.contains("页码无效") || page.contains("页面不存在") || page.contains("没有找到符合条件的资源")) {
                return;
            }

            page = page.replace("<font color=\"red\">", "");
            page = page.replace("</font>", "");
            Pattern pattern = Pattern.compile("<a rel=\"noreferrer.*?class=\"home\".*?</span>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(page);
            ArrayList<GetPiece> getPieces = new ArrayList<>();
            while (matcher.find()) {
                GetPiece getPiece = new GetPiece(matcher.group(0));
                getPiece.setPriority(10);
                getPieces.add(getPiece);
                getPiece.start();
            }

            try {
                for (GetPiece temp : getPieces) {
                    if (isInterrupted()) {
                        temp.interrupt();
                        continue;
                    }
                    temp.join(1000);
                    temp.interrupt();
                }
            } catch (InterruptedException e) {
                for (GetPiece temp : getPieces) {
                    temp.interrupt();
                }
            }
        }
    }

    public static class getResources extends Thread {
        private String key;
        private int pageNum = 1;  //用来指示爬取的页数

        public getResources(String key, int pageNum) {
            this.key = key;
            this.pageNum = pageNum;
        }

        public void run() {
            searchResult.clear();
            getOperations.clear();
            try {
//                for (int i = 1; i <= pageNum; i++) {
                GetResult getResult = new GetResult(new URL("http://www.slimego.cn/search.html?q=" + URLEncoder.encode(key, "UTF-8") + "&page=" + Integer.toString(pageNum) + "&rows=10"));
                getOperations.add(getResult);
                getResult.setPriority(10);
                getResult.start();
//                }
            } catch (Exception e) {
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

    private static class Info {
        String title;
        String link;
        String des;
        String blink;
        String host;
        String more;

        public void print() {
            System.out.println("title: " + title);
            System.out.println("link: " + link);
            System.out.println("des: " + des);
            System.out.println("blink: " + blink);
            System.out.println("host: " + host);
            System.out.println("more: " + more);
        }

    }

    public static ArrayList<Resource> getResult() {
        return searchResult;
    }
}
