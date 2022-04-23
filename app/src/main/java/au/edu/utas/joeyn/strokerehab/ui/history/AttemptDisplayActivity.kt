package au.edu.utas.joeyn.strokerehab.ui.history

import android.Manifest
import android.R.attr.bitmap
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.joeyn.strokerehab.Record
import au.edu.utas.joeyn.strokerehab.databinding.ActivityAttemptDisplayBinding
import au.edu.utas.joeyn.strokerehab.databinding.ListViewItemForAttemptMessageBinding
import au.edu.utas.joeyn.strokerehab.databinding.ListViewItemThreeTextBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


const val ATTEMPT_ID_KEY = "attempt_id_key"
const val IMAGE_LOG_KEY = "image"
const val STROKE_PHOTOS_PATH = "StrokeRehab"
const val PERMISSION_CODE_READ = 1002
const val PERMISSION_CODE_WRITE = 1003

class AttemptDisplayActivity : AppCompatActivity() {





    private lateinit var ui : ActivityAttemptDisplayBinding

    val db = Firebase.firestore

    var documentIDForPhoto : String? = null
    var record : Record? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAttemptDisplayBinding.inflate(layoutInflater)



        ui.recordList.adapter = RecordItemAdapter()
        ui.recordList.layoutManager = LinearLayoutManager(ui.root.context)

        //get the record
        val documentID = intent.getStringExtra(ATTEMPT_ID_KEY)
        documentIDForPhoto = documentID
        title = "Loading ($documentID)"
        if (documentID != null) {
            db.collection("Records")
                .document(documentID)
                .get()
                .addOnSuccessListener { result ->
                    record = result.toObject<Record>()

                    if (record?.start == null){
                        record?.start = Timestamp(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(documentID))
                    }

                    title = (record?.title ?: "") +
                            SimpleDateFormat("  -  dd MMM yyyy  -  hh:mm:ss a", Locale.ENGLISH)
                                .format(record?.start?.toDate() ?: Date(0))

                    //x repetitions in x.xxx seconds
                    val lastMessage = record?.messages?.last()
                    if (lastMessage != null){
                        ui.repetitionsInSeconds.text = (lastMessage.rep ?: "?").toString() +
                                " repetitions in " +
                                (((lastMessage.datetime?.toDate()?.time ?: 0) -
                                        (record?.start?.toDate()?.time ?: 0)) / 1000f) +
                                " seconds"
                    }


                    //correct button presses
                    var correctButtonPresses = 0
                    record?.messages?.forEach { m ->
                        if (m.correctPress == true){
                            correctButtonPresses++
                        }
                    }
                    ui.correctButtonPresses.text = correctButtonPresses.toString() + " correct presses"


                    (ui.recordList.adapter as RecordItemAdapter).notifyDataSetChanged()
                }


            ui.deleteButton.setOnClickListener {

                val builder = AlertDialog.Builder(ui.root.context)
                builder.setMessage("Are you sure you want to permanently delete this record?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->
                        // Delete selected note from database
                        db.collection("Records")
                            .document(documentID).delete()
                        finish()
                    }
                    .setNegativeButton("No") { dialog, id ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()


            }
        }


        //TODO share button functionality


        //TODO camera button functionality
        ui.cameraButton.setOnClickListener {
            checkPermissionForImage()

            //getResult.launch(null)
        }



        updateBackgroundImage()

        setContentView(ui.root)
    }



    inner class ItemView(var ui: ListViewItemForAttemptMessageBinding) : RecyclerView.ViewHolder(ui.root) {}

    inner class RecordItemAdapter() : RecyclerView.Adapter<ItemView>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemView {
            val ui = ListViewItemForAttemptMessageBinding.inflate(layoutInflater, parent, false)   //inflate a new row from the my_list_item.xml
            return ItemView(ui)
        }

        override fun onBindViewHolder(holder: ItemView, position: Int) {

            val message = record?.messages?.get(position)


            if (message != null){
                //message
                holder.ui.leftText.text = message.message

                //time
                val time = (message.datetime?.toDate() ?: Date(0))
                val attemptStart = (record?.start?.toDate() ?: Date(0))
                val difference = (time.time - attemptStart.time) / 1000f //time after start in seconds
                holder.ui.middleText.text = String.format("+ %.1f s", difference)


                //correct or not
                holder.ui.rightText.text =
                    when (message.correctPress) {
                        true -> "✅️"
                        false -> "❌️️"
                        null -> "️"
                    }


            }

        }

        override fun getItemCount(): Int {
            return record?.messages?.size ?: 1
        }
    }





    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionCoarse = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)


                requestPermissions(permission, PERMISSION_CODE_READ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                requestPermissions(permissionCoarse, PERMISSION_CODE_WRITE) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            } else {

                val galleryIntent = Intent(Intent.ACTION_PICK)
                galleryIntent.type = "image/*"
                val cameraIntent = Intent("android.media.action.IMAGE_CAPTURE")
                val chooser = Intent.createChooser(galleryIntent, "Select an image for this attempt")
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))


                //TODO: Get the image

                getResult.launch(chooser)




            }
        }
    }



    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val value = it.data?.getStringExtra("input")
                Toast.makeText(ui.root.context, "message", Toast.LENGTH_LONG).show()


                if (it.data?.data != null){
                    //IMAGE URI = it.data?.data

                    Log.d(IMAGE_LOG_KEY, it.data?.data.toString())


                    val imageStream = getContentResolver().openInputStream(it.data?.data!!);
                    val imageBitmap = BitmapFactory.decodeStream(imageStream);


                    if (imageBitmap != null){
                        Log.d(IMAGE_LOG_KEY, bitmapToFile(imageBitmap).toString())
                    }


                    Log.d(IMAGE_LOG_KEY, it.data?.data.toString())
                }

                if (it.data?.extras?.get("data") != null){
                    //IMAGE BITMAP
                    val imageBitmap = it.data?.extras?.get("data") as Bitmap
                    Log.d(IMAGE_LOG_KEY, imageBitmap.toString())


                    Log.d(IMAGE_LOG_KEY, bitmapToFile(imageBitmap).toString())
                }






            }

            updateBackgroundImage()
        }



    // Method to save an bitmap to a file
    //Taken from https://www.android--code.com/2018/04/android-kotlin-convert-bitmap-to-file.html
    private fun bitmapToFile(bitmap:Bitmap): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(applicationContext)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file,"$documentIDForPhoto.jpg")

        try{
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }





    //updates the background images URI
    private fun updateBackgroundImage() {


        val wrapper = ContextWrapper(applicationContext)
        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file,"$documentIDForPhoto.jpg")
        val uri = Uri.fromFile(file);

        ui.imageView.setImageURI(uri)
    }
}