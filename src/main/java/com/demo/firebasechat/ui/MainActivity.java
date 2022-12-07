package com.demo.firebasechat.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.firebasechat.MyApp;
import com.demo.firebasechat.R;
import com.demo.firebasechat.adapters.RoomAdapter;
import com.demo.firebasechat.models.Room;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        RoomAdapter.ChatroomRecyclerClickListener {

    private static final String TAG = "MainActivity";

    private ProgressBar mProgressBar;
    private RoomAdapter mRoomAdapter;
    private RecyclerView mChatroomRecyclerView;
    private ListenerRegistration mChatroomEventListener;
    private FirebaseFirestore mDb;

    private ArrayList<Room> mRooms = new ArrayList<>();
    private Set<String> mChatroomIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.progressBar);
        mChatroomRecyclerView = findViewById(R.id.chatrooms_recycler_view);

        findViewById(R.id.fab_create_chatroom).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();

        initSupportActionBar();
        initChatroomRecyclerView();
    }

    private void initSupportActionBar() {
        setTitle("Chatrooms");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_create_chatroom: {
                newChatroomDialog();
            }
        }
    }

    private void initChatroomRecyclerView() {
        mRoomAdapter = new RoomAdapter(mRooms, this);
        mChatroomRecyclerView.setAdapter(mRoomAdapter);
        mChatroomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getChatrooms() {

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        mDb.setFirestoreSettings(settings);

        CollectionReference chatroomsCollection = mDb
                .collection(getString(R.string.collection_chatrooms));

        mChatroomEventListener = chatroomsCollection.addSnapshotListener((queryDocumentSnapshots, e) -> {
            Log.d(TAG, "onEvent: called.");

            if (e != null) {
                Log.e(TAG, "onEvent: Listen failed.", e);
                return;
            }

            if (queryDocumentSnapshots != null) {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                    Room room = doc.toObject(Room.class);
                    if (!mChatroomIds.contains(room.getChatroom_id())) {
                        mChatroomIds.add(room.getChatroom_id());
                        mRooms.add(room);
                    }
                }
                mRoomAdapter.notifyDataSetChanged();
            }

        });
    }

    private void buildNewChatroom(String chatroomName) {

        final Room room = new Room();
        room.setTitle(chatroomName);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        mDb.setFirestoreSettings(settings);

        DocumentReference newChatroomRef = mDb
                .collection(getString(R.string.collection_chatrooms))
                .document();

        room.setChatroom_id(newChatroomRef.getId());

        newChatroomRef.set(room).addOnCompleteListener(task -> {
            hideDialog();

            if (task.isSuccessful()) {
                navChatroomActivity(room);
            } else {
                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void navChatroomActivity(Room room) {
        Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
        intent.putExtra(getString(R.string.intent_chatroom), room);
        startActivity(intent);
    }

    private void newChatroomDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a chatroom name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("CREATE", (dialog, which) -> {
            if (!input.getText().toString().equals("")) {
                buildNewChatroom(input.getText().toString());
            } else {
                Toast.makeText(MainActivity.this, "Enter a chatroom name", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatroomEventListener != null) {
            mChatroomEventListener.remove();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChatrooms();
    }

    @Override
    public void onChatroomSelected(int position) {
        navChatroomActivity(mRooms.get(position));
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting: {
                LangDialog dialog = new LangDialog(MainActivity.this) {
                    @Override
                    public void OnSelectLang(String lang) {
                        ((MyApp) (getApplicationContext())).setLang(lang);
                    }
                };
                dialog.show();
                return true;
            }
            case R.id.action_sign_out: {
                signOut();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }

        }
    }

    private void hideDialog() {
        mProgressBar.setVisibility(View.GONE);
    }


}
