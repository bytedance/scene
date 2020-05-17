package org.qiibeta.lib1

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bytedance.scene.ui.template.AppCompatScene
import com.bytedance.scenerouter.annotation.RouterValue
import com.bytedance.scenerouter.annotation.SceneUrl
import com.bytedance.scenerouter.core.SceneRouters
import java.io.Serializable
import java.util.*


@SceneUrl("/test1", "/test2", "/test3")
class LibraryDetailScene : AppCompatScene() {
    @JvmField
    @RouterValue("haha")
    var var00: String? = null

    @JvmField
    @RouterValue("char211111")
    var var01: CharSequence? = null

    @JvmField
    @RouterValue
    var var02: Array<CharSequence>? = null

    @JvmField
    @RouterValue
    var var03: ArrayList<String>? = null

    @JvmField
    @RouterValue
    var var04: Array<String>? = null

    @JvmField
    @RouterValue
    var var05: Bundle? = null

    @JvmField
    @RouterValue
    var var06: ArrayList<CharSequence>? = null

    @JvmField
    @RouterValue
    var var07: ArrayList<Int>? = null

    @JvmField
    @RouterValue
    var var08: Uri? = null

    @JvmField
    @RouterValue
    var var09: FFF? = null

    @JvmField
    @RouterValue
    var var101: Array<Uri>? = null

    @JvmField
    @RouterValue
    var var0 = 0

    @JvmField
    @RouterValue
    var var1: Long = 0

    @JvmField
    @RouterValue
    var var2 = 0.toChar()

    @JvmField
    @RouterValue
    var var3 = 0f

    @JvmField
    @RouterValue
    var var4: Short = 0

    @JvmField
    @RouterValue
    var var5 = false

    @JvmField
    @RouterValue
    var var6: Byte = 0

    @JvmField
    @RouterValue
    var var7 = 0.0

    @JvmField
    @RouterValue
    var var8: IntArray? = null

    @JvmField
    @RouterValue
    var var9: LongArray? = null

    @JvmField
    @RouterValue
    var var10: CharArray? = null

    @JvmField
    @RouterValue
    var var11: FloatArray? = null

    @JvmField
    @RouterValue
    var var12: ShortArray? = null

    @JvmField
    @RouterValue
    var var13: BooleanArray? = null

    @JvmField
    @RouterValue
    var var14: ByteArray? = null

    @JvmField
    @RouterValue
    var var15: DoubleArray? = null

    @JvmField
    @RouterValue
    var var16: ArrayList<Uri>? = null

    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return View(requireSceneContext())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle("Library Scene")
        SceneRouters.bind(this)
        Toast.makeText(getSceneContext(), var00, Toast.LENGTH_SHORT).show()
    }

    class FFF : Serializable
}
