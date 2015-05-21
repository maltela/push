
package development.myapplication;


// Admin Funktion zum Erstellen von Notifications
// ToDO Push-Funktion integrieren

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import development.myapplication.R;

public class MainActivity extends Activity {

   public static final String EXTRA_MESSAGE = "message";
   public static final String PROPERTY_REG_ID = "registration_id";
   private static final String PROPERTY_APP_VERSION = "appVersion";
   private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

   String SENDER_ID = "122610241119";
   static final String TAG = "GCMDemo";

   NotificationManager NM;
   EditText one,two,three;
   GoogleCloudMessaging gcm;
   AtomicInteger msgId = new AtomicInteger();
   SharedPreferences prefs;
   Context context;
   String regid;

   @Override
   protected void onCreate(Bundle savedInstanceState) {


      gcm = GoogleCloudMessaging.getInstance(this);
      regid = getRegistrationId(context);

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      one = (EditText)findViewById(R.id.editText1);
      two = (EditText)findViewById(R.id.editText2);
      three = (EditText)findViewById(R.id.editText3);


      if (regid.isEmpty()) {
         //registerInBackground();

         new GcmRegistrationAsyncTask(this).execute();

         Log.i(TAG, "Registrierung ID ");
         }


   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }


   @SuppressWarnings("deprecation")
   public void notify(View vobj){
      String title = one.getText().toString();
      String subject = two.getText().toString();
      String body = three.getText().toString();
      NM=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
      Notification notify=new Notification(android.R.drawable.
      stat_notify_more,title,System.currentTimeMillis());
      PendingIntent pending=PendingIntent.getActivity(
      getApplicationContext(),0, new Intent(),0);
      notify.setLatestEventInfo(getApplicationContext(),subject,body,pending);
      NM.notify(0, notify);
   }
   private String getRegistrationId(Context context) {
      final SharedPreferences prefs = getGCMPreferences(context);
      String registrationId = prefs.getString(PROPERTY_REG_ID, "");
      if (registrationId.isEmpty()) {
         Log.i(TAG, "Registration not found.");
         return "";
      }
      // Check if app was updated; if so, it must clear the registration ID
      // since the existing registration ID is not guaranteed to work with
      // the new app version.
      int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
      int currentVersion = getAppVersion(context);
      if (registeredVersion != currentVersion) {
         Log.i(TAG, "App version changed.");
         return "";
      }
      return registrationId;
   }

   /**
    * @return Application's {@code SharedPreferences}.
    */
   private SharedPreferences getGCMPreferences(Context context) {
      // This sample app persists the registration ID in shared preferences, but
      // how you store the registration ID in your app is up to you.
      return getSharedPreferences(MainActivity.class.getSimpleName(),
              Context.MODE_PRIVATE);
   }

   private static int getAppVersion(Context context) {
      try {
         PackageInfo packageInfo = context.getPackageManager()
                 .getPackageInfo(context.getPackageName(), 0);
         return packageInfo.versionCode;
      } catch (PackageManager.NameNotFoundException e) {
         // should never happen
         throw new RuntimeException("Could not get package name: " + e);
      }
   }
   private void registerInBackground() {
      new AsyncTask<Void, Void, String>() {
         @Override
         protected String doInBackground(Void... params) {
            String msg = "";
            try {
               if (gcm == null) {
                  gcm = GoogleCloudMessaging.getInstance(context);
               }
               regid = gcm.register(SENDER_ID);
               msg = "Device registered, registration ID=" + regid;
               Log.i(TAG, "ID :" + msg);
               // You should send the registration ID to your server over HTTP,
               // so it can use GCM/HTTP or CCS to send messages to your app.
               // The request to your server should be authenticated if your app
               // is using accounts.
              // sendRegistrationIdToBackend();

               // For this demo: we don't need to send it because the device
               // will send upstream messages to a server that echo back the
               // message using the 'from' address in the message.

               // Persist the registration ID - no need to register again.
             //  storeRegistrationId(context, regid);
            } catch (IOException ex) {
               msg = "Error :" + ex.getMessage();
               // If there is an error, don't just keep trying to register.
               // Require the user to click a button again, or perform
               // exponential back-off.
            }
            return msg;
         }


      }.execute(null, null, null);


   }
   private void sendRegistrationIdToBackend() {
      // Your implementation here.
   }
   private void storeRegistrationId(Context context, String regId) {
      final SharedPreferences prefs = getGCMPreferences(context);
      int appVersion = getAppVersion(context);
      Log.i(TAG, "Saving regId on app version " + appVersion);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putString(PROPERTY_REG_ID, regId);
      editor.putInt(PROPERTY_APP_VERSION, appVersion);
      editor.commit();
   }
}