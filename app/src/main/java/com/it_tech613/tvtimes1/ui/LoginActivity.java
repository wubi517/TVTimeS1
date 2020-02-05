package com.it_tech613.tvtimes1.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.it_tech613.tvtimes1.BuildConfig;
import com.it_tech613.tvtimes1.R;
import com.it_tech613.tvtimes1.apps.Constants;
import com.it_tech613.tvtimes1.apps.MyApp;
import com.it_tech613.tvtimes1.models.*;
import com.it_tech613.tvtimes1.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText name_txt, pass_txt, mac_address;
    private ProgressBar progressBar;
    private String user;
    private String password;
    private CheckBox checkBox;
    boolean is_remember = false;
    private LinearLayout lay_mac, lay_user_pass;
    private boolean is_pass_mode = true;
    private Button btn_change_mode, btn_login;
    SharedPreferences serveripdetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        serveripdetails = this.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (MyApp.instance.getPreference().get(Constants.getMacAddress()) == null)
            MyApp.mac_address = Utils.getPhoneMac(LoginActivity.this);
        else
            MyApp.mac_address = (String) MyApp.instance.getPreference().get(Constants.getMacAddress());

        RelativeLayout main_lay = findViewById(R.id.main_lay);
        Bitmap myImage = getBitmapFromURL(Constants.GetLoginImage(LoginActivity.this));
        Drawable dr = new BitmapDrawable(myImage);
        main_lay.setBackgroundDrawable(dr);
        progressBar = findViewById(R.id.login_progress);
        name_txt = findViewById(R.id.login_name);
        pass_txt =  findViewById(R.id.login_pass);
        checkBox = findViewById(R.id.checkbox);
        checkBox.setOnClickListener(this);
        lay_mac = findViewById(R.id.lay_mac);
        lay_user_pass = findViewById(R.id.lay_user_pass);
        btn_change_mode = (Button)findViewById(R.id.btn_change_mode);
        btn_change_mode.setVisibility(View.GONE);
        btn_change_mode.setOnClickListener(this);
        mac_address = findViewById(R.id.mac_address);
        btn_login = findViewById(R.id.login_btn);
        btn_login.setOnClickListener(this);
        btn_change_mode.setEnabled(true);
        btn_login.setEnabled(true);
