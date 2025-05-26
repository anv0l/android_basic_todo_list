package com.example.todolist.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.todolist.R
import com.example.todolist.databinding.EditDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NewItemDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listName = NewItemDialogFragmentArgs.fromBundle(requireArguments()).listName

        val binding = EditDialogBinding.inflate(layoutInflater)
        binding.txtNewListContainer.hint = getString(R.string.new_item_name_hint)
        binding.txtNewList.setText(getString(R.string.new_item_default_text))
        val navController = findNavController()
        return requireContext().let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.new_item_dialog_title, listName))
                .setView(binding.root)
                .setPositiveButton(getString(R.string.new_item_dialog_positive)) { _, _ ->
                    val text = binding.txtNewList.text.toString()
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        NEW_ITEM_NAME,
                        text
                    )
                }
                .setNegativeButton(getString(R.string.new_item_dialog_negative)) { _, _ ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        NEW_ITEM_NAME,
                        null
                    )
                }
                .create()
        }
    }

    companion object {
        const val NEW_ITEM_NAME = "new_list_name"
    }
}