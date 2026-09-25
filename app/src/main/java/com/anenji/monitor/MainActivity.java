package com.anenji.monitor;
import android.Manifest;import android.app.*;import android.content.*;import android.content.pm.PackageManager;import android.graphics.Color;import android.os.Bundle;import android.text.InputType;import android.view.Gravity;import android.widget.*;
public class MainActivity extends Activity{
 TextView state,grid,batt,load,last,error;
 TextView tv(String x,int sz){TextView v=new TextView(this);v.setText(x);v.setTextSize(sz);v.setPadding(0,12,0,12);return v;}
 public void onCreate(Bundle b){super.onCreate(b);if(android.os.Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},7);
 ScrollView sv=new ScrollView(this);LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(44,55,44,44);sv.addView(r);
 TextView h=tv("Anenji Monitor",30);h.setTextColor(Color.BLACK);r.addView(h);r.addView(tv("SmartESS • контроль мережі та інвертора",16));
 state=tv("● Контроль вимкнено",20);r.addView(state);grid=tv("Мережа: —",24);r.addView(grid);batt=tv("АКБ: —",20);r.addView(batt);load=tv("Навантаження: —",20);r.addView(load);last=tv("Останнє оновлення: —",15);r.addView(last);error=tv("",15);error.setTextColor(Color.RED);r.addView(error);
 EditText u=new EditText(this);u.setHint("Логін SmartESS");r.addView(u);EditText p=new EditText(this);p.setHint("Пароль SmartESS");p.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);r.addView(p);
 Button start=new Button(this);start.setText("УВІМКНУТИ КОНТРОЛЬ");r.addView(start);Button stop=new Button(this);stop.setText("ВИМКНУТИ");r.addView(stop);setContentView(sv);
 android.content.SharedPreferences sp=getSharedPreferences("cfg",MODE_PRIVATE);u.setText(sp.getString("u",""));if(sp.getBoolean("running",false)){state.setText("● Контроль запущено");startForegroundService(new Intent(this,MonitorService.class));}
 start.setOnClickListener(v->{String us=u.getText().toString().trim(),pw=p.getText().toString();if(us.isEmpty()||pw.isEmpty()){Toast.makeText(this,"Введи логін і пароль SmartESS",Toast.LENGTH_LONG).show();return;}sp.edit().putString("u",us).putString("p",pw).putBoolean("running",true).apply();startForegroundService(new Intent(this,MonitorService.class));state.setText("● Підключення до SmartESS…");});
 stop.setOnClickListener(v->{sp.edit().putBoolean("running",false).apply();stopService(new Intent(this,MonitorService.class));state.setText("● Контроль вимкнено");});
 registerReceiver(new BroadcastReceiver(){public void onReceive(Context c,Intent i){state.setText(i.getStringExtra("state"));grid.setText(i.getStringExtra("grid"));batt.setText(i.getStringExtra("batt"));load.setText(i.getStringExtra("load"));last.setText(i.getStringExtra("last"));error.setText(i.getStringExtra("error"));}},new IntentFilter("com.anenji.monitor.STATUS"),Context.RECEIVER_NOT_EXPORTED);
 }}