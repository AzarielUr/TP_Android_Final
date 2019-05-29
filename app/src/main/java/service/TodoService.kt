package service

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import models.Todo

object TodoService {

    private val COLLECTION_NAME = "todos"

    // --- COLLECTION REFERENCE ---

    val todoCollection: CollectionReference
        get() = FirebaseFirestore.getInstance().collection(COLLECTION_NAME)

    // --- CREATE ---

    fun createTodo(uid: String, title: String): Task<Void> {
        val userToCreate = Todo(uid, title, false)
        return TodoService.todoCollection.document(uid).set(userToCreate)
    }

    // --- GET ---

    fun getTodo(uid: String): Task<DocumentSnapshot> {
        return TodoService.todoCollection.document(uid).get()
    }

    // --- UPDATE ---

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