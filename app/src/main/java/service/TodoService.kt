package service

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.grpc.Context
import models.Todo
import java.io.ByteArrayOutputStream
import java.util.*

object TodoService {

    private val COLLECTION_NAME = "todos"
    private val IMAGE_STORAGE_NAME = "images"
    private val ONE_MEGABYTE: Long = 1024 * 1024

    // --- COLLECTION REFERENCE ---

    val todoCollection: CollectionReference
        get() = FirebaseFirestore.getInstance().collection(COLLECTION_NAME)

    val imagesRef: StorageReference = FirebaseStorage.getInstance().reference


    // -- GET ---
    fun getIncompleteTodos() : Task<QuerySnapshot> {
        return todoCollection.whereEqualTo("done", false).get()
    }

    fun getCompletedTodos() : Task<QuerySnapshot> {
        return todoCollection.whereEqualTo("done", true).get()
    }


    // --- CREATE ---

    fun createTodo(title: String, comment: String?): Task<DocumentReference> {
        val id = UUID.randomUUID().toString()
        val todoToCreate = Todo(id, title, comment, false)
        return TodoService.todoCollection.add(todoToCreate)
    }

    fun uploadImage(id: String, image: Bitmap) : UploadTask
    {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        return imagesRef.child("$IMAGE_STORAGE_NAME/$id").putBytes(data)
    }


    // --- GET ---

    fun getTodo(uid: String): Task<DocumentSnapshot> {
        return TodoService.todoCollection.document(uid).get()
    }

    fun getImage(id: String): Task<ByteArray> {
        val ref = imagesRef.child("$IMAGE_STORAGE_NAME/$id")
        return ref.getBytes(ONE_MEGABYTE)
    }

    // --- UPDATE ---
    fun updateTodo(id: String, title: String, comment: String, done: Boolean, image: Bitmap?): Task<Void>
    {
        val todoToUpdate = Todo(id, title, comment, done)
        return TodoService.todoCollection.document(id).update(
            "title", title,
            "comment", comment,
            "done", done
        )
    }

    fun updateCompletion(id: String, done: Boolean): Task<Void> {
        return TodoService.todoCollection.document(id).update("done", done)
    }


    // --- DELETE ---

    fun deleteTodo(id: String): Task<Void> {
        return TodoService.todoCollection.document(id).delete()
    }

}