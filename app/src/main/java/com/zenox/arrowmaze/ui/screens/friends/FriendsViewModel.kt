package com.zenox.arrowmaze.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenox.arrowmaze.data.remote.firebase.FirestoreService
import com.zenox.arrowmaze.data.repository.AuthRepository
import com.zenox.arrowmaze.domain.model.FriendRequest
import com.zenox.arrowmaze.domain.model.PlayerProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val firestoreService: FirestoreService,
    private val authRepository: AuthRepository
) : ViewModel() {

    data class FriendsUiState(
        val friends: List<PlayerProfile> = emptyList(),
        val pendingRequests: List<FriendRequest> = emptyList(),
        val searchQuery: String = "",
        val searchResults: List<PlayerProfile> = emptyList(),
        val isLoading: Boolean = true,
        val showSearch: Boolean = false
    )

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
        loadPendingRequests()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            try {
                val uid = authRepository.getUserUid() ?: return@launch
                val result = firestoreService.getFriends(uid)
                val docs = result.getOrDefault(emptyList())
                val friends = docs.mapNotNull { doc ->
                    try {
                        PlayerProfile(
                            uid = doc.id,
                            nickname = doc.getString("nickname") ?: "",
                            playerName = doc.getString("playerName") ?: doc.getString("nickname") ?: "Unknown",
                            avatarUrl = doc.getString("avatarUrl") ?: "",
                            country = doc.getString("country") ?: "Global",
                            level = doc.getLong("level")?.toInt() ?: 0,
                            coins = doc.getLong("coins")?.toInt() ?: 0
                        )
                    } catch (_: Exception) { null }
                }
                _uiState.value = _uiState.value.copy(friends = friends, isLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun loadPendingRequests() {
        viewModelScope.launch {
            try {
                val uid = authRepository.getUserUid() ?: return@launch
                // Load pending friend requests where toUid == currentUser
                val docs = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("friendRequests")
                    .whereEqualTo("toUid", uid)
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()
                    .documents
                val requests = docs.map { doc ->
                    FriendRequest(
                        uid = doc.id,
                        playerName = doc.getString("fromName") ?: "Unknown",
                        avatarUrl = doc.getString("avatarUrl") ?: "",
                        status = com.zenox.arrowmaze.domain.model.FriendStatus.PENDING,
                        timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                    )
                }
                _uiState.value = _uiState.value.copy(pendingRequests = requests)
            } catch (_: Exception) {}
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun searchPlayers(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList())
                return@launch
            }
            val result = firestoreService.searchPlayers(query)
            val docs = result.getOrDefault(emptyList())
            val uid = authRepository.getUserUid()
            val profiles = docs.mapNotNull { doc ->
                if (doc.id == uid) return@mapNotNull null
                try {
                    PlayerProfile(
                        uid = doc.id,
                        nickname = doc.getString("nickname") ?: "",
                        playerName = doc.getString("playerName") ?: doc.getString("nickname") ?: "Unknown",
                        avatarUrl = doc.getString("avatarUrl") ?: "",
                        country = doc.getString("country") ?: "Global",
                        level = doc.getLong("level")?.toInt() ?: 0
                    )
                } catch (_: Exception) { null }
            }
            _uiState.value = _uiState.value.copy(searchResults = profiles, showSearch = true)
        }
    }

    fun sendFriendRequest(uid: String) {
        viewModelScope.launch {
            val myUid = authRepository.getUserUid() ?: return@launch
            val myName = _uiState.value.friends.firstOrNull()?.playerName ?: "Player"
            firestoreService.sendFriendRequest(myUid, uid, myName)
        }
    }

    fun acceptFriendRequest(fromUid: String) {
        viewModelScope.launch {
            val myUid = authRepository.getUserUid() ?: return@launch
            firestoreService.acceptFriendRequest(myUid, fromUid)
            loadFriends()
            loadPendingRequests()
        }
    }

    fun removeFriend(friendUid: String) {
        viewModelScope.launch {
            val myUid = authRepository.getUserUid() ?: return@launch
            firestoreService.removeFriend(myUid, friendUid)
            loadFriends()
        }
    }

    fun blockPlayer(uid: String) {
        viewModelScope.launch {
            val myUid = authRepository.getUserUid() ?: return@launch
            firestoreService.blockPlayer(myUid, uid)
            loadFriends()
            loadPendingRequests()
        }
    }

    fun toggleSearch() {
        _uiState.value = _uiState.value.copy(showSearch = !_uiState.value.showSearch, searchQuery = "", searchResults = emptyList())
    }
}