package com.capacitorjs.plugins.pushnotifications;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import java.util.Map;

public class MessagingService extends FirebaseMessagingService {

    private static final String packageName = "org.ilovefreegle.direct";
    private static final String channelId = "PushDefaultForeground";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        boolean fired = PushNotificationsPlugin.sendRemoteMessage(remoteMessage);
        if( !fired){
          sendServiceNotification(remoteMessage); // If app not alive 
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        PushNotificationsPlugin.onNewToken(s);
    }

    private void sendServiceNotification(@NonNull RemoteMessage remoteMessage) {
      Map<String, String> msgdata = remoteMessage.getData();
      if (msgdata == null) return;

      String title = msgdata.get("title").toString();
      int notId = Integer.parseInt(msgdata.get("notId").toString());

      try{
        Intent intent = new Intent(this, Class.forName(packageName+".MainActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notId, intent, PendingIntent.FLAG_IMMUTABLE);

        int pushIcon = android.R.drawable.ic_dialog_info;
        Bundle bundle = null;
        Resources r = null;
        ApplicationInfo applicationInfo = null;
        int appIconResId = 0;
        try {
            applicationInfo = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            bundle = applicationInfo.metaData;
            r = getPackageManager().getResourcesForApplication(packageName);
            appIconResId = applicationInfo.icon;
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (bundle != null && bundle.getInt("com.google.firebase.messaging.default_notification_icon") != 0) {
            pushIcon = bundle.getInt("com.google.firebase.messaging.default_notification_icon");
        }
        Notification.Builder builder =
                new Notification.Builder(this, channelId)
                        .setContentTitle(title)
                        //.setContentText("")
                        .setSmallIcon(pushIcon)
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setColor(Color.GREEN)
                        .setContentIntent(pendingIntent);
        PushNotificationsPlugin.setLargeIcon(builder,r,appIconResId);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notId, builder.build());
      } catch (Exception e) {
        Log.e("PushNotifications", "sendServiceNotification exception "+e.getMessage());
      }
    }
}
