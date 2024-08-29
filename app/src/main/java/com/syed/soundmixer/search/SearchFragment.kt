package com.syed.soundmixer.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.syed.soundmixer.databinding.FragmentSearchBinding
import com.syed.soundmixer.sound.SoundPlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding

    @Inject
    lateinit var soundPlayer: SoundPlayer

    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        val searchAdapter = SearchAdapter(
            soundPlayer = soundPlayer,
            onDownloadClicked = { sound ->
                binding.progressBar.visibility = View.VISIBLE
                viewModel.downloadSound(sound.id.toString(), onSuccess = {
                    Toast.makeText(requireContext(), "Downloaded successfully!", Toast.LENGTH_SHORT)
                        .show()

                    binding.progressBar.visibility = View.GONE

                }, onError = {
                    Toast.makeText(requireContext(), "Download Failed!", Toast.LENGTH_SHORT)
                        .show()
                    binding.progressBar.visibility = View.GONE
                })
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.itemCount - 1) {
                    viewModel.loadNextPage()
                }
            }
        })
        observeViewModel(searchAdapter)

        binding.searchButton.setOnClickListener {
            val query = binding.searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchSounds(query)
            }
        }
        return binding.root
    }

    private fun observeViewModel(adapter: SearchAdapter) {
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            adapter.submitList(results)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
