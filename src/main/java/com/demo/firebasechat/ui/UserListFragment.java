package com.demo.firebasechat.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demo.firebasechat.R;
import com.demo.firebasechat.adapters.UserAdapter;
import com.demo.firebasechat.models.User;

import java.util.ArrayList;

public class UserListFragment extends Fragment {

    private static final String TAG = "FriendListFragment";

    private RecyclerView mUserListRecyclerView;
    private ArrayList<User> mUserList = new ArrayList<>();
    private UserAdapter mUserAdapter;

    public static UserListFragment newInstance() {
        return new UserListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserList = getArguments().getParcelableArrayList(getString(R.string.intent_user_list));
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        mUserListRecyclerView = view.findViewById(R.id.user_list_recycler_view);

        initUserListRecyclerView();
        return view;
    }


    private void initUserListRecyclerView() {
        mUserAdapter = new UserAdapter(mUserList);
        mUserListRecyclerView.setAdapter(mUserAdapter);
        mUserListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
}



















