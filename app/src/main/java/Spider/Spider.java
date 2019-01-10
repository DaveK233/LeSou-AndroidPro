package Spider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Spider {
    private static final int slime = 0;
    private static final int pansou = 1;
    private static final int pansousou = 2;
    protected static String getPage(URL url) {
        StringBuilder str = new StringBuilder();
        byte[] b = new byte[1024];
        int n, count = 1;

        //尝试100次,若失败则等待100ms再去请求，若成功则返回结果
        while (count != 0) {
            try {
                count--;
                String userAgent = UserAgent.getUserAgent();
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setInstanceFollowRedirects(false);
                httpURLConnection.setConnectTimeout(1000);
                httpURLConnection.setRequestProperty("User-Agent", userAgent);
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader bufr = new BufferedReader(isr);
                String temp;
                while ((temp = bufr.readLine())!=null) {
                    str.append(temp);
                }
                httpURLConnection.disconnect();
                in.close();

                if(!str.toString().contains("Bad GateWay"))
                    return str.toString();
                else
                    return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    protected static Resource getYun(URL url){
        try {
            if (url.toString().contains("yun.baidu"))
                url = new URL(url.toString().replace("yun.baidu", "pan.baidu"));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Resource result = new Resource();
        result.setUrl(url.toString());
        String page = getPage(url);

        if(page == null || page.contains("链接不存在") || page.contains("页面不存在") || page.contains("链接已过期") || page.contains("已经被取消了")) {
            result.setEffective("无效");
            return result;
        }

        Pattern pattern = Pattern.compile("<title>.*?_免费高速", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(page);
        String name = "未知";
        if(matcher.find()){
            name = matcher.group(0);
            name = name.replace("<title>", "");
            name = name.replace("_免费高速", "");
            name = name.trim();
        }
        result.setName(name);

        pattern = Pattern.compile("share-file-info\">.*?</span>", Pattern.DOTALL);
        matcher = pattern.matcher(page);
        String uploadTime = "未知";
        if(matcher.find()){
            uploadTime = matcher.group(0);
            uploadTime = uploadTime.replace("share-file-info\">", "");
            uploadTime = uploadTime.replace("<span>", "");
            uploadTime = uploadTime.replace("</span>", "");
            uploadTime = uploadTime.trim();
//            System.out.println(uploadTime);
        }
        result.setUploadTime(uploadTime);

        pattern = Pattern.compile("share-person-username global-ellipsis.*?</a>", Pattern.DOTALL);
        matcher = pattern.matcher(page);
        String uploader = "未知";
        if(matcher.find()){
            uploader = matcher.group(0);
            uploader = uploader.replace("share-person-username global-ellipsis\">", "");
            uploader = uploader.replace("</a>", "");
            uploader = uploader.trim();
        }
        result.setUploader(uploader);

        if(page.contains("个人专辑")&&page.contains("album"))
        {
            result.setType("album");
        }
        else if(page.contains("multi_file")){
            result.setType("directory");
        }
        else if(name.lastIndexOf(".") > 0 && name.lastIndexOf(".") < name.length()-1){
            if(Resource.fileFormats.contains(name.substring(name.lastIndexOf(".")+1)))
                result.setType(name.substring(name.lastIndexOf(".")+1));
        }

        result.setSize("未知");

        if(result.getName().equals("未知"))
            result.setEffective("未知");
        else
            result.setEffective("有效");
        return result;
    }

    //用于搜索的接口，key为关键词，不能为空；engine为指定搜索引擎，0为slime，1为pansou，2为pansousou；page为指定页数。若参数有误则返回null。
    public static ArrayList<Resource> searchResource(String key, int engine, int page){
        if(key == null || key.equals(""))
            return null;

        if(engine == slime){
            Slime.getResources search = new Slime.getResources(key, page);
            search.setPriority(10);
            search.start();

            try{
                search.join(3000);
                search.interrupt();
                return Slime.getResult();
            }
            catch (Exception e){
                search.interrupt();
                e.printStackTrace();
            }
        }
        else if(engine == pansou){
            PanSou.getResources search = new PanSou.getResources(key, page);
            search.setPriority(10);
            search.start();

            try{
                search.join(3000);
                search.interrupt();
                return PanSou.getResult();
            }
            catch (Exception e){
                search.interrupt();
                e.printStackTrace();
            }
        }
        else if(engine == pansousou){
            PanSouSou.getResources search = new PanSouSou.getResources(key, page);
            search.setPriority(10);
            search.start();

            try{
                search.join(3000);
                search.interrupt();
                return PanSouSou.getResult();
            }
            catch (Exception e){
                search.interrupt();
                e.printStackTrace();
            }
        }

        return null;
    }

//    public static void main(String[] args) {
//        ArrayList<Resource> resources = searchResource("魔兽争霸", 0, 1);
////        System.out.println("Slime result size : " + resources.size());
////        for(Resource temp : resources)
////            temp.print();
//        favourites.resources = resources;
//        SearchResult.serialize("Result");
//        SearchResult.resources.clear();
////        resources = searchResource("魔兽争霸", 1, 1);
////        System.out.println("Pansou result size : " + resources.size());
////        for(Resource temp : resources)
////            temp.print();
////
////        resources = searchResource("魔兽争霸", 2, 1);
////        System.out.println("Pansousou result size : " + resources.size());
////        for(Resource temp : resources)
////            temp.print();
//        SearchResult.deserialize("Result");
//        for(Resource temp : SearchResult.resources)
//            temp.print();
//    }
}
