package com.example.todolist.ui.list.main

import android.graphics.BlendMode
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.todolist.R
import com.example.todolist.databinding.FragmentTaskListsBinding
import com.example.todolist.dialogs.DeleteListsDialogFragment
import com.example.todolist.dialogs.EditListNameDialogFragment
import com.example.todolist.dialogs.NewListDialogFragment
import com.example.todolist.dialogs.NewListDialogFragmentDirections
import com.example.todolist.ui.common.PrefsViewModel
import com.example.todolist.ui.common.helpers.navController
import com.example.todolist.ui.options.SortListBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentTaskList : Fragment() {
    private val viewModel: ListViewModel by viewModels()
    private val prefsViewModel: PrefsViewModel by viewModels()
    private lateinit var binding: FragmentTaskListsBinding
    private lateinit var adapter: TaskListAdapter
    private lateinit var actionModeCallback: ActionMode.Callback
    private var actionMode: ActionMode? = null

    // debug
    private var actionModeStarting = false
    private var actionModeRetryCount = 0
    private val maxRetries = 3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsViewModel.initCols()
        prefsViewModel.initMaxPreviewItems()
        prefsViewModel.initSortListOptions()

        setupActionModeCallback()
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
        }, viewLifecycleOwner)

        adapter = TaskListAdapter({ selectedListId ->
            val selectedList = viewModel.taskListsWithPreview.value[selectedListId]
            val action = FragmentTaskListDirections.actionListsToList()
            if (viewModel.checkedLists.value.isNotEmpty()) {
                viewModel.toggleList(selectedList.id)
            } else {
                viewModel.selectList(selectedList.id)
                navController.navigate(action)
            }
        }, { checkedListId ->
            viewModel.toggleList(viewModel.taskListsWithPreview.value[checkedListId].id)
        }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.taskListsWithPreview.collect { lists ->
                    adapter.submitList(lists)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.checkedLists.collect { checkedLists ->
                if (checkedLists.isNotEmpty()) {
                    startActionModeIfNeeded("Selected: ${checkedLists.size}")
                    if (checkedLists.size > 1) {
                        actionMode?.menu?.findItem(R.id.menu_rename_list)?.isEnabled = false
                        actionMode?.menu?.findItem(R.id.menu_rename_list)?.iconTintBlendMode =
                            BlendMode.DARKEN

                        actionMode?.menu?.findItem(R.id.menu_add_to_home_screen)?.isEnabled =
                            false
                    } else {
                        actionMode?.menu?.findItem(R.id.menu_rename_list)?.isEnabled = true
                        actionMode?.menu?.findItem(R.id.menu_add_to_home_screen)?.isEnabled =
                            true
                    }
                } else {
                    cleanupActionMode()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            prefsViewModel.listColumns.collect { cols ->
                binding.listsAppBar.menu.findItem(R.id.menu_toggle_grid_columns).setIcon(
                    ContextCompat.getDrawable(
                        requireContext(),
                        if (cols == 1) R.drawable.view_comfy_alt_24dp else R.drawable.view_agenda_24dp
                    )
                )
                binding.recListActive.layoutManager = GridLayoutManager(requireContext(), cols)
            }
        }

        setupDialogObserver()
        setupRecyclerView()
        setupActionBar()
        setupFab()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task_lists, container, false)
        binding = FragmentTaskListsBinding.bind(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        view?.post {
            if (viewModel.checkedLists.value.isNotEmpty()  && actionMode == null) {
//                startActionMode()
                startActionModeIfNeeded("Selected: ${viewModel.checkedLists.value.size}")
            }
        }
    }

    //debug
    override fun onDestroyView() {
        actionMode?.finish()
//        actionMode = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.recListActive
        recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddNewList.setOnClickListener {
            showNewListDialog()
        }
    }

    private fun showNewListDialog() {
        val actionDialog = FragmentTaskListDirections.actionListsToAddNewListDialog()
        navController.navigate(actionDialog)
    }

    private fun setupDialogObserver() {
        // new list
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(
            NewListDialogFragment.NEW_LIST_NAME
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null && result != "") {
                viewModel.addEmptyList(result)
                val action = NewListDialogFragmentDirections.actionDialogNewListToTaskItems()
                navController.navigate(action)
            }
        }

        // edit list
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(
            EditListNameDialogFragment.NEW_LIST_NAME
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null && result != "") {
                viewModel.renameList(result)
            }
        }

        // delete checked lists
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean?>(
            DeleteListsDialogFragment.DELETE_LISTS_RESULT
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null && result != false) {
                viewModel.deleteCheckedLists()
            }
        }
    }

    // debug?
    private fun startActionModeIfNeeded(title: String) {
        if (actionMode != null) {
            actionMode?.title = title
            return
        }
        if (actionModeStarting) return

        val checkedCount = viewModel.checkedLists.value.size
        if (checkedCount <= 0) return

        actionModeStarting = true
        view?.post {
            try {
                actionMode = tryStartingActionMode(title)
                if (actionMode == null && actionModeRetryCount < maxRetries) {
                    actionModeRetryCount++
                    view?.postDelayed({ startActionModeIfNeeded(title) }, 100L)
                } else {
                    actionModeRetryCount = 0
                }
            } finally {
                actionModeStarting = false
            }
        }
    }

    private fun tryStartingActionMode(title: String): ActionMode? {
        return try {
            requireActivity().startActionMode(actionModeCallback).also {
                it?.title = title
                Log.d("ActionMode", "Successfully started")
            }
        } catch (e: Exception) {
            Log.w("ActionMode", "Failed to start: ${e.message}")
            null
        }
    }

    private fun cleanupActionMode() {
        if (actionMode != null) {
            actionMode?.finish()
            actionMode = null
        }
    }

    private fun setupActionBar() {
        var temp: String
        binding.listsAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_show_sort_options -> {
                    val sortListBottomSheet = SortListBottomSheet()
                    sortListBottomSheet.show(parentFragmentManager, SortListBottomSheet.TAG)
                    true
                }

                R.id.menu_toggle_grid_columns -> {
                    prefsViewModel.changeListView()
                    true
                }

                R.id.menu_add_empty_list -> {
                    temp = "Add empty list"
                    showNewListDialog()
                    true
                }

                R.id.menu_import_list -> {
                    navController.navigate(FragmentTaskListDirections.actionListsToImport())
                    true
                }

                R.id.menu_settings -> {
                    navController.navigate(FragmentTaskListDirections.actionListsToSettings())
                    true
                }

                else -> false
            }
        }
    }

    private fun setupActionModeCallback() {
        actionModeCallback = object : ActionMode.Callback {
            private var destroyed = false
            private var valid = true

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                destroyed = false
                valid = true
                mode?.menuInflater?.inflate(R.menu.menu_list_contextual, menu)

//                MenuInflater(context).inflate(R.menu.menu_list_contextual, menu)
                Log.d("ActionMode", "onCreateActionMode")

                binding.listsAppBar.isEnabled = false
                binding.listsAppBar.alpha = 0.5f
                binding.listsAppBar.menu.setGroupEnabled(0, false)
                binding.fabAddNewList.isEnabled = false

                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.menu_add_to_home_screen -> {
                        Toast.makeText(context, "Add to home screen", Toast.LENGTH_SHORT).show()
                        mode?.finish()
                        true
                    }

                    R.id.menu_rename_list -> {
                        showEditListNameDialog()
                        mode?.finish()
                        true
                    }

                    R.id.menu_delete_selected_lists -> {
                        showDeleteListsDialog()
                        // Don't have to finish here due to either all checked lists will be deleted (positive) or remain checked (negative)
                        true
                    }

                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                if (!valid) return
                valid = false
                if (destroyed) return
                destroyed = true
                binding.listsAppBar.isEnabled = true
                binding.listsAppBar.alpha = 1f
                binding.listsAppBar.menu.setGroupEnabled(0, true)
                binding.fabAddNewList.isEnabled = true
                viewModel.clearListChecks()
                actionMode = null
            }
        }
    }

    private fun showEditListNameDialog() {
        val listId = viewModel.checkedLists.value.first()
        viewModel.selectList(listId)
        lifecycleScope.launch {
            val listName = viewModel.getListName(listId)
            val action = FragmentTaskListDirections.actionListsToDialogEditListName(listName)
            navController.navigate(action)
        }
    }

    private fun showDeleteListsDialog() {
        val action =
            viewModel.checkedLists.value.first()
                .let {
                    FragmentTaskListDirections.actionListsToDialogDeleteLists(
                        "Selected lists will be permanently deleted.",
                        "Delete selected lists?" // TODO: add plural string
                    )
                }
        navController.navigate(action)
    }
}