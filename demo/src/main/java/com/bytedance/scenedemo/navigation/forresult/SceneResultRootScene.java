package com.bytedance.scenedemo.navigation.forresult;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.ActivityResultCallback;
import com.bytedance.scene.interfaces.PermissionResultCallback;

/**
 * Created by JiangQi on 8/3/18.
 */
public class SceneResultRootScene extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(getActivity());
        textView.setText(getNavigationScene().getStackHistory());
        layout.addView(textView);

        Button button = new Button(getActivity());
        button.setText("Scene从Scene拿结果");
        button.setAllCaps(false);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SceneResultScene_0.class);
            }
        });

        ImageView imageView = new ImageView(getActivity());
        layout.addView(imageView, new ViewGroup.LayoutParams(300, 100));

        button = new Button(getActivity());
        button.setText("Scene从Activity拿结果");
        button.setAllCaps(false);
        layout.addView(button);
        final ImageView finalImageView = imageView;
        button.setOnClickListener(new View.OnClickListener() {
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
                            finalImageView.setImageURI(uri);
                        }
                    }
                });
            }
        });

        button = new Button(getActivity());
        button.setText("极端Case，Scene从Activity拿结果，Activity直接返回不给结果");
        button.setAllCaps(false);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), TestActivityResultActivity.class);
                getNavigationScene().startActivityForResult(intent, 5, new ActivityResultCallback() {
                    @Override
                    public void onResult(int resultCode, @Nullable Intent result) {
                        Toast.makeText(requireApplicationContext(), "回调", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        button = new Button(getActivity());
        button.setText("Activity启动Scene（托管到SceneContainerActivity），并且拿结果");
        button.setAllCaps(false);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().startActivity(new Intent(requireActivity(), ActivityToSceneDemoActivity.class));
            }
        });

        imageView = new ImageView(getActivity());
        layout.addView(imageView, new ViewGroup.LayoutParams(300, 100));

        button = new Button(getActivity());
        button.setText("权限申请");
        button.setAllCaps(false);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123, new PermissionResultCallback() {
                    @Override
                    public void onResult(@Nullable int[] grantResults) {
                        if (grantResults.length > 0
                                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getActivity(), "成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        return layout;
    }
}
