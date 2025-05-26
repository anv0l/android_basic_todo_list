package com.example.todolist.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.todolist.R
import com.example.todolist.databinding.EditDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NewListDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = EditDialogBinding.inflate(layoutInflater)
        binding.txtNewListContainer.hint = "New list name"
        val navController = findNavController()
        return requireContext().let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Enter new list name:")
                .setView(binding.root)
                .setPositiveButton(getString(R.string.new_item_dialog_positive)) { _, _ ->
                    val text = binding.txtNewList.text.toString()
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        NEW_LIST_NAME,
                        text
                    )
                }
                .setNegativeButton(getString(R.string.new_item_dialog_negative)) { _, _ ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        NEW_LIST_NAME,
                        null
                    )
                }
                .create()
        }
    }

    companion object {
        const val NEW_LIST_NAME = "new_list_name"
    }
}