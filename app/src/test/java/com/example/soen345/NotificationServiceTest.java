package com.example.soen345;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    private FirebaseFirestore db;
    private CollectionReference usersCollection;
    private CollectionReference mailCollection;
    private CollectionReference smsCollection;
    private DocumentReference userDocRef;

    @Before
    public void setUp() {
        db = mock(FirebaseFirestore.class);
        usersCollection = mock(CollectionReference.class);
        mailCollection = mock(CollectionReference.class);
        smsCollection = mock(CollectionReference.class);
        userDocRef = mock(DocumentReference.class);

        when(db.collection("users")).thenReturn(usersCollection);
        when(db.collection("mail")).thenReturn(mailCollection);
        when(db.collection("sms")).thenReturn(smsCollection);

        when(usersCollection.document("user123")).thenReturn(userDocRef);
    }

    @Test
    public void notifyBookingConfirmed_nullEvent_returnsError() {
        AtomicBoolean success = new AtomicBoolean(true);
        AtomicReference<String> error = new AtomicReference<>();

        NotificationService.notifyBookingConfirmed(db, "user123", null, (ok, err) -> {
            success.set(ok);
            error.set(err);
        });

        assertFalse(success.get());
        assertEquals("Event required", error.get());
        verify(db, never()).collection("users");
    }

    @Test
    public void notifyBookingCancelled_nullEvent_returnsError() {
        AtomicBoolean success = new AtomicBoolean(true);
        AtomicReference<String> error = new AtomicReference<>();

        NotificationService.notifyBookingCancelled(db, "user123", null, (ok, err) -> {
            success.set(ok);
            error.set(err);
        });

        assertFalse(success.get());
        assertEquals("Event required", error.get());
        verify(db, never()).collection("users");
    }

    @Test
    public void notifyBookingConfirmed_blankUserId_returnsError() {
        Event event = new Event("e1", "Jazz Night", "2026-03-15", "Montreal", "Music");
        AtomicBoolean success = new AtomicBoolean(true);
        AtomicReference<String> error = new AtomicReference<>();

        NotificationService.notifyBookingConfirmed(db, "   ", event, (ok, err) -> {
            success.set(ok);
            error.set(err);
        });

        assertFalse(success.get());
        assertEquals("User ID required", error.get());
        verify(db, never()).collection("users");
    }

    @Test
    public void notifyBookingConfirmed_userNotFound_returnsError() {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        @SuppressWarnings("unchecked")
        Task<DocumentSnapshot> getTask = mock(Task.class);

        when(userDocRef.get()).thenReturn(getTask);
        when(snapshot.exists()).thenReturn(false);

        stubSuccessTask(getTask, snapshot);

        Event event = new Event("e1", "Jazz Night", "2026-03-15", "Montreal", "Music");
        AtomicBoolean success = new AtomicBoolean(true);
        AtomicReference<String> error = new AtomicReference<>();

        NotificationService.notifyBookingConfirmed(db, "user123", event, (ok, err) -> {
            success.set(ok);
            error.set(err);
        });

        assertFalse(success.get());
        assertEquals("User not found", error.get());
    }

    @Test
    public void notifyBookingConfirmed_userLoadFails_returnsError() {
        @SuppressWarnings("unchecked")
        Task<DocumentSnapshot> getTask = mock(Task.class);

        when(userDocRef.get()).thenReturn(getTask);

        stubFailureTask(getTask, new RuntimeException("Firestore failed"));

        Event event = new Event("e1", "Jazz Night", "2026-03-15", "Montreal", "Music");
        AtomicBoolean success = new AtomicBoolean(true);
        AtomicReference<String> error = new AtomicReference<>();

        NotificationService.notifyBookingConfirmed(db, "user123", event, (ok, err) -> {
            success.set(ok);
            error.set(err);
        });

        assertFalse(success.get());
        assertEquals("Firestore failed", error.get());
    }

    @Test
    public void notifyBookingConfirmed_emailQueueSuccess_returnsSuccess() {
        User user = new User("user123", "test@test.com", "5141234567", false);
        mockUserFetchSuccess(user);

        @SuppressWarnings("unchecked")
        Task<DocumentReference> addTask = mock(Task.class);
        when(mailCollection.add(anyMap())).thenReturn(addTask);
        stubSuccessTask(addTask, mock(DocumentReference.class));

        Event event = new Event("e1", "Jazz Night", "2026-03-15", "Montreal", "Music");
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> error = new AtomicReference<>();

        NotificationService.notifyBookingConfirmed(db, "user123", event, (ok, err) -> {
            success.set(ok);
            error.set(err);
        });

        assertTrue(success.get());
        assertNull(error.get());
        verify(mailCollection).add(anyMap());
        verify(smsCollection, never()).add(anyMap());
    }

    @Test
    public void notifyBookingConfirmed_emailFails_smsSucceeds_returnsSuccess() {
        User user = new User("user123", "test@test.com", "5141234567", false);
        mockUserFetchSuccess(user);

        @SuppressWarnings("unchecked")
        Task<DocumentReference> mailTask = mock(Task.class);
        @SuppressWarnings("unchecked")
        Task<DocumentReference> smsTask = mock(Task.class);

        when(mailCollection.add(anyMap())).thenReturn(mailTask);
        when(smsCollection.add(anyMap())).thenReturn(smsTask);

        stubFailureTask(mailTask, new RuntimeException("Mail failed"));
        stubSuccessTask(smsTask, mock(DocumentReference.class));

        Event event = new Event("e1", "Jazz Night", "2026-03-15", "Montreal", "Music");
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> error = new AtomicReference<>();

        NotificationService.notifyBookingConfirmed(db, "user123", event, (ok, err) -> {
            success.set(ok);
            error.set(err);
        });

        assertTrue(success.get());
        assertNull(error.get());
        verify(mailCollection).add(anyMap());
        verify(smsCollection).add(anyMap());
    }

    @Test
    public void notifyBookingConfirmed_emailFails_smsFails_returnsError() {
        User user = new User("user123", "test@test.com", "5141234567", false);
        mockUserFetchSuccess(user);

        @SuppressWarnings("unchecked")
        Task<DocumentReference> mailTask = mock(Task.class);
        @SuppressWarnings("unchecked")
        Task<DocumentReference> smsTask = mock(Task.class);

        when(mailCollection.add(anyMap())).thenReturn(mailTask);
        when(smsCollection.add(anyMap())).thenReturn(smsTask);

        stubFailureTask(mailTask, new RuntimeException("Mail failed"));
        stubFailureTask(smsTask, new RuntimeException("SMS failed"));

        Event event = new Event("e1", "Jazz Night", "2026-03-15", "Montreal", "Music");
        AtomicBoolean success = new AtomicBoolean(true);
        AtomicReference<String> error = new AtomicReference<>();

        NotificationService.notifyBookingConfirmed(db, "user123", event, (ok, err) -> {
            success.set(ok);
            error.set(err);
        });

        assertFalse(success.get());
        assertEquals("SMS failed", error.get());
    }

    @Test
    public void notifyBookingConfirmed_phoneOnly_queuesSmsDirectly() {
        User user = new User("user123", "", "5141234567", false);
        mockUserFetchSuccess(user);

        @SuppressWarnings("unchecked")
        Task<DocumentReference> smsTask = mock(Task.class);
        when(smsCollection.add(anyMap())).thenReturn(smsTask);
        stubSuccessTask(smsTask, mock(DocumentReference.class));

        Event event = new Event("e1", "Jazz Night", "2026-03-15", "Montreal", "Music");
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> error = new AtomicReference<>();

        NotificationService.notifyBookingConfirmed(db, "user123", event, (ok, err) -> {
            success.set(ok);
            error.set(err);
        });

        assertTrue(success.get());
        assertNull(error.get());
        verify(mailCollection, never()).add(anyMap());
        verify(smsCollection).add(anyMap());
    }

    @Test
    public void notifyBookingConfirmed_noEmailNoPhone_returnsError() {
        User user = new User("user123", "", "", false);
        mockUserFetchSuccess(user);

        Event event = new Event("e1", "Jazz Night", "2026-03-15", "Montreal", "Music");
        AtomicBoolean success = new AtomicBoolean(true);
        AtomicReference<String> error = new AtomicReference<>();

        NotificationService.notifyBookingConfirmed(db, "user123", event, (ok, err) -> {
            success.set(ok);
            error.set(err);
        });

        assertFalse(success.get());
        assertEquals("User has no email or phone", error.get());
        verify(mailCollection, never()).add(anyMap());
        verify(smsCollection, never()).add(anyMap());
    }

    @Test
    public void notifyBookingCancelled_emailQueueSuccess_returnsSuccess() {
        User user = new User("user123", "test@test.com", "5141234567", false);
        mockUserFetchSuccess(user);

        @SuppressWarnings("unchecked")
        Task<DocumentReference> addTask = mock(Task.class);
        when(mailCollection.add(anyMap())).thenReturn(addTask);
        stubSuccessTask(addTask, mock(DocumentReference.class));

        Event event = new Event("e2", "Comedy Show", "2026-03-20", "Downtown", "Comedy");
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> error = new AtomicReference<>();

        NotificationService.notifyBookingCancelled(db, "user123", event, (ok, err) -> {
            success.set(ok);
            error.set(err);
        });

        assertTrue(success.get());
        assertNull(error.get());
        verify(mailCollection).add(anyMap());
    }

    @Test
    public void notifyBookingConfirmed_emailPayloadContainsExpectedFields() {
        User user = new User("user123", "test@test.com", "5141234567", false);
        mockUserFetchSuccess(user);

        @SuppressWarnings("unchecked")
        Task<DocumentReference> addTask = mock(Task.class);
        when(mailCollection.add(anyMap())).thenReturn(addTask);
        stubSuccessTask(addTask, mock(DocumentReference.class));

        Event event = new Event("e1", "Jazz Night", "2026-03-15", "Montreal", "Music");

        NotificationService.notifyBookingConfirmed(db, "user123", event, (ok, err) -> { });

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mailCollection).add(captor.capture());

        Map<String, Object> mailDoc = captor.getValue();
        assertEquals("test@test.com", mailDoc.get("to"));

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) mailDoc.get("message");
        assertEquals("Booking Confirmed", message.get("subject"));
        assertTrue(((String) message.get("text")).contains("Jazz Night"));
    }

    @Test
    public void notifyBookingConfirmed_smsPayloadContainsExpectedFields() {
        User user = new User("user123", "", "5141234567", false);
        mockUserFetchSuccess(user);

        @SuppressWarnings("unchecked")
        Task<DocumentReference> smsTask = mock(Task.class);
        when(smsCollection.add(anyMap())).thenReturn(smsTask);
        stubSuccessTask(smsTask, mock(DocumentReference.class));

        Event event = new Event("e1", "Jazz Night", "2026-03-15", "Montreal", "Music");

        NotificationService.notifyBookingConfirmed(db, "user123", event, (ok, err) -> { });

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(smsCollection).add(captor.capture());

        Map<String, Object> smsDoc = captor.getValue();
        assertEquals("5141234567", smsDoc.get("to"));
        assertTrue(((String) smsDoc.get("message")).contains("Jazz Night"));
    }

    private void mockUserFetchSuccess(User user) {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        @SuppressWarnings("unchecked")
        Task<DocumentSnapshot> getTask = mock(Task.class);

        when(userDocRef.get()).thenReturn(getTask);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(User.class)).thenReturn(user);

        stubSuccessTask(getTask, snapshot);
    }

    private <T> void stubSuccessTask(Task<T> task, T result) {
        when(task.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<T> listener = invocation.getArgument(0);
            listener.onSuccess(result);
            return task;
        });

        when(task.addOnFailureListener(any())).thenReturn(task);
    }

    private <T> void stubFailureTask(Task<T> task, Exception exception) {
        when(task.addOnSuccessListener(any())).thenReturn(task);

        when(task.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(exception);
            return task;
        });
    }
}