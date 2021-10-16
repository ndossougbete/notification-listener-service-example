package com.github.chagall.notificationlistenerexample

import android.app.Notification
import android.app.Person
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import android.util.Log
import androidx.core.app.NotificationCompat
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient


/**
 * MIT License
 *
 * Copyright (c) 2016 Fábio Alves Martins Pereira (Chagall)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class NotificationListenerExampleService : NotificationListenerService() {
    private object ApplicationPackageNames {
        const val FACEBOOK_PACK_NAME = "com.facebook.katana"
        const val FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca"
        const val WHATSAPP_PACK_NAME = "com.whatsapp"
        const val INSTAGRAM_PACK_NAME = "com.instagram.android"
        const val GOOGLE_CHAT_PACK_NAME = "com.google.android.apps.dynamite"
    }

    object InterceptedNotificationCode {
        const val FACEBOOK_CODE = 1
        const val WHATSAPP_CODE = 2
        const val INSTAGRAM_CODE = 3
        const val GOOGLE_CHAT_CODE = 4
        const val OTHER_NOTIFICATIONS_CODE = 5 // We ignore all notification with code == 4
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notificationCode = processNotification(sbn)

//        if(notificationCode == InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) return;
        val intent = Intent("com.github.chagall.notificationlistenerexample")
        intent.putExtra("Notification Code", notificationCode)
        sendBroadcast(intent)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val notificationCode = processNotification(sbn)

//        if(notificationCode == InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE)  return;
        val activeNotifications = this.activeNotifications
        if (activeNotifications != null && activeNotifications.isNotEmpty()) {
            for (i in activeNotifications.indices) {
                if (notificationCode == processNotification(activeNotifications[i])) {
                    val intent = Intent("com.github.chagall.notificationlistenerexample")
                    intent.putExtra("Notification Code", notificationCode)
                    sendBroadcast(intent)
                    break
                }
            }
        }
    }

    data class NotificationDetails(val title: String?, val content: String?, val author: String?) {

        companion object {
            fun fromSbn(sbn: StatusBarNotification): NotificationDetails {
                val msgCompat = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(sbn.notification);
                if (msgCompat != null) {
                    val messages = msgCompat.messages;
                    messages.removeAll(msgCompat.historicMessages);

                    return NotificationDetails(
                            msgCompat.conversationTitle?.toString(),
                            msgCompat.messages.last()?.toString(),
                            msgCompat.user.name?.toString(),
                    )
                }

                return NotificationDetails(
                        sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
                        sbn.notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
                        null
                )
            }
        }

        fun isEmpty() = title.isNullOrEmpty() || author.isNullOrEmpty();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun processNotification(sbn: StatusBarNotification): Int {
        val notificationDetails = NotificationDetails.fromSbn(sbn)
        Log.w("NCC", "Matched notification from $packageName: $notificationDetails")
        if (!notificationDetails.isEmpty()) {
            val bundle = sbn.notification.extras
            for (key in bundle.keySet()) {
                Log.d("NCC", key + " : " + if (bundle[key] != null) bundle[key] else "NULL")
            }
        }




        val packageName = sbn.packageName
        return if (packageName == ApplicationPackageNames.FACEBOOK_PACK_NAME || packageName == ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME) {
            InterceptedNotificationCode.FACEBOOK_CODE
        } else if (packageName == ApplicationPackageNames.INSTAGRAM_PACK_NAME) {
            InterceptedNotificationCode.INSTAGRAM_CODE
        } else if (packageName == ApplicationPackageNames.WHATSAPP_PACK_NAME) {
            InterceptedNotificationCode.WHATSAPP_CODE
        } else if (packageName == ApplicationPackageNames.GOOGLE_CHAT_PACK_NAME) {
            InterceptedNotificationCode.GOOGLE_CHAT_CODE
        } else {
            InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE
        }
    }

}
