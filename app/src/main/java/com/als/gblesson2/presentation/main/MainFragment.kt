package com.als.gblesson2.presentation.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.als.gblesson2.*
import com.als.gblesson2.data.states.AppState
import com.als.gblesson2.data.dto.Weather
import com.als.gblesson2.databinding.FragmentMainBinding
import com.als.gblesson2.data.SharedPrefsConstants
import com.als.gblesson2.presentation.details.DetailsFragment

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private val adapter = MainFragmentAdapter(object : OnItemViewClickListener {
        override fun onItemClick(weather: Weather) {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, DetailsFragment().also { fragment ->
                    fragment.arguments =
                        Bundle().also { bundle ->
                            bundle.putParcelable(
                                DetailsFragment.BUNDLE_EXTRA,
                                weather
                            )
                        }
                })?.addToBackStack("")?.commit()
        }

    })
    private var isDataSetRus: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainFragmentRecyclerView.adapter = adapter
        binding.mainFragmentFAB.setOnClickListener { changeCitiesDataSet() }
        viewModel.liveData.observe(viewLifecycleOwner, Observer { renderData(it) })
        loadCities()
    }

    override fun onDestroy() {
        adapter.removeListener()
        super.onDestroy()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun loadCities(){
        activity?.let {
            isDataSetRus = it.getPreferences(Context.MODE_PRIVATE).getBoolean(SharedPrefsConstants.IS_RUS_KEY, true)
            if (isDataSetRus) {
                viewModel.getWeatherFromLocalSourceRus()
            } else {
                viewModel.getWeatherFromLocalSourceWorld()
            }
        }
    }

    private fun changeCitiesDataSet() {
        isDataSetRus = !isDataSetRus
        val sharedPrefs = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPrefs?.edit()
        editor?.let {
            it.putBoolean(SharedPrefsConstants.IS_RUS_KEY, isDataSetRus)
            it.apply()
        }
        loadCities()
    }

    private fun renderData(appState: AppState) {
        when (appState) {
            is AppState.Success -> {
                binding.mainFragmentLoadingLayout.hide()
                // binding.mainFragmentLoadingLayout.hideIf {appState.weatherData.isNotEmpty() }
                adapter.setWeather(appState.weatherData)
            }
            is AppState.Loading -> {
                binding.mainFragmentLoadingLayout.show()
            }
            is AppState.Error -> {
                binding.mainFragmentLoadingLayout.hide()
                binding.mainFragmentFAB.showSnackBar(
                    text = getString(R.string.error),
                    actionText = getString(R.string.reload),
                    action = { viewModel.getWeatherFromLocalSourceRus() }
                )
            }
            else -> {
                binding.mainFragmentLoadingLayout.hide()
                binding.mainFragmentFAB.showSnackBar(
                    text = getString(R.string.error),
                    actionText = getString(R.string.reload),
                    action = { viewModel.getWeatherFromLocalSourceRus() }
                )
            }
        }
    }

    interface OnItemViewClickListener {
        fun onItemClick(weather: Weather)
    }

    companion object {
        fun newInstance() =
            MainFragment()
    }
}

