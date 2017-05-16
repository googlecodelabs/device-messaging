/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.devicemessagingcodelab;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

/**
 * Service that simulates handling an incoming message.
 */
public class MessagingService extends IntentService {
    private static final String TAG = MessagingService.class.getSimpleName();

    /**
     * The Action that indicates that a new message notification
     * should be sent by this Service.
     */
    public static final String SEND_MESSAGE_ACTION =
            "com.example.android.devicemessagingcodelab.ACTION_SEND_MESSAGE";
    public static final String READ_ACTION =
            "com.example.android.devicemessagingcodelab.ACTION_MESSAGE_READ";
    public static final String REPLY_ACTION =
            "com.example.android.devicemessagingcodelab.ACTION_MESSAGE_REPLY";
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

    public MessagingService() {
        super(MessagingService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Handle intent to send a new notification.
        if (intent != null && SEND_MESSAGE_ACTION.equals(intent.getAction())) {

            sendNotificationForConversation(
                    Conversations.CONVERSATION_ID,
                    Conversations.SENDER_NAME,
                    Conversations.getUnreadMessage(),
                    System.currentTimeMillis());
        }
    }

    // Creates an intent that will be triggered when a message is read.
    private Intent getMessageReadIntent(int id) {
        return new Intent().setAction(READ_ACTION).putExtra(CONVERSATION_ID, id);
    }

    // Creates an Intent that will be triggered when a voice reply is received.
    private Intent getMessageReplyIntent(int conversationId) {
        return new Intent().setAction(REPLY_ACTION).putExtra(CONVERSATION_ID, conversationId);
    }

    private void sendNotificationForConversation(
            int conversationId,
            String sender,
            String message,
            long timestamp) {

        // A pending Intent for reads.
        PendingIntent readPendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                conversationId,
                getMessageReadIntent(conversationId),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Building a Pending Intent for the reply action to trigger.
        PendingIntent replyIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                conversationId,
                getMessageReplyIntent(conversationId),
                PendingIntent.FLAG_UPDATE_CURRENT);

        /// TODO: Add the code to create the UnreadConversation.
        // Build a RemoteInput for receiving voice input from devices.
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY).build();

        // Create the UnreadConversation and populate it with the participant name,
        // read and reply intents.
        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConversationBuilder =
                new NotificationCompat.CarExtender.UnreadConversation.Builder(sender)
                        .setLatestTimestamp(timestamp)
                        .setReadPendingIntent(readPendingIntent)
                        .setReplyAction(replyIntent, remoteInput);

        // Note: Add messages from oldest to newest to the UnreadConversation.Builder.
        // Since we are sending a single message here we simply add the message.
        // In a real world application there could be multiple unread messages which
        // should be ordered and added from oldest to newest.
        unreadConversationBuilder.addMessage(message);
        /// End create UnreadConversation.

        /// TODO: Add the code to allow in-line reply on Wear 2.0.
        // Wear 2.0 allows for in-line actions, which will be used for "reply".
        NotificationCompat.Action.WearableExtender inlineActionForWear2 =
                new NotificationCompat.Action.WearableExtender()
                        .setHintDisplayActionInline(true)
                        .setHintLaunchesActivity(false);
        /// End in-line action for Wear 2.0.

        // Add an action to allow replies.
        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_reply_white_24dp,
                        getString(R.string.reply_action),
                        replyIntent)
                        /// TODO: Add better wear support.
                        .addRemoteInput(remoteInput)
                        .extend(inlineActionForWear2)
                        .build();

        // Add messages with the MessagingStyle notification.
        NotificationCompat.MessagingStyle messagingStyle =
                new NotificationCompat.MessagingStyle(sender)
                        .addMessage(message, timestamp, sender);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setStyle(messagingStyle)
                        .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(
                                getApplicationContext().getResources(), R.drawable.android_contact))
                        .setContentText(message)
                        .setWhen(timestamp)
                        .addAction(replyAction)
                        .setContentTitle(sender)
                        .setContentIntent(readPendingIntent)
                        /// TODO: Extend the notification with CarExtender.
                        .extend(new NotificationCompat.CarExtender()
                                .setUnreadConversation(unreadConversationBuilder.build()))
                        /// End
                        ;

        Log.d(TAG, "Sending notification " + conversationId + " conversation: " + message);
        NotificationManagerCompat.from(this).notify(conversationId, builder.build());
    }
}
