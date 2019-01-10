package com.example.lesou;

import android.Manifest;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

import Spider.Resource;
import Spider.Spider;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int engineType = 1;
    private long mPressedTime;
    private int item_id;
    private int pageNum = 1;
    private boolean statusHidden = false;
    private String queryStr = "";
    private SearchView mSearchView;
    private ListView mListView;
    private MenuItem mSwitch;
    private View footView;
    private ArrayList<Resource> result = new ArrayList<>();
    private ArrayList<String> resultStr = new ArrayList<>();
    private ArrayAdapter adapter;
    private Resource selectedResource = new Resource();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        mSearchView = (SearchView)findViewById(R.id.m_search);
        mListView = (ListView) findViewById(R.id.list_main);
        mSwitch = (MenuItem) findViewById(R.id.action_switch);
//        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
//                    loadRemnantListItem();
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//
//            }
//
//        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                item_id = position;
                if(statusHidden)
                    selectedResource = getValidRes(result, item_id);
                else
                    selectedResource = result.get(item_id);
                if(!selectedResource.getEffective().equals("无效")) {
                    Intent intent = new Intent(MainActivity.this, ItemDetail.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("effective", selectedResource.getEffective());
                    bundle.putString("type", selectedResource.getType());
                    bundle.putString("name", selectedResource.getName());
                    bundle.putString("size", selectedResource.getSize());
                    bundle.putString("uploader", selectedResource.getUploader());
                    bundle.putString("time", selectedResource.getUploadTime());
                    bundle.putString("url", selectedResource.getUrl());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
//                Toast.makeText(MainActivity.this, selectedResource.getEffective(), Toast.LENGTH_SHORT).show();
            }
        });

//        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, resultStr);
//        mListView.setAdapter(adapter);
//        mListView.setTextFilterEnabled(true);
//        this.registerForContextMenu(mListView);
        mSearchView.setQueryHint("默认使用线路1，右上角切换备线");

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                queryStr = query;
                result.clear();
                ArrayList<Resource> temp = Spider.searchResource(queryStr, engineType, pageNum);
                resultStr.clear();
                for(int i = 0; i < temp.size(); i++) {
                    if(temp.get(i) != null) {
                        if(temp.get(i).getEffective().equals("无效")) {
                            if(statusHidden) {
                                continue;
                            }
                            resultStr.add(temp.get(i).getName() + "（链接已失效）");
                            result.add(temp.get(i));
                        }
                        else {
                            resultStr.add(temp.get(i).getName());
                            result.add(temp.get(i));
                        }

                    }
//                    else {
//                        Toast.makeText(MainActivity.this, "已添加到收藏夹", Toast.LENGTH_SHORT).show();
//                    }
                }
                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, resultStr);
                mListView.setAdapter(adapter);
                mListView.setTextFilterEnabled(false);
                if(resultStr.size() == 0) {
                    Toast.makeText(MainActivity.this, "哇哦，没有结果嗷…再搜搜？", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    mListView.setFilterText(newText);
                }else{
                    mListView.clearTextFilter();
                }
                return false;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadRemnantListItem();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void loadRemnantListItem() {
        pageNum++;
        ArrayList<Resource> temp = Spider.searchResource(queryStr, engineType, pageNum);
        if(temp == null) {
            return;
        }
        for(int i = 0; i < temp.size(); i++) {
            if(temp.get(i) != null) {
                if(temp.get(i).getEffective().equals("无效")) {
                    if(statusHidden) {
                        continue;
                    }
                    resultStr.add(temp.get(i).getName() + "（链接已失效）");
                    result.add(temp.get(i));
                }
                else {
                    resultStr.add(temp.get(i).getName());
                    result.add(temp.get(i));
                }
            }
        }
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, resultStr);
        mListView.setAdapter(adapter);
        mListView.setTextFilterEnabled(false);
        if(resultStr.size()==0) {
            Toast.makeText(MainActivity.this, "喔唷，还是没有鸭！再刷刷？", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("操作");
        menu.add(1,1,1,"添加到收藏夹");
        menu.add(1,2,1,"复制链接地址");
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//        item_id = info.position;
//        //设置菜单布局
//        MenuInflater inflater = new MenuInflater(MainActivity.this);
//        inflater.inflate(R.menu.m,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case 1:
                Toast.makeText(MainActivity.this, "已添加到收藏夹", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(MainActivity.this, "已复制链接地址", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            this.finish();
            System.exit(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
//        mSearchView = (SearchView)findViewById(R.id.m_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        mSearchView = (SearchView)findViewById(R.id.m_search);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_line1) {
            engineType = 1;
            mSearchView.setQueryHint("使用线路1进行搜索");
        }
        else if(id == R.id.action_line2) {
            engineType = 0;
            mSearchView.setQueryHint("使用线路2进行搜索");
        }
        else if(id == R.id.action_line3) {
            engineType = 2;
            mSearchView.setQueryHint("使用线路3进行搜索");
        }
        else if(id == R.id.action_switch) {
            statusHidden = !statusHidden;
            if(mSwitch != null) {
                mSwitch.setTitle("haha");
            }
            resultStr.clear();
            for(int i = 0; i < result.size(); i++) {
                if(result.get(i) != null) {
                    if(result.get(i).getEffective().equals("无效")) {
                        if(statusHidden) {
                            continue;
                        }
                        resultStr.add(result.get(i).getName() + "（链接已失效）");
                    }
                    else {
                        resultStr.add(result.get(i).getName());
                    }
                }
            }
            adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, resultStr);
            mListView.setAdapter(adapter);
            mListView.setTextFilterEnabled(false);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_search) {

        }
        else if(id == R.id.nav_favourite) {
            Intent intent=new Intent(MainActivity.this, Favorite.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
        else if(id == R.id.nav_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("关于").setMessage("乐搜LeSou网盘搜索器 v1.0 Beta\n\nAuthor: Tan Qinhan, Jin Dawei\n\nLast Updated on Jan 10 2019.").setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }
        else if(id == R.id.nav_declare) {
            String alertStr = "本软件仅提供网络公开分享资源的搜索和收藏功能，搜索结果均来自网络。本软件不对任何搜索结果负责，若搜索结果涉及违法或侵权，最终解释权归网盘服务提供商或上传者所有。";
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("免责声明").setMessage(alertStr).setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public Resource getValidRes(ArrayList<Resource> resources, int index)
    {
        index++;
        for(Resource temp : resources)
        {
            if(!temp.getEffective().equals("无效")){
                index--;
                if(index == 0){
                    return temp;
                }
            }
        }
        return null;
    }
}
