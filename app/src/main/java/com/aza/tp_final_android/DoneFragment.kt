package com.aza.tp_final_android

import adapter.TodoAdapter
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.fragment_home.view.*
import models.Todo
import service.TodoService


class DoneFragment : Fragment(), TodoAdapter.TodoViewHolder.TodoListClickListener {
    private var listener: OnFragmentInteractionListener? = null

    private var todoList: MutableList<Todo> = mutableListOf()
    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var adapter: TodoAdapter
    private lateinit var progress: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_done, container, false)

        progress = ProgressDialog(activity)

        todoRecyclerView = view.recycler_view_todo
        todoRecyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        todoRecyclerView.layoutManager = layoutManager



        return view
    }

    override fun onResume() {
        super.onResume()
        showData()
    }

    private fun showData() {

        todoList.clear()

        progress.setTitle(getString(R.string.get_todos_progress))
        progress.show()

        TodoService.getCompletedTodos()
            .addOnCompleteListener {
                progress.dismiss()

                if (!it.isSuccessful) {
                    Toast.makeText(context, getString(R.string.get_todos_failure), Toast.LENGTH_SHORT).show()
                } else {
                    for (doc: DocumentSnapshot in it.result!!) {
                        val id = doc.id
                        val title = doc.getString("title").toString()
                        val comment = doc.getString("comment")

                        val todo = Todo(id, title, comment)

                        todoList.add(todo)
                    }

                    adapter = TodoAdapter(this, todoList)
                    todoRecyclerView.adapter = adapter
                }
            }
    }

    override fun onTodoClick(view: View, position: Int) {

        val id = todoList[position].id ?: ""
        val title = todoList[position].title
        val comment = todoList[position].comment ?: ""

        TodoActivity.newIntent(context, id, title, comment, true).run {
            startActivity(this)
        }
    }

    override fun onTodoLongClick(view: View, position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        val options = arrayOf(getString(R.string.mark_incomplete_dialog), getString(R.string.delete_dialog))
        builder.setItems(options) { _, which ->
            run {
                if (which == 0) //Update
                {
                    markIncomplete(position)
                }
                if (which == 1) //Delete
                {
                    deleteTodo(position)
                }
            }
        }.create().show()
    }

    private fun markIncomplete(position: Int)
    {
        val id = todoList[position].id ?: ""
        TodoService.updateCompletion(id, false)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(activity, getString(R.string.incompleted_message), Toast.LENGTH_SHORT).show()
                    showData()
                } else {
                    Toast.makeText(activity, getString(R.string.incompleted_error_message), Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun deleteTodo(position: Int)
    {
        progress.setTitle(getString(R.string.delete_todo_progress))
        progress.show()

        val id = todoList[position].id ?: ""

        TodoService.deleteTodo(id).addOnCompleteListener{
            if (it.isSuccessful){
                Toast.makeText(activity, getString(R.string.deleted_message), Toast.LENGTH_SHORT).show()
                showData()
            }
            else {
                Toast.makeText(activity, getString(R.string.delete_error_message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
