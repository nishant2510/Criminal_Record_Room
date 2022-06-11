package com.example.minorproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.chat_item.*
import kotlinx.android.synthetic.main.fragment_chat.*

const val MESSAGE_BASE_PATH = "messages"
class ChatFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: FirebaseDatabase
    private lateinit var adapter: FirebaseRecyclerAdapter<UserMessage, ViewHolder?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        send_message_button.setOnClickListener {
            val messageText = message_text.text.toString()
            if(messageText.isBlank()){
                Toast.makeText(requireContext(), "Please Enter A Message To Send", Toast.LENGTH_LONG).show()
            }
            else{
                val ref = dbRef.getReference(MESSAGE_BASE_PATH).push()

                //Creating the object of UserMessage data class
                val userMessage = UserMessage(messageText, auth.currentUser?.email?:"Unknown")
                ref.setValue(userMessage).addOnSuccessListener {
                    message_text.setText("")
                }.addOnFailureListener{
                    Toast.makeText(requireContext(), "Failed To Send The Message ${it.toString()}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        //Recycler View
        chat_view.layoutManager = LinearLayoutManager(requireActivity())
        setupMessageList()
    }

    private fun setupMessageList() {
        val query: Query = dbRef.reference.child(MESSAGE_BASE_PATH)
        val options: FirebaseRecyclerOptions<UserMessage> = FirebaseRecyclerOptions.Builder<UserMessage>()
            .setQuery(query) {
                UserMessage(it.child("message").value.toString(), it.child("email").value.toString())
            }.build()

        adapter = object: FirebaseRecyclerAdapter<UserMessage, ViewHolder?>(options){
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(p0: ViewHolder, p1: Int, p2: UserMessage) {
                p0.bind(p2)
            }
        }
        chat_view.adapter = adapter
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(userMessage: UserMessage){
            with(userMessage){
                user_email.text="Sender - $email"
                user_message.text="Message - $message"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}