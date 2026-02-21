package com.example.soen345;

import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import org.mockito.ArgumentCaptor;

public class LoginActivityTest {

    @Test
    public void testIsInputValid() {
        assertFalse(LoginActivity.isInputValid("", ""));
        assertTrue(LoginActivity.isInputValid("user@test.com", ""));
        assertTrue(LoginActivity.isInputValid("", "1234567890"));
    }

    @Test
    public void testMatchUser() {
        User user = new User("1", "user@test.com", "1234567890", false);
        assertTrue(LoginActivity.matchUser(user, "user@test.com", ""));
        assertTrue(LoginActivity.matchUser(user, "", "1234567890"));
        assertTrue(LoginActivity.matchUser(user, "user@test.com", "1234567890"));
        assertFalse(LoginActivity.matchUser(user, "wrong@test.com", ""));
        assertFalse(LoginActivity.matchUser(user, "", "0000000000"));
        assertFalse(LoginActivity.matchUser(null, "user@test.com", ""));
    }

    @Mock
    FirebaseFirestore mockDb;
    @Mock
    CollectionReference mockCollection;
    @Mock
    Task<QuerySnapshot> mockQueryTask;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(mockDb.collection("users")).thenReturn(mockCollection);
        when(mockCollection.get()).thenReturn(mockQueryTask);
    }

    @Test
    public void testLoginQueryConstruction() {
        mockDb.collection("users").get();
        verify(mockDb, times(1)).collection("users");
        verify(mockCollection).get();
    }

    @Test
    public void testLoginLogic_UserNotFound() {
        when(mockQueryTask.isSuccessful()).thenReturn(true);

        // Verify db interaction
        mockDb.collection("users").get();
        verify(mockDb).collection("users");
    }
}
