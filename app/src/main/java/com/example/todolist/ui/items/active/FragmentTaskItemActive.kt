package com.example.todolist.ui.items.active

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todolist.R
import com.example.todolist.data.local.entities.TaskListItem
import com.example.todolist.databinding.FragmentTaskItemActiveBinding
import com.example.todolist.dialogs.DeleteListsDialogFragment
import com.example.todolist.dialogs.EditListNameDialogFragment
import com.example.todolist.dialogs.NewItemDialogFragment
import com.example.todolist.ui.common.helpers.navController
import com.example.todolist.ui.items.edit.EditItemViewModel
import com.example.todolist.ui.list.main.ListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentTaskItemActive : Fragment() {
    private val viewModel: ListViewModel by viewModels()
    private val itemsViewModel: EditItemViewModel by viewModels()
    private lateinit var binding: FragmentTaskItemActiveBinding
    private lateinit var adapter: TaskItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setTitles()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBackToList()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.itemsAppBar.setNavigationOnClickListener {
            navigateBackToList()
        }

        binding.txtAppBar.setOnClickListener {
            showEditListNameDialog()
        }

        super.onViewCreated(view, savedInstanceState)

        adapter = TaskItemAdapter { selectedItemId ->
            val item = viewModel.selectedListItemsGrouped.value[selectedItemId]
            if (item is TaskListItem.Item) {
                itemsViewModel.toggleItemForSelectedList(item.taskItem.id)
            }
        }

        lifecycleScope.launch {
            viewModel.selectedListItemsGrouped.collect { list ->
                adapter.submitList(list)
            }
        }

        lifecycleScope.launch {
            viewModel.selectedListName.collect { listName ->
                binding.txtAppBar.text = listName
            }
        }

        lifecycleScope.launch {
            itemsViewModel.isAllItemsChecked.collect { isChecked ->
                binding.itemsAppBar.menu.findItem(R.id.menu_toggle_all_itms).setIcon(
                    ContextCompat.getDrawable(
                        requireContext(),
                        if (isChecked) R.drawable.remove_done_24dp else R.drawable.done_all_24dp
                    )
                )
            }
        }

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                setTitles()
            }
        })

        binding.fabAddNewItem.setOnClickListener {
            showNewItemDialog()
        }

        val recyclerView = binding.recListActive
        recyclerView.adapter = adapter

        val touchHelper = ItemTouchHelper(TaskItemMoveCallback { _, _ ->
        })
        touchHelper.attachToRecyclerView(recyclerView)

        setupActionBar()
        setupDialogObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task_item_active, container, false)
        binding = FragmentTaskItemActiveBinding.bind(view)
        return view
    }

    private fun navigateBackToList() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(destinationId = R.id.task_lists, inclusive = false)
            .build()

        navController.navigate(R.id.task_lists, null, navOptions)
    }

    private fun setupDialogObservers() {
        // add new item
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(
            NewItemDialogFragment.NEW_ITEM_NAME
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null && result != "") {
                itemsViewModel.addItem(result)
                navController.currentBackStackEntry?.savedStateHandle?.set(NewItemDialogFragment.NEW_ITEM_NAME, "")// remove<String>(NewItemDialogFragment.NEW_ITEM_NAME)
            }
        }

        // edit title
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(
            EditListNameDialogFragment.NEW_LIST_NAME
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null && result != "") {
                viewModel.renameList(result)
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>(EditListNameDialogFragment.NEW_LIST_NAME)
            }
        }

        // delete item
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean?>(
            DeleteListsDialogFragment.DELETE_LISTS_RESULT
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null && result != false) {
                viewModel.deleteSelectedList()
                navigateBackToList()
            }
        }
    }

    private fun showNewItemDialog() {
        val action =
            FragmentTaskItemActiveDirections.actionSelectedListToDialogNewItem(viewModel.taskListsWithPreview.value.find {
                it.id == viewModel.selectedListId.value
            }?.listName ?: "<Unknown list>")
        navController.navigate(action)
    }

    private fun showEditListNameDialog() {

        val action =
            FragmentTaskItemActiveDirections.actionSelectedListToDialogEditListName(viewModel.taskListsWithPreview.value.find {
                it.id == viewModel.selectedListId.value
            }?.listName ?: "<Unknown list>")
        navController.navigate(action)
    }

    private fun setTitles() {
        viewModel.updateHeaderTitles(
            toBeDoneTitle = getString(R.string.list_items_group_to_be_done),
            doneTitle = getString(R.string.list_items_group_done)
        )
    }

    private fun setupActionBar() {
        binding.itemsAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_toggle_all_itms -> {
                    itemsViewModel.toggleAllItemsForSelectedList()
                    true
                }

                R.id.menu_delete_list -> {
                    val action =
                        FragmentTaskItemActiveDirections.actionSelectedListToDialogDeleteLists(
                            "Selected lists will be permanently deleted.",
                            "Delete selected lists?" // TODO: add plural string
                        )
                    navController.navigate(action)
                    true
                }

                R.id.menu_edit_list -> {
                    navController.navigate(FragmentTaskItemActiveDirections.actionItemsToEditItems())
                    true
                }

                else -> false
            }
        }
    }
}