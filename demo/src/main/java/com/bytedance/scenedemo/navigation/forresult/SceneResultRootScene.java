package com.bytedance.scenedemo.navigation.forresult;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.ActivityResultCallback;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/3/18.
 */
public class SceneResultRootScene extends Scene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.nav_scene_result_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        TextView name = getView().findViewById(R.id.name);
        name.setText(getNavigationScene().getStackHistory());

        final ImageView image = getView().findViewById(R.id.image);
        Button btn2 = getView().findViewById(R.id.btn2);
        btn2.setText(getString(R.string.nav_result_scene_to_activity));
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent = Intent.createChooser(intent, "Select Image");

                getNavigationScene().startActivityForResult(intent, 12345, new ActivityResultCallback() {
                    @Override
                    public void onResult(int resultCode, @Nullable Intent result) {
                        if (resultCode == Activity.RESULT_OK) {
                            Uri uri = result.getData();
                            image.setImageURI(uri);
                        }
                    }
                });
            }
        });

        Button btn3 = getView().findViewById(R.id.btn3);
        btn3.setText(getString(R.string.nav_result_scene_to_activity_without_result));
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), TestActivityResultActivity.class);
                getNavigationScene().startActivityForResult(intent, 5, new ActivityResultCallback() {
                    @Override
                    public void onResult(int resultCode, @Nullable Intent result) {
                        Toast.makeText(requireApplicationContext(), getString(R.string.nav_result_callback_tip), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Button btn4 = getView().findViewById(R.id.btn4);
        btn4.setText(getString(R.string.nav_result_activity_to_scene));
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().startActivity(new Intent(requireActivity(), ActivityToSceneDemoActivity.class));
            }
        });
    }
}
