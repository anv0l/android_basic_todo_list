package com.example.todolist.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeleteItemDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val navController = findNavController()
        val title = DeleteListsDialogFragmentArgs.fromBundle(requireArguments()).title
        val confirmation = DeleteListsDialogFragmentArgs.fromBundle(requireArguments()).confirmation

        return requireContext().let {
            MaterialAlertDialogBuilder(it)
                .setTitle(title)
                .setMessage(confirmation)
                .setPositiveButton("Delete") { _, _ ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        DELETE_ITEMS_RESULT,
                        true
                    )
                }
                .setNegativeButton("Cancel") { _, _ ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        DELETE_ITEMS_RESULT,
                        false
                    )
                }
                .create()
        }
    }

    companion object {
        const val DELETE_ITEMS_RESULT = "delete_items_result"
    }
}