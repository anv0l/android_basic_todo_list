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
    @Query("Select * from task_lists order by dateModified desc")
    fun getAllListsSorted(): Flow<List<TaskListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TaskListEntity): Long

    @Query("select listName from task_lists tl where id=:listId")
    fun getListName(listId: Long): Flow<String>

    @Query("update task_lists set listName=:listName where id = :listId")
    suspend fun renameList(listId: Long, listName: String)

    @Delete
    suspend fun deleteList(list: TaskListEntity)

    @Query("select count(*) from task_lists where checked=1")
    fun getSelectedListCount(): Flow<Int>

    @Query("select * from task_lists where checked = 1")
    fun getSelectedLists(): Flow<List<TaskListEntity>>

    @Query(
        "select * from task_items where list_id=:listId "
    )
    fun observeItemsForList(listId: Long): Flow<List<TaskItemEntity>>

    // todo: does updating check status need to update lastModified value as well?
    @Query(
        "update task_items " +
                "   set is_checked=not is_checked, " +
                "       dateModified =:dateModified " +
                " where list_id=:listId and id=:itemId"
    )
    suspend fun toggleItem(listId: Long, itemId: Long, dateModified: Instant = Instant.now())

    @Insert
    suspend fun insertItem(listItem: TaskItemEntity): Long

    @Query("delete from task_items where id=:itemId")
    suspend fun deleteItem(itemId: Long)

    // TODO: ordering and checkboxes
//    @Query("select * from task_items where list_id=:listId order by dateModified desc limit :rowCount")
    @Query(
        "select * from task_items where list_id=:listId order by " +
                "case when is_checked = 0 then 0 else 1 end, dateModified desc " +
//                "case when is_checked = 1 then -dateModified else dateModified end " +
                "limit :rowCount"
    )
    fun getTaskItemsPreview(listId: Long, rowCount: Int): Flow<List<TaskItemEntity>>

    @Query(
        "update task_items" +
                "  set is_checked = not (select max(is_checked) from task_items where list_id=:listId), " +
                "      dateModified=:dateModified " +
                "where list_id = :listId"
    )
    suspend fun toggleAllItems(listId: Long, dateModified: Instant = Instant.now())

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
    suspend fun swapItems(oldListId: Long, newListId: Long)

    @Query("update task_items set itemText=:itemName, dateModified = :dateModified  where id=:itemId")
    suspend fun renameItem(itemId: Long, itemName: String, dateModified: Instant = Instant.now())

    @Query(
        "select count(*) = 0 " +
                "  from task_items " +
                " where list_id = :listId" +
                "   and is_checked = 0"
    )
    fun observeAllItemsChecked(listId: Long): Flow<Boolean>
}