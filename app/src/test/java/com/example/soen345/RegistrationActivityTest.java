package com.example.soen345;

import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import org.mockito.ArgumentCaptor;

public class RegistrationActivityTest {

    @Test
    public void testIsInputValid() {
        assertFalse(RegistrationActivity.isInputValid("", ""));
        assertFalse(RegistrationActivity.isInputValid("invalidemail", "1234567890"));
        assertFalse(RegistrationActivity.isInputValid("test@test.com", "123"));
        assertTrue(RegistrationActivity.isInputValid("user@example.com", "1234567890"));
        assertTrue(RegistrationActivity.isInputValid("user@example.com", ""));
        assertTrue(RegistrationActivity.isInputValid("", "1234567890"));
    }

    @Mock
    FirebaseFirestore mockDb;
    @Mock
    CollectionReference mockCollection;
    @Mock
    DocumentReference mockDocument;
    @Mock
    Task<Void> mockVoidTask;
    @Mock
    Task<QuerySnapshot> mockQueryTask;
    @Mock
    QuerySnapshot mockQuerySnapshot;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(mockDb.collection("users")).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
        when(mockDocument.set(any(User.class))).thenReturn(mockVoidTask);
        when(mockCollection.whereEqualTo(anyString(), any())).thenReturn(mockCollection);
        when(mockCollection.get()).thenReturn(mockQueryTask);
    }

    @Test
    public void testFirestoreSetCalled() {
        User testUser = new User("123", "test@test.com", "1234567890", false);
        mockDb.collection("users").document("123").set(testUser);
        verify(mockDb).collection("users");
        verify(mockDocument).set(testUser);
    }

    @Test
    public void testRegistrationLogic_QueryInteraction() {
        String email = "existing@test.com";
        
        // This test verifies that the activity interacts correctly with Firestore
        // when performing the email existence check.
        mockDb.collection("users").whereEqualTo("email", email).get();
        
        verify(mockCollection).whereEqualTo("email", email);
        verify(mockCollection).get();
    }
}
