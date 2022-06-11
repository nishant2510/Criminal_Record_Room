package com.example.minorproject.ui

import android.app.Activity.RESULT_OK
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minorproject.BuildConfig
import com.example.minorproject.R
import com.example.minorproject.data.Criminal
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_criminal_list.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import com.google.firebase.auth.FirebaseUser
import androidx.annotation.NonNull
import com.example.minorproject.authentication.LoginFragment
import com.example.minorproject.ui.CriminalListAdapter
import com.example.minorproject.ui.CriminalListFragmentDirections
import com.example.minorproject.ui.createFile
import com.google.firebase.auth.FirebaseAuth.AuthStateListener

const val READ_FILE_REQUEST = 3
const val CREATE_FILE_REQUEST = 7

class CriminalListFragment : Fragment(){

    private lateinit var viewModel: CriminalListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this).get(CriminalListViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_criminal_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with(criminal_list){
            layoutManager = LinearLayoutManager(activity)
            adapter = CriminalListAdapter{
                findNavController().navigate(CriminalListFragmentDirections.actionCriminalListFragmentToCriminalDetailFragment(it))
            }
        }

        add_criminal.setOnClickListener{
            findNavController().navigate(CriminalListFragmentDirections.actionCriminalListFragmentToCriminalDetailFragment(0))
        }

        viewModel.criminals.observe(viewLifecycleOwner, {
            (criminal_list.adapter as CriminalListAdapter).submitList(it)
        })
    }

    //Inflating the menu in our fragment
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_import_data -> {
                importEmployees()
                true
            }
            R.id.menu_export_data -> {
                //Using GlobalScope so that even if user leaves the fragment import will occur
                GlobalScope.launch {
                    exportCriminals()
                }
                true
            }
            R.id.menu_last_added_criminal -> {
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return true
                val name = sharedPref.getString(LAST_ADDED_CRIMINAL_KEY.toString(), "")
                if(!name.isNullOrEmpty()){
                    Toast.makeText(activity!!, "Last Added Criminal : $name", Toast.LENGTH_LONG).show()
                } else{
                    Toast.makeText(activity!!, "No Criminals Added Yet", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.menu_admin_chat -> {
                findNavController().navigate(CriminalListFragmentDirections.actionCriminalListFragmentToChatFragment())
                true
            }
            R.id.menu_about_us -> {
                findNavController().navigate(CriminalListFragmentDirections.actionCriminalListFragmentToAboutFragment())
                true
            }
            R.id.menu_contact_us -> {
                findNavController().navigate(CriminalListFragmentDirections.actionCriminalListFragmentToContactFragment())
                true
            }
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut();
                findNavController().navigate(CriminalListFragmentDirections.actionCriminalListFragmentToLoginFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode==RESULT_OK){
            when (requestCode){
                READ_FILE_REQUEST -> {
                    data?.data.also{ uri ->
                        GlobalScope.launch {
                            if (uri != null) {
                                readFromFile(uri)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun importEmployees() {
        //Using intent to let the other app show the available CSV files so that user can choose one of them
        Intent(Intent.ACTION_GET_CONTENT).also { readFileIntent ->
            readFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
            readFileIntent.type = "text/*"
            //Checking if  there is any activity which meets our above stated requirement
            readFileIntent.resolveActivity(activity!!.packageManager)?.also {
                startActivityForResult(readFileIntent, READ_FILE_REQUEST)
            }
        }
    }

    private suspend fun readFromFile(uri: Uri) {
        try {
            //Using contentResolver to access the file with the given uri
            activity!!.applicationContext.contentResolver.openFileDescriptor(uri, "r")?.use {
                withContext(Dispatchers.IO) {
                    FileInputStream(it.fileDescriptor).use {
                        parseCSVFile(it)
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private suspend fun parseCSVFile(stream: FileInputStream) {
        val criminals = mutableListOf<Criminal>()

        BufferedReader(InputStreamReader(stream)).forEachLine {
            val tokens = it.split(",")
            criminals.add(Criminal(id=0, name = tokens[0], aadhaarNumber = tokens[1], priority = tokens[2].toInt(), age = tokens[3].toInt(), gender = tokens[4].toInt(),
                crimeDescription = tokens[5], crimeDate = tokens[6], crimeTime = tokens[7], crimeLocation = tokens[8], officerOnDuty = tokens[9],  photo = ""))
        }

        if(criminals.isNotEmpty()){
            viewModel.insertCriminals(criminals)
        }
    }

    private suspend fun exportCriminals() {
        var csvFile: File? = null
        //Switching context
        withContext(Dispatchers.IO) {
            csvFile = try {
                createFile(activity!!, "Documents", "csv")
            } catch (ex: IOException) {
                Toast.makeText(
                    activity!!,
                    "Error occurred while creating file: {ex.message}",
                    Toast.LENGTH_SHORT
                ).show()
                null
            }

            csvFile?.printWriter()?.use { out ->
                val criminals = viewModel.getCriminalList()
                if (criminals.isNotEmpty()) {
                    criminals.forEach {
                        out.println(it.name + "," + it.aadhaarNumber + "," + it.priority + "," + it.age + "," + it.gender + "," +
                                it.crimeDescription + "," + it.crimeDate + "," + it.crimeTime + "," + it.crimeLocation + "," + it.officerOnDuty)
                    }
                }
            }
        }
        withContext(Dispatchers.Main) {
            csvFile?.let {
                //Getting uri of exported file
                val uri = FileProvider.getUriForFile(
                    activity!!,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    it
                )
                launchFile(uri, "csv")
            }
        }
    }

    private fun launchFile(uri: Uri, ext: String) {
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, mimeType)
        if(intent.resolveActivity(activity!!.packageManager) != null){
            startActivity(intent)
        }
        else{
            Toast.makeText(activity!!, "No app found to read CSV file", Toast.LENGTH_SHORT).show()
        }
    }
}