package com.demo.firebasechat.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.firebasechat.R;
import com.demo.firebasechat.MyApp;
import com.demo.firebasechat.adapters.MessageAdapter;
import com.demo.firebasechat.models.User;
import com.demo.firebasechat.models.Message;
import com.demo.firebasechat.models.Room;
import com.google.android.material.snackbar.Snackbar;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatRoomActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "ChatRoomActivity";
    private ListenerRegistration mChatMessageEventListener, mUserListEventListener;
    private RecyclerView mChatMessageRecyclerView;
    private MessageAdapter mMessageAdapter;
    private FirebaseFirestore mDb;
    private ArrayList<Message> mMessages = new ArrayList<>();
    private Set<String> mMessageIds = new HashSet<>();
    private ArrayList<User> mUserList = new ArrayList<>();

    private Room mRoom;
    private EditText mMessage;
    private Translate translate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        mMessage = findViewById(R.id.input_message);
        mChatMessageRecyclerView = findViewById(R.id.chatmessage_recycler_view);

        findViewById(R.id.checkmark).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();

        getIncomingIntent();
        initChatroomRecyclerView();
        getChatroomUsers();

        if (checkInternetConnection()) {
            getTranslateService();
        }
    }

    public void getTranslateService() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try (InputStream is = getResources().openRawResource(R.raw.credentials)) {
            //Get credentials:
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);
            //Set credentials and get translate service:
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();

        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }

    private String translate(String message) {
        Translation translation = translate.translate(message, Translate.TranslateOption.targetLanguage(  ((MyApp) (getApplicationContext())).getLang()), Translate.TranslateOption.model("base"));
        return translation.getTranslatedText();
    }

    public boolean checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

        return connected;
    }

    private void getChatMessages() {

        CollectionReference messagesRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mRoom.getChatroom_id())
                .collection(getString(R.string.collection_chat_messages));

        mChatMessageEventListener = messagesRef
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "onEvent: Listen failed.", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                            Message message = doc.toObject(Message.class);
                            if (!mMessageIds.contains(message.getMessage_id())) {
                                mMessageIds.add(message.getMessage_id());
                                message.setMessage_trans(translate(message.getMessage()));
                                mMessages.add(message);
                                mChatMessageRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
                            }

                        }
                        mMessageAdapter.notifyDataSetChanged();

                    }
                });
    }

    private void getChatroomUsers() {

        CollectionReference usersRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mRoom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list));

        mUserListEventListener = usersRef
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "onEvent: Listen failed.", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {

                        mUserList.clear();
                        mUserList = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            User user = doc.toObject(User.class);
                            mUserList.add(user);
                        }

                        Log.d(TAG, "onEvent: user list size: " + mUserList.size());
                    }
                });
    }

    private void initChatroomRecyclerView() {
        mMessageAdapter = new MessageAdapter(mMessages, new ArrayList<User>(), this);
        mChatMessageRecyclerView.setAdapter(mMessageAdapter);
        mChatMessageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mChatMessageRecyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                mChatMessageRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mMessages.size() > 0) {
                            mChatMessageRecyclerView.smoothScrollToPosition(
                                    mChatMessageRecyclerView.getAdapter().getItemCount() - 1);
                        }

                    }
                }, 100);
            }
        });

    }


    private void insertNewMessage() {
        String message = mMessage.getText().toString();

        if (!message.equals("")) {
            message = message.replaceAll(System.getProperty("line.separator"), "");

            DocumentReference newMessageDoc = mDb
                    .collection(getString(R.string.collection_chatrooms))
                    .document(mRoom.getChatroom_id())
                    .collection(getString(R.string.collection_chat_messages))
                    .document();

            Message newMessage = new Message();
            newMessage.setMessage(message);
            newMessage.setMessage_id(newMessageDoc.getId());

            User user = ((MyApp) (getApplicationContext())).getUser();
            Log.d(TAG, "insertNewMessage: retrieved user client: " + user.toString());
            newMessage.setUser(user);

            newMessageDoc.set(newMessage).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    clearMessage();
                } else {
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void clearMessage() {
        mMessage.setText("");
    }

    private void inflateUserListFragment() {
        hideSoftKeyboard();

        UserListFragment fragment = UserListFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.intent_user_list), mUserList);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.user_list_container, fragment, getString(R.string.fragment_user_list));
        transaction.addToBackStack(getString(R.string.fragment_user_list));
        transaction.commit();
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    private void getIncomingIntent() {
        if (getIntent().hasExtra(getString(R.string.intent_chatroom))) {
            mRoom = getIntent().getParcelableExtra(getString(R.string.intent_chatroom));
            setChatroomName();
            joinChatroom();
        }
    }

    private void leaveChatroom() {

        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mRoom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list))
                .document(FirebaseAuth.getInstance().getUid());

        joinChatroomRef.delete();
    }

    private void joinChatroom() {

        DocumentReference joinChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document(mRoom.getChatroom_id())
                .collection(getString(R.string.collection_chatroom_user_list))
                .document(FirebaseAuth.getInstance().getUid());

        User user = ((MyApp) (getApplicationContext())).getUser();

        Log.d(TAG, "joinChatroom: user: " + user.getEmail());
        joinChatroomRef.set(user);
    }

    private void setChatroomName() {
        getSupportActionBar().setTitle(mRoom.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChatMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatMessageEventListener != null) {
            mChatMessageEventListener.remove();
        }
        if (mUserListEventListener != null) {
            mUserListEventListener.remove();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatroom_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                UserListFragment fragment =
                        (UserListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_user_list));
                if (fragment != null) {
                    if (fragment.isVisible()) {
                        getSupportFragmentManager().popBackStack();
                        return true;
                    }
                }
                finish();
                return true;
            }
            case R.id.action_chatroom_user_list: {
                inflateUserListFragment();
                return true;
            }
            case R.id.action_chatroom_leave: {
                leaveChatroom();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkmark: {
                insertNewMessage();
            }
        }
    }

}
