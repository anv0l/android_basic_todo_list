package com.example.todolist.ui.list.importer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todolist.databinding.FragmentImportTaskListBinding
import com.example.todolist.ui.common.helpers.navController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentTaskListImporter : Fragment() {
    private lateinit var binding: FragmentImportTaskListBinding
    private val importerViewModel: TaskListImporterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.importAppBar.setNavigationOnClickListener {
            navController.popBackStack()
        }

        binding.btnImport.setOnClickListener {
            val listName = binding.txtNewList.text.toString()
            val listItems = binding.txtImport.text.toString().split("\n").filter { it != "" }

            importerViewModel.importList(listName, listItems)

            navController.navigate(FragmentTaskListImporterDirections.actionImportToItems())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImportTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }
}