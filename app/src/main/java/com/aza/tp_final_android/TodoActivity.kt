package com.aza.tp_final_android

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.synthetic.main.activity_todo.*
import service.TodoService

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


    private lateinit var et_title: EditText
    private lateinit var et_comment: EditText
    private lateinit var btn_submit: Button
    private lateinit var progress: ProgressDialog
    private var actionBar: ActionBar? = null


    lateinit var id: String
    lateinit var title: String
    lateinit var comment: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        et_title = input_todo_title
        et_comment = input_todo_comment
        btn_submit = btn_update_todo

        progress = ProgressDialog(this)
        progress.setTitle(getString(R.string.update_todo_progress))

        actionBar = supportActionBar

        val bundle = intent.extras
        id = bundle?.getString("ID_EXTRA") ?: ""
        title = bundle?.getString("TITLE_EXTRA") ?: ""
        comment = bundle?.getString("COMMENT_EXTRA") ?: ""


        actionBar?.title = "Update $title"
        et_title.setText(title)
        et_comment.setText(comment)

        btn_submit.setOnClickListener(View.OnClickListener {
            progress.show()

            //TODO: Change done
            TodoService.updateTodo(id, title, comment, false)
                .addOnCompleteListener(OnCompleteListener {
                    progress.dismiss()

                    if (it.isSuccessful){
                        Toast.makeText(this, getString(R.string.updated_todo_text), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, getString(R.string.updated_todo_text_fail), Toast.LENGTH_SHORT).show()
                    }
                })
        })
    }

}
