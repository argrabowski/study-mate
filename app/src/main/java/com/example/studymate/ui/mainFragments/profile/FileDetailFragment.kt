package com.example.studymate.ui.mainFragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.studymate.R
import com.example.studymate.data.model.File
import com.example.studymate.databinding.FragmentFileDetailBinding

class FileDetailFragment : Fragment() {

    private lateinit var file: File
    private lateinit var binding: FragmentFileDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFileDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fileDetailToolbar.setNavigationOnClickListener{
            navigateBack()
        }

        file = requireArguments().getSerializable("file") as File

        binding.filePathTextView.text = file.filePath
        binding.nameTextView.text = file.name
        binding.descriptionTextView.text = file.description
        binding.translateLanguageTextView.text = file.tanslateLanguage
        binding.creatorTextView.text = file.creator
        binding.transcriptTextView.text = file.transscript
        binding.createdDateTextView.text = file.cratedDate.toString()
        binding.downloadUrlTextView.text = file.downloadURL
        binding.translatedTextView.text = file.translatedText
    }

    private fun navigateBack() {
        if (parentFragmentManager.backStackEntryCount > 1)
            parentFragmentManager.popBackStack()
        else {
            val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
            ft.replace(R.id.main_fragment, ProfileFragment())
            ft.commit()
        }
    }
}