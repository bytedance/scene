package com.bytedance.scenedemo.animation.sharedelement;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.ChangeBounds;
import android.support.transition.ChangeClipBounds;
import android.support.transition.ChangeTransform;
import android.support.transition.Slide;
import android.support.transition.Transition;
import android.support.transition.TransitionSet;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bytedance.scene.animation.NavigationTransitionExecutor;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 8/30/18.
 */
public class SharedElementScene0 extends GroupScene implements ComposedAdapter.OnItemCLickListener {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.scene_transition, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<Integer> images = new ArrayList<>();
        images.add(R.drawable.chiot1);
        images.add(R.drawable.chiot2);
        images.add(R.drawable.chiot3);
        images.add(R.drawable.chiot4);
        images.add(R.drawable.chiot5);
        images.add(R.drawable.chiot6);
        images.add(R.drawable.chiot7);
        images.add(R.drawable.chiot8);

        RecyclerView mRecyclerView = getView().findViewById(R.id.recyclerview);

        final RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(layoutManager);

        final ComposedAdapter adapter = new ComposedAdapter(images);
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(View imageView, View textView, int imageRefId, String imageTransitionName) {
        final String textViewContent = ((TextView) textView).getText().toString();
        Bundle bundle = new Bundle();
        bundle.putString("textViewContent", textViewContent);
        bundle.putString("imageTransitionName", imageTransitionName);
        bundle.putInt("imageRefId", imageRefId);

        List<String> a = new ArrayList<>();
        a.add(imageTransitionName);
//        a.add(textViewContent);

//        getNavigationScene().push(SharedElementScene1.class, bundle, new PushOptions.Builder().setAnimation(new Test2(a)).build());
    }

//    class Test2 extends SharedElementSceneTransitionExecutor {
//        List<String> a = new ArrayList<>();
//
//        public Test2(List<String> a) {
//            this.a = a;
//        }
//
//        @Override
//        protected List<String> getSharedElementTransition() {
//            return a;
//        }
//    }

    class Test extends NavigationTransitionExecutor {
        List<String> a = new ArrayList<>();

        public Test(List<String> a) {
            this.a = a;
        }

        @Override
        protected Transition getSharedElementTransition() {
//            Transition transitionSet = new TransitionSet().addTransition(new ChangeClipBounds())
//                    .addTransition(new ChangeBounds());
            Transition transitionSet = new TransitionSet().addTransition(new ChangeBounds()).addTransition(new ChangeClipBounds()).addTransition(new ChangeTransform());

//            ChangeTransform changeTransform = new ChangeTransform();

//            transitionSet = new ChangeTransform();
            for (String s : a) {
                transitionSet.addTarget(s);
            }
            return transitionSet;
        }

        @Override
        protected Transition getOthersTransition() {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.BOTTOM);
            return slide;
//            return null;
        }
    }

    private static ImageView a(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));

        ImageView imageView = new ImageView(view.getContext());
        imageView.setImageBitmap(bitmap);
        imageView.layout(0, 0, view.getWidth(), view.getHeight());
        return imageView;
    }

    class OverlayChangeBounds extends ChangeBounds {
//        @Nullable
//        @Override
//        public Animator createAnimator(@NonNull final ViewGroup sceneRoot, @Nullable TransitionValues startValues, @Nullable final TransitionValues endValues) {
//            if (startValues == null || endValues == null) {
//                return super.createAnimator(sceneRoot, startValues, endValues);
//            }
//            View startView = startValues.view;
//            final View endView = endValues.view;
//
//            if (getTargetIds().contains(endView.getId()) || getTargetNames().contains(ViewCompat.getTransitionName(endView))) {
//                startView.setVisibility(View.INVISIBLE);
//                endView.setVisibility(View.INVISIBLE);
//
//                endValues.view = a(startValues.view);
//                sceneRoot.getOverlay().add(endValues.view);
//                Animator animator = super.createAnimator(sceneRoot, startValues, endValues);
//                animator.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        super.onAnimationEnd(animation);
//                        endView.setVisibility(View.VISIBLE);
//                        sceneRoot.getOverlay().remove(endValues.view);
//                    }
//                });
//                return animator;
//            }
//            return super.createAnimator(sceneRoot, startValues, endValues);
//        }
    }

    //        override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
//        if (startValues == null || endValues == null) return super.createAnimator(sceneRoot, startValues, endValues)
//
//        val startView = startValues.view
//        val endView = endValues.view
//
//        if (endView.id in targetIds || targetNames?.contains(endView.transitionName) == true) {
//            startView.visibility = View.INVISIBLE
//            endView.visibility = View.INVISIBLE
//
//            endValues.view = startValues.view.toImageView()
//            sceneRoot.overlay.add(endValues.view)
//
//            return super.createAnimator(sceneRoot, startValues, endValues)?.apply {
//                addListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation:Animator) {
//                        endView.visibility = View.VISIBLE
//                        sceneRoot.overlay.remove(endValues.view)
//                    }
//                })
//            }
//        }
//
//        return super.createAnimator(sceneRoot, startValues, endValues)
//        }

}
