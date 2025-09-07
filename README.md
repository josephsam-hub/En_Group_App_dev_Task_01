📖 Story Postcard

🎨 An Android app that generates random short stories, overlays them on custom backgrounds, speaks them aloud, and lets you save/share postcard-style images.

✨ Features

⚡ Random Story Generator → Mixes characters, places, actions, and morals.

🖼️ Custom Background → Pick any image from your gallery.

🎭 Emojis & Fun → Add random emojis to spice up your story.

🔊 Text-to-Speech → Reads your story aloud.

💾 Save as Postcard → Exports the entire card as an image into your gallery.

📤 Share Anywhere → Send your postcard via WhatsApp, Instagram, Gmail, etc.

📱 Modern Android → Uses Activity Result APIs, MediaStore, and works from Android 5 (Lollipop) to Android 14.

🛠️ Tech Stack

Language: Kotlin

UI: XML Layout (LinearLayout + FrameLayout + ScrollView)

Core APIs:

TextToSpeech

ActivityResultContracts (Gallery picker + Permissions)

MediaStore (modern image saving)

Canvas + Bitmap (postcard rendering)

Min SDK: 21 (Lollipop)

Target SDK: 34 (Android 14)

📂 Project Structure
StoryPostcard/
 ├── app/
 │   ├── src/
 │   │   ├── main/
 │   │   │   ├── java/com/example/storypostcard/MainActivity.kt
 │   │   │   ├── res/
 │   │   │   │   ├── layout/activity_main.xml
 │   │   │   │   ├── drawable/ (icons, backgrounds if any)
 │   │   │   │   ├── values/ (themes, colors, strings)
 │   │   │   └── AndroidManifest.xml
 │   ├── build.gradle
 ├── gradle/
 └── README.md
