package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BuildArtifactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildArtifactDao {
    @Query("SELECT * FROM build_artifacts ORDER BY timestamp DESC")
    fun getAllArtifacts(): Flow<List<BuildArtifactEntity>>

    @Query("SELECT * FROM build_artifacts WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getArtifactsForProject(projectId: String): Flow<List<BuildArtifactEntity>>

    @Query("SELECT * FROM build_artifacts WHERE id = :id LIMIT 1")
    suspend fun getArtifactById(id: String): BuildArtifactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtifact(artifact: BuildArtifactEntity)

    @Delete
    suspend fun deleteArtifact(artifact: BuildArtifactEntity)

    @Query("DELETE FROM build_artifacts WHERE id = :id")
    suspend fun deleteArtifactById(id: String)
}
