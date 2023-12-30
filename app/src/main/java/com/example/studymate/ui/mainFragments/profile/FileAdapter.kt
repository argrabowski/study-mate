package com.example.studymate.ui.mainFragments.profile

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studymate.R
import com.example.studymate.data.model.ApplicationState
import com.example.studymate.data.model.File
import com.example.studymate.databinding.FileRowBinding
import com.example.studymate.ui.mainFragments.home.Constants.Companion.WRITE_EXTERNAL_PERMISSION_CODE
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.FileOutputStream



class FileAdapter(
    private var values: List<File>,
    private val activity: Activity,
    private val context: Context
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(itemView: View?, file: File)
    }

    fun filterList(filterlist: List<File>) {
        // below line is to add our filtered
        // list in our course array list.
        values = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }

    private lateinit var listener: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FileRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.itemName.text = item.name
        holder.itemDescription.text = item.description
        val storageReference = Firebase.storage.reference.child(item.filePath)
        Log.d("ADAPTER", item.filePath)
        holder.btnDownload.setOnClickListener {
            Log.d("ADAPTER", "trying to download")
            saveToGallery(item, holder.itemImage)
        }

        holder.btnShare.setOnClickListener {
            Log.d("ADAPTER", "copying to clipboard")
            setClipboard(context, item.downloadURL)
        }

        Glide.with(context)
            .load(storageReference)
//            .override(395, 200)
            .placeholder(R.mipmap.placeholder)
            .into(holder.itemImage)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FileRowBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val itemName: TextView = binding.cardTitle
        val itemDescription: TextView = binding.cardSubTitle
        val itemImage: ImageView = binding.cardImage
        val btnDownload: ImageButton = binding.fileDownloadBtn
        val btnShare: ImageButton = binding.fileShareBtn

        init {
            // Attach a click listener to the entire row view
            itemView.setOnClickListener(this)
        }

        override fun toString(): String {
            return super.toString() + " '" + itemName.text + "'"
        }

        @Override
        override fun onClick(p0: View?) {
            val position = absoluteAdapterPosition // gets item position
            //Log.d("ASD", "CLICK")
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                val file: File = values[position]
                listener.onItemClick(itemView, file)
            }
        }


    }

    private fun setClipboard(context: Context, text: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
            clipboard.text = text
        } else {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
        }
    }

    private fun hasWriteStoragePermission() = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED


    private fun saveToGallery(file: File, imageView: ImageView){

        if(!hasWriteStoragePermission() && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Log.d("TEST", "Require Write External Permission")
            val perms = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(activity, perms.toTypedArray(), WRITE_EXTERNAL_PERMISSION_CODE)
            return
        }

        val bitmapDraw:BitmapDrawable = imageView.drawable as (BitmapDrawable)
        val bitmap: Bitmap = bitmapDraw.bitmap

        val fileName = file.name + "-" + System.currentTimeMillis() + ".jpeg"

        try {
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, fileName , file.description);
            Toast.makeText(context, "File Downloaded Successfully", Toast.LENGTH_SHORT).show()
        }
        catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Unable to Download File", Toast.LENGTH_SHORT).show()
        }
    }

}