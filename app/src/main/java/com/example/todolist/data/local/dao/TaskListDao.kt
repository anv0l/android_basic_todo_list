package com.example.todolist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.todolist.data.local.entities.TaskItemEntity
import com.example.todolist.data.local.entities.TaskListEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TaskListDao {
    // todo: do we need to worry about paging yet?
    @Query("Select * from task_lists")
    fun getAllLists(): Flow<List<TaskListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TaskListEntity)

    @Query("select listName from task_lists tl where id=:listId")
    fun getListName(listId: String): Flow<String>

    @Query("update task_lists set listName=:listName where id = :listId")
    suspend fun renameList(listId: String, listName: String)

    @Delete
    suspend fun deleteList(list: TaskListEntity)

    @Query(
        "select * from task_items where list_id=:listId "
    )
    fun observeItemsForList(listId: String): Flow<List<TaskItemEntity>>


    // todo: does updating check status need to update lastModified value as well?
    @Query(
        "update task_items " +
                "   set is_checked=not is_checked, " +
                "       dateModified =:dateModified " +
                " where list_id=:listId and id=:itemId"
    )
    suspend fun toggleItem(listId: String, itemId: String, dateModified: Instant = Instant.now())

    @Query(
        "update task_items" +
                "   set is_checked = not is_checked, " +
                "       dateModified=:dateModified" +
                " where id=:itemId"
    )
    suspend fun toggleItem(itemId: String, dateModified: Instant = Instant.now())

    @Insert
    suspend fun insertItem(listItem: TaskItemEntity)

    @Query("delete from task_items where id=:itemId")
    suspend fun deleteItem(itemId: String)

    // TODO: ordering and checkboxes
//    @Query("select * from task_items where list_id=:listId order by dateModified desc limit :rowCount")
    @Query(
        "select * from task_items where list_id=:listId order by " +
                "case when is_checked = 0 then 0 else 1 end, dateModified desc " +
//                "case when is_checked = 1 then -dateModified else dateModified end " +
                "limit :rowCount"
    )
    fun getTaskItemsPreview(listId: String, rowCount: Int): Flow<List<TaskItemEntity>>

    @Query(
        "update task_items" +
                "  set is_checked = not (select max(is_checked) from task_items where list_id=:listId), " +
                "      dateModified=:dateModified " +
                "where list_id = :listId"
    )
    suspend fun toggleAllItems(listId: String, dateModified: Instant = Instant.now())

    /*
    * Swaps items in the list; we use dateModified as ordering value
    * */
    @Query(
        "with check_values as (" +
                "select " +
                "(select is_checked from task_items where id=:oldListId) as old_check," +
                "(select is_checked from task_items where id=:newListId) as new_check" +
                ")" +
                "update task_items " +
                "   set dateModified = case " +
                "  when id=:oldListId and (select old_check = new_check from check_values) then (select dateModified from task_items where id=:newListId) " +
                "  when id=:newListId and (select old_check = new_check from check_values) then (select dateModified from task_items where id=:oldListId) " +
                "  else dateModified" +
                "   end" +
                " where id in(:oldListId, :newListId)"
    )
    suspend fun swapItems(oldListId: String, newListId: String)

    @Query("update task_items set itemText=:itemName, dateModified = :dateModified  where id=:itemId")
    suspend fun renameItem(itemId: String, itemName: String, dateModified: Instant = Instant.now())

    @Query(
        "select count(*) = 0 " +
                "  from task_items " +
                " where list_id = :listId" +
                "   and is_checked = 0"
    )
    fun observeAllItemsChecked(listId: String): Flow<Boolean>

    /**
     * Start: Widget
     */
    // this can be different from observeItemsForList
    @Query("select * from task_items where list_id = :listId")
    fun getListItemsForWidget(listId: String): Flow<List<TaskItemEntity>>

    @Query("select * from task_items where id = :itemId")
    fun getItemForWidget(itemId: String): Flow<TaskItemEntity>
    /**
     * End: Widget
     */

    /**
    * Start: Sync
    */
    @Query("select * from task_lists l" +
           " where exists (select 1 from task_items i where i.list_id = l.id and i.dateModified > :lastSync)")
    fun getUnsyncedLists(lastSync: Instant = Instant.MIN): Flow<List<TaskListEntity>>

    @Query("select * from task_items where list_id=:listId and dateModified > :lastSync")
    fun getUnsyncedItems(listId: String, lastSync: Instant = Instant.MIN): Flow<List<TaskItemEntity>>
    /**
     * End: Sync
     */
}