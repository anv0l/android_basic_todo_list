package com.example.todolist.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.todolist.R
import com.example.todolist.databinding.EditDialogBinding
import com.example.todolist.ui.common.helpers.navController
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditItemNameDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val itemName = EditItemNameDialogFragmentArgs.fromBundle(requireArguments()).itemName

        val binding = EditDialogBinding.inflate(layoutInflater)
        binding.txtNewListContainer.hint = "Item name"
        binding.txtNewList.setText(itemName)
        return requireContext().let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Edit item:")
                .setView(binding.root)
                .setPositiveButton("Save") { _, _ ->
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
        const val NEW_ITEM_NAME = "item_name"
    }
}