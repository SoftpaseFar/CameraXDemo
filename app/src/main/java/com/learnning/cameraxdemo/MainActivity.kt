package com.learnning.cameraxdemo

import android.Manifest;
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.learnning.cameraxdemo.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * ActivityMoinBinding.infLate它的作用是viewBinding绑定activity_main.xml
         * -保证了数据类型的安全
         * -防止了空指针
         * - 简洁
         * 总之，比以前使用findViewById的方法要好
         * layoutInflater是一个试图导入器，用于加载XML转成相应的视图
         * 加载页面
         */
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // 动态申请权限
        if (allPermissionGranted()) {
            // start camera
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION)
        }

        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera() {
        // var 定义一个正常的变量，值可以改变
        // val 定义一个变量，值不可以改变
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // 添加监听事件
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                val cameraControl = camera.cameraControl
                val cameraInfo = camera.cameraInfo

                // 控制闪光灯
//                 cameraControl.enableTorch(true)


                // 手势
                // 第一步创建GestureDetector对象
                val gestureDetector =
                    GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                        private var previousDistance = -1f

                        override fun onScroll(
                            e1: MotionEvent?,
                            e2: MotionEvent,
                            distanceX: Float,
                            distanceY: Float
                        ): Boolean {
                            val pointerCount = e2.pointerCount
                            if (pointerCount < 2) return false // 至少需要两个手指进行缩放

                            val curZoomRatio = cameraInfo.zoomState.value?.zoomRatio ?: 1f

                            val deltaX = e2.getX(0) - e2.getX(1)
                            val deltaY = e2.getY(0) - e2.getY(1)
                            val distance = sqrt(deltaX * deltaX + deltaY * deltaY)

                            if (previousDistance == -1f) {
                                previousDistance = distance
                                return true
                            }

                            val scaleFactor = (distance - previousDistance) / 100f // 可调整缩放速度

                            val zoomRatio = curZoomRatio * (1 + scaleFactor)
                            val maxZoomRatio = cameraInfo.zoomState.value?.maxZoomRatio ?: 1f
                            val minZoomRatio = 1f // 设置最小缩放比例
                            cameraControl.setZoomRatio(
                                zoomRatio.coerceIn(
                                    minZoomRatio,
                                    maxZoomRatio
                                )
                            )

                            previousDistance = distance

                            return true
                        }

                    })
                // 第二步 监听时间
                viewBinding.viewFinder.setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                    true
                }


            } catch (exec: Exception) {
                Log.e(TAG, "Failed:", exec)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // 检查权限是否都已经给予
    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    // 点击拍照按钮
    private fun takePhoto() {
        Log.d(TAG, "test...")
        val imageCap = imageCapture ?: return
        println("Testing")
        val name =
            SimpleDateFormat(FILENAME_FORMAT, Locale.CHINA).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCap.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Failed to capture photo: ${exception}", exception)
                }


                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val msg = "Successes to capture photo: ${outputFileResults.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    // 权限给予回调
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (allPermissionGranted()) {
                startCamera()
                Log.d(TAG, "have been granted!")
            } else {
                Toast.makeText(
                    this, "Permissions not granted by the user!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    // 静态常量
    companion object {
        private const val TAG = "CameraXDemo: "
        private const val REQUEST_CODE_PERMISSION = 10;
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        // camera
        // record audio
        // write
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }


}