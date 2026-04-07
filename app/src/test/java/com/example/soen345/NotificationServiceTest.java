package com.example.soen345;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import androidx.core.content.ContextCompat;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class NotificationServiceTest {

    @Test
    public void sendSms_nullContext_returnsFalse() {
        boolean result = NotificationService.sendSms(null, "5145502878", "hello");
        assertFalse(result);
    }

    @Test
    public void sendSms_nullPhone_returnsFalse() {
        Context context = ApplicationProvider.getApplicationContext();

        boolean result = NotificationService.sendSms(context, null, "hello");

        assertFalse(result);
    }

    @Test
    public void sendSms_blankPhone_returnsFalse() {
        Context context = ApplicationProvider.getApplicationContext();

        boolean result = NotificationService.sendSms(context, "   ", "hello");

        assertFalse(result);
    }

    @Test
    public void sendSms_nullMessage_returnsFalse() {
        Context context = ApplicationProvider.getApplicationContext();

        boolean result = NotificationService.sendSms(context, "5145502878", null);

        assertFalse(result);
    }

    @Test
    public void sendSms_permissionDenied_returnsFalse() {
        Context context = ApplicationProvider.getApplicationContext();

        try (MockedStatic<ContextCompat> contextCompatMock = mockStatic(ContextCompat.class)) {
            contextCompatMock.when(() ->
                    ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            ).thenReturn(PackageManager.PERMISSION_DENIED);

            boolean result = NotificationService.sendSms(context, "5145502878", "hello");

            assertFalse(result);
        }
    }

    @Test
    public void sendSms_permissionGrantedAndSmsSucceeds_returnsTrue() {
        Context context = ApplicationProvider.getApplicationContext();
        SmsManager smsManager = mock(SmsManager.class);

        try (MockedStatic<ContextCompat> contextCompatMock = mockStatic(ContextCompat.class);
             MockedStatic<SmsManager> smsManagerStaticMock = mockStatic(SmsManager.class)) {

            contextCompatMock.when(() ->
                    ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            ).thenReturn(PackageManager.PERMISSION_GRANTED);

            smsManagerStaticMock.when(SmsManager::getDefault).thenReturn(smsManager);

            boolean result = NotificationService.sendSms(context, "5145502878", "hello");

            assertTrue(result);
            verify(smsManager).sendTextMessage("5145502878", null, "hello", null, null);
        }
    }

    @Test
    public void sendSms_trimsPhoneBeforeSending() {
        Context context = ApplicationProvider.getApplicationContext();
        SmsManager smsManager = mock(SmsManager.class);

        try (MockedStatic<ContextCompat> contextCompatMock = mockStatic(ContextCompat.class);
             MockedStatic<SmsManager> smsManagerStaticMock = mockStatic(SmsManager.class)) {

            contextCompatMock.when(() ->
                    ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            ).thenReturn(PackageManager.PERMISSION_GRANTED);

            smsManagerStaticMock.when(SmsManager::getDefault).thenReturn(smsManager);

            boolean result = NotificationService.sendSms(context, " 5145502878 ", "hello");

            assertTrue(result);
            verify(smsManager).sendTextMessage("5145502878", null, "hello", null, null);
        }
    }

    @Test
    public void sendSms_smsManagerThrows_returnsFalse() {
        Context context = ApplicationProvider.getApplicationContext();
        SmsManager smsManager = mock(SmsManager.class);

        doThrow(new RuntimeException("SMS failed"))
                .when(smsManager)
                .sendTextMessage(anyString(), isNull(), anyString(), isNull(), isNull());

        try (MockedStatic<ContextCompat> contextCompatMock = mockStatic(ContextCompat.class);
             MockedStatic<SmsManager> smsManagerStaticMock = mockStatic(SmsManager.class)) {

            contextCompatMock.when(() ->
                    ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            ).thenReturn(PackageManager.PERMISSION_GRANTED);

            smsManagerStaticMock.when(SmsManager::getDefault).thenReturn(smsManager);

            boolean result = NotificationService.sendSms(context, "5145502878", "hello");

            assertFalse(result);
        }
    }
}