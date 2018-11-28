package com.bytedance.scenedemo.animation.sharedelement;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 8/30/18.
 */
public class SharedElementScene1 extends GroupScene {
    private ImageView mImageView;
    private TextView mTextView;

    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.scene_composed, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String textViewContent = getArguments().getString("textViewContent");
        String imageTransitionName = getArguments().getString("imageTransitionName");
        int imageRefId = getArguments().getInt("imageRefId");

        mImageView = getView().findViewById(R.id.view_imageview);
        mTextView = getView().findViewById(R.id.view_name);

        mTextView.setVisibility(View.GONE);

        mImageView.setImageResource(imageRefId);
//        mTextView.setText(textViewContent);

//        ViewCompat.setTransitionName(mTextView, textViewContent);
        ViewCompat.setTransitionName(mImageView, imageTransitionName);
        add(R.id.container, new ChildScene0(), "ChildScene0");
    }

    public static class ChildScene0 extends GroupScene {

        @NonNull
        @Override
        public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return (ViewGroup) inflater.inflate(R.layout.scene_dog, container, false);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            final List<Integer> images = new ArrayList<>();
            images.add(R.drawable.dog1);
            images.add(R.drawable.dog2);
            images.add(R.drawable.dog3);

            RecyclerView mRecyclerView = getView().findViewById(R.id.recyclerview_dog);

            final RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 1);
            final DogAdapter dogAdapter = new DogAdapter(images);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(dogAdapter);
        }
    }
}
