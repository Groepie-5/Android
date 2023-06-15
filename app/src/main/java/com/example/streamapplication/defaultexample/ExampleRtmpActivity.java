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

package com.example.streamapplication.defaultexample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.streamapplication.ChatAdapter;
import com.example.streamapplication.MainActivity;
import com.example.streamapplication.RetrofitAPI;
import com.example.streamapplication.models.Message;
import com.example.streamapplication.models.StreamSession;
import com.example.streamapplication.models.TransparantPerson;
import com.example.streamapplication.models.TruYouAccount;
import com.google.gson.Gson;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.example.streamapplication.R;
import com.example.streamapplication.utils.PathUtils;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

public class ExampleRtmpActivity extends AppCompatActivity
    implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback {

  private RtmpCamera1 rtmpCamera1;
  private Button startStopButton;
  private RecyclerView chatBox;
  private ArrayList<Message> mMessages;
  private final Gson gson = new Gson();
  protected ChatAdapter chatAdapter;

  private Socket mSocket;
  {
    try {
      mSocket = IO.socket("http://145.49.6.101:3000");
    } catch (URISyntaxException e) {
      System.out.println(e);
    }
  }
  private EditText etUrl;
  private EditText chatInput;

  private String currentDateAndTime = "";
  private File folder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_example);
    folder = PathUtils.getRecordPath();
    SurfaceView surfaceView = findViewById(R.id.surfaceView);

    // Initialize dataset, this data would usually come from a local content provider or
    // remote server.
    initDataset();

    startStopButton = findViewById(R.id.b_start_stop);
    startStopButton.setOnClickListener(this);

    Button sendButton = findViewById(R.id.send_button);
    sendButton.setOnClickListener(this);

    chatBox = findViewById(R.id.chat_box);
    chatBox.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    chatAdapter = new ChatAdapter(mMessages);
    chatBox.setAdapter(chatAdapter);

    Button switchCamera = findViewById(R.id.switch_camera);
    switchCamera.setOnClickListener(this);

    etUrl = findViewById(R.id.et_rtp_url);
    etUrl.setHint(R.string.hint_rtmp);

    chatInput = findViewById(R.id.chat_input);

    rtmpCamera1 = new RtmpCamera1(surfaceView, this);
    rtmpCamera1.setReTries(10);

    surfaceView.getHolder().addCallback(this);

    Intent intent = getIntent();
    String streamerName = intent.getStringExtra("streamerName"); //if it's a string you stored.
    String secretKey = intent.getStringExtra("secretKey"); //if it's a string you stored.

    Toast toast = Toast.makeText(this /* MyActivity */, "Your streamername is: "+ streamerName + "\n Your Secretkey is: "+ secretKey, Toast.LENGTH_LONG);
    toast.show();

    mSocket.on("message-broadcast", onNewMessage);
    mSocket.connect();
  }

  private void initDataset() {
    mMessages = new ArrayList<>();
  }

  @Override
  public void onConnectionStartedRtmp(String rtmpUrl) {
  }

  @Override
  public void onConnectionSuccessRtmp() {
    runOnUiThread(() -> Toast.makeText(ExampleRtmpActivity.this, "Connection success", Toast.LENGTH_SHORT).show());
  }

  @Override
  public void onConnectionFailedRtmp(final String reason) {
    runOnUiThread(() -> {
      if (rtmpCamera1.reTry(5000, reason, null)) {
        Toast.makeText(ExampleRtmpActivity.this, "Retry", Toast.LENGTH_SHORT)
            .show();
      } else {
        Toast.makeText(ExampleRtmpActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
            .show();
        rtmpCamera1.stopStream();
        startStopButton.setText(R.string.start_button);
      }
    });
  }

  @Override
  public void onNewBitrateRtmp(final long bitrate) {

  }

  @Override
  public void onDisconnectRtmp() {
    runOnUiThread(() -> Toast.makeText(ExampleRtmpActivity.this, "Disconnected", Toast.LENGTH_SHORT).show());
  }

  @Override
  public void onAuthErrorRtmp() {
    runOnUiThread(() -> {
      Toast.makeText(ExampleRtmpActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
      rtmpCamera1.stopStream();
      startStopButton.setText(R.string.start_button);
    });
  }

  @Override
  public void onAuthSuccessRtmp() {
    runOnUiThread(() -> Toast.makeText(ExampleRtmpActivity.this, "Auth success", Toast.LENGTH_SHORT).show());
  }

  @Override
  public void onClick(View view) {
    int id = view.getId();
    if (id == R.id.b_start_stop) {

      final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
      LayoutInflater inflater = this.getLayoutInflater();
      View dialogView = inflater.inflate(R.layout.streamsession_view, null);

      final EditText editText = (EditText) dialogView.findViewById(R.id.tagline);
      Button btn_submit = (Button) dialogView.findViewById(R.id.btn_submit);
      Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_cancel);

      SharedPreferences userInfo = this.getSharedPreferences("application", Context.MODE_PRIVATE);
      String storedName = userInfo.getString("NAME", null);
      String storedStreamTitle = userInfo.getString("STREAM-TITLE", null);

      Retrofit retrofit = new Retrofit.Builder()
              .baseUrl("http://localhost:8000/") // Replace with your API's base URL
              .addConverterFactory(GsonConverterFactory.create())
              .build();

      RetrofitAPI apiService = retrofit.create(RetrofitAPI.class);

      StreamSession streamSession = new StreamSession(
              storedName,
              storedStreamTitle
      );

      if(storedStreamTitle != null){
        editText.setText(storedStreamTitle);
      }

      btn_submit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          //todo: check if usernamehash = usernamehash = auth = ja

          SharedPreferences.Editor editor = userInfo.edit();
          editor.putString("STREAM-TITLE", editText.toString());
          //post/put req change user Stream title
          Call<StreamSession> call = apiService.updateData(streamSession);
          call.enqueue(new Callback<StreamSession>() {
            @Override
            public void onResponse(Call<StreamSession> call, Response<StreamSession> response) {
              // Handle successful response
              if (response.isSuccessful()) {
                // Request successful, handle response here
              } else {
                // Request failed, handle error here
              }
            }
            @Override
            public void onFailure(Call<StreamSession> call, Throwable t) {
              // Handle network failure
            }
          });
          dialogBuilder.dismiss();
        }
      });

      btn_cancel.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          dialogBuilder.dismiss();
        }
      });

      dialogBuilder.setView(dialogView);
      dialogBuilder.show();

      if (!rtmpCamera1.isStreaming()) {
        if (rtmpCamera1.isRecording()
                || rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
          startStopButton.setText(R.string.stop_button);
          rtmpCamera1.startStream(etUrl.getText().toString());
//          rtmpCamera1.startStream("rtmp://145.49.6.220:1935/live/STREAM_NAME2");
        } else {
          Toast.makeText(this, "Error preparing stream, This device cant do it",
                  Toast.LENGTH_SHORT).show();
        }
      } else {
        startStopButton.setText(R.string.start_button);
        rtmpCamera1.stopStream();
      }
    } else if (id == R.id.switch_camera) {
      try {
        rtmpCamera1.switchCamera();
      } catch (CameraOpenException e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
      }
    } else if (id == R.id.send_button) {
        String messageText = chatInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
          return;
        }

        //TODO: Replace with dynamic sender and receiver
        TransparantPerson receiver = new TransparantPerson(0, "Kaas");
        TruYouAccount sender = new TruYouAccount(1, "Jan");
        Message message = new Message(messageText, sender, receiver, new Date());

        mSocket.emit("message", gson.toJson(message));
        chatInput.setText("");
        addMessage(message);
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {

  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    rtmpCamera1.startPreview();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    if (rtmpCamera1.isRecording()) {
      rtmpCamera1.stopRecord();
      PathUtils.updateGallery(this, folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
      Toast.makeText(this,
          "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
          Toast.LENGTH_SHORT).show();
      currentDateAndTime = "";
    }

    if (rtmpCamera1.isStreaming()) {
      rtmpCamera1.stopStream();
      startStopButton.setText(getResources().getString(R.string.start_button));
    }

    mSocket.disconnect();
    mSocket.off("message-broadcast", onNewMessage);

    rtmpCamera1.stopPreview();
  }
  private void addMessage(Message message) {
    mMessages.add(message);
    chatAdapter.notifyItemInserted(mMessages.size() - 1);
    chatBox.scrollToPosition(chatAdapter.getItemCount() - 1);
  }
  private final Emitter.Listener onNewMessage = args -> runOnUiThread(() -> {
    Message message = gson.fromJson(args[0].toString(), Message.class);
    addMessage(message);
  });
}

