package com.example.storypostcard

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var postcardLayout: View
    private lateinit var bgImage: ImageView
    private lateinit var storyText: TextView
    private lateinit var generateBtn: Button
    private lateinit var pickBtn: Button
    private lateinit var emojiBtn: Button
    private lateinit var speakBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var shareBtn: Button

    private var tts: TextToSpeech? = null
    private var lastSavedUri: Uri? = null

    private val random = Random()

    private val emojis = arrayOf("🧙‍♂️", "🐶", "🤖", "🐉", "🕵️‍♂️", "👽", "🦸‍♀️", "🌟", "🌈")
    private val start = arrayOf(
        "A prince", "A dog", "A human", "A detective", "A superhero", "A scientist",
        "A clown", "A farmer", "A ghost", "A ninja"
    )
    private val background = arrayOf(
        "in Paris", "on the Moon", "in a haunted house", "under the sea", "in a jungle",
        "at school", "inside a video game", "in the desert", "on a flying ship", "in a secret lab"
    )
    private val action = arrayOf(
        "found a magical key", "stole a rainbow", "built a time machine",
        "discovered treasure", "saved the day", "learned to fly",
        "battled a monster", "invented a robot", "met a talking tree", "helped a stranger"
    )
    private val morals = arrayOf(
        "Always believe in yourself.",
        "Friendship is the greatest treasure.",
        "Curiosity leads to adventure.",
        "Courage is stronger than fear.",
        "Sharing makes life better.",
        "Never give up on your dreams."
    )

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            bgImage.setImageURI(it)
        }
    }

   
    private val requestWritePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            Toast.makeText(this, "Permission granted — try saving again", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission denied. Can't save on older Android versions.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        postcardLayout = findViewById(R.id.postcardLayout)
        bgImage = findViewById(R.id.bgImage)
        storyText = findViewById(R.id.storyText)
        generateBtn = findViewById(R.id.generateBtn)
        pickBtn = findViewById(R.id.pickBtn)
        emojiBtn = findViewById(R.id.emojiBtn)
        speakBtn = findViewById(R.id.speakBtn)
        saveBtn = findViewById(R.id.saveBtn)
        shareBtn = findViewById(R.id.shareBtn)

        tts = TextToSpeech(this, this)

        generateBtn.setOnClickListener {
            val story = generateStory()
            storyText.text = story
        }

        pickBtn.setOnClickListener {
            
            pickImageLauncher.launch("image/*")
        }

        emojiBtn.setOnClickListener {
            val e = emojis[random.nextInt(emojis.size)]
            storyText.append("\n\n$e")
        }

        speakBtn.setOnClickListener {
            val text = storyText.text.toString()
            if (text.isNotBlank()) {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "storyId")
            } else {
                Toast.makeText(this, "Generate a story first!", Toast.LENGTH_SHORT).show()
            }
        }

        saveBtn.setOnClickListener {
            
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestWritePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    return@setOnClickListener
                }
            }
            val bitmap = getBitmapFromView(postcardLayout)
            val uri = saveBitmapToGallery(bitmap)
            if (uri != null) {
                lastSavedUri = uri
                Toast.makeText(this, "Saved to gallery", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
            }
        }

        shareBtn.setOnClickListener {
            
            if (lastSavedUri != null) {
                shareImage(lastSavedUri!!)
            } else {
                val bitmap = getBitmapFromView(postcardLayout)
                val uri = saveBitmapToGallery(bitmap)
                uri?.let { shareImage(it) } ?: run { Toast.makeText(this, "Share failed", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun generateStory(): String {
        val s = start[random.nextInt(start.size)]
        val b = background[random.nextInt(background.size)]
        val a = action[random.nextInt(action.size)]
        val m = morals[random.nextInt(morals.size)]
        return "$s $b $a.\n\nMoral: $m"
    }

    private fun getBitmapFromView(view: View): Bitmap {
       
        if (view.width == 0 || view.height == 0) {
            val specWidth = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
            val specHeight = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
            view.measure(specWidth, specHeight)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bg = view.background
        if (bg != null) {
            bg.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Uri? {
        val filename = "StoryPostcard_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/StoryPostcards")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues)
                uri?.let {
                    fos = resolver.openOutputStream(it)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                    fos?.close()
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                    return it
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/StoryPostcards"
                val file = File(imagesDir)
                if (!file.exists()) file.mkdirs()
                val image = File(file, filename)
                fos = FileOutputStream(image)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.close()
                val uri = Uri.fromFile(image)
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
                return uri
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            fos?.close()
        }
        return null
    }

    private fun shareImage(uri: Uri) {
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(share, "Share your Story Postcard"))
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.ENGLISH
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

}
