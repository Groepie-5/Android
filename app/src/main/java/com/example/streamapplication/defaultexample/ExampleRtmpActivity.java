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

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.streamapplication.ChatAdapter;
import com.example.streamapplication.models.Message;
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


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

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
      mSocket = IO.socket("http://145.49.21.153:3000");
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
    mSocket.on("valid-message", onRejectedMessage);
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

        String payload = getNonce(message.getSender()) + ";" + message.getMessageText();

      try {
        message.setSignature(Sign(payload));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      Log.e("TESTER1234",gson.toJson(message));
      Log.e("TESTER1234","Nee Man");


      mSocket.emit("message", gson.toJson(message));
        chatInput.setText("");
        addMessage(message);
    }
  }

  public String Sign(String message) throws Exception {

    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));

    String privateKeyString = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEugIBADANBgkqhkiG9w0BAQEFAASCBKQwggSgAgEAAoIBAQCvbmTflnAOe4WA\n" +
            "ZGsXRAT6JIMOE/m5y7hFhjDlnSgBiYoFzMsI8F2vzbH8jsx2ziq9L9MJ8aBY+Ng2\n" +
            "QhjBSFQoWspOT1WyhxNaS5XgVhcwtgyHL0mDwSEFSvA/lmEOHY90QuCzKUFXLbeU\n" +
            "U2N8AinpnJA0/6MegBxgOZ11pQjXRLX5HuhTeb6bpYAZmpRUkiPN7cy7jEVFQabY\n" +
            "4RH7vvjcmPr+EOzE8RxUTNCleekxcFuIPY3I7oEa9bs7z+ZVPKqKITG/CKFkvRKv\n" +
            "qnybM9Al/tc5TrfsZ4frK1URKd1fCeYYuE7V66dIzGb+poOORCQrXSLfkGZ3Iwes\n" +
            "7oo2hVh9AgMBAAECgf8baGBcWnVqqrVI3ssD9LfREXcKUT8RqDDZkaO1xmCReAw3\n" +
            "DyZqKJmJJNwAzv4TrRHSqI4CCHsYCHJ6xyHmHz4DgP5iLgem96WU68eS42H0oEMU\n" +
            "pXQj5j/MfRB2wWOGFDm5EJ5NG7E15MY2Seu0abfVCONeNHa3JrR2z1SN4DC7BY2U\n" +
            "u3qXP4whwBtdNxKLFVduN123mjyAGrIGMo5Y79xgyqntLxPjS4ZLg8x6QZGqa6Jg\n" +
            "Xa34chUzzneumT2MwvlNmFXYAOBXRqocPubcOooncDdKRdqZ/xRaTuYXC2y7MuMM\n" +
            "o8WLQNTFIzuTRUKqO6qmEm/cqOJ7M5pj/tuSaAECgYEA81uCiWqygQOTDAGyZcaE\n" +
            "nwRHljiZ1o7rRPIlFQgjRgKeY9u7txeYF247OOGS4q4S7qxs+61dXpbZsuxspOEZ\n" +
            "IcJivYjv8jOaT0DQ2yfHPx0UtesquAmdgCVLhD9edZsePTs11Bvf0sV6XjQk7PZu\n" +
            "MiHp5RjREs9CSipUikdf4H0CgYEAuIuC0uL9tCTjgpFh6vp5WAulCF1byGTJe6Cf\n" +
            "A7MNKsueY+GGbdzDWsWhGbcf/InbalHpN20LeOlzAraIc8jwX2NQYYM0552geePP\n" +
            "hmVF2FtTn69rbbqjNaScPUDaIq84urLRGYdOx4zk3IjsC0OOFf1IxvCpt0oHXN+t\n" +
            "orls2AECgYAtMrXP5+03YP6SoE4N8Qz4q8bP87s+ylSP/Zk9isFiY2IfkdQPcWZi\n" +
            "E6sUKXEqgMIragLjy5Mn2kScoqSbCuOsDXphUWmfRk5GofcofP7YjgImt4K7o62I\n" +
            "+2RHL63PkfvPy4t31aWAdAUCMhUZnbthvELAthc+sxfQxoPlGtSH0QKBgDWjFtsE\n" +
            "boi1UArbBoKtWidk+wp7V/nekVEFVjJVEDaoB9kv60pzJ7RyTGiU+Q8FYmh8djRN\n" +
            "1U/HSk43j2FXvcV7sBkncXEAN2w18lM1jB9eK+f1rFuwK0+kEGUdPElodCyPXIb2\n" +
            "9Ma7BKm0giaj8+AgRc7MlAdZ1NoBiQ4KpsABAoGAfEh8XrsGrgqRt+lrGFaYGRZa\n" +
            "N8OMXWGUbPVbF+Qm9Xzzdu1CI76lprJLVu9uTSpttKcxMacxAntgigp8kxEHYtP+\n" +
            "RD0t8nkRaQRtLUXDRRXLEiZf6XaODrdAzaxFgvVrEZTZWeuRXsPWrv1YZeJJMVn2\n" +
            "GVDqyYnglMc365+JA3A=\n" +
            "-----END PRIVATE KEY-----";

    privateKeyString = privateKeyString.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");

    // Decode the Base64-encoded private key string
    byte[] privateKeyBytes = new byte[0];
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
    }

    // Generate the private key object
    PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

    // Create the Cipher instance and initialize it for encryption
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, privateKey);

    // Encrypt the hash
    byte[] encryptedBytes = cipher.doFinal(hash);

    // Encode the encrypted bytes as Base64 for easier storage or transmission
    String encryptedHash = null;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      encryptedHash = Base64.getEncoder().encodeToString(encryptedBytes);
    }

    System.out.println("Encrypted Hash: " + encryptedHash);

    return encryptedHash;
  }

  public String getNonce(String sender) {
    try {
      // Specify the URL endpoint
      URL url = new URL("http://145.49.21.153:3000/api/crypto/nonce");

      // Create the HttpURLConnection object
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Set the request method to POST
      connection.setRequestMethod("POST");

      // Enable output and input streams
      connection.setDoOutput(true);
      connection.setDoInput(true);

      // Set request headers
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Accept", "application/json");

      // Create the request body
      String requestBody = "{\"username\": "+sender+"}";

      // Write the request body to the connection's output stream
      DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
      outputStream.writeBytes(requestBody);
      outputStream.flush();
      outputStream.close();

      // Get the response code
      int responseCode = connection.getResponseCode();
      System.out.println("Response Code: " + responseCode);

      // Read the response
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      StringBuilder response = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();

      // Print the response
      System.out.println("Response: " + response.toString());

      // Disconnect the connection
      connection.disconnect();

      return response.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return e.toString();
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
  private final Emitter.Listener onRejectedMessage = args -> runOnUiThread(() -> {
    Log.e("TESTER1234","Yippie");
    String validMessageText = args[0].toString();
    Log.e("TESTER1234",validMessageText);
    boolean validMessage = Boolean.parseBoolean(validMessageText);
    if (!validMessage) {
      Log.e("TESTER1234","Invalid Message");
      Toast toast = Toast.makeText(this, "Your message was rejected tue to improper authentication.", Toast.LENGTH_LONG);
      toast.show();
    }
  });
}

