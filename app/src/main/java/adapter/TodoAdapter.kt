package adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.aza.tp_final_android.R
import kotlinx.android.synthetic.main.todo_list_element.view.*
import models.Todo

class TodoAdapter(
    private val listener: TodoViewHolder.TodoListClickListener,
    private var todoList: List<Todo>
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    class TodoViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val titleTv: TextView = itemView.todo_item_title
        val commentTv: TextView = itemView.todo_item_comment

        init {
            itemView.setOnClickListener {
                clickListener.onTodoClick(it, adapterPosition)
            }

            itemView.setOnLongClickListener {
                clickListener.onTodoLongClick(it, adapterPosition)
                true
            }
        }

        private lateinit var clickListener: TodoListClickListener

        interface TodoListClickListener{
            fun onTodoClick(view: View, position: Int)
            fun onTodoLongClick(view: View, position: Int)
        }

        fun setOnClickListener(cl: TodoListClickListener){
            clickListener = cl
        }
    }

    override fun onCreateViewHolder(vg: ViewGroup, i: Int): TodoViewHolder {
        val itemView: View = LayoutInflater.from(vg.context).inflate(R.layout.todo_list_element, vg, false)

        val viewHolder = TodoViewHolder(itemView)
        viewHolder.setOnClickListener(listener)

        return viewHolder
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    override fun onBindViewHolder(vh: TodoViewHolder, i: Int) {
        vh.titleTv.text = todoList[i].title
        vh.commentTv.text = todoList[i].comment
    }


}