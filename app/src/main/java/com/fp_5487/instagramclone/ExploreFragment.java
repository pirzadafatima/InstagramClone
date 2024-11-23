package com.fp_5487.instagramclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ExploreFragment extends Fragment {

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        Button commentsButton = view.findViewById(R.id.button_comments);
        commentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CommentsActivity.class);
            startActivity(intent);
        });


        return view;
    }

}
