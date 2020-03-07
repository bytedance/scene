package com.bytedance.app_lib;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bytedance.scene.ui.template.AppCompatScene;
import com.bytedance.scenerouter.annotation.RouterValue;
import com.bytedance.scenerouter.annotation.SceneUrl;
import com.bytedance.scenerouter.core.SceneRouters;

import java.io.Serializable;
import java.util.ArrayList;

@SceneUrl({"/test1", "/test2", "/test3"})
public class LibraryDetailScene extends AppCompatScene {
    @RouterValue("haha")
    public String var00;

    @RouterValue("char211111")
    public CharSequence var01;

    @RouterValue
    public CharSequence[] var02;

    @RouterValue
    public ArrayList<String> var03;

    @RouterValue
    public String[] var04;

    @RouterValue
    public Bundle var05;

    @RouterValue
    public ArrayList<CharSequence> var06;

    @RouterValue
    public ArrayList<Integer> var07;

    @RouterValue
    public Uri var08;

    @RouterValue
    public FFF var09;

    @RouterValue
    public Uri[] var101;

    @RouterValue
    public int var0;

    @RouterValue
    public long var1;

    @RouterValue
    public char var2;

    @RouterValue
    public float var3;

    @RouterValue
    public short var4;

    @RouterValue
    public boolean var5;

    @RouterValue
    public byte var6;

    @RouterValue
    public double var7;

    @RouterValue
    public int[] var8;

    @RouterValue
    public long[] var9;

    @RouterValue
    public char[] var10;

    @RouterValue
    public float[] var11;

    @RouterValue
    public short[] var12;

    @RouterValue
    public boolean[] var13;

    @RouterValue
    public byte[] var14;

    @RouterValue
    public double[] var15;

    @RouterValue
    public ArrayList<Uri> var16;

    @NonNull
    @Override
    public View onCreateContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireSceneContext());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle("Library Scene");
        SceneRouters.bind(this);
        Toast.makeText(getSceneContext(), var00, Toast.LENGTH_SHORT).show();
    }

    public static class FFF implements Serializable {

    }
}
