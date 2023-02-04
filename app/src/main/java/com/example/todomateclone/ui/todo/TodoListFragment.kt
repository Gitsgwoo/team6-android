package com.example.todomateclone.ui.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todomateclone.databinding.FragmentTodoListBinding
import com.example.todomateclone.network.dto.TaskDTO
import com.example.todomateclone.ui.TodoListAdapter
import com.example.todomateclone.viewmodel.TodoViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodoListFragment : Fragment(), OnDismissListener, OnDismissListenerAdder {
    private lateinit var binding: FragmentTodoListBinding

    private val viewModel: TodoViewModel by viewModel()
    private val args: TodoListFragmentArgs by navArgs()
    lateinit var adapter: TodoListAdapter

    override fun onDismiss() {
        refreshTask()
    }

    override fun onDismissFix(task: TaskDTO) {
        val bottomSheetDialog = TodoAdderFragment(binding.dateTextView.text.toString(), task)
        bottomSheetDialog.setOnDismissListenerAdder(this)
        bottomSheetDialog.show(requireFragmentManager(), "BottomSheetDialog")
        refreshTask()
        binding.dateTextView.visibility = VISIBLE
        refreshTask()
    }

    override fun onDismissDelay(task: TaskDTO) {
        lifecycleScope.launch {viewModel.delayTodo(task.id)}
        refreshTask()
        binding.dateTextView.visibility = VISIBLE

    }

    override fun onDismissDelete(task: TaskDTO) {
        lifecycleScope.launch {viewModel.deleteTodo(task.id)}
        refreshTask()
        binding.dateTextView.visibility = VISIBLE

    }

    override fun onDismissToday(task: TaskDTO) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val todaysdate = today.format(formatter)
        lifecycleScope.launch {viewModel.changeTodo(task.name, todaysdate, task.start_time, task.end_time, task.id)}
        refreshTask()
        binding.dateTextView.visibility = VISIBLE

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TodoListAdapter(
            { task -> checkFinal(task) },
            { task -> callTodoFixer(task)},
            args.searchedId > 0
        )

        binding.recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)


        val todaysdate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        lateinit var yearstr: String
        lateinit var monthstr: String
        lateinit var daystr: String

        binding.apply {
            if(args.searchedId>0) {
                floatingActionButton.visibility = INVISIBLE
            }

            dateTextView.text = todaysdate
            if(args.searchedId>0) refreshSearchedTask() //search한 다른사람의 task
            else refreshTask() //나의 task

            calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
                dateTextView.visibility = View.VISIBLE
                if(month<9) monthstr="0"+(month+1).toString()
                else monthstr=(month+1).toString()
                if(dayOfMonth<10) daystr="0"+dayOfMonth.toString()
                else daystr=dayOfMonth.toString()
                yearstr=year.toString()

                dateTextView.text = String.format("%s-%s-%s", yearstr, monthstr, daystr)

                if(args.searchedId>0) refreshSearchedTask() //search한 다른사람의 task
                else refreshTask() //나의 task
            }
        }

        binding.floatingActionButton.setOnClickListener {
            val bottomSheetDialog = TodoAdderFragment(binding.dateTextView.text.toString(), null)
            bottomSheetDialog.setOnDismissListenerAdder(this)
            bottomSheetDialog.show(requireFragmentManager(), "BottomSheetDialog")
        }

        binding.refreshButton.setOnClickListener {
            if(args.searchedId>0) refreshSearchedTask() //search한 다른사람의 task
            else refreshTask() //나의 task
        }

        binding.goDiary.setOnClickListener{
            val action = TodoListFragmentDirections.actionTodoListFragmentToDiaryCalendarFragment(args.searchedId)
            this.findNavController().navigate(action)
        }


    }

    fun refreshTask() {
        viewLifecycleOwner.lifecycleScope.launch {
            val pager = viewModel.createPager(binding.dateTextView.text.toString())
            pager.collect { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    fun refreshSearchedTask() {
        viewLifecycleOwner.lifecycleScope.launch {
            val pager = viewModel.createSearchedPager(binding.dateTextView.text.toString(), args.searchedId)
            pager.collect { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    fun checkFinal(task: TaskDTO) {
        lifecycleScope.launch {viewModel.checkTodo(task.id)}
        refreshTask()
    }
    //
    fun callTodoFixer(task: TaskDTO) {
        val bottomSheetDialog = TodoFixerFragment(task)
        bottomSheetDialog.setOnDismissListener(this)
        bottomSheetDialog.show(requireFragmentManager(), "BottomSheetDialog")
    }

}