//        type = getIntent().getIntExtra("type",0);
        toggleMode(!MyApp.is_mac);
        MyApp.instance.loadVersion();
        TextView version_txt = findViewById(R.id.app_version_code);
        MyApp.version_str = "v " + MyApp.version_name;
        version_txt.setText(MyApp.version_str);
        ImageView logo = (ImageView) findViewById(R.id.logo);
        Glide.with(LoginActivity.this)
                .load(Constants.GetIcon(LoginActivity.this))
                .apply(new RequestOptions().error(R.drawable.icon).placeholder(R.drawable.icon_default).signature(new ObjectKey("myKey5")))
                .into(logo);
        logo.setOnClickListener(this);
    }

    private void callLogin() {
        try {
            runOnUiThread(()->{
                btn_change_mode.setEnabled(false);
                btn_login.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
            });
            long startTime = System.nanoTime();
            String responseBody = MyApp.instance.getIptvclient().authenticate(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            Log.e(getClass().getSimpleName(),responseBody);
            Log.e("BugCheck","authenticate success "+MethodeDuration);
            MyApp.user = user;
            MyApp.pass = password;
            JSONObject u_m;
            try {
                JSONObject map = new JSONObject(responseBody);
                u_m = map.getJSONObject("user_info");
                if (!u_m.has("username")) {
                    runOnUiThread(()->{
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Username is incorrect", Toast.LENGTH_LONG).show();
                    });
                } else {
                    MyApp.created_at = u_m.getString("created_at");
                    MyApp.status = u_m.getString("status");
                    if(!MyApp.status.equalsIgnoreCase("Active")){
                        runOnUiThread(()->{
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Username is incorrect", Toast.LENGTH_LONG).show();
                        });
                        return;
                    }
                    MyApp.is_trail = u_m.getString("is_trial");
                    MyApp.active_cons = u_m.getString("active_cons");
                    MyApp.max_cons = u_m.getString("max_connections");
                    String exp_date;
                    try{
                        exp_date = u_m.getString("exp_date");
                    }catch (Exception e){
                        exp_date = "unlimited";
                    }
                    LoginModel loginModel = new LoginModel();
                    loginModel.setUser_name(MyApp.user);
                    loginModel.setPassword(MyApp.pass);
                    try{
                        loginModel.setExp_date(exp_date);
                    }catch (Exception e){
                        loginModel.setExp_date("unlimited");
                    }
                    MyApp.loginModel = loginModel;
                    Log.e("remember",String.valueOf(is_remember));
                    if(checkBox.isChecked()){
                        if (is_pass_mode)
                            MyApp.instance.getPreference().put(Constants.getLoginInfo(), loginModel);
                        else
                            MyApp.instance.getPreference().put(Constants.getMacAddress(), MyApp.mac_address.toUpperCase());
                    }
                    JSONObject serverInfo= map.getJSONObject("server_info");
                    String  my_timestamp= serverInfo.getString("timestamp_now");
                    String server_timestamp= serverInfo.getString("time_now");
                    Constants.setServerTimeOffset(my_timestamp,server_timestamp);
                    callVodCategory();
                }
            } catch (JSONException e) {
                runOnUiThread(()->{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Username is incorrect", Toast.LENGTH_LONG).show();
                    btn_change_mode.setEnabled(true);
                    btn_login.setEnabled(true);
                });
                e.printStackTrace();

            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                btn_change_mode.setEnabled(true);
                btn_login.setEnabled(true);
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLogin()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN UNSUCCESSFUL PLEASE CHECK YOUR LOGIN DETAILS OR CONTACT YOUR PROVIDER");
                connectionDlg.show();
            });
        }
    }

    private void callVodCategory(){
        try {
            long startTime = System.nanoTime();
            //api call here
            String map = MyApp.instance.getIptvclient().getMovieCategories(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            //Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getMovieCategories success "+MethodeDuration);
            try {

                Gson gson=new Gson();
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<CategoryModel> categories;
                categories = new ArrayList<>();

                categories.add(getRecentMovies());
                categories.add(new CategoryModel(Constants.all_id,Constants.All,""));
                categories.add(getFavoriteCategory());
                categories.add(new CategoryModel(Constants.no_name_id,Constants.No_Name_Category,""));
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModel>>(){}.getType()));
                MyApp.vod_categories = categories;
            }catch (Exception e){

            }
            callLiveCategory();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                btn_change_mode.setEnabled(true);
                btn_login.setEnabled(true);
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callVodCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }
    }

    private CategoryModel getFavoriteCategory() {
        CategoryModel categoryModel=new CategoryModel(Constants.fav_id,Constants.Favorites,"");
        List<MovieModel> favMovies = (List<MovieModel>) MyApp.instance.getPreference().get(Constants.getFAV_VOD_MOVIES());
        if (favMovies!=null){
            MyApp.favMovieModels=favMovies;
            categoryModel.setMovieModels(favMovies);
        }
        else {
            MyApp.favMovieModels=new ArrayList<>();
            categoryModel.setMovieModels(new ArrayList<>());
        }
        return categoryModel;
    }

    private CategoryModel getRecentMovies() {
        CategoryModel recentCategory = new CategoryModel(Constants.recent_id,Constants.Recently_Viewed,"");
        List<MovieModel> recentMovies=(List<MovieModel>) MyApp.instance.getPreference().get(Constants.getRecentMovies());
        if (recentMovies!=null){
            MyApp.recentMovieModels=recentMovies;
            recentCategory.setMovieModels(recentMovies);
        }
        else {
            MyApp.recentMovieModels=new ArrayList<>();
            recentCategory.setMovieModels(new ArrayList<>());
        }
        return recentCategory;
    }

    private void callLiveCategory(){
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getLiveCategories(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            //Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getLiveCategories success "+MethodeDuration);
            try {
                Gson gson=new Gson();
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<CategoryModel> categories;
                categories = new ArrayList<>();
                categories.add(new CategoryModel(Constants.recent_id,Constants.Recently_Viewed,""));
                categories.add(new CategoryModel(Constants.all_id,Constants.All,""));
                categories.add(new CategoryModel(Constants.fav_id,Constants.Favorites,""));
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModel>>(){}.getType()));
                MyApp.live_categories = categories;
                for (CategoryModel categoryModel: categories){
                    String category_name = categoryModel.getName().toLowerCase();
                    if(category_name.contains("adult")||category_name.contains("xxx")){
                        Constants.xxx_category_id = categoryModel.getId();
                        Log.e("LoginActivity","xxx_category_id: "+Constants.xxx_category_id);
                    }
                }
            }catch (Exception e){

            }

            callSeriesCategory();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                btn_change_mode.setEnabled(true);
                btn_login.setEnabled(true);
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLiveCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }
    }

    private void callSeriesCategory(){
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getSeriesCategories(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            //Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getSeriesCategories success "+MethodeDuration);
            try {
                Gson gson=new Gson();
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<CategoryModel> categories;
                categories = new ArrayList<>();
                categories.add(new CategoryModel(Constants.recent_id,Constants.Recently_Viewed,""));
                categories.add(new CategoryModel(Constants.all_id,Constants.All,""));
                categories.add(new CategoryModel(Constants.fav_id,Constants.Favorites,""));
                categories.add(new CategoryModel(Constants.no_name_id,Constants.No_Name_Category,""));
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModel>>(){}.getType()));
                MyApp.series_categories = categories;
            }catch (Exception e){

            }

            callLiveStreams();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                btn_change_mode.setEnabled(true);
                btn_login.setEnabled(true);
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callSeriesCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }
    }

    private void callLiveStreams(){
        try{
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getLiveStreams(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
           // Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getLiveStreams success "+MethodeDuration);
            try {
                Gson gson=new Gson();
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<EPGChannel> epgChannels=new ArrayList<>();
                try{
                    epgChannels = new ArrayList<>(gson.fromJson(map, new TypeToken<List<EPGChannel>>() {}.getType()));
                }catch (Exception e){
                    JSONArray response;
                    try {
                        response=new JSONArray(map);
                        for (int i=0;i<response.length();i++){
                            JSONObject jsonObject=response.getJSONObject(i);
                            EPGChannel epgChannel=new EPGChannel();
                            try{
                                epgChannel.setNumber(jsonObject.getString("num"));
                            }catch (JSONException e1){
                                epgChannel.setNumber("");
                            }
                            try{
                                epgChannel.setName(jsonObject.getString("name"));
                            }catch (JSONException e2){
                                epgChannel.setName("");
                            }
                            try{
                                epgChannel.setStream_type(jsonObject.getString("stream_type"));
                            }catch (JSONException e3){
                                epgChannel.setStream_type("");
                            }
                            try{
                                epgChannel.setStream_id(jsonObject.getInt("stream_id"));
                            }catch (JSONException e4){
                                epgChannel.setStream_id(-1);
                            }
                            try{
                                epgChannel.setImageURL(jsonObject.getString("stream_icon"));
                            }catch (JSONException e5){
                                epgChannel.setImageURL("");
                            }
                            try{
                                epgChannel.setChannelID(jsonObject.getInt("epg_channel_id"));
                            }catch (JSONException e1){
                                epgChannel.setChannelID(-1);
                            }
                            try{
                                epgChannel.setAdded(jsonObject.getString("added"));
                            }catch (JSONException e1){
                                epgChannel.setAdded("");
                            }
                            try{
                                epgChannel.setCategory_id(jsonObject.getInt("category_id"));
                                if (epgChannel.getCategory_id()==Constants.xxx_category_id)
                                    epgChannel.setIs_locked(true);
                                else epgChannel.setIs_locked(false);
                            }catch (JSONException e1){
                                epgChannel.setCategory_id(-1);
                            }
                            try{
                                epgChannel.setCustom_sid(jsonObject.getString("custom_sid"));
                            }catch (JSONException e1){
                                epgChannel.setCustom_sid("");
                            }
                            try{
                                epgChannel.setTv_archive(jsonObject.getInt("tv_archive"));
                            }catch (JSONException e1){
                                epgChannel.setTv_archive(0);
                            }
                            try{
                                epgChannel.setDirect_source(jsonObject.getString("direct_source"));
                            }catch (JSONException e1){
                                epgChannel.setDirect_source("");
                            }
                            try{
                                epgChannel.setTv_archive_duration(jsonObject.getString("tv_archive_duration"));
                            }catch (JSONException e1){
                                epgChannel.setTv_archive_duration("0");
                            }
                            epgChannels.add(epgChannel);
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                MyApp.channel_size = epgChannels.size();
                Map<String, List<EPGChannel>> back_map = new HashMap<String, List<EPGChannel>>();
                back_map.put("channels", epgChannels);
                MyApp.backup_map = back_map;
                List<FullModel> fullModels = new ArrayList<>();

                fullModels.add(new FullModel(Constants.recent_id, getRecentChannels(epgChannels), Constants.Recently_Viewed,1));
                fullModels.add(new FullModel(Constants.all_id, epgChannels,Constants.All,0));
                fullModels.add(new FullModel(Constants.fav_id,getFavoriteChannels(epgChannels),Constants.Favorites,0));
                List<String> datas = new ArrayList<>();
                datas.add("All Channel");
                datas.add(Constants.Favorites);
                for(int i = Constants.unCount_number; i< MyApp.live_categories.size(); i++){
                    int category_id = MyApp.live_categories.get(i).getId();
                    String category_name = MyApp.live_categories.get(i).getName();
                    int count =0;
                    List<EPGChannel> chModels = new ArrayList<>();
                    for(int j = 0; j< epgChannels.size(); j++){
                        EPGChannel chModel = epgChannels.get(j);
                        if(category_id==chModel.getCategory_id()){
                            chModels.add(chModel);
                            if (chModel.getTv_archive()==1) count+=1;
                        }
                    }
//                    if(chModels.size()==0){
//                        continue;
//                    }
                    datas.add(MyApp.live_categories.get(i).getName());
                    fullModels.add(new FullModel(category_id,chModels,category_name,count));
                    Log.e("catchable_count",count+"");
                }
                MyApp.fullModels = fullModels;
                MyApp.maindatas = datas;
                int count_catchable=0;
                for (int i=Constants.unCount_number;i<MyApp.fullModels.size();i++){
                    count_catchable+=MyApp.fullModels.get(i).getCatchable_count();
                }
                Constants.getAllFullModel(MyApp.fullModels).setCatchable_count(count_catchable);
                Log.e("total catchable_count",count_catchable+"");

            }catch (Exception e){

            }

            callSeries();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                btn_change_mode.setEnabled(true);
                btn_login.setEnabled(true);
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLiveStreams()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }
    }

    private void callSeries() {
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getSeries(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
//            Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getSeries success "+MethodeDuration);
            try {
                Gson gson=new Gson();
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<SeriesModel> allSeriesModels = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(map);
                    for (int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        try {
                            SeriesModel seriesModel = gson.fromJson(jsonObject.toString(),SeriesModel.class);
                            allSeriesModels.add(seriesModel);
                        }catch (Exception ignored){}
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//            allSeriesModels.addAll((Collection<? extends SeriesModel>) gson.fromJson(map, new TypeToken<List<SeriesModel>>(){}.getType()));
                MyApp.seriesModels = allSeriesModels;
                putRecentSeriesModels(allSeriesModels);
                putFavSeriesModels(allSeriesModels);
                Constants.getRecentCatetory(MyApp.series_categories).setSeriesModels(MyApp.recentSeriesModels);
                Constants.getAllCategory(MyApp.series_categories).setSeriesModels(allSeriesModels);
                Constants.getFavoriteCatetory(MyApp.series_categories).setSeriesModels(MyApp.favSeriesModels);
                Constants.putSeries(allSeriesModels);

            }catch (Exception e){

            }

            callMovieStreams();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                btn_change_mode.setEnabled(true);
                btn_login.setEnabled(true);
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callSeries()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }
    }

    private void putRecentSeriesModels(List<SeriesModel> allSeriesModels) {
        List<String> fav_series_names=(List<String>) MyApp.instance.getPreference().get(Constants.getRecentSeries());
        MyApp.recentSeriesModels=new ArrayList<>();
        if (fav_series_names!=null){
            if (fav_series_names.size()!=0)
                for (SeriesModel seriesModel:allSeriesModels){
                    for (String name:fav_series_names){
                        if (name.equals(seriesModel.getName())) {
                            MyApp.recentSeriesModels.add(seriesModel);
                            break;
                        }
                    }
                }
            Log.e("recent_series_models",MyApp.recentSeriesModels.size()+" seriesRecent "+fav_series_names.size()+" names");
        }
    }

    private void putFavSeriesModels(List<SeriesModel> allSeriesModels) {
        List<String> fav_series_names=(List<String>) MyApp.instance.getPreference().get(Constants.getFAV_SERIES());
        MyApp.favSeriesModels=new ArrayList<>();
        if (fav_series_names!=null){
            if (fav_series_names.size()!=0)
                for (SeriesModel seriesModel:allSeriesModels){
                    for (String name:fav_series_names){
                        if (name.equals(seriesModel.getName())) {
                            MyApp.favSeriesModels.add(seriesModel);
                            seriesModel.setIs_favorite(true);
                            break;
                        }
                    }
                }
            Log.e("fav_series_models",MyApp.favSeriesModels.size()+" seriesFav "+fav_series_names.size()+" names");
        }
    }

    private void callMovieStreams() {
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getMovies(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
//            Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getMovies success "+MethodeDuration);
            try {
                Gson gson=new Gson();
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<MovieModel> movieModels = new ArrayList<>(gson.fromJson(map, new TypeToken<List<MovieModel>>() {}.getType()));
                MyApp.movieModels = movieModels;
                Constants.getAllCategory(MyApp.vod_categories).setMovieModels(movieModels);
                Constants.putMovies(movieModels);
            }catch (Exception e){

            }

            getAuthorization();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                btn_change_mode.setEnabled(true);
                btn_login.setEnabled(true);
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callMovieStreams()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }
    }

    private List<EPGChannel> getRecentChannels(List<EPGChannel> epgChannels){
        List<EPGChannel> recentChannels=new ArrayList<>();

        if(MyApp.instance.getPreference().get(Constants.getFAV_LIVE_CHANNELS())!=null){
            List<String> recent_channel_names=(List<String>) MyApp.instance.getPreference().get(Constants.getRecentChannels());
            for(int j=0;j< recent_channel_names.size();j++){
                for(int i = 0;i<epgChannels.size();i++){
                    if(epgChannels.get(i).getName().equals(recent_channel_names.get(j))){
                        recentChannels.add(epgChannels.get(i));
                    }
                }
            }
        }
        return recentChannels;
    }

    private List<EPGChannel> getFavoriteChannels(List<EPGChannel> epgChannels){
        List<EPGChannel> favChannels=new ArrayList<>();

        if(MyApp.instance.getPreference().get(Constants.getFAV_LIVE_CHANNELS())!=null){
            List<String> fav_channel_names=(List<String>) MyApp.instance.getPreference().get(Constants.getFAV_LIVE_CHANNELS());
            for(int j=0;j< fav_channel_names.size();j++){
                for(int i = 0;i<epgChannels.size();i++){
                    if(epgChannels.get(i).getName().equals(fav_channel_names.get(j))){
                        epgChannels.get(i).setIs_favorite(true);
                        favChannels.add(epgChannels.get(i));
                    }else {
                        epgChannels.get(i).setIs_favorite(false);
                    }
                }
            }
        }
        Log.e("fav_live_streams_models",favChannels.size()+"");
        return favChannels;
    }
    public Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getAuthorization(){
        try {
            long startTime = System.nanoTime();
//api call here
            String response = MyApp.instance.getIptvclient().auth1(Constants.GetKey(this));
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            Log.e("BugCheck","auth1 success "+MethodeDuration);
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("status")) {
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    finish();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(LoginActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
                    btn_change_mode.setEnabled(true);
                    btn_login.setEnabled(true);
                }
            }catch (JSONException e){
                e.printStackTrace();
                btn_change_mode.setEnabled(true);
                btn_login.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            btn_change_mode.setEnabled(true);
            btn_login.setEnabled(true);
        }
    }

    private void macLogin(String mac){
        btn_change_mode.setEnabled(false);
        btn_login.setEnabled(false);
        MyApp.mac_address = mac;
        new Thread(() -> {
            try {
                Log.e("macGetTokenApi",MyApp.instance.getIptvclient().getUrl()+" "+MyApp.instance.getIptvclient().getToken());
                String response = MyApp.instance.getIptvclient().macGetTokenApi(mac,MyApp.time_zone);
                Log.e("macGetTokenApi",response+" "+MyApp.instance.getIptvclient().getUrl()+" "+MyApp.instance.getIptvclient().getToken());
                JSONObject jsonObject = new JSONObject(response);
                Gson gson = new Gson();
                String id = "-1";
                JSONArray jsonArray = jsonObject.getJSONArray("js");
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    MacCategoryModel macCategoryModel = gson.fromJson(object.toString(),MacCategoryModel.class);
                    if (!macCategoryModel.getTitle().toLowerCase().equals("all")){
                        id=macCategoryModel.getId();
                        break;
                    }
                }
//                id = "-1";
                if (!id.equals("-1")){
                    response = MyApp.instance.getIptvclient().macGetOrderedList(id);
                    Log.e("macGetOrderedList",response);
                    jsonObject = new JSONObject(response);
                    JSONObject js = jsonObject.getJSONObject("js");
                    JSONArray data = js.getJSONArray("data");
                    JSONObject item = data.getJSONObject(0);
                    String cmd = item.getString("cmd");
                    if (cmd.contains("localhost")){
                        MyApp.is_local = true;
                        response = MyApp.instance.getIptvclient().macCmd(cmd);
                        Log.e("macCmd",response);
                        jsonObject = new JSONObject(response);
                        js = jsonObject.getJSONObject("js");
                        cmd = js.getString("cmd");
                    }
                    cmd = cmd.replaceAll("ffmpeg","").replaceAll("auto","");
                    try {
                        user = cmd.split("live/")[1].split("/")[0];
                        password = cmd.split("live/")[1].split("/")[1];
                    }catch (Exception e){
                        user = cmd.split("/")[3];
                        password = cmd.split("/")[4];
                    }
                    Log.e("user",user+" "+password);
                    callLogin();
                }else {
                    response = MyApp.instance.getIptvclient().macGetVodCategory();
                    Log.e("macGetVodCategory",response);
                    jsonObject = new JSONObject(response);
                    jsonArray = jsonObject.getJSONArray("js");
                    for (int i=0;i<jsonArray.length();i++){
                        JSONObject object = jsonArray.getJSONObject(i);
                        MacCategoryModel macCategoryModel = gson.fromJson(object.toString(),MacCategoryModel.class);
                        if (!macCategoryModel.getTitle().toLowerCase().equals("all")){
                            id=macCategoryModel.getId();
                            break;
                        }
                    }
                    if (!id.equals("-1")){
                        response = MyApp.instance.getIptvclient().macGetVodOrderedList(id);
                        Log.e("macGetVodOrderedList",response);
                        jsonObject = new JSONObject(response);
                        JSONObject js = jsonObject.getJSONObject("js");
                        JSONArray data = js.getJSONArray("data");
                        JSONObject item = data.getJSONObject(0);
                        String cmd = item.getString("cmd");
                        Log.e("cmd",cmd);
                        response = MyApp.instance.getIptvclient().macVodCmd(cmd);
                        Log.e("macVodCmd",response);
                        jsonObject = new JSONObject(response);
                        js = jsonObject.getJSONObject("js");
                        cmd = js.getString("cmd");
                        cmd = cmd.replaceAll("ffmpeg","").replaceAll("auto","");
                        String str_domain = cmd.replaceAll("ffmpeg","").replaceAll("auto","");
                        str_domain = str_domain.split("/")[0]+str_domain.split("/")[1]+str_domain.split("/")[2]+"/";
                        try {
                                    SharedPreferences.Editor server_editor = serveripdetails.edit();
                            server_editor.putString("ip",str_domain);
                            server_editor.apply();
                        }catch (Exception ignored){
                        }
                        try {
                            user = cmd.split("live/")[1].split("/")[0];
                            password = cmd.split("live/")[1].split("/")[1];
                        }catch (Exception e){
                            user = cmd.split("/")[4];
                            password = cmd.split("/")[5];
                        }
                        MyApp.is_local = true;
                        Log.e("user",user+" "+password);
                        callLogin();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(()->{
                    Toast.makeText(LoginActivity.this, getString(R.string.invalid_login_info),Toast.LENGTH_LONG).show();
                    btn_change_mode.setEnabled(true);
                    btn_login.setEnabled(true);
                });
            }
        }).start();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                if (is_pass_mode){
                    if (name_txt.getText().toString().isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "User name cannot be blank.", Toast.LENGTH_LONG).show();
                        return;
                    } else if (pass_txt.getText().toString().isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Password cannot be blank.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    user = name_txt.getText().toString();
                    password = pass_txt.getText().toString();
                    new Thread(this::callLogin).start();
                }else {
                    if (mac_address.getText().toString().isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Mac address cannot be blank.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    macLogin(mac_address.getText().toString());
                }

                break;
            case R.id.checkbox:
                is_remember = checkBox.isChecked();
                break;
            case R.id.logo:
                showVpnPinDlg();
                break;
            case R.id.btn_change_mode:
                toggleMode(!is_pass_mode);
                break;
        }
    }

    private void toggleMode(boolean b) {
        is_pass_mode = b;
        if (is_pass_mode){
            lay_user_pass.setVisibility(View.VISIBLE);
            lay_mac.setVisibility(View.GONE);
            btn_change_mode.setText(R.string.press_here_to_login_with_mac_address);
            if(MyApp.instance.getPreference().get(Constants.getLoginInfo())!=null){
                LoginModel loginModel = (LoginModel) MyApp.instance.getPreference().get(Constants.getLoginInfo());
                user = loginModel.getUser_name();
                password = loginModel.getPassword();
                name_txt.setText(user);
                pass_txt.setText(password);
                checkBox.setChecked(true);
                new Thread(this::callLogin).start();
            }
//            name_txt.setText("stetest");
//            pass_txt.setText("uOpRcmMM");
        }else {
            lay_user_pass.setVisibility(View.GONE);
            lay_mac.setVisibility(View.VISIBLE);
            mac_address.setText(MyApp.mac_address);
//            mac_address.setText("00:1A:79:0E:38:B7");
            btn_change_mode.setText(R.string.press_here_to_login_with_user_pass);
            if (MyApp.instance.getPreference().get(Constants.getMacAddress()) != null) {
                checkBox.setChecked(true);
                macLogin(MyApp.mac_address);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View view = getCurrentFocus();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                    showVpnPinDlg();
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void showVpnPinDlg() {
        VPNPinDlg pinDlg = new VPNPinDlg(LoginActivity.this, new VPNPinDlg.DlgPinListener() {
            @Override
            public void OnYesClick(Dialog dialog, String pin_code) {
                String pin = Constants.GetPin4(LoginActivity.this);
                if(pin_code.equalsIgnoreCase(pin)){
                    dialog.dismiss();
                    startActivity(new Intent(LoginActivity.this,VpnActivity.class));
                }else {
                    Toast.makeText(LoginActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void OnCancelClick(Dialog dialog, String pin_code) {
                dialog.dismiss();
            }
        });
        pinDlg.show();
    }
}
