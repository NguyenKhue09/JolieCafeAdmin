package com.nt118.joliecafeadmin.firebase.firebasefirestore

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.nt118.joliecafeadmin.models.UploadFileToFirebaseResult
import com.nt118.joliecafeadmin.ui.activities.add_product.AddNewProductActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class FirebaseStorage {

    private val mFireStorage = FirebaseStorage.getInstance()
    val uploadImageResult = MutableStateFlow<UploadFileToFirebaseResult<String>>(UploadFileToFirebaseResult.Idle())

    fun uploadFile(file: Uri, fileName: String, root: String) {
        val ref = mFireStorage.reference
            .child("$root/$fileName")
        val uploadTask = ref.putFile(file)

        uploadImageResult.value = UploadFileToFirebaseResult.Loading()

        uploadTask.addOnPausedListener {
            println("Upload image paused")
        }.addOnFailureListener {
            uploadImageResult.value = UploadFileToFirebaseResult.Error(data = null, errorMessage = "Upload image failed")
        }.addOnSuccessListener { _ ->
            ref.downloadUrl.addOnSuccessListener {
                uploadImageResult.value = UploadFileToFirebaseResult.Success(downloadUri = it.toString())
            }.addOnFailureListener {
                uploadImageResult.value = UploadFileToFirebaseResult.Error(data = null, errorMessage = "Get image download url failed")
            }
        }
    }

    fun chooseFile(ActivityResultLauncher: ActivityResultLauncher<Intent>) {
        var getFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        getFileIntent = Intent.createChooser(getFileIntent, "Select picture")
        ActivityResultLauncher.launch(getFileIntent)
    }
}