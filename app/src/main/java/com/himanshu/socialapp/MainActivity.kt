package com.himanshu.socialapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.himanshu.socialapp.daos.PostDao
import com.himanshu.socialapp.daos.UserDao
import com.himanshu.socialapp.models.Post
import com.himanshu.socialapp.models.User
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity(), IPostAdapter ,DPostAdapter {
    private lateinit var adapter: PostAdapter
    private lateinit var postDao: PostDao
    private lateinit var usrDao: UserDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       
        fab.setOnClickListener{
          val intent = Intent(this,CreatePostActivity::class.java)
            startActivity(intent)
        }
        var user: User? = null
        val currentUserId = Firebase.auth.currentUser!!.uid
        GlobalScope.launch {
            usrDao = UserDao()
            user = usrDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            cUserName.text = user!!.displayName
            Glide.with(cUserImage.context).asBitmap().load(user!!.imageUrl).into(
                BitmapImageViewTarget(cUserImage)
            )
        }



        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        postDao = PostDao()
        val postsCollections = postDao.postCollections
        val query = postsCollections.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        adapter = PostAdapter(recyclerViewOptions,this,this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }

    override fun onDeleteClicked(postId: String) {
        postDao.deletePost(postId)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.log_out -> {

                    val builder = AlertDialog.Builder(this)
                      builder.setTitle("Log Out")
                     builder.setIcon(R.drawable.lg)
                builder.setMessage("Are you sure to log out?")
                builder.setPositiveButton("Yes"){dialogInterface, which ->
                    Firebase.auth.signOut()
                    startActivity(Intent(this,SignInActivity::class.java))
                }
                builder.setNegativeButton("No"){dialogInterface, which ->

                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()


                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
