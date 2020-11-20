//package com.example.alexandria;
///**
// * UI tests for AddBookActivity
// * @author Xueying Luo
// */
//
//import android.app.Activity;
//import android.widget.EditText;
//
//import androidx.annotation.NonNull;
//import androidx.test.platform.app.InstrumentationRegistry;
//
//import androidx.test.rule.ActivityTestRule;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FieldValue;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.robotium.solo.Solo;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//
//import java.util.ArrayList;
//
//import static junit.framework.TestCase.assertFalse;
//import static junit.framework.TestCase.assertTrue;
//
//
//public class AddBookActivityTest {
//    private Solo solo;
//
//    @Rule
//    public ActivityTestRule<MainActivity> rule =
//            new ActivityTestRule<>(MainActivity.class, true, true);
//
//    @Before
//    public void setUp() throws Exception {
//        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
//    }
//
//    @Test
//    public void startMainActivity() throws Exception {
//        Activity activity = rule.getActivity();
//    }
//
//    @Test
//    public void checkSwitchActivity() {
//
//        // log in
//        solo.assertCurrentActivity("Wrong activity", MainActivity.class);
//        solo.enterText((EditText) solo.getView(R.id.editTextEmailLogin), "testuser1@fake.com");
//        solo.enterText((EditText) solo.getView(R.id.editTextTextPassword), "123456789");
//        solo.clickOnButton("Login");
//
//        // switch to home page
//        solo.waitForActivity(HomeActivity.class, 2000);
//        solo.assertCurrentActivity("Wrong activity", HomeActivity.class);
//        solo.clickOnButton("My Book");
//
//        // switch to My Book page
//        solo.waitForActivity(MyBookActivity.class, 2000);
//        solo.assertCurrentActivity("Wrong activity", MyBookActivity.class);
//        solo.clickOnView(solo.getView(R.id.addBook));
//
//        // switch to Add Book page
//        solo.waitForActivity(AddBookActivity.class, 2000);
//        solo.assertCurrentActivity("Wrong activity", AddBookActivity.class);
//
//    }
//
//
//    @Test
//    public void checkAddBook() {
//
//        // log in
//        String email = "testuser1@fake.com";
//        solo.assertCurrentActivity("Wrong activity", MainActivity.class);
//        solo.enterText((EditText) solo.getView(R.id.editTextEmailLogin), email);
//        solo.enterText((EditText) solo.getView(R.id.editTextTextPassword), "123456789");
//        solo.clickOnButton("Login");
//
//        // switch to home page
//        solo.waitForActivity(HomeActivity.class, 2000);
//        solo.assertCurrentActivity("Wrong activity", HomeActivity.class);
//        solo.clickOnButton("My Book");
//
//        // switch to My Book page
//        solo.waitForActivity(MyBookActivity.class, 2000);
//        solo.assertCurrentActivity("Wrong activity", MyBookActivity.class);
//        solo.clickOnView(solo.getView(R.id.addBook));
//
//        // switch to Add Book page
//        solo.waitForActivity(AddBookActivity.class, 2000);
//        solo.assertCurrentActivity("Wrong activity", AddBookActivity.class);
//
//        // add book
//        solo.enterText((EditText) solo.getView(R.id.addBookTitle), "book title");
//        solo.enterText((EditText) solo.getView(R.id.addBookISBN), "9999900000123");
//        solo.enterText((EditText) solo.getView(R.id.addBookAuthor), "someone");
//        solo.enterText((EditText) solo.getView(R.id.addBookDescr), "descr");
//        solo.clickOnButton("ADD BOOK");
//
//        // switch back to My Book page
//        solo.waitForActivity(MyBookActivity.class, 2000);
//        solo.assertCurrentActivity("Wrong activity", MyBookActivity.class);
//
//    }
//
//    @Test
//    public void checkBookExists(){
//
//        // check if book is in books collection & in user's book list then delete the book
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference bookRef = db.collection("books").document("9999900000123-1");
//        DocumentReference userRef = db.collection("users").document("testuser1@fake.com");
//
//        bookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                DocumentSnapshot document = task.getResult();
//                if (task.isSuccessful()) {
//                    assertTrue(document.exists());
//
//                    ArrayList<String> authorList = (ArrayList<String>) document.getData().get("authors");
//                    String author = authorList.get(0);
//
//                    assertTrue(document.get("title").equals("book title"));
//                    assertTrue(author.equals("someone"));
//                    assertTrue(document.get("isbn").equals("9999900000123"));
//                    assertTrue(document.get("description").equals("descr"));
//
//                    // delete book from collection
//                    bookRef.delete()
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    assertFalse(document.exists());
//                                }
//                            });
//
//                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            DocumentSnapshot document = task.getResult();
//
//                            ArrayList<String> bookList = (ArrayList<String>) document.getData().get("books");
//                            bookList.contains(bookRef);
//
//                            // delete book from user's book list
//                            userRef.update("books", FieldValue.arrayRemove(bookRef))
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            ArrayList<String> bookList = (ArrayList<String>) document.getData().get("books");
//                                            assertFalse(bookList.contains(bookRef));
//
//                                        }
//                                    });
//                        }
//                    });
//                }
//            }
//        });
//    }
//
//    @After
//    public void tearDown() throws Exception{
//        solo.finishOpenedActivities();
//    }
//
//}
