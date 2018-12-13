package com.regis.cosnier.emu48;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int INTENT_SETTINGS = 1;
    private static final String TAG = "MainActivity";
    private MainScreenView mainScreenView;
    SharedPreferences sharedPreferences;
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        ViewGroup mainScreenContainer = (ViewGroup)findViewById(R.id.main_screen_container);
        mainScreenView = new MainScreenView(this); //, currentProject);
//        mainScreenView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
//                    if(motionEvent.getY() < 0.3f * mainScreenView.getHeight()) {
//                        if(toolbar.getVisibility() == View.GONE)
//                            toolbar.setVisibility(View.VISIBLE);
//                        else
//                            toolbar.setVisibility(View.GONE);
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
        toolbar.setVisibility(View.GONE);
        mainScreenContainer.addView(mainScreenView, 0);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateFromPreferences();
        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                updateFromPreferences();
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);


        AssetManager assetManager = getResources().getAssets();
        NativeLib.start(assetManager, mainScreenView.getBitmapMainScreen(), this, mainScreenView);

        String lastDocumentUrl = sharedPreferences.getString("lastDocument", "");
        if(lastDocumentUrl.length() > 0)
            NativeLib.onFileOpen(lastDocumentUrl);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            OnSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_new) {
            OnFileNew();
        } else if (id == R.id.nav_open) {
            OnFileOpen();
        } else if (id == R.id.nav_save) {
            OnFileSave();
        } else if (id == R.id.nav_save_as) {
            OnFileSaveAs();
        } else if (id == R.id.nav_close) {
            OnFileClose();
        } else if (id == R.id.nav_settings) {
            OnSettings();
        } else if (id == R.id.nav_load_object) {
            OnObjectLoad();
        } else if (id == R.id.nav_save_object) {
            OnObjectSave();
        } else if (id == R.id.nav_copy_screen) {
            OnViewCopy();
        } else if (id == R.id.nav_copy_stack) {
            OnStackCopy();
        } else if (id == R.id.nav_paste_stack) {
            OnStackPaste();
        } else if (id == R.id.nav_reset_calculator) {
            OnViewReset();
        } else if (id == R.id.nav_backup_save) {
            OnBackupSave();
        } else if (id == R.id.nav_backup_restore) {
            OnBackupRestore();
        } else if (id == R.id.nav_backup_delete) {
            OnBackupDelete();
        } else if (id == R.id.nav_change_kml_script) {
            OnViewScript();
        } else if (id == R.id.nav_help) {
            OnTopics();
        } else if (id == R.id.nav_about) {
            OnAbout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class KMLScriptItem {
        public String filename;
        public String title;
        public String model;
    }
    ArrayList<KMLScriptItem> kmlScripts;

    private void OnFileNew() {
        if(kmlScripts == null) {
            kmlScripts = new ArrayList<>();
            AssetManager assetManager = getAssets();
            String[] calculatorsAssetFilenames = new String[0];
            try {
                calculatorsAssetFilenames = assetManager.list("calculators");
            } catch (IOException e) {
                e.printStackTrace();
            }
            String cKmlType = null; //"S";
            kmlScripts.clear();
            Pattern patternGlobalTitle = Pattern.compile("\\s*Title\\s+\"(.*)\"");
            Pattern patternGlobalModel = Pattern.compile("\\s*Model\\s+\"(.*)\"");
            Matcher m;
            for (String calculatorsAssetFilename : calculatorsAssetFilenames) {
                if (calculatorsAssetFilename.toLowerCase().lastIndexOf(".kml") != -1) {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(assetManager.open("calculators/" + calculatorsAssetFilename), "UTF-8"));
                        // do reading, usually loop until end of file reading
                        String mLine;
                        boolean inGlobal = false;
                        String title = null;
                        String model = null;
                        while ((mLine = reader.readLine()) != null) {
                            //process line
                            if (mLine.indexOf("Global") == 0) {
                                inGlobal = true;
                                title = null;
                                model = null;
                                continue;
                            }
                            if (inGlobal) {
                                if (mLine.indexOf("End") == 0) {
                                    KMLScriptItem newKMLScriptItem = new KMLScriptItem();
                                    newKMLScriptItem.filename = calculatorsAssetFilename;
                                    newKMLScriptItem.title = title;
                                    newKMLScriptItem.model = model;
                                    kmlScripts.add(newKMLScriptItem);
                                    title = null;
                                    model = null;
                                    break;
                                }

                                m = patternGlobalTitle.matcher(mLine);
                                if (m.find()) {
                                    title = m.group(1);
                                }
                                m = patternGlobalModel.matcher(mLine);
                                if (m.find()) {
                                    model = m.group(1);
                                }
                            }
                        }
                    } catch (IOException e) {
                        //log the exception
                        e.printStackTrace();
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                //log the exception
                            }
                        }
                    }
                }
            }
            Collections.sort(kmlScripts, new Comparator<KMLScriptItem>() {
                @Override
                public int compare(KMLScriptItem lhs, KMLScriptItem rhs) {
                    return lhs.title.compareTo(rhs.title);
                }
            });
        }


        final String[] kmlScriptTitles = new String[kmlScripts.size()];
        for (int i = 0; i < kmlScripts.size(); i++)
            kmlScriptTitles[i] = kmlScripts.get(i).title;
        new AlertDialog.Builder(this)
            .setTitle("Pick a calculator")
            .setItems(kmlScriptTitles, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String kmlScriptFilename = kmlScripts.get(which).filename;
                    NativeLib.onFileNew(kmlScriptFilename);
                }
            }).show();
    }

    public static int INTENT_GETOPENFILENAME = 1;
    public static int INTENT_GETSAVEFILENAME = 2;

    private void OnFileOpen() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.setType("YOUR FILETYPE"); //not needed, but maybe usefull
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "emu48-state.e48");
        startActivityForResult(intent, INTENT_GETOPENFILENAME);
    }
    private void OnFileSave() {
        NativeLib.onFileSave();
    }
    private void OnFileSaveAs() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        int model = NativeLib.getCurrentModel();
        String extension = "e48"; // HP48SX/GX
        switch (model) {
            case '6':
            case 'A':
                extension = "e38"; // HP38G
                break;
            case 'E':
                extension = "e39"; // HP39/40G
                break;
            case 'X':
                extension = "e49"; // HP49G
                break;
        }
        intent.putExtra(Intent.EXTRA_TITLE, "emu48-state." + extension);
        startActivityForResult(intent, INTENT_GETSAVEFILENAME);
    }
    private void OnFileClose() {
        NativeLib.onFileClose();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastDocument", "");
        editor.commit();
    }
    private void OnSettings() {
        startActivityForResult(new Intent(this, SettingsActivity.class), INTENT_SETTINGS);
    }

    private void OnObjectLoad() {
        NativeLib.onObjectLoad();

    }
    private void OnObjectSave() {
        NativeLib.onObjectSave();

    }
    private void OnViewCopy() {
        NativeLib.onViewCopy();

    }
    private void OnStackCopy() {
        //https://developer.android.com/guide/topics/text/copy-paste
        NativeLib.onStackCopy();

    }
    private void OnStackPaste() {
        NativeLib.onStackPaste();

    }
    private void OnViewReset() {
        NativeLib.onViewReset();

    }
    private void OnBackupSave() {
        NativeLib.onBackupSave();

    }
    private void OnBackupRestore() {
        NativeLib.onBackupRestore();

    }
    private void OnBackupDelete() {
        NativeLib.onBackupDelete();

    }
    private void OnViewScript() {
        //NativeLib.onViewScript();
    }
    private void OnTopics() {
    }
    private void OnAbout() {
    }

    @Override
    protected void onDestroy() {

        NativeLib.stop();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == Activity.RESULT_OK) {

            if(requestCode == INTENT_GETOPENFILENAME) {
                Uri uri = data.getData();

                //just as an example, I am writing a String to the Uri I received from the user:
                Log.d(TAG, "onActivityResult INTENT_GETOPENFILENAME " + uri.toString());
                String url = uri.toString();
                if(NativeLib.onFileOpen(url) != 0) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("lastDocument", url);
                    editor.commit();
                }
            } else if(requestCode == INTENT_GETSAVEFILENAME) {
                Uri uri = data.getData();

                //just as an example, I am writing a String to the Uri I received from the user:
                Log.d(TAG, "onActivityResult INTENT_GETSAVEFILENAME " + uri.toString());
                String url = uri.toString();
                if(NativeLib.onFileSaveAs(url) != 0) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("lastDocument", url);
                    editor.commit();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    final int GENERIC_READ   = 1;
    final int GENERIC_WRITE  = 2;
    int openFileFromContentResolver(String url, int writeAccess) {
        //https://stackoverflow.com/a/31677287
        Uri uri = Uri.parse(url);
        ParcelFileDescriptor filePfd;
        try {
            String mode = "";
            if((writeAccess & GENERIC_READ) == GENERIC_READ)
                mode += "r";
            if((writeAccess & GENERIC_WRITE) == GENERIC_WRITE)
                mode += "w";
            filePfd = getContentResolver().openFileDescriptor(uri, mode);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        int fd = filePfd != null ? filePfd.getFd() : 0;
        return fd;
    }

    private void updateFromPreferences() {
        //int settingsInput = Integer.parseInt(sharedPreferences.getString("settings_input", "0"));

        boolean settingsRealspeed = sharedPreferences.getBoolean("settings_realspeed", false);
        boolean settingsGrayscale = sharedPreferences.getBoolean("settings_grayscale", false);
//        boolean settingsAlwaysontopt = sharedPreferences.getBoolean("settings_alwaysontop", false);
//        boolean settingsActfollowsmouset = sharedPreferences.getBoolean("settings_actfollowsmouse", false);
//        boolean settingsSingleinstancet = sharedPreferences.getBoolean("settings_singleinstance", false);
        boolean settingsAutosave = sharedPreferences.getBoolean("settings_autosave", false);
        boolean settingsAutosaveonexit = sharedPreferences.getBoolean("settings_autosaveonexit", false);
        boolean settingsObjectloadwarning = sharedPreferences.getBoolean("settings_objectloadwarning", false);
        boolean settingsAlwaysdisplog = sharedPreferences.getBoolean("settings_alwaysdisplog", false);
        boolean settingsPort1en = sharedPreferences.getBoolean("settings_port1en", false);
        boolean settingsPort1wr = sharedPreferences.getBoolean("settings_port1wr", false);
        boolean settingsPort2en = sharedPreferences.getBoolean("settings_port2en", false);
        boolean settingsPort2wr = sharedPreferences.getBoolean("settings_port2wr", false);
        String settingsPort2load = sharedPreferences.getString("settings_port2load", "");

        NativeLib.setConfiguration(settingsRealspeed ? 1 : 0, settingsGrayscale ? 1 : 0, settingsAutosave ? 1 : 0,
            settingsAutosaveonexit ? 1 : 0, settingsObjectloadwarning ? 1 : 0, settingsAlwaysdisplog ? 1 : 0,
            settingsPort1en ? 1 : 0, settingsPort1wr ? 1 : 0,
            settingsPort2en ? 1 : 0, settingsPort2wr ? 1 : 0, settingsPort2load);
    }
}
