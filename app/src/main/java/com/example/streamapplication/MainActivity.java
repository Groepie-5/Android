/*
 * Copyright (C) 2021 pedroSG94.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.streamapplication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.pedro.rtpstreamer.backgroundexample.BackgroundActivity;
//import com.pedro.rtpstreamer.customexample.RtmpActivity;
//import com.pedro.rtpstreamer.customexample.RtspActivity;
//import com.pedro.rtpstreamer.defaultexample.ExampleRtmpActivity;
//import com.pedro.rtpstreamer.defaultexample.ExampleRtspActivity;
//import com.pedro.rtpstreamer.displayexample.DisplayActivity;
//import com.pedro.rtpstreamer.filestreamexample.RtmpFromFileActivity;
//import com.pedro.rtpstreamer.filestreamexample.RtspFromFileActivity;
//import com.pedro.rtpstreamer.openglexample.OpenGlRtmpActivity;
//import com.pedro.rtpstreamer.openglexample.OpenGlRtspActivity;
//import com.pedro.rtpstreamer.rotation.RotationExampleActivity;
//import com.pedro.rtpstreamer.surfacemodeexample.SurfaceModeRtmpActivity;
//import com.pedro.rtpstreamer.surfacemodeexample.SurfaceModeRtspActivity;
//import com.pedro.rtpstreamer.texturemodeexample.TextureModeRtmpActivity;
//import com.pedro.rtpstreamer.texturemodeexample.TextureModeRtspActivity;
//import com.pedro.rtpstreamer.utils.ActivityLink;
//import com.pedro.rtpstreamer.utils.ImageAdapter;

import java.util.List;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import com.example.streamapplication.defaultexample.ExampleRtmpActivity;
import com.example.streamapplication.utils.*;
import com.pedro.rtsp.BuildConfig;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private GridView list;

    private Button btn;
    private List<ActivityLink> activities;

    private final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private final String[] PERMISSIONS_A_13 = {
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(getString(R.string.version, BuildConfig.VERSION_NAME));

//        list = findViewById(R.id.list);
//        createList();
//        setListAdapter(activities);
        btn = findViewById(R.id.tv_start_streaming);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showAlertDialog();
            }
        });
        requestPermissions();
    }

    public void showAlertDialog() {
        AlertDialog.Builder authenticate = new AlertDialog.Builder(this);
        authenticate.setTitle("Authenticate Yourself");
        authenticate.setMessage("Input your Streamer Name and Secret Key if it is not set.");
        Context context = authenticate.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

// Add a TextView here for the "Title" label, as noted in the comments
        final EditText nameBox = new EditText(context);
        nameBox.setHint("Streamer Name");
        layout.addView(nameBox); // Notice this is an add method

// Add another TextView here for the "Description" label
        final EditText secretKeyBox = new EditText(context);
        secretKeyBox.setHint("Secret Key");
        layout.addView(secretKeyBox); // Another add method
        authenticate.setView(layout);
        SharedPreferences userInfo = this.getSharedPreferences("application", Context.MODE_PRIVATE);
        String storedName = userInfo.getString("NAME", null);
        String storedKey = userInfo.getString("KEY", null);
        if (!(storedName == null) && !(storedKey == null)) {
            nameBox.setText(storedName);
            secretKeyBox.setText(storedKey);
        }
        authenticate.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String streamerName = nameBox.getText().toString();
                String secretKey = secretKeyBox.getText().toString();
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putString("NAME", streamerName);
                editor.putString("KEY", secretKey);
                editor.apply();

                if (streamerName.equals("bob") && secretKey.equals("geheim")) {

                    Intent activityChangeIntent = new Intent(MainActivity.this, ExampleRtmpActivity.class);
                    activityChangeIntent.putExtra("streamerName", streamerName); //Optional parameters
                    activityChangeIntent.putExtra("secretKey", secretKey); //Optional parameters
                    MainActivity.this.startActivity(activityChangeIntent);
                    overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
                } else {
                    Toast toast = Toast.makeText(MainActivity.this, "Not Validated", Toast.LENGTH_LONG);
                    toast.show();
                }

            }
        });
        authenticate.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        authenticate.create().show();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermissions(this)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_A_13, 1);
            }
        } else {
            if (!hasPermissions(this)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
            }
        }
    }

//    @SuppressLint("NewApi")
//    private void createList() {
//        activities = new ArrayList<>();
//        activities.add(new ActivityLink(new Intent(this, ExampleRtmpActivity.class),
//                getString(R.string.default_rtmp), JELLY_BEAN));
//        activities.add(new ActivityLink(new Intent(this, ExampleRtspActivity.class),
//                getString(R.string.default_rtsp), JELLY_BEAN));
//    }

//    private void setListAdapter(List<ActivityLink> activities) {
//        list.setAdapter(new ImageAdapter(activities));
//        list.setOnItemClickListener(this);
//    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (hasPermissions(this)) {
            ActivityLink link = activities.get(i);
            int minSdk = link.getMinSdk();
            if (Build.VERSION.SDK_INT >= minSdk) {
                startActivity(link.getIntent());
                overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
            } else {
                showMinSdkError(minSdk);
            }
        } else {
            showPermissionsErrorAndRequest();
        }
    }

    private void showMinSdkError(int minSdk) {
        String named;
        switch (minSdk) {
            case JELLY_BEAN_MR2:
                named = "JELLY_BEAN_MR2";
                break;
            case LOLLIPOP:
                named = "LOLLIPOP";
                break;
            default:
                named = "JELLY_BEAN";
                break;
        }
        Toast.makeText(this, "You need min Android " + named + " (API " + minSdk + " )",
                Toast.LENGTH_SHORT).show();
    }

    private void showPermissionsErrorAndRequest() {
        Toast.makeText(this, "You need permissions before", Toast.LENGTH_SHORT).show();
        requestPermissions();
    }

    private boolean hasPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return hasPermissions(context, PERMISSIONS_A_13);
        } else {
            return hasPermissions(context, PERMISSIONS);
        }
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}