package com.example.mytask.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytask.data.models.Priority
import com.example.mytask.data.models.TodoTask
import com.example.mytask.data.repository.TodoRepository
import com.example.mytask.util.Action
import com.example.mytask.util.RequestState
import com.example.mytask.util.SearchAppBarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    val action: MutableState<Action> = mutableStateOf(Action.NO_ACTION)

    val id: MutableState<Int> = mutableStateOf(0)
    val title:MutableState<String> = mutableStateOf("")
    val description: MutableState<String> = mutableStateOf("")
    val priority: MutableState<Priority> = mutableStateOf(Priority.LOW)

    val searchAppBarState: MutableState<SearchAppBarState> =
        mutableStateOf(SearchAppBarState.CLOSED)
    val searchTextState: MutableState<String> = mutableStateOf("")

    private val _allTasks =
        MutableStateFlow<RequestState<List<TodoTask>>>(RequestState.Idle)
    val allTasks:StateFlow<RequestState<List<TodoTask>>> = _allTasks

    fun getAllTasks(){
        _allTasks.value = RequestState.Loading
        try {
            viewModelScope.launch {
                repository.getAllTasks.collect {
                    _allTasks.value = RequestState.Success(it)
                }
            }
        }catch (e: Exception){
            _allTasks.value = RequestState.Error(e)
        }
    }

    private val _searchTasks =
        MutableStateFlow<RequestState<List<TodoTask>>>(RequestState.Idle)
    val searchedTasks:StateFlow<RequestState<List<TodoTask>>> = _searchTasks

    fun searchDatabase(searchQuery: String){
        _searchTasks.value = RequestState.Loading
        try {
            viewModelScope.launch {
                repository.searchDatabase(searchQuery = searchQuery).collect { searchedTasks ->
                    _allTasks.value = RequestState.Success(searchedTasks)
                }
            }
        } catch (e: Exception){
            _searchTasks.value = RequestState.Error(e)
        }
        searchAppBarState.value = SearchAppBarState.TRIGGERED
    }

    private val _selectedTask: MutableStateFlow<TodoTask?> = MutableStateFlow(null)

    val selectedTask: StateFlow<TodoTask?> = _selectedTask

    fun getSelectedTask(taskId: Int){
        viewModelScope.launch {
            repository.getSelectedTask(taskId = taskId).collect{ task ->
                _selectedTask.value = task
            }
        }
    }

    private fun addTask(){
        viewModelScope.launch(Dispatchers.IO) {
            val todoTask = TodoTask(
                title = title.value,
                description = description.value,
                priority = priority.value
            )
            repository.addTask(todoTask = todoTask)
        }
    }

    private fun updateTask(){
        viewModelScope.launch(Dispatchers.IO) {
            val todoTask = TodoTask(
                id = id.value,
                title = title.value,
                description = description.value,
                priority = priority.value
            )
            repository.updateTask(todoTask = todoTask)
        }
    }

    private fun deleteTask(){
        viewModelScope.launch(Dispatchers.IO) {
            val todoTask = TodoTask(
                id = id.value,
                title = title.value,
                description = description.value,
                priority = priority.value
            )
            repository.deleteTask(todoTask = todoTask)

        }
    }

    fun handleDatabaseActions(action: Action){
        when(action){
            Action.ADD -> {
                addTask()
            }
            Action.UPDATE -> {
                updateTask()
            }
            Action.DELETE -> {
                deleteTask()
            }
            Action.DELETE_ALL -> {

            }
            Action.UNDO -> {
                addTask()
            }
            else -> {

            }
        }
        this.action.value = Action.NO_ACTION
    }

    fun updateTaskField(selectedTask: TodoTask?){
        if(selectedTask != null){
            id.value = selectedTask.id
            title.value = selectedTask.title
            description.value = selectedTask.description
            priority.value = selectedTask.priority
        }else{
            id.value = 0
            title.value = ""
            description.value = ""
            priority.value = Priority.LOW
        }
    }

    fun updateTitle(newTitle: String){
        if(newTitle.length < 20){
            title.value = newTitle
        }
    }

    fun validateFields(): Boolean{
        return title.value.isNotEmpty() && description.value.isNotEmpty()
    }

}