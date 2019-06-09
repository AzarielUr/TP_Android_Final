package com.aza.tp_final_android

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.ActionBar
import android.util.Log
import android.widget.*
import com.google.firebase.storage.StorageException
import kotlinx.android.synthetic.main.activity_todo.*
import kotlinx.android.synthetic.main.activity_todo.input_todo_comment
import kotlinx.android.synthetic.main.activity_todo.input_todo_title
import kotlinx.android.synthetic.main.activity_todo.todo_image
import service.TodoService
import java.io.File
import java.io.FileOutputStream
import android.support.v4.content.FileProvider
import java.lang.Exception


class TodoActivity : AppCompatActivity() {

    companion object {
        private const val ID_EXTRA = "ID_EXTRA"
        private const val TITLE_EXTRA = "TITLE_EXTRA"
        private const val COMMENT_EXTRA = "COMMENT_EXTRA"
        private const val DONE_EXTRA = "DONE_EXTRA"

        fun newIntent(context: Context?, id: String, title: String, comment: String, done: Boolean): Intent {
            val intent = Intent(context, TodoActivity::class.java)
            intent.putExtra(ID_EXTRA, id)
            intent.putExtra(TITLE_EXTRA, title)
            intent.putExtra(COMMENT_EXTRA, comment)
            intent.putExtra(DONE_EXTRA, done)
            return intent
        }
    }


    private lateinit var tv_title: TextView
    private lateinit var sw_completion: Switch
    private lateinit var et_title: EditText
    private lateinit var et_comment: EditText
    private lateinit var iv_todo: ImageView
    private lateinit var btn_submit: Button
    private lateinit var progress: ProgressDialog

    private var actionBar: ActionBar? = null


    private lateinit var id: String
    private lateinit var title: String
    private lateinit var comment: String
    private var done: Boolean = false;
    private var image: Bitmap? = null

    private var imageChanged: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        tv_title = tv_todo_title
        et_title = input_todo_title
        et_comment = input_todo_comment
        btn_submit = btn_update_todo
        iv_todo = todo_image
        sw_completion = completion_switch

        progress = ProgressDialog(this)
        progress.setTitle(getString(R.string.update_todo_progress))

        actionBar = supportActionBar

        val bundle = intent.extras
        id = bundle?.getString(ID_EXTRA) ?: ""
        title = bundle?.getString(TITLE_EXTRA) ?: ""
        comment = bundle?.getString(COMMENT_EXTRA) ?: ""
        done = bundle?.getBoolean(DONE_EXTRA) ?: false

        sw_completion.isChecked = done
        updateSwitchText(done)

        if (title != "") tv_title.text = title

        actionBar?.title = "Update $title"
        et_title.setText(title)
        et_comment.setText(comment)

        sw_completion.setOnCheckedChangeListener{ buttonView, isChecked ->

            updateSwitchText(isChecked)

            TodoService.updateCompletion(id, isChecked)
                .addOnCompleteListener{
                    done = isChecked
                    if (isChecked)
                    {
                        Toast.makeText(this, getString(R.string.done_message), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(this, getString(R.string.done_error_message), Toast.LENGTH_SHORT).show()
                }
        }

        iv_todo.setOnClickListener{
            dispatchTakePictureIntent()
        }

        iv_todo.setOnLongClickListener{
            exportImage()
            true
        }

        btn_submit.setOnClickListener {
            progress.show()

            TodoService.updateTodo(id, title, comment, false, null)
                .addOnCompleteListener {
                    progress.dismiss()

                    if (it.isSuccessful){

                        tv_title.text = title

                        if (imageChanged)
                        {
                            image?.let {
                                    img -> TodoService.uploadImage(id, img)
                                .addOnFailureListener{
                                    Toast.makeText(this, getString(R.string.update_todo_image_fail), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        Toast.makeText(this, getString(R.string.updated_todo_text), Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this, getString(R.string.updated_todo_text_fail), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        downloadTodoImage()
    }

    private fun updateSwitchText(isChecked: Boolean){
        if (isChecked)
        {
            sw_completion.text = getString(R.string.switch_done)
        }
        else
        {
            sw_completion.text = getString(R.string.switch_incomplete)
        }
    }

    private fun downloadTodoImage()
    {
        TodoService.getImage(id)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                image = bitmap
                todo_image.setImageBitmap(image)
            }
            .addOnFailureListener{
                if (it is StorageException)
                {
                    if (it.httpResultCode == 404)
                    {
                        Log.w(ContentValues.TAG, "Todo has no image")
                    }
                    else
                    {
                        Toast.makeText(this, getString(R.string.get_todo_image_fail), Toast.LENGTH_SHORT).show()
                        Log.w(ContentValues.TAG, "Error downloading image", it)
                    }
                }
            }
    }

    private val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            image = data?.extras?.get("data") as Bitmap
            imageChanged = true
            iv_todo.setImageBitmap(image)
        }
    }

    private fun exportImage() {

        if (image != null) {
            try{
                val cachePath = File(cacheDir, "images")
                cachePath.mkdirs()
                val stream = FileOutputStream("$cachePath/image.png")
                image!!.compress(Bitmap.CompressFormat.PNG,100, stream)
                stream.close()


                val newFile = File(cachePath, "image.png")

                val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", newFile)

                Intent(Intent.ACTION_SEND).also {sharePictureIntent ->
                    sharePictureIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    sharePictureIntent.type = "image/jpeg"

                    sharePictureIntent.resolveActivity(packageManager)?.also {
                        startActivity(Intent.createChooser(sharePictureIntent, getString(R.string.export_image)))
                    }
                }
            }
            catch (e: Exception ) {e.printStackTrace()}
        }
    }
}
