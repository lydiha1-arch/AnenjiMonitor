package com.anenji.monitor;
import android.Manifest; import android.app.*; import android.content.*; import android.content.pm.PackageManager; import android.graphics.Color; import android.os.Bundle; import android.text.InputType; import android.widget.*;
public class MainActivity extends Activity {
 public void onCreate(Bundle b){super.onCreate(b);
  if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},7);
  LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.VERTICAL); r.setPadding(48,70,48,40);
  TextView t=new TextView(this); t.setText("Anenji Monitor"); t.setTextSize(28); t.setTextColor(Color.BLACK); r.addView(t);
  TextView s=new TextView(this); s.setText("Контроль мережі 220 В через SmartESS"); s.setTextSize(16); s.setPadding(0,10,0,30); r.addView(s);
  EditText u=new EditText(this); u.setHint("Логін SmartESS"); r.addView(u);
  EditText p=new EditText(this); p.setHint("Пароль SmartESS"); p.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); r.addView(p);
  Button start=new Button(this); start.setText("УВІМКНУТИ КОНТРОЛЬ"); r.addView(start);
  Button stop=new Button(this); stop.setText("ВИМКНУТИ"); r.addView(stop);
  TextView st=new TextView(this); st.setTextSize(18); st.setPadding(0,30,0,0); r.addView(st); setContentView(r);
  android.content.SharedPreferences sp=getSharedPreferences("cfg",MODE_PRIVATE); u.setText(sp.getString("u","")); st.setText(sp.getBoolean("running",false)?"Контроль працює":"Контроль вимкнено");
  start.setOnClickListener(v->{String us=u.getText().toString().trim(),pw=p.getText().toString(); if(us.isEmpty()||pw.isEmpty()){Toast.makeText(this,"Введи логін і пароль SmartESS",Toast.LENGTH_LONG).show();return;} sp.edit().putString("u",us).putString("p",pw).putBoolean("running",true).apply(); startForegroundService(new Intent(this,MonitorService.class)); st.setText("Контроль працює");});
  stop.setOnClickListener(v->{sp.edit().putBoolean("running",false).apply(); stopService(new Intent(this,MonitorService.class)); st.setText("Контроль вимкнено");});
 }
}