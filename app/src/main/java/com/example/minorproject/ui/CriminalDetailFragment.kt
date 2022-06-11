package com.example.minorproject.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.minorproject.BuildConfig
import com.example.minorproject.R
import com.example.minorproject.data.CasePriority
import com.example.minorproject.data.Criminal
import com.example.minorproject.data.Gender
import com.example.minorproject.ui.CriminalDetailFragmentArgs
import com.example.minorproject.ui.CriminalDetailViewModel
import com.example.minorproject.ui.createFile
import kotlinx.android.synthetic.main.fragment_criminal_detail.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

const val CAMERA_PHOTO_REQUEST = 1
const val GALLERY_PHOTO_REQUEST = 2
const val LAST_ADDED_CRIMINAL_KEY = 6

class CriminalDetailFragment : Fragment() {
    private var selectedPhotoPath: String = ""

    //Giving camera permission to application
    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }
            if (allAreGranted) {
                clickPhoto()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //init views / binding
        photo_from_camera.setOnClickListener {
            val appPermission = arrayOf(Manifest.permission.CAMERA)
            activityResultLauncher.launch(appPermission)
        }
    }

    // Creating instance of CriminalDetailViewModel
    private lateinit var viewModel: CriminalDetailViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CriminalDetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_criminal_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Populating the Case Priority spinner
        val priorities = mutableListOf<String>()
        CasePriority.values().forEach { priorities.add(it.name) }
        val arrayAdapter =
            ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, priorities)
        case_priority.adapter = arrayAdapter

        // Populating the Criminal Age Spinner
        val ages = mutableListOf<Int>()
        for (i in 12 until 120) {
            ages.add(i)
        }
        criminal_age.adapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, ages)

        criminal_photo.setImageResource(R.drawable.blank)
        criminal_photo.tag = ""

        val id = CriminalDetailFragmentArgs.fromBundle(requireArguments()).id
        viewModel.setCriminalId(id)
        viewModel.criminal.observe(viewLifecycleOwner, {
            it?.let { setData(it) }
        })

        save_criminal.setOnClickListener {
            saveCriminal()
        }

        delete_criminal.setOnClickListener {
            deleteCriminal()
        }

        criminal_photo.setOnClickListener {
            criminal_photo.setImageResource(R.drawable.blank)
            criminal_photo.tag = ""
        }

        photo_from_gallery.setOnClickListener {
            pickPhoto()
        }
    }

    private fun setData(criminal: Criminal) {
        with(criminal.photo) {
            if (isNotEmpty()) {
                criminal_photo.setImageURI(Uri.parse(this))
                criminal_photo.tag = this
            } else {
                criminal_photo.setImageResource(R.drawable.blank)
                criminal_photo.tag = ""
            }
        }
        criminal_name.setText(criminal.name)
        aadhaar_number.setText(criminal.aadhaarNumber)
        case_priority.setSelection(criminal.priority)
        criminal_age.setSelection(criminal.age - 12)
        crime_description.setText(criminal.crimeDescription)
        crime_date.setText(criminal.crimeDate)
        crime_time.setText(criminal.crimeTime)
        crime_location.setText(criminal.crimeLocation)
        officer_on_duty.setText(criminal.officerOnDuty)


        when (criminal.gender) {
            Gender.Male.ordinal -> {
                gender_male.isChecked = true
            }
            Gender.Female.ordinal -> {
                gender_female.isChecked = true
            }
            else -> {
                gender_other.isChecked = true
            }
        }
    }

    private fun saveCriminal() {
        val name = criminal_name.text.toString()
        val aadhaar = aadhaar_number.text.toString()
        val priority = case_priority.selectedItemPosition
        val age = criminal_age.selectedItemPosition + 12
        val description = crime_description.text.toString()
        val date = crime_date.text.toString()
        val time = crime_time.text.toString()
        val location = crime_location.toString()
        val officer = officer_on_duty.text.toString()

        // Getting the selected radio button
        val selected = criminal_gender_group.findViewById<RadioButton>(criminal_gender_group.checkedRadioButtonId)
        var gender = Gender.Other.ordinal
        if (selected.text == Gender.Male.name) {
            gender = Gender.Male.ordinal
        } else if (selected.text == Gender.Female.name) {
            gender = Gender.Female.ordinal
        }

        val photo = criminal_photo.tag as String

        //Validating the data
        if(name.isBlank()){
            Toast.makeText(activity!!, "Please Enter The Name Of Criminal", Toast.LENGTH_SHORT).show()
            return
        }
        if(aadhaar.isBlank() || aadhaar.length!=12){
            Toast.makeText(activity!!, "Please Enter A Valid Aadhar Number Of Criminal", Toast.LENGTH_SHORT).show()
            return
        }

        if(description.isBlank()){
            Toast.makeText(activity!!, "Please Enter The Description Of Crime", Toast.LENGTH_SHORT).show()
            return
        }

        if(date.isBlank()){
            Toast.makeText(activity!!, "Please Enter The Date Of Crime", Toast.LENGTH_SHORT).show()
            return
        }

        if(time.isBlank()){
            Toast.makeText(activity!!, "Please Enter The Time Of Crime", Toast.LENGTH_SHORT).show()
            return
        }

        if(location.isBlank()){
            Toast.makeText(activity!!, "Please Enter The Location Of Crime", Toast.LENGTH_SHORT).show()
            return
        }

        if(officer.isBlank()){
            Toast.makeText(activity!!, "Please Enter The Name Of Officer On Duty", Toast.LENGTH_SHORT).show()
            return
        }

        //Adding the data to the database
        val criminal = Criminal(
                viewModel.criminalId.value!!,
                name,
                aadhaar,
                priority,
                age,
                gender,
                description,
                date,
                time,
                location,
                officer,
                photo
        )
        viewModel.saveCriminal(criminal)

        if(viewModel.criminalId.value == 0L) {
            val sharedPref = activity!!.getPreferences(Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString(LAST_ADDED_CRIMINAL_KEY.toString(), name)
                commit()
            }
        }
        activity!!.onBackPressed()
    }

    private fun deleteCriminal() {
        viewModel.deleteCriminal()
        activity!!.onBackPressed()
    }

    private fun clickPhoto() {
        //Creating a file
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                val photoFile: File? = try {
                    createFile(activity!!, Environment.DIRECTORY_PICTURES, "jpg")
                } catch (ex: IOException) {
                    Toast.makeText(activity!!, "Error Occurred while creating file: {ex:message}", Toast.LENGTH_LONG).show()
                    null
                }
                //Getting a Uri for our file
                photoFile?.also {
                    selectedPhotoPath = it.absolutePath
                    val photoUri: Uri = FileProvider.getUriForFile(activity!!, BuildConfig.APPLICATION_ID + ".fileprovider", it)
                    //Passing the file through the intent and letting the camera application to click the photo
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, CAMERA_PHOTO_REQUEST)
                }
            }
        }
    }

    private fun pickPhoto() {
        //Creating an intent
        val pickPhotoIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        //Checking if there is any app for this functionality
        pickPhotoIntent.resolveActivity(activity!!.packageManager)?.also {
            startActivityForResult(pickPhotoIntent, GALLERY_PHOTO_REQUEST)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_PHOTO_REQUEST -> {
                    val uri = Uri.fromFile(File(selectedPhotoPath))
                    criminal_photo.setImageURI(uri)
                    criminal_photo.tag = uri.toString()
                }
                GALLERY_PHOTO_REQUEST -> {
                    data?.data?.also { uri ->
                        //Creating a empty file
                        val photoFile: File? = try {
                            createFile(requireActivity(), Environment.DIRECTORY_PICTURES, "jpg")
                        } catch (ex: IOException) {
                            Toast.makeText(
                                requireActivity(),
                                "Error Occurred while creating file: {ex:message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            null
                        }
                        photoFile?.also {
                            try {
                                //Creating instance of contentResolver to get the uri of the selected image
                                val resolver = requireActivity().contentResolver
                                resolver.openInputStream(uri).use { stream ->
                                    val output = FileOutputStream(photoFile)
                                    //Copying the uri of selected image to empty file
                                    stream!!.copyTo(output)
                                }
                                val fileUri = Uri.fromFile(photoFile)
                                criminal_photo.setImageURI(fileUri)
                                criminal_photo.tag = fileUri.toString()
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }
}