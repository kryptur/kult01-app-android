package de.lbader.apps.kult01;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    public WebView myWebView;
    private WebAppInterface mWebApp;
    public SwipeRefreshLayout swipeContainer;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    public JSONArray navigation;
    private int lvl1, lvl2, level;

    // New navigation
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ViewSwitcher viewSwitcher;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        myWebView.saveState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = getString(R.string.app_title);
        toolbar.setTitle(mTitle);
        setSupportActionBar(toolbar);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        myWebView = (WebView) findViewById(R.id.webView);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);


        mWebApp = new WebAppInterface(this);
        myWebView.addJavascriptInterface(mWebApp, "App");

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString("Kult01-App");
        myWebView.setWebChromeClient(new WebChromeClient());
        myWebView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri parsed = Uri.parse(url);

                if (url.startsWith("mailto:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, parsed);
                    startActivity(intent);
                    return true;
                } else if (!url.contains("http://kult01.de/") && !url.contains("http://www.kult01.de/")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, parsed);
                    startActivity(intent);
                    return true;
                } else {
                    swipeContainer.setEnabled(true);
                    view.loadUrl(url);
                    return false;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                swipeContainer.setRefreshing(true);
            }
        });

        myWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        // Parse param
        if (getIntent().getData() != null) {
            Uri data = getIntent().getData();
            String scheme = data.getScheme();
            String path = data.getEncodedSchemeSpecificPart();
            myWebView.loadUrl(scheme + "://" + path);
        } else {
            if (savedInstanceState != null) {
                myWebView.restoreState(savedInstanceState);
            } else {
                myWebView.loadUrl("http://kult01.de");
            }
        }




        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myWebView.reload();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeContainer.setEnabled(true);



        navigation = null;
        navigationView.getMenu().add("Navigation wird geladen...");
        new RequestTask("navi", new RequestTask.RequestCallback() {
            @Override
            public void callback(JSONObject res) {
                if (res != null) {
                    try {
                        navigation = res.getJSONArray("data");
                        lvl1 = 0;
                        lvl2 = 0;
                        level = 0;
                        initNavi();
                    } catch (JSONException ex) {
                        Log.e("JSON", ex.getMessage());
                    }
                }
            }
        }).execute("");

        ImageView helpButton = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.help_button);

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myWebView.loadUrl("http://kult01.de/app-nutzung-android.aspx");
                drawerLayout.closeDrawers();
            }
        });
    }

    private void initNavi() throws JSONException {
        JSONArray elements = navigation;

        String title;
        JSONObject elem;

        Menu topM = navigationView.getMenu();
        topM.clear();

        buildMenu(topM, elements, "");
    }

    private void buildMenu(Menu topMenu, JSONArray elements, String category) throws JSONException {
        String title;

        Menu menu = topMenu;

        if (level > 0) {
            topMenu.add("Zurück").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                 @Override
                 public boolean onMenuItemClick(MenuItem item) {
                     level--;
                     showNavi();
                     return false;
                 }
            });
            menu = topMenu.addSubMenu(category);
        }



        for (int i = 0; i < elements.length(); ++i) {
            final JSONObject elem = elements.getJSONObject(i);
            title = elem.getString("title").replace("<br />", "\n");
            /*if (elem.getJSONArray("children").length() > 0) {
                SubMenu subMenu = topMenu.addSubMenu(title);
                buildMenu(subMenu, elem.getJSONArray("children"));
            } else {
                topMenu.add(title);
            }*/
            final int id = i;
            final boolean hasSub = elem.getJSONArray("children").length() > 0;
            MenuItem item = menu.add(title);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    try {
                        if (hasSub) {
                            switch (level) {
                                case 0:
                                    lvl1 = id;
                                    level++;
                                    break;
                                case 1:
                                    lvl2 = id;
                                    level++;
                                    break;
                                default:
                                    break;
                            }
                            showNavi();
                        } else {
                            String link = elem.getString("link");
                            if (!link.equals("#")) {
                                myWebView.loadUrl("http://kult01.de/" + link);
                                drawerLayout.closeDrawers();
                            }
                        }
                    } catch (JSONException ex) {

                    }
                    return false;

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_infobox:
                myWebView.loadUrl("javascript:toggleInfobox()");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showNavi() {
        JSONArray elements = navigation;
        String title = "";
        try {
            switch (level) {
                case 0:
                    elements = navigation;
                    break;
                case 1:
                    elements = navigation.getJSONObject(lvl1).getJSONArray("children");
                    title = navigation.getJSONObject(lvl1).getString("title").replace("<br />", "\n");
                    break;
                case 2:
                    elements = navigation.getJSONObject(lvl1).getJSONArray("children").getJSONObject(lvl2).getJSONArray("children");
                    title = navigation.getJSONObject(lvl1).getJSONArray("children").getJSONObject(lvl2).getString("title").replace("<br />", "\n");
            }

            Menu menu = navigationView.getMenu();
            menu.clear();
            buildMenu(menu, elements, title);
        } catch (JSONException ex) {
            Log.e("JSON", ex.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            finish();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public int getStatusBarHeight() {
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            return getResources().getDimensionPixelSize(resId);
        } else {
            return 0;
        }
    }



}
