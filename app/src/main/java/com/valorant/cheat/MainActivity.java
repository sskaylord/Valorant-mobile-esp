package com.valorant.cheat;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.content.*;
import android.graphics.*;
import android.provider.*;
import android.net.*;
import java.util.*;

public class MainActivity extends Activity {
    private Switch espSwitch;
    private Switch boxSwitch, skeletonSwitch, lineSwitch, healthSwitch;
    private Button launchBtn;
    private TextView statusText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
            }
        }
        
        setupUI();
    }
    
    private void setupUI() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackgroundColor(Color.parseColor("#0a0a0a"));
        
        TextView title = new TextView(this);
        title.setText("VALORANT MOBILE ESP");
        title.setTextColor(Color.parseColor("#ff4444"));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 30);
        layout.addView(title);
        
        LinearLayout espRow = new LinearLayout(this);
        espRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView espLabel = new TextView(this);
        espLabel.setText("ESP: ");
        espLabel.setTextColor(Color.WHITE);
        espLabel.setTextSize(18);
        espSwitch = new Switch(this);
        espSwitch.setChecked(true);
        espRow.addView(espLabel);
        espRow.addView(espSwitch);
        layout.addView(espRow);
        
        LinearLayout boxRow = new LinearLayout(this);
        boxRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView boxLabel = new TextView(this);
        boxLabel.setText("2D KUTU: ");
        boxLabel.setTextColor(Color.WHITE);
        boxSwitch = new Switch(this);
        boxSwitch.setChecked(true);
        boxRow.addView(boxLabel);
        boxRow.addView(boxSwitch);
        layout.addView(boxRow);
        
        LinearLayout skelRow = new LinearLayout(this);
        skelRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView skelLabel = new TextView(this);
        skelLabel.setText("SKELETON: ");
        skelLabel.setTextColor(Color.WHITE);
        skeletonSwitch = new Switch(this);
        skeletonSwitch.setChecked(true);
        skelRow.addView(skelLabel);
        skelRow.addView(skeletonSwitch);
        layout.addView(skelRow);
        
        LinearLayout lineRow = new LinearLayout(this);
        lineRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView lineLabel = new TextView(this);
        lineLabel.setText("CIZGI: ");
        lineLabel.setTextColor(Color.WHITE);
        lineSwitch = new Switch(this);
        lineSwitch.setChecked(true);
        lineRow.addView(lineLabel);
        lineRow.addView(lineSwitch);
        layout.addView(lineRow);
        
        LinearLayout healthRow = new LinearLayout(this);
        healthRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView healthLabel = new TextView(this);
        healthLabel.setText("SAGLIK: ");
        healthLabel.setTextColor(Color.WHITE);
        healthSwitch = new Switch(this);
        healthSwitch.setChecked(true);
        healthRow.addView(healthLabel);
        healthRow.addView(healthSwitch);
        layout.addView(healthRow);
        
        launchBtn = new Button(this);
        launchBtn.setText("VALORANT'I BASLAT");
        launchBtn.setTextColor(Color.WHITE);
        launchBtn.setBackgroundColor(Color.parseColor("#cc0000"));
        launchBtn.setTextSize(18);
        launchBtn.setPadding(20, 20, 20, 20);
        launchBtn.setOnClickListener(v -> launchGame());
        layout.addView(launchBtn);
        
        statusText = new TextView(this);
        statusText.setText("HAZIR");
        statusText.setTextColor(Color.GREEN);
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 20, 0, 20);
        layout.addView(statusText);
        
        scrollView.addView(layout);
        setContentView(scrollView);
    }
    
    private void launchGame() {
        statusText.setText("BASLATILIYOR...");
        statusText.setTextColor(Color.YELLOW);
        
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                
                Intent intent = getPackageManager().getLaunchIntentForPackage(
                    "com.riotgames.valorant.mobile");
                    
                if (intent != null) {
                    startActivity(intent);
                    Thread.sleep(3000);
                    
                    Intent overlayIntent = new Intent(this, OverlayService.class);
                    overlayIntent.putExtra("esp", espSwitch.isChecked());
                    overlayIntent.putExtra("box", boxSwitch.isChecked());
                    overlayIntent.putExtra("skeleton", skeletonSwitch.isChecked());
                    overlayIntent.putExtra("lines", lineSwitch.isChecked());
                    overlayIntent.putExtra("health", healthSwitch.isChecked());
                    
                    startService(overlayIntent);
                    
                    runOnUiThread(() -> {
                        statusText.setText("ESP AKTIF!");
                        statusText.setTextColor(Color.GREEN);
                    });
                } else {
                    runOnUiThread(() -> {
                        statusText.setText("VALORANT BULUNAMADI!");
                        statusText.setTextColor(Color.RED);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    statusText.setText("HATA: " + e.getMessage());
                    statusText.setTextColor(Color.RED);
                });
            }
        }).start();
    }
                          }
