package com.example.todolist.ui.items.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todolist.R
import com.example.todolist.databinding.FragmentTaskItemEditBinding
import com.example.todolist.dialogs.DeleteItemDialogFragment
import com.example.todolist.dialogs.EditItemNameDialogFragment
import com.example.todolist.ui.common.helpers.navController
import com.example.todolist.ui.items.active.TaskItemMoveCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentEditTaskItem : Fragment() {
    private val editItemViewModel: EditItemViewModel by viewModels()
    private lateinit var binding: FragmentTaskItemEditBinding
    private lateinit var adapter: EditTaskItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.itemsAppBarContainer.setNavigationOnClickListener {
            navController.popBackStack()
        }

        adapter = EditTaskItemAdapter({ selectedItem ->
            val selectedItemId = editItemViewModel.itemsForSelectedList.value[selectedItem].id
            editItemViewModel.toggleItem(selectedItemId)
            adapter.updateCheckedItems(editItemViewModel.checkedItems.value)
        },
            { selectedItem ->
                val selectedItemId = editItemViewModel.itemsForSelectedList.value[selectedItem].id
                editItemViewModel.selectItem(selectedItemId)
                val selectedItemName =
                    editItemViewModel.itemsForSelectedList.value[selectedItem].itemText
                val action =
                    FragmentEditTaskItemDirections.actionEditItemToDialogEdit(selectedItemName)
                navController.navigate(action)
            })

        lifecycleScope.launch {
            editItemViewModel.itemsForSelectedList.collect { list ->
                adapter.submitList(list)
            }
        }

        lifecycleScope.launch {
            editItemViewModel.checkedItems.collect { value ->
                binding.itemsAppBarContainer.menu.findItem(R.id.menu_delete_items)?.isEnabled =
                    value.isNotEmpty()
            }
        }

        val recyclerView = binding.recListActive
        recyclerView.adapter = adapter

        val touchHelper = ItemTouchHelper(TaskItemMoveCallback { _, _ ->
        })
        touchHelper.attachToRecyclerView(recyclerView)

        setupDialogListeners()
        setupActionBar()
    }

    private fun setupActionBar() {
        binding.itemsAppBarContainer.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.menu_delete_items -> {
                    showDeleteItemDialog()
                    true
                }

                else -> false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task_item_edit, container, false)
        binding = FragmentTaskItemEditBinding.bind(view)
        return view
    }

    private fun setupDialogListeners() {
        // Edit
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(
            EditItemNameDialogFragment.NEW_ITEM_NAME
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null && result != "") {
                editItemViewModel.renameItem(editItemViewModel.selectedItemId.value, result)
            }
        }


        // Delete
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean?>(
            DeleteItemDialogFragment.DELETE_ITEMS_RESULT
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null && result != false) {
                editItemViewModel.deleteCheckedItems()
            }
        }
    }

    private fun showDeleteItemDialog() {
        navController.navigate(
            FragmentEditTaskItemDirections.actionEditItemsToDeleteItems(
                "Selected items will be permanently deleted.",
                "Delete selected items?" // TODO: add plural string
            )
        )
    }
}