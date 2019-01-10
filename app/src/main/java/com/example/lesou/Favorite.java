package com.example.lesou;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import Spider.Resource;
import Spider.SearchResult;
import Spider.Spider;

public class Favorite extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Uri uri;
    private int item_id;
    private long mPressedTime;
    private String addr;
    private ArrayList<String> resultStr = new ArrayList<>();
    private ArrayList<Resource> result = new ArrayList<>();
    private ListView mListView;
    public static SearchResult favourites = new SearchResult();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_favourite);
        setSupportActionBar(toolbar);

        favourites.deserialize("favourites");
        result.addAll(favourites.resources);

        mListView = (ListView)findViewById(R.id.list_fav);
        resultStr.clear();
        for(int i = 0; i < result.size(); i++) {
            if(result.get(i) != null) {
                resultStr.add(result.get(i).getName());
            }
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(Favorite.this, android.R.layout.simple_list_item_1, resultStr);
        mListView.setAdapter(adapter);
        mListView.setTextFilterEnabled(false);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                item_id = position;
            }
        });

        this.registerForContextMenu(mListView);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_fav);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_fav);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_fav);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
        long mNowTime = System.currentTimeMillis();//获取第一次按键时间
        if((mNowTime - mPressedTime) > 2000){//比较两次按键时间差
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mPressedTime = mNowTime;
        }
        else{//退出程序
            finish();
            System.exit(0);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("操作");
        menu.add(1,1,1,"从收藏夹删除");
        menu.add(1,2,1,"复制链接地址");
        menu.add(1,3,1,"在浏览器中打开链接");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenu.ContextMenuInfo info = item.getMenuInfo();
        AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo) info;
        item_id = contextMenuInfo.position;
        switch(item.getItemId()) {
            case 1:
                favourites.resources.remove(item_id);
                if(result != null)
                    result.clear();
                result.addAll(favourites.resources);
                resultStr.clear();
                for(int i = 0; i < result.size(); i++) {
                    if(result.get(i) != null) {
                        resultStr.add(result.get(i).getName());
                    }
                }
                ArrayAdapter adapter = new ArrayAdapter<String>(Favorite.this, android.R.layout.simple_list_item_1, resultStr);
                mListView.setAdapter(adapter);
                mListView.setTextFilterEnabled(false);
                Favorite.favourites.serialize("favourites");
                Toast.makeText(Favorite.this, "已从收藏夹删除", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                ClipboardManager mClipboaedManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipdata = ClipData.newPlainText("Label2", result.get(item_id).getUrl());
                mClipboaedManager.setPrimaryClip(mClipdata);
                Toast.makeText(Favorite.this, "已复制链接地址", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Intent intent= new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(result.get(item_id).getUrl());
                intent.setData(content_url);
                startActivity(intent);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.favorite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.id_fav_opensysbrowser) {
//            Intent intent= new Intent();
//            intent.setAction("android.intent.action.VIEW");
//            Uri content_url = Uri.parse("http://pan.baidu.com");
//            intent.setData(content_url);
//            startActivity(intent);
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_search_fav) {
            Intent intent=new Intent(Favorite.this, MainActivity.class);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_fav);
            drawer.closeDrawer(GravityCompat.START);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
            return true;
        }

        else if(id == R.id.nav_about_fav) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("关于").setMessage("乐搜LeSou网盘搜索器 v1.0 Beta\n\nAuthor: Tan Qinhan, Jin Dawei\n\nLast Updated on Jan 10 2019.").setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }

        else if(id == R.id.nav_declare_fav) {
            String alertStr = "本软件仅提供网络公开分享资源的搜索和收藏功能，搜索结果均来自网络。本软件不对任何搜索结果负责，若搜索结果涉及违法或侵权，最终解释权归网盘服务提供商或上传者所有。";
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("免责声明").setMessage(alertStr).setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_fav);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
