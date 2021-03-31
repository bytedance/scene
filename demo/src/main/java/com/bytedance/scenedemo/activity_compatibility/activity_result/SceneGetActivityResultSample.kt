package com.bytedance.scenedemo.activity_compatibility.activity_result

import com.bytedance.scene.ui.template.AppCompatScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.bytedance.scenedemo.R
import android.content.Intent
import android.provider.MediaStore
import com.bytedance.scene.interfaces.ActivityResultCallback
import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.bytedance.scenedemo.navigation.forresult.TestActivityResultActivity
import android.widget.Toast

/**
 * Created by JiangQi on 8/3/18.
 */
class SceneGetActivityResultSample : AppCompatScene() {
    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.nav_scene_result_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle(R.string.main_activity_btn_scene_get_activity_result)
        val image = view.findViewById<ImageView>(R.id.image)
        val btn2 = view.findViewById<Button>(R.id.btn2)
        btn2.text = getString(R.string.nav_result_scene_to_activity)
        btn2.setOnClickListener {
            var intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            intent = Intent.createChooser(intent, "Select Image")
            requireNavigationScene().startActivityForResult(intent, 12345) { resultCode, result ->
                if (resultCode == Activity.RESULT_OK) {
                    val uri = result!!.data
                    image.setImageURI(uri)
                }
            }
        }
        val btn3 = view.findViewById<Button>(R.id.btn3)
        btn3.text = getString(R.string.nav_result_scene_to_activity_without_result)
        btn3.setOnClickListener {
            val intent = Intent(requireActivity(), TestActivityResultActivity::class.java)
            requireNavigationScene().startActivityForResult(intent, 5) { resultCode, result ->
                Toast.makeText(
                    requireApplicationContext(),
                    getString(R.string.nav_result_callback_tip),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}