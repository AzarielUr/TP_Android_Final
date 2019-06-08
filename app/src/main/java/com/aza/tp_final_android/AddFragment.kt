package com.aza.tp_final_android

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add.*
import service.TodoService




class AddFragment : Fragment() {

    private lateinit var et_title: EditText
    private lateinit var et_comment: EditText
    private lateinit var btn_submit: Button
    private lateinit var todo_image: ImageView
    private lateinit var progress: ProgressDialog
    private var image: Bitmap? = null


    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add, container, false)

        et_title = view.findViewById(R.id.input_todo_title)
        et_comment = view.findViewById(R.id.input_todo_comment)
        todo_image = view.findViewById(R.id.todo_image)
        btn_submit = view.findViewById(R.id.add_todo_submit)

        todo_image.setOnClickListener {
            dispatchTakePictureIntent()
        }

        btn_submit.setOnClickListener {
            onAddTodo()
        }

        progress = ProgressDialog(activity)
        progress.setTitle(getString(R.string.add_todo_progress))

        return view

    }

    private fun onAddTodo(){
        val title: String = et_title.text.toString()
        val comment: String? = et_comment.text.toString()

        progress.show()


        TodoService.createTodo(title, comment)
            .addOnCompleteListener {
                progress.dismiss()
            }
            .addOnSuccessListener {
                val id = it.id

                if (image != null)
                {
                    TodoService.uploadImage(id, image!!)
                        .addOnSuccessListener {
                            Toast.makeText(activity, getString(R.string.added_todo_text), Toast.LENGTH_SHORT).show()
                            (activity as MainActivity).navigation.selectedItemId = R.id.navigation_home
                        }
                        .addOnFailureListener{
                            Toast.makeText(activity, getString(R.string.added_todo_image_fail), Toast.LENGTH_SHORT).show()
                            Log.w(TAG, "Error adding document", it)
                        }
                }
                else {

                    Toast.makeText(activity, getString(R.string.added_todo_text), Toast.LENGTH_SHORT).show()
                    (activity as MainActivity).navigation.selectedItemId = R.id.navigation_home
                }

            }
            .addOnFailureListener{
                Toast.makeText(activity, getString(R.string.added_todo_text_fail), Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Error adding document", it)
            }
    }

    private val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(context?.packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            image = data.extras.get("data") as Bitmap
            todo_image.setImageBitmap(image)
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
