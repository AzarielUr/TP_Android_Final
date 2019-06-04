package service

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import models.Todo
import java.util.*

object TodoService {

    private val COLLECTION_NAME = "todos"

    // --- COLLECTION REFERENCE ---

    val todoCollection: CollectionReference
        get() = FirebaseFirestore.getInstance().collection(COLLECTION_NAME)

    // --- CREATE ---

    fun createTodo(title: String, comment: String?): Task<Void> {
        val todoToCreate = Todo(null, title, comment)
        val id = UUID.randomUUID().toString()
        return TodoService.todoCollection.document(id).set(todoToCreate)
    }


    // --- GET ---

    fun getTodo(uid: String): Task<DocumentSnapshot> {
        return TodoService.todoCollection.document(uid).get()
    }

    // --- UPDATE ---
    fun updateTodo(id: String, title: String, comment: String, done: Boolean): Task<Void>
    {
        val todoToUpdate = Todo(id, title, comment, done)
        return TodoService.todoCollection.document(id).update(
            "title", title,
            "comment", comment,
            "done", done
        )
    }

    fun updateTitle(username: String, uid: String): Task<Void> {
        return TodoService.todoCollection.document(uid).update("title", username)
    }

    fun updateCompletion(done: Boolean, uid: String): Task<Void> {
        return TodoService.todoCollection.document(uid).update("done", done)
    }


    // --- DELETE ---

    fun deleteTodo(uid: String): Task<Void> {
        return TodoService.todoCollection.document(uid).delete()
    }

}