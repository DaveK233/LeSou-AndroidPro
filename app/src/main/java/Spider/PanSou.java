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

public class PanSou {
    private static Gson gson = new Gson();
    private static ArrayList<Resource> searchResult = new ArrayList<>();
    private static ArrayList<GetResult> getOperations = new ArrayList<>(); //用来保存针对不同url的get操作

    private static class GetPiece extends Thread{
        private String json;

        GetPiece(String json){this.json = json;}

        public void run(){
            if(isInterrupted())
                return;

            try {
                Info info = gson.fromJson(json, Info.class);
//                info.print();
                String reName = "未知", reType = "未知", reUrl = "未知", reUploader = "未知", reUploadTime = "未知", reSize = "未知";
                Pattern pattern;
                Matcher matcher;

                if(info.title != null && !info.title.equals("null"))
                    reName = info.title;
                if(info.link != null && !info.link.equals("null"))
                    reUrl = info.link;
                if(reName.lastIndexOf(".") > 0)
                    if(Resource.fileFormats.contains(reName.substring(reName.lastIndexOf(".")+1)))
                        reType = reName.substring(reName.lastIndexOf(".")+1);
                if(info.des != null && !info.des.equals("null")){
                    pattern = Pattern.compile("分享时间: .*?\\.");
                    matcher = pattern.matcher(info.des);
                    if(matcher.find()){
                        reUploadTime = matcher.group(0);
                        reUploadTime = reUploadTime.replace("分享时间: ", "");
                        reUploadTime = reUploadTime.replace(".", "");
                        reUploadTime = reUploadTime.replace("T", " ");
                        reUploadTime = reUploadTime.trim();
                    }

                    if(info.des.contains("文件大小")){
                        reSize = info.des.substring(info.des.indexOf("文件大小"));
                        reSize = reSize.replace("文件大小: ", "");
                        reSize = reSize.trim();
                    }
                }

                if(isInterrupted())
                    return;

                Resource result = getYun(new URL(reUrl));
                result.setSize(reSize);
                if(result.getName().equals("未知"))
                    result.setName(reName);
                if(result.getType().equals("未知"))
                    result.setType(reType);
                if(result.getUploadTime().equals("未知"))
                    result.setUploadTime(reUploadTime);

                if(isInterrupted())
                    return;
//                result.print();
                searchResult.add(result);
            }
            catch (Exception e){
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
            if (!page.contains("\"data\"")) {
                return;
            }

            int index1 = page.indexOf("[");
            int index2 = page.indexOf("]");
            if(index1 < 0 || index2 < 0)
                return;
            page = page.substring(index1 + 1, index2);
            if(page.length() == 0)
                return;
//            System.out.println(page);

            String []resourceList = page.split("},\\{");
            ArrayList<GetPiece> getPieces = new ArrayList<>();

            for(String piece : resourceList){
//                System.out.println(piece);

                if(!piece.contains("}"))
                    piece += "}";
                if(!piece.contains("{"))
                    piece = "{" + piece;

                GetPiece getPiece = new GetPiece(piece);
                getPiece.setPriority(10);
                getPieces.add(getPiece);
                getPiece.start();
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
                    GetResult getResult = new GetResult(new URL("http://106.15.195.249:8011/search_new?callback=jQuery172034288858029460734_1546615297776&q="+ URLEncoder.encode(key, "UTF-8") +"&p="+Integer.toString(pageNum)+"&_=1546615297814"));
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

    private static class Info{
        String title;
        String link;
        String des;
        String blink;
        String host;
        String more;

        public void print(){
            System.out.println("title: "+title);
            System.out.println("link: "+link);
            System.out.println("des: "+des);
            System.out.println("blink: "+blink);
            System.out.println("host: "+host);
            System.out.println("more: "+more);
        }
    }

    public static ArrayList<Resource> getResult(){
        return searchResult;
    }
}
