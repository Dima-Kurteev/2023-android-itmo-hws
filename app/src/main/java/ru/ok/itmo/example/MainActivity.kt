package ru.ok.itmo.example

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

import ru.ok.itmo.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!
    private val handler: Handler by lazy { Handler(mainLooper) }
    private var thread: Thread? = null
    @Volatile
    private var progress: Int = 100
    private var timeStep: Long = 100L

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(PROGRESS_VALUE, progress)
        outState.putLong(TIME_STEP_VALUE, timeStep)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        restoreArgs(savedInstanceState)
        initListeners()

        if (progress < 100){
            thread = getProgressThread().also { it.start() }
        }
    }

    private fun restoreArgs(savedInstanceState: Bundle?){
        savedInstanceState?.getInt(PROGRESS_VALUE)?.let {
            progress = it
        }
        savedInstanceState?.getLong(TIME_STEP_VALUE)?.let {
            timeStep = it
        }
    }

    private fun initListeners(){
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            timeStep = when(checkedId){
                binding.radioButton50.id -> 50L
                binding.radioButton100.id -> 100L
                binding.radioButton300.id -> 300L
                binding.radioButton500.id -> 500L
                else -> throw RuntimeException("Unknown id")
            }
        }
        binding.restart.setOnClickListener {
            thread?.interrupt()
            progress = 0
            thread = getProgressThread().also { it.start() }
        }
    }

    private fun getProgressThread(): Thread = Thread {
        while (progress < 100){
            if (Thread.currentThread().isInterrupted) return@Thread
            handler.post {
                binding.progressBar.progress = progress + 1
            }
            try {
                Thread.sleep(timeStep)
            }catch (e: InterruptedException){
                return@Thread
            }
            progress++
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        thread?.interrupt()
        _binding = null
    }

    companion object {
        private const val TIME_STEP_VALUE = "time_step"
        private const val PROGRESS_VALUE = "progress"
    }
}