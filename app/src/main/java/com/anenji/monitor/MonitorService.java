package com.anenji.monitor;
import android.app.*; import android.content.*; import org.json.*; import java.io.*; import java.net.*; import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.util.*;
public class MonitorService extends Service {
 static final String BASE="https://api.dessmonitor.com/public/", KEY="bnrl_frRFjEz8Mkn", CH1="monitor", CH2="alerts";
 String token,secret,user,pass; boolean known=false,last=false; volatile boolean stop=false;
 public void onCreate(){super.onCreate(); NotificationManager n=getSystemService(NotificationManager.class);
  n.createNotificationChannel(new NotificationChannel(CH1,"Anenji Monitor",NotificationManager.IMPORTANCE_LOW));
  n.createNotificationChannel(new NotificationChannel(CH2,"Зміни мережі 220 В",NotificationManager.IMPORTANCE_HIGH));
  startForeground(1,new Notification.Builder(this,CH1).setSmallIcon(android.R.drawable.ic_lock_idle_charging).setContentTitle("Anenji Monitor").setContentText("Контроль мережі працює").setOngoing(true).build());
 }
 public int onStartCommand(Intent i,int f,int id){android.content.SharedPreferences p=getSharedPreferences("cfg",MODE_PRIVATE); user=p.getString("u",""); pass=p.getString("p",""); stop=false;
  new Thread(()->{while(!stop&&getSharedPreferences("cfg",MODE_PRIVATE).getBoolean("running",false)){try{boolean on=grid();if(known&&on!=last)alert(on);last=on;known=true;}catch(Exception e){token=null;secret=null;}try{Thread.sleep(60000);}catch(Exception e){break;}}}).start(); return START_STICKY;
 }
 boolean grid() throws Exception {auth(); JSONObject pl=req("queryPlants",m("pagesize","50")); JSONArray plants=pl.getJSONObject("dat").getJSONArray("plant"); String pid=String.valueOf(plants.getJSONObject(0).get("pid"));
  JSONObject ds=req("webQueryDeviceEs",m("pid",pid,"pagesize","50")); JSONArray da=ds.getJSONObject("dat").getJSONArray("device"); JSONObject d=da.getJSONObject(0);
  JSONArray x=req("queryDeviceLastData",m("pn",d.optString("pn"),"devcode",d.optString("devcode","0"),"devaddr",d.optString("devaddr","1"),"sn",d.optString("sn"),"i18n","en")).getJSONArray("dat");
  Double v=null; for(int z=0;z<x.length();z++){JSONObject o=x.getJSONObject(z);String t=o.optString("title").toLowerCase(Locale.ROOT);if(t.contains("grid voltage")||t.contains("mains voltage")||t.contains("utility voltage")||t.equals("ac input voltage")){try{v=Double.parseDouble(o.optString("val").replace(",","."));break;}catch(Exception ignored){}}}
  if(v==null)throw new Exception("No grid voltage"); return v>100;
 }
 void auth() throws Exception {if(token!=null)return;String salt=String.valueOf(System.currentTimeMillis());String tail="&action=authSource&usr="+enc(user)+"&company-key="+KEY+"&source=1&_app_client_=web&_app_id_=ha-dessmonitor&_app_version_=2.4.0";JSONObject j=get(BASE+"?sign="+sha(salt+sha(pass)+tail)+"&salt="+salt+tail);if(j.optInt("err",-1)!=0)throw new Exception("Login");JSONObject d=j.getJSONObject("dat");token=d.getString("token");secret=d.getString("secret");}
 JSONObject req(String ac,LinkedHashMap<String,String> p)throws Exception{String salt=String.valueOf(System.currentTimeMillis());StringBuilder t=new StringBuilder("&action=").append(ac);for(Map.Entry<String,String>e:p.entrySet())t.append("&").append(e.getKey()).append("=").append(enc(e.getValue()));JSONObject j=get(BASE+"?sign="+sha(salt+secret+token+t)+"&salt="+salt+"&token="+token+t);if(j.optInt("err",-1)!=0)throw new Exception("API");return j;}
 JSONObject get(String u)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(20000);try(BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream(),StandardCharsets.UTF_8))){StringBuilder s=new StringBuilder();String l;while((l=r.readLine())!=null)s.append(l);return new JSONObject(s.toString());}}
 void alert(boolean on){getSystemService(NotificationManager.class).notify(on?3:2,new Notification.Builder(this,CH2).setSmallIcon(on?android.R.drawable.ic_lock_idle_charging:android.R.drawable.stat_notify_error).setContentTitle(on?"Світло з’явилося":"Зникла мережа 220 В").setContentText(on?"SmartESS знову бачить мережу":"Інвертор не бачить зовнішню мережу").setAutoCancel(true).build());}
 static LinkedHashMap<String,String> m(String...x){LinkedHashMap<String,String>r=new LinkedHashMap<>();for(int i=0;i<x.length;i+=2)r.put(x[i],x[i+1]);return r;}
 static String enc(String s)throws Exception{return URLEncoder.encode(s,"UTF-8");}
 static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-1").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder r=new StringBuilder();for(byte x:b)r.append(String.format("%02x",x));return r.toString();}
 public void onDestroy(){stop=true;super.onDestroy();} public android.os.IBinder onBind(Intent i){return null;}
}