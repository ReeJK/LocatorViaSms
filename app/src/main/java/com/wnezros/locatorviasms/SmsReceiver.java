package com.wnezros.locatorviasms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;

import java.util.Set;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            SmsMessage[] messages = getMessagesFromIntent(intent);
            if (messages != null)
                ProcessMessages(context, messages);
        }
    }

    private static SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");

        int pduCount = pdus.length;
        SmsMessage[] messages = new SmsMessage[pduCount];

        for (int i = 0; i < pduCount; i++) {
            byte[] pdu = (byte[]) pdus[i];
            messages[i] = SmsMessage.createFromPdu(pdu);
        }

        return messages;
    }

    private boolean ProcessMessages(Context context, SmsMessage[] messages) {
        boolean allOk = true;
        for (SmsMessage sms : messages) {
            if(!ProcessMessage(context, sms))
                allOk = false;
        }

        return allOk;
    }

    private boolean ProcessMessage(Context context, SmsMessage sms) {
        String text = sms.getMessageBody();
        String from = sms.getOriginatingAddress();

        if (isRequestMessage(context, from, text)) {
            Intent intent = new Intent(context, SmsLocationReceiver.class);
            intent.putExtra("from", from);
            context.startService(intent);
            return true;
        }

        return false;
    }

    private boolean isRequestMessage(Context context, String from, String text) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return isAllowedPhone(prefs, from) && isCodePhrase(prefs, text);
    }

    private boolean isCodePhrase(SharedPreferences prefs, String text) {
        String[] phrases = Settings.getPhrases(prefs);
        for (String phrase : phrases) {
            String pl = phrase.toLowerCase();
            String tl = text.toLowerCase();
            if(pl.equals(tl) || (pl.endsWith("*") && tl.startsWith(pl.substring(0, pl.length() -1))))
                return true;
        }

        return false;
    }

    private boolean isAllowedPhone(SharedPreferences prefs, String address) {
        boolean isBlacklist = Settings.getIsPhonesBlacklist(prefs);

        String[] phones = Settings.getPhones(prefs);
        for (String phone : phones) {
            if (isSamePhone(phone, address))
                return !isBlacklist;
        }

        return !isBlacklist;
    }

    private boolean isSamePhone(String a, String b) {
        if (PhoneNumberUtils.compare(a, b))
            return true;

        a = PhoneNumberUtils.stripSeparators(a);
        b = PhoneNumberUtils.stripSeparators(b);

        if (b.startsWith("8") && b.length() > 10)
            b = b.substring(b.length() - 10, b.length());
        if (a.startsWith("8") && a.length() > 10)
            a = a.substring(a.length() - 10, a.length());

        return PhoneNumberUtils.compare(a, b);
    }
}

