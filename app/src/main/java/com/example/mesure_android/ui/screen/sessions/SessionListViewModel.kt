package com.example.mesure_android.ui.screen.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mesure_android.data.local.database.entity.SessionEntity
import com.example.mesure_android.data.local.database.entity.SiteEntity
import com.example.mesure_android.data.repository.PointRepository
import com.example.mesure_android.data.repository.SiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionWithSite(
    val session: SessionEntity,
    val site: SiteEntity,
    val pointCount: Int
)

data class SessionListUiState(
    val sessions: List<SessionWithSite> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SessionListViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val pointRepository: PointRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionListUiState())
    val uiState: StateFlow<SessionListUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            siteRepository.getAllSessions().collect { sessions ->
                val sessionsWithSites = sessions.mapNotNull { session ->
                    val site = siteRepository.getSiteById(session.siteId)
                    if (site != null) {
                        val pointCount = pointRepository.getPointCountForSession(session.id)
                        SessionWithSite(session, site, pointCount)
                    } else null
                }
                _uiState.value = SessionListUiState(
                    sessions = sessionsWithSites,
                    isLoading = false
                )
            }
        }
    }
}
