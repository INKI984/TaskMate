package com.taskmate.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.taskmate.app.R
import com.taskmate.app.TaskMateApp
import com.taskmate.app.databinding.FragmentProfileBinding
import com.taskmate.app.ui.auth.LoginActivity
import com.taskmate.app.ui.main.TaskViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModel.Factory((requireActivity().application as TaskMateApp).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        binding.tvEmail.text = when {
            user?.isAnonymous == true -> getString(R.string.anonymous_user)
            !user?.email.isNullOrBlank() -> user?.email
            !user?.displayName.isNullOrBlank() -> user?.displayName
            else -> getString(R.string.unknown_user)
        }
        binding.tvUid.text = getString(R.string.user_id_label, user?.uid ?: "-")

        viewModel.openCount.observe(viewLifecycleOwner) { count ->
            binding.tvOpenCount.text = getString(R.string.open_tasks_count, count)
        }

        binding.btnSignOut.setOnClickListener { signOut() }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
