package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.BuildArtifactEntity
import com.example.data.model.BuildLogMessage
import com.example.data.model.DeviceOrientation
import com.example.data.model.LogLevel
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileNode
import com.example.data.model.RuntimeLogcatEntry
import com.example.data.model.SimulatorRuntimeState
import com.example.engine.ApkCompilerEngine
import com.example.engine.ApkInstallerExporter
import com.example.engine.BuildProgressEvent
import com.example.engine.SampleProjectType
import com.example.engine.ZipProjectManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NavigationTab(val label: String) {
    PROJECTS("Projects"),
    COMPILER("Compiler"),
    SIMULATOR("Simulator"),
    APKS("APKs & Downloads")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val zipManager = ZipProjectManager(application)
    val compilerEngine = ApkCompilerEngine(application, database)
    val exporter = ApkInstallerExporter(application)

    val projects: StateFlow<List<ProjectEntity>> = database.projectDao().getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artifacts: StateFlow<List<BuildArtifactEntity>> = database.buildArtifactDao().getAllArtifacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(NavigationTab.PROJECTS)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
    val selectedProject: StateFlow<ProjectEntity?> = _selectedProject.asStateFlow()

    private val _selectedArtifact = MutableStateFlow<BuildArtifactEntity?>(null)
    val selectedArtifact: StateFlow<BuildArtifactEntity?> = _selectedArtifact.asStateFlow()

    // Compiler State
    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    private val _compileProgress = MutableStateFlow(0f)
    val compileProgress: StateFlow<Float> = _compileProgress.asStateFlow()

    private val _compileCurrentTask = MutableStateFlow("")
    val compileCurrentTask: StateFlow<String> = _compileCurrentTask.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<BuildLogMessage>>(emptyList())
    val terminalLogs: StateFlow<List<BuildLogMessage>> = _terminalLogs.asStateFlow()

    // File Tree & Code Editor State
    private val _projectFileTree = MutableStateFlow<ProjectFileNode?>(null)
    val projectFileTree: StateFlow<ProjectFileNode?> = _projectFileTree.asStateFlow()

    private val _selectedFilePath = MutableStateFlow<String?>(null)
    val selectedFilePath: StateFlow<String?> = _selectedFilePath.asStateFlow()

    private val _selectedFileContent = MutableStateFlow<String?>(null)
    val selectedFileContent: StateFlow<String?> = _selectedFileContent.asStateFlow()

    // Simulator State
    private val _simulatorState = MutableStateFlow(SimulatorRuntimeState())
    val simulatorState: StateFlow<SimulatorRuntimeState> = _simulatorState.asStateFlow()

    private val _simulatorLogs = MutableStateFlow<List<RuntimeLogcatEntry>>(emptyList())
    val simulatorLogs: StateFlow<List<RuntimeLogcatEntry>> = _simulatorLogs.asStateFlow()

    private val _activeSimulatedProject = MutableStateFlow<ProjectEntity?>(null)
    val activeSimulatedProject: StateFlow<ProjectEntity?> = _activeSimulatedProject.asStateFlow()

    // User Feedback Toast/Snackbar
    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val toastEvents = _toastEvents.asSharedFlow()

    init {
        // Collect compiler engine events
        viewModelScope.launch {
            compilerEngine.events.collect { event ->
                when (event) {
                    is BuildProgressEvent.Log -> {
                        _terminalLogs.value = _terminalLogs.value + event.log
                    }
                    is BuildProgressEvent.Progress -> {
                        _compileProgress.value = event.percentage
                        _compileCurrentTask.value = event.currentTask
                    }
                    is BuildProgressEvent.Finished -> {
                        _isCompiling.value = false
                        _selectedArtifact.value = event.artifact
                        _toastEvents.tryEmit("Build Succeeded! APK ready: ${event.artifact.projectName}")
                        // Add simulator entry
                        addSimulatorLog("I", "PackageManager", "Package ${event.artifact.packageName} installed to simulated system")
                    }
                    is BuildProgressEvent.Failed -> {
                        _isCompiling.value = false
                        _toastEvents.tryEmit("Build Failed: ${event.error}")
                    }
                }
            }
        }

        // Initialize sample projects if none exist
        viewModelScope.launch {
            val existing = database.projectDao().getProjectByIdSync("init_check")
            projects.collect { list ->
                if (list.isEmpty()) {
                    loadStarterProject(SampleProjectType.QUANTUM_PULSE, showToast = false)
                    loadStarterProject(SampleProjectType.NEUMORPHIC_CALC, showToast = false)
                    loadStarterProject(SampleProjectType.RETRO_PAINTER, showToast = false)
                    loadStarterProject(SampleProjectType.SPACE_ARCADE, showToast = false)
                } else if (_selectedProject.value == null) {
                    _selectedProject.value = list.firstOrNull()
                    _activeSimulatedProject.value = list.firstOrNull()
                }
            }
        }
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun selectProject(project: ProjectEntity) {
        _selectedProject.value = project
        loadFileTree(project)
    }

    fun importZipUri(uri: Uri) {
        viewModelScope.launch {
            _toastEvents.tryEmit("Importing and extracting ZIP package...")
            val result = zipManager.importZipFromUri(uri)
            result.onSuccess { project ->
                database.projectDao().insertProject(project)
                _selectedProject.value = project
                loadFileTree(project)
                _toastEvents.tryEmit("Successfully imported '${project.name}' (${project.fileCount} files)")
            }.onFailure { err ->
                _toastEvents.tryEmit("Failed to import ZIP: ${err.message}")
            }
        }
    }

    fun loadStarterProject(type: SampleProjectType, showToast: Boolean = true) {
        viewModelScope.launch {
            val project = zipManager.createSampleProject(type)
            database.projectDao().insertProject(project)
            if (_selectedProject.value == null) {
                _selectedProject.value = project
                _activeSimulatedProject.value = project
                loadFileTree(project)
            }
            if (showToast) {
                _toastEvents.tryEmit("Created starter template '${project.name}'")
            }
        }
    }

    fun loadFileTree(project: ProjectEntity) {
        viewModelScope.launch {
            val tree = zipManager.getProjectFileTree(project.sourceDirPath)
            _projectFileTree.value = tree
        }
    }

    fun openFile(node: ProjectFileNode) {
        if (node.isDirectory) return
        viewModelScope.launch {
            _selectedFilePath.value = node.absolutePath
            _selectedFileContent.value = zipManager.readFileContent(node.absolutePath)
        }
    }

    fun closeFileEditor() {
        _selectedFilePath.value = null
        _selectedFileContent.value = null
    }

    fun saveFileEdit(newContent: String) {
        val path = _selectedFilePath.value ?: return
        viewModelScope.launch {
            val success = zipManager.saveFileContent(path, newContent)
            if (success) {
                _selectedFileContent.value = newContent
                _toastEvents.tryEmit("File changes saved successfully")
            } else {
                _toastEvents.tryEmit("Failed to save changes to file")
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            zipManager.deleteProjectFiles(project.sourceDirPath)
            database.projectDao().deleteProject(project)
            if (_selectedProject.value?.id == project.id) {
                _selectedProject.value = null
                _projectFileTree.value = null
            }
            if (_activeSimulatedProject.value?.id == project.id) {
                _activeSimulatedProject.value = null
            }
            _toastEvents.tryEmit("Deleted project '${project.name}'")
        }
    }

    fun startCompilation(targetProject: ProjectEntity? = null) {
        val projectToBuild = targetProject ?: _selectedProject.value
        if (projectToBuild == null) {
            _toastEvents.tryEmit("No project selected to build")
            return
        }

        if (_isCompiling.value) {
            _toastEvents.tryEmit("Build is already in progress")
            return
        }

        _isCompiling.value = true
        _compileProgress.value = 0f
        _compileCurrentTask.value = "Initializing"
        _terminalLogs.value = emptyList()
        _currentTab.value = NavigationTab.COMPILER

        viewModelScope.launch {
            val result = compilerEngine.compileProject(projectToBuild)
            result.onSuccess { artifact ->
                _activeSimulatedProject.value = projectToBuild
            }
        }
    }

    fun clearTerminalLogs() {
        _terminalLogs.value = emptyList()
    }

    fun launchInSimulator(project: ProjectEntity) {
        _activeSimulatedProject.value = project
        _currentTab.value = NavigationTab.SIMULATOR
        restartSimulator()
        addSimulatorLog("I", "ActivityTaskManager", "START u0 {act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=${project.packageName}/.MainActivity}")
    }

    fun toggleSimulatorOrientation() {
        val current = _simulatorState.value.orientation
        val next = if (current == DeviceOrientation.PORTRAIT) DeviceOrientation.LANDSCAPE else DeviceOrientation.PORTRAIT
        _simulatorState.value = _simulatorState.value.copy(orientation = next)
        addSimulatorLog("D", "WindowManager", "Screen rotation changed to $next")
    }

    fun restartSimulator() {
        _simulatorState.value = _simulatorState.value.copy(
            isRunning = true,
            fps = 60,
            memoryUsageMb = (38..48).random()
        )
        val proj = _activeSimulatedProject.value
        addSimulatorLog("I", "ProcessManager", "Restarting sandbox instance for ${proj?.packageName ?: "system"}")
        addSimulatorLog("D", "ArtMethod", "JIT cache initialized: 256KB")
    }

    fun toggleSimulatorPerfOverlay() {
        _simulatorState.value = _simulatorState.value.copy(
            showDevOverlay = !_simulatorState.value.showDevOverlay
        )
    }

    fun toggleSimulatorLogcat() {
        _simulatorState.value = _simulatorState.value.copy(
            showLogcat = !_simulatorState.value.showLogcat
        )
    }

    fun toggleSimulatorDarkTheme() {
        _simulatorState.value = _simulatorState.value.copy(
            isDarkMode = !_simulatorState.value.isDarkMode
        )
    }

    fun addSimulatorLog(priority: String, tag: String, message: String) {
        val timeString = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val entry = RuntimeLogcatEntry(timeString, tag, priority, message)
        _simulatorLogs.value = (_simulatorLogs.value + entry).takeLast(200)
    }

    fun saveApkToDocument(apkPath: String, targetUri: Uri) {
        viewModelScope.launch {
            val result = exporter.saveApkToDocumentUri(apkPath, targetUri)
            result.onSuccess { bytes ->
                _toastEvents.tryEmit("APK exported successfully (${bytes / 1024} KB)")
            }.onFailure { err ->
                _toastEvents.tryEmit("Failed to export APK: ${err.message}")
            }
        }
    }

    fun deleteArtifact(artifact: BuildArtifactEntity) {
        viewModelScope.launch {
            try {
                java.io.File(artifact.apkPath).delete()
            } catch (_: Exception) {}
            database.buildArtifactDao().deleteArtifact(artifact)
            _toastEvents.tryEmit("Deleted APK artifact: ${artifact.projectName}")
        }
    }
}
