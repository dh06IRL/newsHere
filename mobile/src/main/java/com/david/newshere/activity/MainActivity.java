package com.david.newshere.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.david.newshere.R;
import com.david.newshere.adapter.RssAdapter;
import com.david.newshere.rss.RssItem;
import com.david.newshere.rss.RssReader;
import com.david.newshere.utils.SystemBarTintManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.pkmmte.pkrss.Article;
import com.pkmmte.pkrss.Callback;
import com.pkmmte.pkrss.PkRSS;
import com.pkmmte.pkrss.parser.Rss2Parser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    LocationClient mPlayServicesLocationClient;
    Context mContext;
    ActionBar actionBar;
    String zipCode;

    GridView grid;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

        brandGlowEffect(mContext, mContext.getResources().getColor(R.color.main_color));

        if(Build.VERSION.SDK_INT == 19){
            setTranslucentNav(true);
            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(MainActivity.this);
            systemBarTintManager.setStatusBarTintEnabled(true);
            systemBarTintManager.setStatusBarTintColor(getResources().getColor(R.color.main_color_dark));
            systemBarTintManager.setNavigationBarTintEnabled(true);
            systemBarTintManager.setNavigationBarTintColor(getResources().getColor(R.color.main_color_dark));
        }

        grid = (GridView) findViewById(R.id.grid);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);

        swipeRefreshLayout.setColorSchemeResources(R.color.main_color, R.color.main_color_dark, R.color.main_color, R.color.main_color_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLocation();
            }
        });

        getLocation();

    }

    public void getLocation(){
        mPlayServicesLocationClient = new LocationClient(mContext, new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                // client has connected, so lets get the location
                if (!mPlayServicesLocationClient.isConnecting()) {
                    final Location lastLocation = mPlayServicesLocationClient.getLastLocation();

                    // disconnect the client to end networking
                    mPlayServicesLocationClient.disconnect();
                    mPlayServicesLocationClient = null;

                    try {
                        Log.d("loc", lastLocation.getLatitude() + "");
                        Log.d("loc", lastLocation.getLongitude() + "");
                        CityAsyncTask cityAsyncTask = new CityAsyncTask(mContext, lastLocation.getLatitude(), lastLocation.getLongitude());
                        cityAsyncTask.execute();
                    } catch (Exception e) {
                        Log.e("news", e.toString());
                    }
                }else{

                }
            }

            @Override
            public void onDisconnected() {
                // client has disconnected
                mPlayServicesLocationClient = null;
            }

        }, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                // something went wrong, may expand here later
                mPlayServicesLocationClient = null;
                Toast.makeText(mContext, "Error getting location", Toast.LENGTH_SHORT).show();
            }
        });

        // get the connection
        mPlayServicesLocationClient.connect();
    }

    public void getNews(){
        new GetRssFeed().execute("https://itunes.apple.com/WebObjects/MZStore.woa/wpa/MRSS/newreleases/sf=143441/limit=100/rss.xml");

        PkRSS.with(this)
                .load("http://news.google.com/news/feeds?output=rss&geo=" + zipCode)
                .callback(new Callback() {
                              @Override
                              public void OnPreLoad() {
                                  runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          swipeRefreshLayout.setRefreshing(true);
                                      }
                                  });
                              }

                              @Override
                              public void OnLoaded(final List<Article> articles) {
                                  runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          RssAdapter rssAdapter = new RssAdapter(mContext, articles, MainActivity.this);
                                          SwingBottomInAnimationAdapter animCardArrayAdapter = new SwingBottomInAnimationAdapter(rssAdapter);
                                          animCardArrayAdapter.setAbsListView(grid);
                                          grid.setAdapter(animCardArrayAdapter);
                                          swipeRefreshLayout.setRefreshing(false);

                                      }
                                  });

                              }

                              @Override
                              public void OnLoadFailed() {
                                  runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          swipeRefreshLayout.setRefreshing(true);
                                      }
                                  });
                              }
                          }

                )
                            .

                    async();
                }

        public class CityAsyncTask extends AsyncTask<String, String, String> {
        Context mContext;
        double latitude;
        double longitude;

        public CityAsyncTask(Context mContext, double latitude, double longitude) {
            this.mContext = mContext;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude,
                        longitude, 10);
                Log.d("weather", "result size: " + addresses.size());
                result = addresses.get(0).getLocality();
                if (addresses.size() > 0) {
                    zipCode = addresses.get(0).getPostalCode();
                    int count = 0;
                    while (zipCode == null && count < addresses.size()) {
                        zipCode = addresses.get(count).getPostalCode();
                        count++;
                    }
                    Log.d("weather", "Zipcode: " + zipCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                Log.d("weather", result);
            } catch (Exception e) {
                Log.e("weather", e.toString());
            }
            getSupportActionBar().setTitle(getString(R.string.app_name) + " - " + zipCode);
            getNews();
        }
    }

    static void brandGlowEffect(Context context, int brandColor) {
        //glow
        try {
            int glowDrawableId = context.getResources().getIdentifier("overscroll_glow", "drawable", "android");
            Drawable androidGlow = context.getResources().getDrawable(glowDrawableId);
            androidGlow.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
            //edge
            int edgeDrawableId = context.getResources().getIdentifier("overscroll_edge", "drawable", "android");
            Drawable androidEdge = context.getResources().getDrawable(edgeDrawableId);
            androidEdge.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
        }catch (Exception e){
            Log.e("parkfans", e.toString());
        }
    }

    @TargetApi(19)
    private void setTranslucentNav(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        final int bits1 = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        if (on) {
            winParams.flags |= bits;
            winParams.flags |= bits1;
        } else {
            winParams.flags &= ~bits;
            winParams.flags &= ~bits1;
        }
        win.setAttributes(winParams);
    }

    private class GetRssFeed extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                RssReader rssReader = new RssReader(params[0]);
                for (RssItem item : rssReader.getItems())
                    Log.v("data", item.getTitle() + " " + item.getImageUrl());
            } catch (Exception e) {
                Log.v("Error Parsing Data", e + "");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            adapter.notifyDataSetChanged();
//            mList.setAdapter(adapter);
        }
    }
}
