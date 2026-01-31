package com.valentinerutto.mywallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.valentinerutto.mywallet.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>
    
    @Query("SELECT * FROM user_profile WHERE customerId = :customerId")
    suspend fun getUserProfileById(customerId: String): UserProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)
    
    @Query("DELETE FROM user_profile")
    suspend fun deleteAllProfiles()

}
