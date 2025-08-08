package org.tensorflow.lite.examples.objectdetection.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.objectdetection.DictionaryManager
import org.tensorflow.lite.examples.objectdetection.DictionaryWord
import org.tensorflow.lite.examples.objectdetection.databinding.ItemDictionaryBinding

class DictionaryAdapter(
    private val words: List<DictionaryWord>,
    private val onItemClicked: (DictionaryWord) -> Unit
) : RecyclerView.Adapter<DictionaryAdapter.WordViewHolder>() {

    inner class WordViewHolder(private val binding: ItemDictionaryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(word: DictionaryWord) {
            binding.textViewWord.text = word.palavra.uppercase()
            val bitmap = DictionaryManager.loadBitmap(itemView.context, word.imageFileName)
            binding.imageViewWord.setImageBitmap(bitmap)

            itemView.setOnClickListener {
                onItemClicked(word)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemDictionaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(words[position])
    }

    override fun getItemCount(): Int = words.size
}