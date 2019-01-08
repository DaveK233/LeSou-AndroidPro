package Spider;

//import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.Serializable;
import java.util.HashSet;

public class Resource implements Serializable {
    private String name = "未知";
    private String url = "未知";
    private String effective = "未知";
    private String uploader = "未知";
    private String uploadTime = "未知";
    private String size = "未知";
    private String type = "未知";
    protected final static HashSet<String> fileFormats = new HashSet<String>(){
        {
            add("3gp");add("apk");add("asf");add("avi");add("bin");add("bmp");
            add("c");add("class");add("conf");add("cpp");add("doc");add("docx");
            add("xls");add("xlsx");add("exe");add("gif");add("gtar");add("gz");
            add("h");add("htm");add("html");add("jar");add("java");add("jpeg");
            add("jpg");add("js");add("log");add("m3u");add("m4a");add("m4b");
            add("m4p");add("m4u");add("m4v");add("mkv");add("mov");add("mp2");
            add("mp3");add("mp4");add("mpc");add("mpe");add("mpeg");add("mpg");
            add("mpg4");add("mpga");add("msg");add("ogg");add("pdf");add("png");
            add("pps");add("ppt");add("pptx");add("prop");add("rc");add("rmvb");
            add("rtf");add("sh");add("tar");add("tgz");add("txt");add("wav");
            add("wma");add("wmv");add("wps");add("xml");add("z");add("zip");
            add("rar");add("epub");add("torrent");
        }
    };

    public Resource(String name, String url, String effective, String uploader, String uploadTime, String size){
        this.name = name;
        this.url = url;
        this.effective = effective;
        this.uploader = uploader;
        this.uploadTime = uploadTime;
        this.size = size;
    }

    public Resource(){}

    public void setName(String name){
        this.name = name;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public void setEffective(String effective){
        this.effective = effective;
    }

    public void setUploader(String uploader){
        this.uploader = uploader;
    }

    public void setUploadTime(String uploadTime){
        this.uploadTime = uploadTime;
    }

    public void setSize(String size){
        this.size = size;
    }

    public void setType(String type) {this.type = type;}

    public String getName(){
        return this.name;
    }

    public String getUrl(){
        return this.url;
    }

    public String getEffective(){
        return this.effective;
    }

    public String getUploader(){
        return this.uploader;
    }

    public String getUploadTime(){
        return this.uploadTime;
    }

    public String getSize(){
        return this.size;
    }

    public String getType() {return this.type;}

    public void print(){
        System.out.print("name: "+name);
        System.out.print(" url: "+url);
        System.out.print(" effective: "+effective);
        System.out.print(" uploader: "+uploader);
        System.out.print(" uploadTime: "+uploadTime);
        System.out.print(" size: "+size);
        System.out.print (" type: "+type);
        System.out.println();
    }
}
