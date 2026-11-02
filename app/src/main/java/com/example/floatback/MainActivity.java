package com.example.floatback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        int p = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(p, p, p, p);

        TextView tv = new TextView(this);
        tv.setTextSize(18);
        tv.setText("1) Open Accessibility settings and turn on \"Float Back\".\n"
                + "2) A round \u25C0 button appears above every app.\n"
                + "    Tap = Back, long-press = Home, drag = move it.\n\n"
                + "If the Accessibility screen is missing on this device, enable the service with adb (see README).");
        root.addView(tv);

        Button btn = new Button(this);
        btn.setText("Open Accessibility settings");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Settings screen not available. Use adb (see README).", Toast.LENGTH_LONG).show();
                }
            }
        });
        root.addView(btn);
        setContentView(root);
    }
}
