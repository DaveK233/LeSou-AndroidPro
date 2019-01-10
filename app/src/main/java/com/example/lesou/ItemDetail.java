package com.example.lesou;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import Spider.Resource;
import Spider.SearchResult;

import static com.example.lesou.Favorite.favourites;

public class ItemDetail extends AppCompatActivity {

    private ListView mListView;
    private List<DetailMenuItem> detailList = new ArrayList<DetailMenuItem>();
    private Resource selectedResource;
    private Button mBtnCpy;
    private Button mBtnFav;
    private Button mBtnOpen;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        if(data != null) {
            selectedResource = new Resource(data.getString("name"), data.getString("url"), data.getString("effective"),
                                            data.getString("uploader"), data.getString("time"), data.getString("size"));
            selectedResource.setType(data.getString("type"));
        }
        initDetails();
        DetailAdapter adapter = new DetailAdapter(ItemDetail.this, R.layout.detail_item, detailList);
        mListView = (ListView)findViewById(R.id.id_details_list);
        mListView.setAdapter(adapter);

        mBtnCpy = (Button) findViewById(R.id.id_copy);
        mBtnCpy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager mClipboaedManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipdata = ClipData.newPlainText("Label", selectedResource.getUrl());
                mClipboaedManager.setPrimaryClip(mClipdata);
                Toast.makeText(ItemDetail.this, "已复制链接地址", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnFav = (Button) findViewById(R.id.id_to_fav);
        mBtnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(favourites.resources.indexOf(selectedResource) < 0) {
                    favourites.resources.add(selectedResource);
                    if (ActivityCompat.checkSelfPermission(ItemDetail.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ItemDetail.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                    }
                    favourites.serialize("favourites");
                    Toast.makeText(ItemDetail.this, "已添加到收藏夹", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBtnOpen = (Button) findViewById(R.id.id_open_in_browser);
        mBtnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(selectedResource.getUrl());
                intent.setData(content_url);
                startActivity(intent);
            }
        });


    }

    private void initDetails() {
        DetailMenuItem name = new DetailMenuItem("文件名：" + selectedResource.getName(), R.drawable.nameicon);
        detailList.add(name);
        DetailMenuItem effective = new DetailMenuItem("有效性：" + selectedResource.getEffective(), R.drawable.effective);
        detailList.add(effective);
        DetailMenuItem uploader = new DetailMenuItem("上传者：" + selectedResource.getUploader(), R.drawable.uploader);
        detailList.add(uploader);
        DetailMenuItem time = new DetailMenuItem("上传时间：" + selectedResource.getUploadTime(), R.drawable.time);
        detailList.add(time);
        DetailMenuItem size = new DetailMenuItem("文件大小：" + selectedResource.getSize(), R.drawable.size);
        detailList.add(size);
        DetailMenuItem type = new DetailMenuItem("文件类型：" + selectedResource.getType(), R.drawable.type);
        detailList.add(type);

    }
}
