package com.bytedance.scenedemo.activity_compatibility.activity_result;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bytedance.scene.interfaces.ActivityResultCallback;
import com.bytedance.scene.ui.template.AppCompatScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.navigation.forresult.TestActivityResultActivity;

/**
 * Created by JiangQi on 8/3/18.
 */
public class SceneGetActivityResultSample extends AppCompatScene {
    @Nullable
    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.nav_scene_result_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle(R.string.main_activity_btn_scene_get_activity_result);

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
    }
}
